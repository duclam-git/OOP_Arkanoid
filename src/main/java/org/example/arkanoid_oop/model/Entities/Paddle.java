package org.example.arkanoid_oop.model.Entities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.arkanoid_oop.model.util.GameSettings.GameMode;

/**
 * Lớp Paddle (ván trượt) kế thừa ImageView.
 * Tự quản lý vị trí và logic di chuyển.
 */
public class Paddle extends ImageView {

    // private static final double PADDLE_WIDTH_DISPLAY = 120; // ĐÃ XÓA
    private static final double PADDLE_HEIGHT_DISPLAY = 30;
    private static final int PADDLE_SPEED = 6;

    private double screenWidth;
    private double currentWidth; // Chiều rộng thực tế sau khi đặt ảnh

    private double startX;
    private double startY;
    private double originalWidth; // Lưu trữ chiều rộng gốc

    // SỬA HÀM KHỞI TẠO (THÊM THAM SỐ 'gameMode')
    public Paddle(double screenWidth, double screenHeight, String skinPath, GameMode gameMode) {
        super(new Image(Paddle.class.getResourceAsStream(skinPath)));

        this.screenWidth = screenWidth;

        // Tính toán chiều rộng gốc dựa trên độ khó
        this.originalWidth = getBasePaddleWidth(gameMode);

        // Thiết lập kích thước cho ảnh
        setFitWidth(this.originalWidth);
        setFitHeight(PADDLE_HEIGHT_DISPLAY); // CỐ ĐỊNH CHIỀU CAO

        this.currentWidth = getFitWidth(); // Lấy chiều rộng sau khi đã đặt setFitWidth

        // Tính toán vị trí ban đầu
        this.startX = (screenWidth - currentWidth) / 2.0;
        this.startY = screenHeight - 60;

        // Áp dụng vị trí ban đầu
        reset();
    }

    /**
     * Lấy chiều rộng cơ sở của ván trượt dựa trên độ khó.
     */
    private double getBasePaddleWidth(GameMode mode) {
        switch (mode) {
            case EASY:
                return 150; // Dễ = Ván to hơn
            case HARD:
                return 90;  // Khó = Ván nhỏ hơn
            case NORMAL:
            default:
                return 120; // Bình thường
        }
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

    // Tăng gấp đôi chiều dài Paddle (Giữ nguyên chiều dọc)
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

    // Đặt chiều dài Paddle về kích thước gốc
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