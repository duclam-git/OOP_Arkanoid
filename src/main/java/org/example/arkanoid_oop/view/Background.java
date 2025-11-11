package org.example.arkanoid_oop.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Background extends ImageView {

    public Background(double screenWidth, double screenHeight) {
        // Tải ảnh từ thư mục resources/images
        Image bgImage = new Image(getClass().getResourceAsStream("/images/background.png"));

        // Đặt ảnh này cho ImageView
        setImage(bgImage);

        // Thiết lập kích thước để vừa khít màn hình
        setFitWidth(screenWidth);
        setFitHeight(screenHeight);

        // Đặt vị trí (0, 0)
        setLayoutX(0);
        setLayoutY(0);
    }
}