package org.example.arkanoid.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;
import java.util.ArrayList;

public class Ball {
    private final ImageView imageView;
    private double dx = 2.5;
    private double dy = -2.5;
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
            imageView.setX(paddle.getNode().getX() + paddle.getNode().getBoundsInParent().getWidth()/2 - imageView.getBoundsInParent().getWidth()/2);
            imageView.setY(paddle.getNode().getY() - imageView.getBoundsInParent().getHeight());
            return;
        }

        // di chuyển
        imageView.setX(imageView.getX() + dx);
        imageView.setY(imageView.getY() + dy);

        // Va chạm tường
        if (imageView.getX() <= 0 || imageView.getX() + imageView.getBoundsInParent().getWidth() >= sceneWidth) dx = -dx;
        if (imageView.getY() <= 0) dy = -dy;

        // Va chạm paddle
        if (imageView.getBoundsInParent().intersects(paddle.getNode().getBoundsInParent())) {
            dy = -Math.abs(dy);
        }

        // Va chạm bricks
        for (Brick brick : bricks) {
            if (!brick.isDestroyed() && imageView.getBoundsInParent().intersects(brick.getNode().getBoundsInParent())) {
                brick.destroy();
                dy = -dy;
                break;
            }
        }

        // Rơi dưới đáy
        if (imageView.getY() > sceneHeight) {
            moving = false;
        }
    }


    // Cho phép chỉnh tốc độ
    public void setSpeed(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }
}
