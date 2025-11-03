package org.example.arkanoid_oop.Entities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.arkanoid_oop.PowerupType;

/**
 * Lớp Powerup đại diện cho vật phẩm rơi xuống
 * mà ván trượt có thể "hứng".
 */
public class Powerup extends ImageView {

    private static final double FALLING_SPEED = 2.5; // Tốc độ rơi
    private PowerupType type;

    public Powerup(double x, double y, PowerupType type) {
        super(); // Gọi hàm khởi tạo của ImageView
        this.type = type;

        // Chọn ảnh dựa trên loại vật phẩm
        String imagePath = "";
        switch (type) {
            case MULTI_BALL:
                imagePath = "/images/powerup_multiball.png";
                break;
            case LASER_PADDLE:
                imagePath = "/images/powerup_laser.png";
                break;
        }

        Image img = new Image(getClass().getResourceAsStream(imagePath));
        setImage(img);

        // Đặt kích thước và vị trí
        setFitWidth(30);
        setFitHeight(30);
        setLayoutX(x - (getFitWidth() / 2)); // Đặt tâm vật phẩm tại tâm gạch
        setLayoutY(y - (getFitHeight() / 2));
    }

    /**
     * Di chuyển vật phẩm rơi xuống.
     * Được gọi từ GamePane.
     */
    public void update() {
        setLayoutY(getLayoutY() + FALLING_SPEED);
    }

    public PowerupType getType() {
        return type;
    }
}