package org.example.arkanoid;

import javafx.application.Application;
import javafx.stage.Stage;

public class ArkanoidApplication extends Application {
    @Override
    public void start(Stage stage) {
        GameScene gameScene = new GameScene(800, 600); // kích thước màn hình
        stage.setScene(gameScene.getScene());
        stage.setTitle("Arkanoid");
        stage.show();

        gameScene.startGame(); // bắt đầu game loop
    }

    public static void main(String[] args) {
        launch();
    }
}
