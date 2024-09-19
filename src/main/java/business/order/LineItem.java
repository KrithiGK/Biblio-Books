package business.order;

public class LineItem {

    private long bookId;
    private long orderId;
    private int quantity;

    public LineItem(long orderId, long bookId, int quantity) {
        this.orderId = orderId;
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public long getBookId() {
        return bookId;
    }

    public long getOrderId() {
        return orderId;
    }

    public int getQuantity() { return quantity; }

    @Override
    public String toString() {
        return "LineItem{" +
                "bookId=" + bookId +
                ", orderId=" + orderId +
                ", quantity=" + quantity +
                '}';
    }
}