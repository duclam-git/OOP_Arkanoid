package org.example.arkanoid_oop.model.Entities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Lớp Laser (đạn laser) kế thừa ImageView.
 * Di chuyển thẳng đứng lên trên.
 */
public class Laser extends ImageView {

    private static final double LASER_SPEED = 5; // Tốc độ di chuyển của tia laser
    private static final double LASER_WIDTH = 40;
    private static final double LASER_HEIGHT = 40;

    public Laser(double startX, double startY) {
        super(new Image(Laser.class.getResourceAsStream("/images/laser.png")));

        // Thiết lập kích thước
        setFitWidth(LASER_WIDTH);
        setFitHeight(LASER_HEIGHT);

        // Đặt vị trí ban đầu (Laser nằm giữa chiều rộng nhưng ở góc trên trái)
        setLayoutX(startX - LASER_WIDTH / 2);
        setLayoutY(startY - LASER_HEIGHT);
    }

    /**
     * Cập nhật vị trí laser (di chuyển thẳng lên).
     * @return true nếu laser ra khỏi màn hình, ngược lại false.
     */
    public boolean update() {
        setLayoutY(getLayoutY() - LASER_SPEED);
        return getLayoutY() < -LASER_HEIGHT; // Kiểm tra nếu laser ra khỏi cạnh trên
    }
}