package org.example.arkanoid_oop.view.Menu;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.arkanoid_oop.model.util.GameSettings.GameMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class HighScoreMenu extends Menu {
    private static final int MAX_SCORES = 10;

    // Giữ nguyên các hàm static (load, save, update)
    private static String getHighScoreFile(GameMode mode) {
        return "highscore_" + mode.name().toLowerCase() + ".txt";
    }

    public static List<Integer> loadHighScores(GameMode mode) {
        List<Integer> highScores = new ArrayList<>();
        try {
            File file = new File(getHighScoreFile(mode));
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    try {
                        int score = Integer.parseInt(scanner.nextLine().trim());
                        highScores.add(score);
                    } catch (NumberFormatException ignored) {
                    }
                }
                scanner.close();
            }
            highScores.sort(Collections.reverseOrder());
            if (highScores.size() > MAX_SCORES) {
                highScores = highScores.subList(0, MAX_SCORES);
            }
        } catch (Exception e) {
            System.out.println("Lỗi: Không thể tải high scores cho chế độ " + mode.getDisplayName());
        }
        if (highScores.isEmpty()) {
            highScores.add(0);
        }
        return highScores;
    }

    public static void saveHighScores(List<Integer> highScores, GameMode mode) {
        highScores.sort(Collections.reverseOrder());
        if (highScores.size() > MAX_SCORES) {
            highScores = highScores.subList(0, MAX_SCORES);
        }

        try {
            PrintWriter writer = new PrintWriter(getHighScoreFile(mode));
            for (int score : highScores) {
                writer.println(score);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            System.err.println("Lỗi khi lưu high scores: " + e.getMessage());
        }
    }

    public static boolean updateHighScores(int newScore, GameMode mode) {
        List<Integer> highScores = loadHighScores(mode);
        boolean isNewTopScore = false;

        if (highScores.size() < MAX_SCORES || newScore > highScores.get(highScores.size() - 1)) {
            if (highScores.size() == 1 && highScores.get(0) == 0) {
                highScores.clear();
            }

            highScores.add(newScore);
            highScores.sort(Collections.reverseOrder());

            if (highScores.size() > MAX_SCORES) {
                highScores = highScores.subList(0, MAX_SCORES);
            }

            saveHighScores(highScores, mode);

            if (!highScores.isEmpty() && highScores.get(0) == newScore) {
                isNewTopScore = true;
            }
        }

        return isNewTopScore;
    }

    // HÀM SHOW()
    public HighScoreMenu(Stage stage) {
        super(stage);
    }

    @Override
    public void show() {
        // ẢNH NỀN
        Image bgImage = new Image(getClass().getResource("/images/concept.png").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(SCREEN_WIDTH);
        bgView.setFitHeight(SCREEN_HEIGHT);
        bgView.setPreserveRatio(false);

        StackPane root = new StackPane();
        root.getChildren().add(bgView);

        // TIÊU ĐỀ CHUNG
        Text title = new Text("HIGH SCORE LEADERBOARD");
        title.setFont(Font.font("Orbitron", 40));
        title.setStyle("-fx-fill: #00ffff; -fx-effect: dropshadow(gaussian, black, 10, 0, 0, 0);");

        // NƠI CHỨA CÁC BẢNG ĐIỂM
        HBox scoreDisplayContainer = new HBox(30);
        scoreDisplayContainer.setAlignment(Pos.CENTER);
        scoreDisplayContainer.setPadding(new Insets(0, 0, 50, 0));

        // BIỂU TƯỢNG LOADING
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(80, 80);
        loadingIndicator.setStyle("-fx-progress-color: #00ffff;");

        // CONTAINER CHÍNH
        VBox mainContainer = new VBox(20, title, loadingIndicator, scoreDisplayContainer);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-border-color: #00ffff; -fx-border-width: 2;");

        // NÚT BACK
        Button backButton = createBackButton();

        root.getChildren().addAll(mainContainer, backButton);


        // LOGIC ĐA LUỒNG
        // 1. Tạo Task để tải tất cả điểm ở luồng nền
        Task<Map<GameMode, List<Integer>>> loadScoresTask = new Task<>() {
            @Override
            protected Map<GameMode, List<Integer>> call() throws Exception {
                // Hàm này chạy trên LUỒNG NỀN (Background Thread)
                Map<GameMode, List<Integer>> allScores = new EnumMap<>(GameMode.class);

                // Giả lập độ trễ một chút để thấy loading
                // Thread.sleep(1000);

                for (GameMode mode : GameMode.values()) {
                    // Gọi hàm đọc file
                    List<Integer> scores = HighScoreMenu.loadHighScores(mode);
                    allScores.put(mode, scores);
                }
                return allScores;
            }
        };

        // 2. Định nghĩa hàm chạy khi Task hoàn thành (Thành công)
        loadScoresTask.setOnSucceeded(event -> {
            // Hàm này chạy trên LUỒNG UI (JavaFX Application Thread)
            Map<GameMode, List<Integer>> allScores = loadScoresTask.getValue();

            // Ẩn loading
            loadingIndicator.setVisible(false);
            mainContainer.getChildren().remove(loadingIndicator);

            // Duyệt qua kết quả và tạo bảng điểm
            for (GameMode mode : GameMode.values()) {
                List<Integer> scores = allScores.get(mode);
                if (scores != null) {
                    VBox modeScoresDisplay = createScoreListDisplay(mode, scores);
                    scoreDisplayContainer.getChildren().add(modeScoresDisplay);
                }
            }
        });

        // 3. Định nghĩa hàm chạy khi Task thất bại
        loadScoresTask.setOnFailed(event -> {
            // Hàm này chạy trên LUỒNG UI
            loadingIndicator.setVisible(false);
            Text errorText = new Text("Lỗi: Không thể tải điểm cao.");
            errorText.setFont(Font.font("Orbitron", 20));
            errorText.setFill(Color.RED);
            mainContainer.getChildren().set(1, errorText); // Thay thế loading bằng text lỗi
        });

        // 4. Khởi động Task trong một Thread mới
        new Thread(loadScoresTask).start();

        // HIỂN THỊ SCENE
        Scene menuScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(menuScene);
        stage.show();
    }

    /**
     * (CẬP NHẬT) Hàm này giờ nhận danh sách điểm đã được tải
     */
    private VBox createScoreListDisplay(GameMode mode, List<Integer> scores) {
        Text modeTitle = new Text(mode.getDisplayName().toUpperCase());
        modeTitle.setFont(Font.font("Orbitron", 22));
        Color modeColor = switch (mode) {
            case EASY -> Color.LIMEGREEN;
            case NORMAL -> Color.LIGHTBLUE;
            case HARD -> Color.ORANGE;
        };
        modeTitle.setFill(modeColor);

        VBox listContainer = new VBox(5);
        listContainer.setAlignment(Pos.CENTER_LEFT);
        listContainer.setMinWidth(SCREEN_WIDTH * 0.2);
        listContainer.setMaxWidth(SCREEN_WIDTH * 0.25);

        for (int i = 0; i < scores.size(); i++) {
            int score = scores.get(i);
            int rank = i + 1;
            Text scoreEntry = new Text(String.format("%2d. %8d", rank, score));
            scoreEntry.setFont(Font.font("Monospace", 24));
            if (rank == 1) scoreEntry.setFill(Color.YELLOW);
            else if (rank <= 3) scoreEntry.setFill(Color.LIGHTBLUE);
            else scoreEntry.setFill(Color.WHITE);
            listContainer.getChildren().add(scoreEntry);
        }

        VBox wrapper = new VBox(10, modeTitle, listContainer);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle("-fx-border-color: " + toWebHex(modeColor) + "; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: rgba(0, 0, 0, 0.3);");
        return wrapper;
    }

    private String toWebHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private Button createBackButton() {
        Button backBtn = new Button("BACK");
        backBtn.setFont(Font.font("Orbitron", 18));
        backBtn.setStyle("""
            -fx-background-color: rgba(0, 0, 0, 0.8);
            -fx-text-fill: white;
            -fx-border-color: #00ffff;
            -fx-border-width: 2;
            -fx-background-radius: 5;
            -fx-border-radius: 5;
            -fx-padding: 8 15;
        """);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color: #00ffff; -fx-text-fill: black; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 8 15;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: white; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 8 15;"));
        backBtn.setOnAction(e -> {
            audio.play("click");
            MainMenu menu = new MainMenu(stage);
            menu.show();
        });

        StackPane.setAlignment(backBtn, Pos.BOTTOM_LEFT);
        StackPane.setMargin(backBtn, new Insets(0, 0, 10, 10));

        return backBtn;
    }
}