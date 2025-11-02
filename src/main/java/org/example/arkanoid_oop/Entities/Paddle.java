package org.example.arkanoid_oop.Entities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Lớp Paddle (ván trượt) kế thừa ImageView.
 * Tự quản lý vị trí và logic di chuyển.
 */
public class Paddle extends ImageView {

    private static final double PADDLE_WIDTH_DISPLAY = 120;
    private static final int PADDLE_SPEED = 6;

    private double screenWidth;
    private double currentWidth; // Chiều rộng thực tế sau khi đặt ảnh

    private double startX;
    private double startY;

    public Paddle(double screenWidth, double screenHeight) {
        super(new Image(Paddle.class.getResourceAsStream("/images/paddle.png")));

        this.screenWidth = screenWidth;

        // Thiết lập kích thước cho ảnh
        setFitWidth(PADDLE_WIDTH_DISPLAY);
        setPreserveRatio(true); // Tỉ lệ ảnh
        this.currentWidth = getFitWidth(); // Lấy chiều rộng sau khi đã đặt setFitWidth

        // Tính toán vị trí ban đầu
        this.startX = (screenWidth - currentWidth) / 2.0;
        this.startY = screenHeight - 60;

        // Áp dụng vị trí ban đầu
        reset();
    }

    /**
     * Hàm đặt ván trượt về vị trí ban đầu (ở giữa, gần đáy).
     */
    public void reset() {
        setLayoutX(startX);
        setLayoutY(startY);
    }

    /**
     * Hàm cập nhật vị trí, được gọi liên tục từ GamePane.
     * @param goLeft Cờ báo di chuyển sang trái
     * @param goRight Cờ báo di chuyển sang phải
     */
    public void update(boolean goLeft, boolean goRight) {
        double x = getLayoutX();

        if (goLeft) {
            x -= PADDLE_SPEED;
        }
        if (goRight) {
            x += PADDLE_SPEED;
        }

        if (x < 0) {
            x = 0;
        }
        if (x > (screenWidth - currentWidth)) {
            x = screenWidth - currentWidth;
        }

        setLayoutX(x);
    }
}