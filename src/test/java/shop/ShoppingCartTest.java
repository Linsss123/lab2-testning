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
}
