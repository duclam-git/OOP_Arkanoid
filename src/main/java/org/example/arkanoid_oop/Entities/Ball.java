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

    public static final double BALL_RADIUS = 10; // Kích thước bán kính hiển thị
    // (MỚI) Hằng số tốc độ
    public static final double BALL_SPEED = 2.5;

    // Tốc độ và hướng di chuyển
    private double dx;
    private double dy;

    private double screenWidth, screenHeight;
    private double startX, startY; // Vị trí ban đầu

    public Ball(double screenWidth, double screenHeight, double startX, double startY) {
        // 1. Tải ảnh quả bóng (hoặc thiên thạch)
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

    // (MỚI) Phương thức khởi tạo cho Multi-Ball, sử dụng vị trí hiện tại
    public Ball(double currentX, double currentY, double screenWidth, double screenHeight, double dx, double dy) {
        this(screenWidth, screenHeight, currentX, currentY); // Gọi constructor chính để khởi tạo hình ảnh và animation
        // Ghi đè lại vị trí và hướng
        setLayoutX(currentX - BALL_RADIUS);
        setLayoutY(currentY - BALL_RADIUS);
        this.dx = dx;
        this.dy = dy;
        this.startX = currentX;
        this.startY = currentY;
    }

    /**
     * Đặt bóng về vị trí và tốc độ ban đầu.
     */
    public void reset() {
        // Đặt vị trí (layoutX/Y là góc trên trái, nên trừ đi bán kính)
        setLayoutX(startX - BALL_RADIUS);
        setLayoutY(startY - BALL_RADIUS);

        // Tốc độ ngẫu nhiên ban đầu
        dx = Math.random() > 0.5 ? BALL_SPEED : -BALL_SPEED; // Sử dụng hằng số
        dy = -BALL_SPEED; // Luôn bay lên
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
        if (x <= 0) {
            setLayoutX(0); // Đẩy bóng về biên
            reverseDx();
        } else if (x >= screenWidth - (BALL_RADIUS * 2)) {
            setLayoutX(screenWidth - (BALL_RADIUS * 2)); // Đẩy bóng về biên
            reverseDx();
        }

        // Va chạm tường trên
        if (y <= 0) {
            setLayoutY(0); // Đẩy bóng về biên
            reverseDy();
        }

        // (MỚI) Giới hạn tốc độ để không bị lỗi nảy do quá nhanh
        if (Math.abs(dx) > BALL_SPEED * 1.5) {
            dx = (dx > 0) ? BALL_SPEED * 1.5 : -BALL_SPEED * 1.5;
        }
    }

    // (MỚI) Thiết lập hướng mới sau va chạm ván trượt
    public void setDirection(double newDx, double newDy) {
        // Đảm bảo tốc độ không đổi, chỉ hướng thay đổi
        double currentSpeed = Math.sqrt(dx * dx + dy * dy);

        // Chuẩn hóa hướng mới
        double magnitude = Math.sqrt(newDx * newDx + newDy * newDy);

        if (magnitude > 0) {
            double ratio = currentSpeed / magnitude;
            this.dx = newDx * ratio;
            this.dy = newDy * ratio;
        } else {
            // Trường hợp lỗi, chỉ đảo dy để tiếp tục
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

    // Thêm getter cho tốc độ cơ sở
    public double getSpeed() {
        return BALL_SPEED;
    }
}