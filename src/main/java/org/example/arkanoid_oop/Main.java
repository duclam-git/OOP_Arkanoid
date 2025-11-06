package org.example.arkanoid_oop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) {

        GamePane gamePane = GamePane.getInstance(SCREEN_WIDTH, SCREEN_HEIGHT);

        // Tạo Scene và đặt GamePane làm nội dung chính
        Scene scene = new Scene(gamePane, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Gửi sự kiện nhấn/thả phím tới GamePane để xử lý
        scene.setOnKeyPressed(event -> gamePane.handleKeyPressed(event.getCode()));
        scene.setOnKeyReleased(event -> gamePane.handleKeyReleased(event.getCode()));

        // Hiển thị cửa sổ
        primaryStage.setTitle("Game Arkanoid (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Yêu cầu GamePane "focus" để nó có thể nhận sự kiện phím
        gamePane.requestFocus();
    }

    // Hàm main để khởi chạy ứng dụng
    public static void main(String[] args) {
        launch(args);
    }
}