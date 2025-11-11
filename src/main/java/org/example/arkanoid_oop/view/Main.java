package org.example.arkanoid_oop.view;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.arkanoid_oop.view.Menu.LoadingMenu;

public class Main extends Application {



    @Override
    public void start(Stage stage) {
        // Bắt đầu bằng màn hình Loading
        LoadingMenu loadingMenu = new LoadingMenu(stage);
        loadingMenu.show();
    }

    // Hàm main để khởi chạy ứng dụng
    public static void main(String[] args) {
        launch(args);
    }
}