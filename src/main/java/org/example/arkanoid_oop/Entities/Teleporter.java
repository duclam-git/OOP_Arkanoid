package org.example.arkanoid_oop.Entities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Lớp Teleporter (Cổng Dịch Chuyển) kế thừa ImageView.
 * Luôn hoạt động theo cặp.
 */
public class Teleporter extends ImageView {

    public static final double TELEPORTER_SIZE = 40;

    private Teleporter partner;
    private long exitCooldownTime = 0;
    private final long COOLDOWN_DURATION_NANO = 500_000_000L; // 0.5 giây

    /**
     * Khởi tạo Teleporter.
     */
    public Teleporter(double x, double y) {
        // Sử dụng ảnh explosive_brick.png làm ảnh teleporter (placeholder)
        Image img = new Image(getClass().getResourceAsStream("/images/teleporter.png"));
        setImage(img);

        setFitWidth(TELEPORTER_SIZE);
        setFitHeight(TELEPORTER_SIZE);
        // Đặt vị trí (layoutX/Y là góc trên trái, cần trừ đi nửa kích thước để đặt tâm tại (x, y))
        setLayoutX(x - TELEPORTER_SIZE / 2.0);
        setLayoutY(y - TELEPORTER_SIZE / 2.0);
    }

    public void setPartner(Teleporter partner) {
        this.partner = partner;
    }

    public Teleporter getPartner() {
        return partner;
    }

    /**
     * Thiết lập thời gian cooldown cho cổng này sau khi bóng thoát ra từ cổng đối tác.
     */
    public void setExitCooldown() {
        this.exitCooldownTime = System.nanoTime() + COOLDOWN_DURATION_NANO;
    }

    /**
     * Kiểm tra xem cổng có đang trong thời gian cooldown hay không.
     */
    public boolean isOnCooldown(long now) {
        return now < exitCooldownTime;
    }

    public double getCenterX() {
        return getLayoutX() + TELEPORTER_SIZE / 2.0;
    }

    public double getCenterY() {
        return getLayoutY() + TELEPORTER_SIZE / 2.0;
    }

    /**
     * Dịch chuyển quả bóng đến vị trí thoát của cổng đối tác và thay đổi hướng ngẫu nhiên.
     * @param ball Quả bóng cần dịch chuyển
     */
    public void teleportBall(Ball ball) {
        if (partner == null) return;

        double exitX = partner.getCenterX();
        double exitY = partner.getCenterY();
        double ballRadius = Ball.BALL_RADIUS; // Sử dụng hằng số từ lớp Ball
        double ballSpeed = ball.getSpeed();

        // 1. Dịch chuyển bóng đến vị trí thoát
        // Đặt bóng ngay trên tâm cổng đối tác
        ball.setLayoutX(exitX - ballRadius);
        // Đặt cao hơn để đảm bảo thoát khỏi vùng va chạm và di chuyển lên
        ball.setLayoutY(exitY - ballRadius * 2);

        // 2. Thay đổi hướng ngẫu nhiên (phân tán)
        // Tạo một giá trị ngẫu nhiên từ -ballSpeed đến +ballSpeed cho hướng ngang (scatteredDx)
        double scatteredDx = (Math.random() * 2 * ballSpeed) - ballSpeed;

        // Hướng dọc (newDy) luôn âm để đảm bảo bóng bay lên
        double newDy = -ballSpeed;

        // Hàm setDirection sẽ chuẩn hóa vector (scatteredDx, newDy) để giữ tốc độ không đổi
        ball.setDirection(scatteredDx, newDy);

        // 3. Đặt cooldown cho cổng đối tác để tránh bóng ngay lập tức chạm lại
        partner.setExitCooldown();
    }
}