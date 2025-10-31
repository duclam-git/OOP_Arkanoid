package org.example.arkanoid_oop.Entities;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Lớp Ball (quả bóng/thiên thạch) kế thừa ImageView.
 * Tự quản lý vị trí và logic di chuyển.
 */
public class Ball extends ImageView {

    private static final double BALL_RADIUS = 10; // Kích thước bán kính hiển thị

    // Tốc độ và hướng di chuyển
    private double dx;
    private double dy;

    private double screenWidth, screenHeight;
    private double startX, startY; // Vị trí ban đầu

    public Ball(double screenWidth, double screenHeight, double startX, double startY) {
        // 1. Tải ảnh quả bóng (hoặc thiên thạch)
        // (Hãy đảm bảo bạn có ảnh "ball.png" trong resources/images)
        super(new Image(Ball.class.getResourceAsStream("/images/ball.png")));

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.startX = startX;
        this.startY = startY;

        // Đặt kích thước
        setFitWidth(BALL_RADIUS * 2);
        setFitHeight(BALL_RADIUS * 2);

        // Đặt vị trí ban đầu
        reset();

        // Tạo một đối tượng animation xoay
        RotateTransition rt = new RotateTransition(Duration.seconds(2), this);
        rt.setByAngle(360); // Xoay 360 độ
        rt.setCycleCount(RotateTransition.INDEFINITE); // Lặp lại vô hạn
        rt.setInterpolator(Interpolator.LINEAR); // Xoay đều, không nhanh chậm

        // Bắt đầu animation
        rt.play();
    }

    /**
     * Đặt bóng về vị trí và tốc độ ban đầu.
     */
    public void reset() {
        // Đặt vị trí (layoutX/Y là góc trên trái, nên trừ đi bán kính)
        setLayoutX(startX - BALL_RADIUS);
        setLayoutY(startY - BALL_RADIUS);

        // Tốc độ ngẫu nhiên ban đầu
        dx = Math.random() > 0.5 ? 3 : -3;
        dy = -3; // Luôn bay lên
    }

    /**
     * Hàm cập nhật vị trí, được gọi từ GamePane.
     */
    public void update() {
        // Di chuyển bóng
        setLayoutX(getLayoutX() + dx);
        setLayoutY(getLayoutY() + dy);

        // Logic va chạm tường
        double x = getLayoutX();
        double y = getLayoutY();

        // Va chạm tường trái hoặc phải
        if (x <= 0 || x >= screenWidth - (BALL_RADIUS * 2)) {
            reverseDx();
        }

        // Va chạm tường trên
        if (y <= 0) {
            reverseDy();
        }
    }

    // Các hàm xử lý va chạm (đảo hướng)
    public void reverseDx() {
        this.dx = -this.dx;
    }

    public void reverseDy() {
        this.dy = -this.dy;
    }

    public double getRadius() {
        return BALL_RADIUS;
    }

    public double getDy() {
        return dy;
    }

    public double getDx() {
        return dx;
    }
}