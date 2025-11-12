package org.example.arkanoid_oop.model.Brick;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Normal_brick extends Brick {

    public Normal_brick(double x, double y) {
        super(x, y);

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
        // Gạch thường bị phá hủy
        setDestroyed(true);
        return true; // Trả về true (đã bị phá hủy)
    }
}