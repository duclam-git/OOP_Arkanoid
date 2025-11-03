package org.example.arkanoid_oop.Brick;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.arkanoid_oop.PowerupType;

import java.util.Random;

public class Powerup_brick extends Brick {

    private PowerupType powerupType;
    private static final Random rand = new Random(); // Biến ngẫu nhiên

    public Powerup_brick(double x, double y) {
        super(x, y);

        // Tải ảnh gạch vật phẩm
        Image img = new Image(getClass().getResourceAsStream("/images/powerup_brick.png"));
        this.view = new ImageView(img);

        this.view.setFitWidth(BRICK_WIDTH);
        this.view.setFitHeight(BRICK_HEIGHT);
        this.view.setLayoutX(x);
        this.view.setLayoutY(y);

        this.scoreValue = 30; // Gạch này giá trị cao

        // (MỚI) Chọn ngẫu nhiên loại vật phẩm gạch này sẽ chứa
        if (rand.nextBoolean()) {
            this.powerupType = PowerupType.MULTI_BALL;
        } else {
            this.powerupType = PowerupType.LASER_PADDLE;
        }
    }

    @Override
    public boolean onHit() {
        // Bị phá hủy ngay lập tức
        setDestroyed(true);
        return true;
    }

    public PowerupType getPowerupType() {
        return powerupType;
    }

    // Hàm lấy tâm viên gạch (để vật phẩm rơi ra từ giữa)
    public double getCenterX() {
        return view.getLayoutX() + BRICK_WIDTH / 2;
    }

    public double getCenterY() {
        return view.getLayoutY() + BRICK_HEIGHT / 2;
    }
}