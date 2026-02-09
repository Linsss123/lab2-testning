package shop;

/**
 * Minimal första implementation för TDD-cykel 1.
 */
public class ShoppingCart {
    private int totalInOre = 0;
    private final java.util.Map<String, Integer> itemTotals = new java.util.HashMap<>();
    private final java.util.Map<String, Integer> unitPrices = new java.util.HashMap<>();
    private final java.util.Map<String, Integer> quantities = new java.util.HashMap<>();

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
        if (quantity <= 0) {
            throw new IllegalArgumentException("Ogiltig kvantitet: måste vara > 0");
        }
        // Spara enhetspris första gången vi ser varan
        unitPrices.putIfAbsent(name, priceInOre);

        int newQuantity = quantities.getOrDefault(name, 0) + quantity;
        quantities.put(name, newQuantity);

        int previousSubtotal = itemTotals.getOrDefault(name, 0);
        int newSubtotal = unitPrices.get(name) * newQuantity;

        itemTotals.put(name, newSubtotal);
        totalInOre += (newSubtotal - previousSubtotal);
    }

    /**
     * Tar bort en vara helt (alla dess enheter) baserat på namn.
     * Gör inget om varan inte finns.
     */
    public void removeItem(String name) {
        Integer subtotal = itemTotals.remove(name);
        if (subtotal != null) {
            totalInOre -= subtotal;
            unitPrices.remove(name);
            quantities.remove(name);
        }
    }

    /**
     * Uppdaterar kvantiteten för en befintlig vara. Om varan inte finns händer inget.
     */
    public void updateQuantity(String name, int newQuantity) {
        Integer unit = unitPrices.get(name);
        if (unit == null) return;

        int previousSubtotal = itemTotals.getOrDefault(name, 0);
        int newSubtotal = unit * newQuantity;

        itemTotals.put(name, newSubtotal);
        quantities.put(name, newQuantity);
        totalInOre += (newSubtotal - previousSubtotal);
    }

    /**
     * Applicerar en procentuell rabatt på nuvarande total.
     * Exempel: percent=10 ger 10% lägre total.
     */
    public void applyPercentageDiscount(int percent) {
        int discount = (totalInOre * percent) / 100;
        totalInOre -= discount;
        // Notera: Rabatten fördelas inte per vara i denna minimala implementation.
    }
}
