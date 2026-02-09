package shop;

/**
 * Minimal första implementation för TDD-cykel 1.
 */
public class ShoppingCart {
    private int totalInOre = 0;

    /**
     * Totalpris i öre. Tom varukorg -> 0.
     */
    public int getTotalPriceInOre() {
        return totalInOre;
    }

    /**
     * Lägger till en vara genom att öka totalpriset med pris * kvantitet.
     */
    public void addItem(String name, int priceInOre, int quantity) {
        totalInOre += priceInOre * quantity;
    }
}
