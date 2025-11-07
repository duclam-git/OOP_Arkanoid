package org.example.arkanoid_oop.Entities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Lớp Paddle (ván trượt) kế thừa ImageView.
 * Tự quản lý vị trí và logic di chuyển.
 */
public class Paddle extends ImageView {

    private static final double PADDLE_WIDTH_DISPLAY = 120;
    private static final double PADDLE_HEIGHT_DISPLAY = 30; // NEW: Thêm hằng số chiều cao cố định
    private static final int PADDLE_SPEED = 6;

    private double screenWidth;
    private double currentWidth; // Chiều rộng thực tế sau khi đặt ảnh

    private double startX;
    private double startY;
    private double originalWidth; // Lưu trữ chiều rộng gốc

    public Paddle(double screenWidth, double screenHeight) {
        super(new Image(Paddle.class.getResourceAsStream("/images/paddle.png")));

        this.screenWidth = screenWidth;

        // Thiết lập kích thước cho ảnh
        setFitWidth(PADDLE_WIDTH_DISPLAY);
        setFitHeight(PADDLE_HEIGHT_DISPLAY); // CỐ ĐỊNH CHIỀU CAO
        // setPreserveRatio(true); // ĐÃ XÓA: Không giữ tỷ lệ để cố định chiều cao

        this.originalWidth = PADDLE_WIDTH_DISPLAY; // Lưu trữ kích thước gốc

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
        // Đảm bảo kích thước trở về ban đầu
        setNormalLength();

        setLayoutX(startX);
        setLayoutY(startY);
    }

    // (CẬP NHẬT) Tăng gấp đôi chiều dài Paddle (Giữ nguyên chiều dọc)
    public void setDoubleLength() {
        if (getFitWidth() == originalWidth) {
            double currentX = getLayoutX();

            // Cập nhật chiều rộng (Dài gấp đôi)
            setFitWidth(originalWidth * 2);
            this.currentWidth = getFitWidth();

            // Chiều cao KHÔNG ĐỔI
            setFitHeight(PADDLE_HEIGHT_DISPLAY);

            // Điều chỉnh vị trí để tâm không đổi
            setLayoutX(currentX - originalWidth / 2.0);

            if (getLayoutX() < 0) setLayoutX(0);
            if (getLayoutX() > (screenWidth - currentWidth)) setLayoutX(screenWidth - currentWidth);
        }
    }

    // (CẬP NHẬT) Đặt chiều dài Paddle về kích thước gốc
    public void setNormalLength() {
        if (getFitWidth() != originalWidth) {
            double currentX = getLayoutX();

            // Cập nhật chiều rộng
            setFitWidth(originalWidth);
            this.currentWidth = getFitWidth();

            // Chiều cao KHÔNG ĐỔI
            setFitHeight(PADDLE_HEIGHT_DISPLAY);

            // Điều chỉnh vị trí để tâm không đổi
            setLayoutX(currentX + originalWidth / 2.0);

            if (getLayoutX() < 0) setLayoutX(0);
            if (getLayoutX() > (screenWidth - currentWidth)) setLayoutX(screenWidth - currentWidth);
        }
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