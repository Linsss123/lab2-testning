package com.example;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Enhetstester för BookingSystem")
class BookingSystemTest {

    // Enkel test double för TimeProvider
    static class FakeTimeProvider implements TimeProvider {
        private LocalDateTime now;
        void setNow(LocalDateTime now) { this.now = now; }
        @Override public LocalDateTime getCurrentTime() { return now; }
    }

    FakeTimeProvider timeProvider;
    FakeRoomRepository roomRepository;
    FakeNotificationService notificationService;

    BookingSystem bookingSystem;

    @BeforeEach
    void setUp() {
        timeProvider = new FakeTimeProvider();
        roomRepository = new FakeRoomRepository();
        notificationService = new FakeNotificationService();
        bookingSystem = new BookingSystem(timeProvider, roomRepository, notificationService);
    }

    //Hjälpmetod
    private static LocalDateTime now() { return LocalDateTime.of(2030, 1, 1, 12, 0); }

    //bookRoom
    @Test
    @DisplayName("bookRoom: lyckas när rum finns och är ledigt, notis skickas")
    void bookRoom_success_sendsNotification() throws Exception {
        LocalDateTime start = now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        timeProvider.setNow(now());

        Room room = new Room("R1", "Rum 1");
        roomRepository.save(room);

        boolean result = bookingSystem.bookRoom("R1", start, end);

        assertThat(result).as("Bokningen ska lyckas").isTrue();
        // save ska anropas (en gång vid initial lagring + en gång vid bokning)
        assertThat(roomRepository.saveCount).isEqualTo(2);
        // Bekräftelse ska skickas
        assertThat(notificationService.bookingConfirmationsSent).isEqualTo(1);
    }

    @Test
    @DisplayName("bookRoom: lyckas även om NotificationService kastar undantag")
    void bookRoom_notificationException_isIgnored() throws Exception {
        LocalDateTime start = now().plusHours(1);
        LocalDateTime end = start.plusHours(1);
        timeProvider.setNow(now());

        Room room = new Room("R1", "Rum 1");
        roomRepository.save(room);

        notificationService.throwOnBookingConfirmation = true;

        boolean result = bookingSystem.bookRoom("R1", start, end);

        assertThat(result).isTrue();
        assertThat(roomRepository.saveCount).isEqualTo(2);
        assertThat(notificationService.bookingConfirmationsSent).isEqualTo(1);
    }

    @Test
    @DisplayName("bookRoom: returnerar false när rummet inte är tillgängligt och gör inga sid-effekter")
    void bookRoom_roomUnavailable_returnsFalse_noSideEffects() throws Exception {
        LocalDateTime start = now().plusHours(1);
        LocalDateTime end = start.plusHours(1);
        timeProvider.setNow(now());

        Room room = new Room("R1", "Rum 1");
        // Lägg till en överlappande bokning för att göra rummet otillgängligt
        room.addBooking(new Booking("B-EXIST", "R1", start.minusMinutes(30), end.minusMinutes(30)));
        roomRepository.save(room);

        boolean result = bookingSystem.bookRoom("R1", start, end);

        assertThat(result).isFalse();
        assertThat(roomRepository.saveCount).isEqualTo(1); // bara initial save i setup ovan
        assertThat(notificationService.bookingConfirmationsSent).isZero();
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> nullArgsForBookRoom() {
        LocalDateTime s = now().plusHours(1);
        LocalDateTime e = s.plusHours(1);
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(null, s, e, "roomId null ska ge IAE"),
                org.junit.jupiter.params.provider.Arguments.of("R1", null, e, "startTime null ska ge IAE"),
                org.junit.jupiter.params.provider.Arguments.of("R1", s, null, "endTime null ska ge IAE")
        );
    }

    @ParameterizedTest(name = "{3}")
    @MethodSource("nullArgsForBookRoom")
    @DisplayName("bookRoom: validerar null-argument")
    void bookRoom_nullValidation(String roomId, LocalDateTime start, LocalDateTime end, String description) {
        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bokning kräver");
    }

    @Test
    @DisplayName("bookRoom: starttid i dåtid kastar IllegalArgumentException")
    void bookRoom_startInPast_throws() {
        timeProvider.setNow(now());

        LocalDateTime start = now().minusMinutes(1);
        LocalDateTime end = now().plusHours(1);

        assertThatThrownBy(() -> bookingSystem.bookRoom("R1", start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dåtid");
    }

    @Test
    @DisplayName("bookRoom: sluttid före starttid kastar IllegalArgumentException")
    void bookRoom_endBeforeStart_throws() {
        timeProvider.setNow(now());

        LocalDateTime start = now().plusHours(2);
        LocalDateTime end = start.minusMinutes(1);

        assertThatThrownBy(() -> bookingSystem.bookRoom("R1", start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");
    }

    @Test
    @DisplayName("bookRoom: rum finns inte → IllegalArgumentException")
    void bookRoom_roomNotFound_throws() {
        timeProvider.setNow(now());
        LocalDateTime start = now().plusHours(1);
        LocalDateTime end = start.plusHours(1);
        // Inget rum "MISSING" lagrat

        assertThatThrownBy(() -> bookingSystem.bookRoom("MISSING", start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rummet existerar inte");
    }

    // --------- getAvailableRooms ---------
    static Stream<org.junit.jupiter.params.provider.Arguments> nullArgsForGetAvailableRooms() {
        LocalDateTime s = now().plusHours(1);
        LocalDateTime e = s.plusHours(1);
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(null, e, "startTime null"),
                org.junit.jupiter.params.provider.Arguments.of(s, null, "endTime null")
        );
    }

    @ParameterizedTest(name = "getAvailableRooms: {2}")
    @MethodSource("nullArgsForGetAvailableRooms")
    void getAvailableRooms_nullValidation(LocalDateTime start, LocalDateTime end, String name) {
        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Måste ange både start- och sluttid");
    }

    @Test
    @DisplayName("getAvailableRooms: sluttid före starttid kastar IllegalArgumentException")
    void getAvailableRooms_endBeforeStart_throws() {
        LocalDateTime start = now().plusHours(2);
        LocalDateTime end = start.minusMinutes(5);
        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");
    }

    @Test
    @DisplayName("getAvailableRooms: filtrerar rum baserat på isAvailable")
    void getAvailableRooms_filtersRooms() {
        LocalDateTime start = now().plusHours(2);
        LocalDateTime end = start.plusHours(1);

        Room r1 = new Room("R1", "Rum 1");
        Room r2 = new Room("R2", "Rum 2");
        Room r3 = new Room("R3", "Rum 3");
        // Gör r2 otillgängligt med en överlappande bokning
        r2.addBooking(new Booking("B2", "R2", start.minusMinutes(15), end.minusMinutes(15)));
        roomRepository.save(r1);
        roomRepository.save(r2);
        roomRepository.save(r3);

        List<Room> available = bookingSystem.getAvailableRooms(start, end);

        assertThat(available).containsExactlyInAnyOrder(r1, r3);
    }

    // --------- cancelBooking ---------
    @Test
    @DisplayName("cancelBooking: bookingId null → IllegalArgumentException")
    void cancelBooking_nullId_throws() {
        assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Boknings-id kan inte vara null");
    }

    @Test
    @DisplayName("cancelBooking: inget rum med bokningen → returnerar false")
    void cancelBooking_noRoomWithBooking_returnsFalse() throws Exception {
        roomRepository.save(new Room("R1", "A"));
        roomRepository.save(new Room("R2", "B"));

        boolean result = bookingSystem.cancelBooking("B1");

        assertThat(result).isFalse();
        assertThat(roomRepository.saveCount).isEqualTo(2); // endast initiala saves
        assertThat(notificationService.cancellationConfirmationsSent).isZero();
    }

    @Test
    @DisplayName("cancelBooking: bokning i dåtid eller påbörjad → IllegalStateException och inga sid-effekter")
    void cancelBooking_startedOrPast_throws() throws Exception {
        Room room = new Room("R1", "Rum 1");
        Booking booking = new Booking("B1", "R1", now().minusMinutes(10), now().plusMinutes(50));
        room.addBooking(booking);
        roomRepository.save(room);
        timeProvider.setNow(now());

        assertThatThrownBy(() -> bookingSystem.cancelBooking("B1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Kan inte avboka");

        // Inga extra saves utöver initial save
        assertThat(roomRepository.saveCount).isEqualTo(1);
        assertThat(notificationService.cancellationConfirmationsSent).isZero();
    }

    @Test
    @DisplayName("cancelBooking: lyckad avbokning sparar rummet och försöker skicka notis")
    void cancelBooking_success() throws Exception {
        Room room = new Room("R1", "Rum 1");
        Booking booking = new Booking("B1", "R1", now().plusHours(2), now().plusHours(3));
        room.addBooking(booking);
        roomRepository.save(room);
        timeProvider.setNow(now());

        boolean result = bookingSystem.cancelBooking("B1");

        assertThat(result).isTrue();
        assertThat(room.hasBooking("B1")).isFalse();
        assertThat(roomRepository.saveCount).isEqualTo(2); // initial save + save vid avbokning
        assertThat(notificationService.cancellationConfirmationsSent).isEqualTo(1);
    }

    @Test
    @DisplayName("cancelBooking: ignorerar NotificationException och returnerar true")
    void cancelBooking_notificationException_ignored() throws Exception {
        Room room = new Room("R1", "Rum 1");
        Booking booking = new Booking("B1", "R1", now().plusHours(2), now().plusHours(3));
        room.addBooking(booking);
        roomRepository.save(room);
        timeProvider.setNow(now());

        notificationService.throwOnCancellation = true;

        boolean result = bookingSystem.cancelBooking("B1");

        assertThat(result).isTrue();
        assertThat(room.hasBooking("B1")).isFalse();
        assertThat(roomRepository.saveCount).isEqualTo(2);
        assertThat(notificationService.cancellationConfirmationsSent).isEqualTo(1);
    }

    // --------- Test doubles ---------
    static class FakeRoomRepository implements RoomRepository {
        private final Map<String, Room> store = new LinkedHashMap<>();
        int saveCount = 0;
        @Override public Optional<Room> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }
        @Override public List<Room> findAll() {
            return List.copyOf(store.values());
        }
        @Override public void save(Room room) {
            saveCount++;
            store.put(room.getId(), room);
        }
    }

    static class FakeNotificationService implements NotificationService {
        boolean throwOnBookingConfirmation = false;
        boolean throwOnCancellation = false;
        int bookingConfirmationsSent = 0;
        int cancellationConfirmationsSent = 0;
        @Override public void sendBookingConfirmation(Booking booking) throws NotificationException {
            bookingConfirmationsSent++;
            if (throwOnBookingConfirmation) throw new NotificationException("simulerat fel");
        }
        @Override public void sendCancellationConfirmation(Booking booking) throws NotificationException {
            cancellationConfirmationsSent++;
            if (throwOnCancellation) throw new NotificationException("simulerat fel");
        }
    }
}
