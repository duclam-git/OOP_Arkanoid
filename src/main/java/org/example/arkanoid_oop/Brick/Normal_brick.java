import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Normal_brick extends Brick {

    public Normal_brick(double x, double y) {
        super(x, y); // Gọi hàm khởi tạo của lớp cha

        // Tải ảnh gạch thường (đảm bảo có file "normal_brick.png")
        Image img = new Image(getClass().getResourceAsStream("/images/normal_brick.png"));
        this.view = new ImageView(img); // Khởi tạo ImageView

        // Đặt kích thước và vị trí
        this.view.setFitWidth(BRICK_WIDTH);
        this.view.setFitHeight(BRICK_HEIGHT);
        this.view.setLayoutX(x);
        this.view.setLayoutY(y);

        this.scoreValue = 10;
    }

    @Override
    public boolean onHit() {
        // Gạch thường bị phá hủy ngay lập tức
        setDestroyed(true);
        return true; // Trả về true (đã bị phá hủy)
    }
}