package org.example.arkanoid.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Brick {
    private final ImageView imageView;
    private boolean destroyed = false;


    public Brick(double x, double y, String imagePath, double width, double height) {
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

    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Khi bị phá hủy, ẩn brick và đánh dấu destroyed
     */
    public void destroy() {
        destroyed = true;
        imageView.setVisible(false);
    }
}
