package org.example.arkanoid_oop.Entities;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import java.util.Random;

public class Ball extends ImageView {

    private static final double BALL_RADIUS = 10;
    private double dx, dy;
    private double screenWidth, screenHeight;
    private double startX, startY;
    private static final Random rand = new Random();

    /**
     * Constructor chính: Dùng khi reset hoặc bắt đầu (4 tham số)
     */
    public Ball(double screenWidth, double screenHeight, double startX, double startY) {
        super(new Image(Ball.class.getResourceAsStream("/images/ball.png")));
        commonInit(screenWidth, screenHeight, startX, startY);
        reset(); // Đặt tốc độ ban đầu
    }

    /**
     * (MỚI) Constructor phụ: Dùng cho Multi-Ball (6 tham số)
     * (Hàm này bị thiếu trong code của bạn, gây lỗi build)
     */
    public Ball(double screenWidth, double screenHeight, double x, double y, double dx, double dy) {
        super(new Image(Ball.class.getResourceAsStream("/images/ball.png")));
        commonInit(screenWidth, screenHeight, x, y);
        setLayoutX(x - BALL_RADIUS);
        setLayoutY(y - BALL_RADIUS);
        this.dx = dx;
        this.dy = dy;
    }

    // Hàm dùng chung
    private void commonInit(double screenWidth, double screenHeight, double startX, double startY) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.startX = startX;
        this.startY = startY;

        setFitWidth(BALL_RADIUS * 2);
        setFitHeight(BALL_RADIUS * 2);

        RotateTransition rt = new RotateTransition(Duration.seconds(2), this);
        rt.setByAngle(360);
        rt.setCycleCount(RotateTransition.INDEFINITE);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.play();
    }

    public void reset() {
        setLayoutX(startX - BALL_RADIUS);
        setLayoutY(startY - BALL_RADIUS);
        dx = rand.nextBoolean() ? 3 : -3;
        dy = -3;
    }

    public void update() {
        setLayoutX(getLayoutX() + dx);
        setLayoutY(getLayoutY() + dy);

        double x = getLayoutX();
        double y = getLayoutY();

        if (x <= 0 || x >= screenWidth - (BALL_RADIUS * 2)) reverseDx();
        if (y <= 0) reverseDy();
    }

    // --- Getters & Setters ---
    public void reverseDx() { this.dx = -this.dx; }
    public void reverseDy() { this.dy = -this.dy; }
    public double getRadius() { return BALL_RADIUS; }
    public double getDy() { return dy; }

    /**
     * (MỚI) Thêm hàm setDx để thay đổi góc nảy
     * (Hàm này bị thiếu trong code của bạn, gây lỗi build)
     */
    public void setDx(double dx) {
        if (dx > 5) dx = 5;
        if (dx < -5) dx = -5;
        this.dx = dx;
    }
}