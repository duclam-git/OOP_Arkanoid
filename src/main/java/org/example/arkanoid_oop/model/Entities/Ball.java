package org.example.arkanoid_oop.model.Entities;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.example.arkanoid_oop.model.util.GameSettings.GameMode; // THÊM IMPORT

/**
 * Lớp Ball (quả bóng/thiên thạch) kế thừa ImageView.
 * Tự quản lý vị trí và logic di chuyển.
 */
public class Ball extends ImageView {

    public static final double BALL_RADIUS = 10; // Kích thước bán kính hiển thị

    // Tốc độ và hướng di chuyển
    private double dx;
    private double dy;
    private double ballSpeed; // THÊM BIẾN TỐC ĐỘ CỦA INSTANCE
    private GameMode gameMode; // THÊM BIẾN ĐỂ LƯU GAMEMODE

    private double screenWidth, screenHeight;
    private double startX, startY; // Vị trí ban đầu

    // Hàm khởi tạo thứ nhất
    public Ball(double screenWidth, double screenHeight, double startX, double startY, String skinPath, GameMode gameMode) {
        super(new Image(Ball.class.getResourceAsStream(skinPath)));

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.startX = startX;
        this.startY = startY;
        this.gameMode = gameMode; // LƯU GAMEMODE

        // Đặt tốc độ dựa trên độ khó
        this.ballSpeed = getBaseBallSpeed(this.gameMode);

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

    // Hàm khởi tạo thứ 2
    public Ball(double currentX, double currentY, double screenWidth, double screenHeight, double dx, double dy, String skinPath, GameMode gameMode) {
        this(screenWidth, screenHeight, currentX, currentY, skinPath, gameMode); // Gọi constructor chính

        // Ghi đè lại vị trí và hướng
        setLayoutX(currentX - BALL_RADIUS);
        setLayoutY(currentY - BALL_RADIUS);
        this.dx = dx;
        this.dy = dy;
        this.startX = currentX;
        this.startY = currentY;
    }

    /**
     * (HÀM MỚI) Lấy tốc độ cơ sở của bóng dựa trên độ khó.
     */
    private double getBaseBallSpeed(GameMode mode) {
        switch (mode) {
            case EASY:
                return 2.0; // Dễ = Bóng chậm
            case HARD:
                return 3.5; // Khó = Bóng nhanh
            case NORMAL:
            default:
                return 2.5; // Bình thường
        }
    }

    /**
     * Đặt bóng về vị trí và tốc độ ban đầu.
     */
    public void reset() {
        // Đặt vị trí (layoutX/Y là góc trên trái, nên trừ đi bán kính)
        setLayoutX(startX - BALL_RADIUS);
        setLayoutY(startY - BALL_RADIUS);

        // Tốc độ ngẫu nhiên ban đầu
        dx = Math.random() > 0.5 ? this.ballSpeed : -this.ballSpeed;
        dy = -this.ballSpeed;
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

        // Giới hạn tốc độ để không bị lỗi nảy do quá nhanh
        if (Math.abs(dx) > this.ballSpeed * 1.5) {
            dx = (dx > 0) ? this.ballSpeed * 1.5 : -this.ballSpeed * 1.5;
        }
    }

    // Thiết lập hướng mới sau va chạm ván trượt
    public void setDirection(double newDx, double newDy) {
        // Đảm bảo tốc độ không đổi, chỉ hướng thay đổi
        double currentSpeed = this.ballSpeed;

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

    // SỬA HÀM NÀY
    public double getSpeed() {
        return this.ballSpeed; // Trả về tốc độ cơ sở của instance này
    }
}