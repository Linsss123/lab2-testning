package shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Cykel 1: Starta enkelt – en ny varukorg har totalpris 0.
 */
class ShoppingCartTest {

    @Test
    @DisplayName("Ny varukorg har totalpris 0 öre")
    void newCart_hasZeroTotal() {
        ShoppingCart cart = new ShoppingCart();

        int totalOre = cart.getTotalPriceInOre();

        assertThat(totalOre).isZero();
    }

    @Test
    @DisplayName("Lägga till en vara uppdaterar totalpriset")
    void addItem_updatesTotal() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem("Äpple", 299, 2); // 2 st à 2,99 kr = 5,98 kr = 598 öre

        assertThat(cart.getTotalPriceInOre()).isEqualTo(598);
    }

    @Test
    @DisplayName("Ta bort en vara minskar totalpriset korrekt")
    void removeItem_decreasesTotal() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem("Äpple", 299, 2); // 598 öre
        cart.addItem("Banan", 199, 1); // +199 = 797 öre

        cart.removeItem("Äpple"); // kvar 199 öre

        assertThat(cart.getTotalPriceInOre()).isEqualTo(199);
    }

    @Test
    @DisplayName("Uppdatera kvantitet justerar totalpriset")
    void updateQuantity_adjustsTotal() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem("Apelsin", 500, 1); // 500 öre
        cart.addItem("Banan", 200, 3);   // +600 = 1100 öre

        cart.updateQuantity("Banan", 1); // Banan subtotal från 600 -> 200, total 700

        assertThat(cart.getTotalPriceInOre()).isEqualTo(700);
    }
}
