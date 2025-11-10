package org.example.arkanoid_oop.Entities; // (Hoặc package bạn muốn, ví dụ: org.example.arkanoid_oop.Effects)

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.example.arkanoid_oop.Entities.Ball; // Import lớp Ball

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lớp này quản lý việc tạo và hiển thị hiệu ứng "đuôi" (motion trail)
 * cho tất cả các quả bóng trong game.
 */
public class BallTrailManager {

    private Pane gamePane; // Pane chính của game để thêm/xóa vệt đuôi
    private List<ImageView> trailParts = new ArrayList<>(); // Danh sách các vệt đuôi
    private Image ballImage; // Ảnh của quả bóng, dùng để tạo bản sao

    // Tốc độ mờ và tần suất tạo đuôi
    private static final double FADE_SPEED = 0.04; // Tốc độ mờ (cao hơn = mờ nhanh hơn)
    private static final int SPAWN_FREQUENCY = 3;  // Tạo đuôi sau mỗi 3 khung hình

    public BallTrailManager(Pane gamePane) {
        this.gamePane = gamePane;

        // Tải ảnh quả bóng một lần duy nhất
        try {
            // Lấy ảnh từ file ball.png
            ballImage = new Image(getClass().getResourceAsStream("/images/ball.png"));
            if (ballImage.isError()) throw new Exception(ballImage.getException());
        } catch (Exception e) {
            System.err.println("Lỗi: Không tải được ảnh 'ball.png' cho hiệu ứng đuôi: " + e.getMessage());
            ballImage = null; // Sẽ không có hiệu ứng nếu ảnh lỗi
        }
    }

    /**
     * Tạo một vệt đuôi mới cho một quả bóng cụ thể.
     * @param ball Quả bóng đang theo dõi
     * @param frameCount Khung hình hiện tại (để quyết định tần suất)
     */
    public void spawnTrail(Ball ball, long frameCount) {
        // Chỉ tạo đuôi nếu ảnh đã được tải VÀ đúng tần suất
        if (ballImage == null || frameCount % SPAWN_FREQUENCY != 0) {
            return;
        }

        // Tạo một ImageView "ma" (ghost)
        ImageView trailGhost = new ImageView(ballImage);
        trailGhost.setFitWidth(Ball.BALL_RADIUS * 2);
        trailGhost.setFitHeight(Ball.BALL_RADIUS * 2);

        // Đặt tại vị trí của bóng
        trailGhost.setLayoutX(ball.getLayoutX());
        trailGhost.setLayoutY(ball.getLayoutY());

        trailGhost.setOpacity(0.5); // Độ mờ ban đầu

        // Thêm vào danh sách và vào màn hình
        trailParts.add(trailGhost);
        gamePane.getChildren().add(trailGhost);

        // Đẩy vệt mờ xuống dưới lớp của bóng (nhưng trên nền)
        trailGhost.toBack();
    }

    /**
     * Cập nhật tất cả các vệt đuôi (làm mờ và xóa).
     * Hàm này phải được gọi MỖI KHUNG HÌNH trong game loop.
     */
    public void update() {
        if (trailParts.isEmpty()) return; // Không có gì để làm

        Iterator<ImageView> it = trailParts.iterator();
        while (it.hasNext()) {
            ImageView trailPart = it.next();
            // Giảm độ mờ
            double newOpacity = trailPart.getOpacity() - FADE_SPEED;

            if (newOpacity <= 0) {
                // Nếu mờ hết -> Xóa
                gamePane.getChildren().remove(trailPart);
                it.remove();
            } else {
                // Nếu còn -> cập nhật độ mờ và làm nó nhỏ lại
                trailPart.setOpacity(newOpacity);
                trailPart.setScaleX(trailPart.getScaleX() * 0.98); // Nhỏ dần
                trailPart.setScaleY(trailPart.getScaleY() * 0.98);
            }
        }
    }

    /**
     * Xóa tất cả các vệt đuôi ngay lập tức (dùng khi reset/mất mạng).
     */
    public void clear() {
        for (ImageView trailPart : trailParts) {
            gamePane.getChildren().remove(trailPart);
        }
        trailParts.clear();
    }
}