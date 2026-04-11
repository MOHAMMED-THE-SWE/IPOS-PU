package service.sales;

import model.Product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CartService {

    public static class CartItem {
        private final Product product;
        private int qty;

        public CartItem(Product product, int qty) {
            this.product = product;
            this.qty = qty;
        }

        public Product getProduct() { return product; }
        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }
        public double getSubtotal() { return product.getUnitPrice() * qty; }
    }

    private final Map<Integer, CartItem> items = new LinkedHashMap<>();

    public void addItem(Product product, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");
        items.merge(product.getItemId(),
            new CartItem(product, qty),
            (existing, newItem) -> { existing.setQty(existing.getQty() + qty); return existing; });
    }

    public void removeItem(int itemId) {
        items.remove(itemId);
    }

    public void updateQty(int itemId, int qty) {
        if (qty <= 0) { removeItem(itemId); return; }
        CartItem ci = items.get(itemId);
        if (ci != null) ci.setQty(qty);
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public double getSubtotal() {
        return items.values().stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public void clear() {
        items.clear();
    }
}
