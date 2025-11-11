package org.example.arkanoid_oop.Brick;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Lớp gạch không thể phá hủy (Impervious).
 * Kế thừa từ Brick và ghi đè onHit() để không bao giờ bị phá hủy.
 * PHIÊN BẢN NÀY GIẢ ĐỊNH /images/impervious_brick.png LUÔN TỒN TẠI.
 */
public class Impervious_brick extends Brick {

    // Tải ảnh gạch (tĩnh để tiết kiệm bộ nhớ)
    private static final Image imperviousImage = new Image(
            Impervious_brick.class.getResourceAsStream("/images/impervious_brick.png")
    );

    public Impervious_brick(double x, double y) {
        super(x, y); // Gọi hàm khởi tạo của lớp cha

        // Sử dụng ảnh đã được tải
        this.view = new ImageView(imperviousImage);

        // Đặt kích thước và vị trí
        this.view.setFitWidth(BRICK_WIDTH);
        this.view.setFitHeight(BRICK_HEIGHT);
        this.view.setLayoutX(x);
        this.view.setLayoutY(y);

        this.scoreValue = 0; // Không cho điểm vì không thể phá
    }

    /**
     * Ghi đè phương thức onHit.
     * Gạch này không bao giờ bị phá hủy.
     * @return luôn trả về false (không bị phá hủy).
     */
    @Override
    public boolean onHit() {
        // Không làm gì cả
        // Luôn trả về false (không bao giờ bị phá hủy)
        return false;
    }
}