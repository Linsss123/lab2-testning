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
}
