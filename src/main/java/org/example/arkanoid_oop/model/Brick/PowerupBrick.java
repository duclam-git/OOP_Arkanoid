package org.example.arkanoid_oop.model.Brick;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.arkanoid_oop.model.util.PowerupType;

import java.util.Random;

public class PowerupBrick extends Brick {

    private PowerupType powerupType;
    private static final Random rand = new Random(); // Biến ngẫu nhiên

    public PowerupBrick(double x, double y) {
        super(x, y);

        Image img = new Image(getClass().getResourceAsStream("/images/powerup_brick.png"));
        this.view = new ImageView(img);

        this.view.setFitWidth(BRICK_WIDTH);
        this.view.setFitHeight(BRICK_HEIGHT);
        this.view.setLayoutX(x);
        this.view.setLayoutY(y);

        this.scoreValue = 30; // Gạch này giá trị cao

        // Chọn ngẫu nhiên loại vật phẩm trong 4 loại
        int chance = rand.nextInt(4); // 0, 1, 2, hoặc 3
        if (chance == 0) {
            this.powerupType = PowerupType.MULTI_BALL;
        } else if (chance == 1) {
            this.powerupType = PowerupType.LASER_PADDLE;
        } else if (chance == 2) {
            this.powerupType = PowerupType.DOUBLE_PADDLE;
        } else {
            this.powerupType = PowerupType.SHIELD;
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