package org.example.arkanoid.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Paddle {
    private final ImageView imageView;
    private final double speed = 3;

    // Thêm width và height để chỉnh kích cỡ
    public Paddle(double x, double y, String imagePath, double width, double height) {
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

    public void moveLeft() {
        if (imageView.getX() > 0)
            imageView.setX(imageView.getX() - speed);
    }

    public void moveRight() {
        if (imageView.getX() + imageView.getBoundsInParent().getWidth() < 800)
            imageView.setX(imageView.getX() + speed);
    }
}
