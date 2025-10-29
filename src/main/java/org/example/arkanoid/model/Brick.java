package org.example.arkanoid.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Brick {
    private final ImageView imageView;
    private boolean destroyed = false;
    private int health;
    private final int maxHealth;
    private final String baseImagePath;

    public Brick(double x, double y, String imagePath, double width, double height) {
        this.baseImagePath = imagePath;
        this.health = extractHealthFromImage(imagePath);
        this.maxHealth = this.health;

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

    public int getMaxHealth() {
        return maxHealth;
    }


    public void hit() {
        if (destroyed) return;

        health--;
        if (health <= 0) {
            destroyed = true;
            imageView.setVisible(false);
        } else {
            // Cập nhật hình ảnh theo lượng máu còn lại
            String nextImage = baseImagePath.replaceAll("\\d+(?=\\.png$)", String.valueOf(health));
            imageView.setImage(new Image(getClass().getResource(nextImage).toExternalForm()));
        }
    }


    private int extractHealthFromImage(String path) {
        try {
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String number = fileName.replaceAll("\\D+", ""); // Lấy phần số
            return number.isEmpty() ? 1 : Integer.parseInt(number);
        } catch (Exception e) {
            return 1; // Mặc định 1 máu nếu không có số
        }
    }
}

