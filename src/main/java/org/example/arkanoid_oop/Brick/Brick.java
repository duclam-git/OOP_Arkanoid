import javafx.scene.image.ImageView;

/**
 * Lớp cha trừu tượng cho tất cả các loại gạch.
 * Quản lý các thuộc tính chung.
 */
public abstract class Brick {

    // Kích thước chuẩn cho mỗi viên gạch
    public static final double BRICK_WIDTH = 75;
    public static final double BRICK_HEIGHT = 30;

    protected ImageView view; // Mỗi gạch sẽ có một ImageView để hiển thị
    protected int scoreValue = 10;
    private boolean destroyed = false; // Cờ báo gạch đã bị phá hủy hoàn toàn

    public Brick(double x, double y) {
        // Hàm khởi tạo này sẽ được các lớp con gọi
    }

    /**
     * Phương thức trừu tượng.
     * Xử lý logic khi bóng va chạm vào gạch.
     * Trả về true nếu gạch bị PHÁ HỦY HOÀN TOÀN (để GamePane xóa nó).
     * Trả về false nếu gạch chỉ bị hỏng (như Hard_brick).
     */
    public abstract boolean onHit();

    // Các hàm Getter chung

    public ImageView getView() {
        return view;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    protected void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}