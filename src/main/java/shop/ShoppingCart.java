package shop;

/**
 * Minimal första implementation för TDD-cykel 1.
 */
public class ShoppingCart {
    private int totalInOre = 0;
    private final java.util.Map<String, Integer> itemTotals = new java.util.HashMap<>();

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
        int delta = priceInOre * quantity;
        totalInOre += delta;
        itemTotals.merge(name, delta, Integer::sum);
    }

    /**
     * Tar bort en vara helt (alla dess enheter) baserat på namn.
     * Gör inget om varan inte finns.
     */
    public void removeItem(String name) {
        Integer subtotal = itemTotals.remove(name);
        if (subtotal != null) {
            totalInOre -= subtotal;
        }
    }
}
