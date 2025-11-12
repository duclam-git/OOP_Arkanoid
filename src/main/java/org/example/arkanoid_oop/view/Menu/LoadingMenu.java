package org.example.arkanoid_oop.view.Menu;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.arkanoid_oop.model.Manager.AudioManager;

public class LoadingMenu extends Menu {

    public LoadingMenu(Stage stage) {
        super(stage);
    }

    @Override
    public void show() {
        // 1. THIẾT LẬP GIAO DIỆN

        // Nền (sử dụng ảnh nền của menu)
        Image bgImage = new Image(getClass().getResource("/images/concept.png").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(SCREEN_WIDTH);
        bgView.setFitHeight(SCREEN_HEIGHT);

        // Chỉ báo đang tải
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(80, 80);
        progressIndicator.setStyle("-fx-progress-color: #00ffff;");

        // Text trạng thái
        Text statusText = new Text("Đang tải...");
        statusText.setFont(Font.font("Orbitron", 20));
        statusText.setFill(Color.WHITE);

        // Gom nhóm chỉ báo và text
        VBox loadingBox = new VBox(20, progressIndicator, statusText);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 30; -fx-background-radius: 10;");

        // Đặt mọi thứ vào StackPane
        StackPane root = new StackPane(bgView, loadingBox);
        root.setAlignment(Pos.CENTER);

        // 2. TẠO TASK ĐA LUỒNG

        // Task này sẽ chạy các tác vụ nặng ở luồng nền
        Task<Void> loadingTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Tác vụ này chạy trên LUỒNG NỀN (Background Thread)

                // 1: Tải Settings
                updateMessage("Đang tải cài đặt...");
                audio.loadSettings(); // Đây là I/O
                updateProgress(1, 4);

                // 2: Tải Âm thanh
                updateMessage("Đang tải âm thanh...");
                audio.preloadSounds(); // Đây là I/O nặng
                updateProgress(2, 4);

                // 3: Tải trước (cache) các ảnh chính
                updateMessage("Đang tải hình ảnh...");
                new Image(getClass().getResource("/images/background.png").toExternalForm());
                new Image(getClass().getResource("/images/paddle.png").toExternalForm());
                new Image(getClass().getResource("/images/ball.png").toExternalForm());
                new Image(getClass().getResource("/images/Heart.png").toExternalForm());
                updateProgress(3, 4);

                // Chờ một chút để kịp thấy
                Thread.sleep(500);
                updateMessage("Hoàn tất!");
                updateProgress(4, 4);
                Thread.sleep(250); // Chờ 1/4s để thấy chữ "Hoàn tất"

                return null;
            }
        };

        // 3. KẾT NỐI UI VÀ TASK

        // Tự động cập nhật ProgressIndicator khi Task cập nhật tiến độ
        progressIndicator.progressProperty().bind(loadingTask.progressProperty());

        // Tự động cập nhật Text khi Task cập nhật message
        statusText.textProperty().bind(loadingTask.messageProperty());

        // 4. ĐỊNH NGHĨA HÀNH ĐỘNG KHI TASK XONG

        // Hàm này sẽ chạy trên LUỒNG UI (JavaFX Thread) sau khi 'call()' hoàn thành
        loadingTask.setOnSucceeded(e -> {
            // Khi tải xong, chuyển sang MainMenu
            MainMenu menu = new MainMenu(stage);
            menu.show();
        });

        // Xử lý nếu Task thất bại
        loadingTask.setOnFailed(e -> {
            statusText.setText("Lỗi! Không thể tải game.");
            statusText.setFill(Color.RED);
            progressIndicator.setVisible(false);
            loadingTask.getException().printStackTrace(); // In lỗi ra console
        });

        // 5. CHẠY TASK VÀ HIỂN THỊ SCENE

        // Khởi động Task trong một Thread mới
        new Thread(loadingTask).start();

        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Arkanoid: Quantum Rift - Loading..."); // Cập nhật tiêu đề cửa sổ
        stage.show();
    }
}