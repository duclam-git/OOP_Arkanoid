package org.example.arkanoid_oop.Brick;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Hard_brick extends Brick {

    private int hitsLeft = 2; // Cần 2 lần chạm

    // Tải trước cả 2 ảnh
    private Image fullImage;
    private Image crackedImage;

    public Hard_brick(double x, double y) {
        super(x, y);

        // Tải 2 ảnh
        this.fullImage = new Image(getClass().getResourceAsStream("/images/hard_brick_full.png"));
        this.crackedImage = new Image(getClass().getResourceAsStream("/images/hard_brick_cracked.png"));

        this.view = new ImageView(fullImage); // Bắt đầu bằng ảnh nguyên vẹn

        this.view.setFitWidth(BRICK_WIDTH);
        this.view.setFitHeight(BRICK_HEIGHT);
        this.view.setLayoutX(x);
        this.view.setLayoutY(y);

        this.scoreValue = 25; // Gạch cứng cho nhiều điểm hơn
    }

    @Override
    public boolean onHit() {
        hitsLeft--; // Trừ 1 mạng

        if (hitsLeft == 1) {
            // Lần chạm đầu tiên: Chuyển sang ảnh nứt
            this.view.setImage(crackedImage);
            return false; // Trả về false (chưa bị phá hủy)
        } else if (hitsLeft == 0) {
            // Lần chạm thứ hai: Bị phá hủy
            setDestroyed(true);
            return true; // Trả về true (đã bị phá hủy)
        }

        return false;
    }
}