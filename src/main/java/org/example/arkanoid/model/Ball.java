package org.example.arkanoid.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;

public class Ball {
    private final ImageView imageView;
    private double dx = 2.2;
    private double dy = -2.2;
    private boolean moving = false;

    public Ball(double x, double y, String imagePath, double width, double height) {
        Image img = new Image(getClass().getResource(imagePath).toExternalForm());
        imageView = new ImageView(img);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setX(x);
        imageView.setY(y);
    }

    public ImageView getNode() {
        return imageView;
    }

    public void launch() {
        moving = true;
    }

    public void updatePosition(double sceneWidth, double sceneHeight, Paddle paddle, List<Brick> bricks) {
        if (!moving) {
            // giữ bóng trên paddle
            imageView.setX(paddle.getNode().getX() + paddle.getNode().getBoundsInParent().getWidth() / 2 - imageView.getBoundsInParent().getWidth() / 2);
            imageView.setY(paddle.getNode().getY() - imageView.getBoundsInParent().getHeight());
            return;
        }

        // Di chuyển bóng
        imageView.setX(imageView.getX() + dx);
        imageView.setY(imageView.getY() + dy);

        // Va chạm tường
        if (imageView.getX() <= 0 || imageView.getX() + imageView.getBoundsInParent().getWidth() >= sceneWidth)
            dx = -dx;
        if (imageView.getY() <= 0)
            dy = -dy;

        // Va chạm paddle (phản xạ theo vị trí chạm)
        if (imageView.getBoundsInParent().intersects(paddle.getNode().getBoundsInParent())) {
            double paddleX = paddle.getNode().getX();
            double paddleWidth = paddle.getNode().getBoundsInParent().getWidth();
            double ballCenterX = imageView.getX() + imageView.getFitWidth() / 2;

            // Tính góc phản xạ theo vị trí chạm trên paddle
            double hitPosition = (ballCenterX - paddleX) / paddleWidth; // từ 0 (trái) -> 1 (phải)
            double angle = (hitPosition - 0.5) * 120; // -60° đến +60°
            double speed = Math.sqrt(dx * dx + dy * dy);

            dx = speed * Math.sin(Math.toRadians(angle));
            dy = -Math.abs(speed * Math.cos(Math.toRadians(angle))); // luôn bật lên
        }

        // Va chạm bricks
        for (Brick brick : bricks) {
            if (!brick.isDestroyed() && imageView.getBoundsInParent().intersects(brick.getNode().getBoundsInParent())) {

                // Tính vị trí tương đối để xác định hướng bật
                double ballCenterX = imageView.getX() + imageView.getFitWidth() / 2;
                double ballCenterY = imageView.getY() + imageView.getFitHeight() / 2;
                double brickX = brick.getNode().getX();
                double brickY = brick.getNode().getY();
                double brickW = brick.getNode().getFitWidth();
                double brickH = brick.getNode().getFitHeight();

                double overlapLeft = (ballCenterX + imageView.getFitWidth() / 2) - brickX;
                double overlapRight = (brickX + brickW) - (ballCenterX - imageView.getFitWidth() / 2);
                double overlapTop = (ballCenterY + imageView.getFitHeight() / 2) - brickY;
                double overlapBottom = (brickY + brickH) - (ballCenterY - imageView.getFitHeight() / 2);

                // Xác định hướng va chạm nhỏ nhất
                double minOverlapX = Math.min(overlapLeft, overlapRight);
                double minOverlapY = Math.min(overlapTop, overlapBottom);

                brick.hit();

                if (minOverlapX < minOverlapY) {
                    dx = -dx; // va bên trái/phải
                } else {
                    dy = -dy; // va trên/dưới
                }
                break;
            }
        }

        // Rơi dưới đáy → dừng lại và chờ reset
        if (imageView.getY() > sceneHeight) {
            moving = false;
        }
    }
}
