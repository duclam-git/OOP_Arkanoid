package org.example.arkanoid_oop.Menu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.arkanoid_oop.GameSettings.GameMode; // NEW IMPORT

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class HighScoreMenu extends Menu {
    private static final int MAX_SCORES = 10;

    // NEW: Phương thức lấy tên file High Score dựa trên chế độ
    private static String getHighScoreFile(GameMode mode) {
        return "highscore_" + mode.name().toLowerCase() + ".txt";
    }

    public HighScoreMenu(Stage stage) {
        super(stage);
    }

    /**
     * Tải danh sách điểm cao (tối đa 10) từ file theo chế độ.
     */
    public static List<Integer> loadHighScores(GameMode mode) { // MODIFIED
        List<Integer> highScores = new ArrayList<>();
        try {
            File file = new File(getHighScoreFile(mode)); // MODIFIED
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

    /**
     * Lưu danh sách điểm cao ra file theo chế độ.
     */
    public static void saveHighScores(List<Integer> highScores, GameMode mode) { // MODIFIED
        highScores.sort(Collections.reverseOrder());
        if (highScores.size() > MAX_SCORES) {
            highScores = highScores.subList(0, MAX_SCORES);
        }

        try {
            PrintWriter writer = new PrintWriter(getHighScoreFile(mode)); // MODIFIED
            for (int score : highScores) {
                writer.println(score);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            System.err.println("Lỗi khi lưu high scores: " + e.getMessage());
        }
    }

    /**
     * Cập nhật điểm mới vào danh sách. Trả về true nếu là điểm cao nhất mới.
     */
    public static boolean updateHighScores(int newScore, GameMode mode) { // MODIFIED
        List<Integer> highScores = loadHighScores(mode); // MODIFIED
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

            saveHighScores(highScores, mode); // MODIFIED

            if (!highScores.isEmpty() && highScores.get(0) == newScore) {
                isNewTopScore = true;
            }
        }

        return isNewTopScore;
    }

    @Override
    public void show() {
        Image bgImage = new Image(getClass().getResource("/images/concept.png").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(SCREEN_WIDTH);
        bgView.setFitHeight(SCREEN_HEIGHT);
        bgView.setPreserveRatio(false);

        StackPane root = new StackPane();
        root.getChildren().add(bgView);

        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-border-color: #00ffff; -fx-border-width: 2;");

        Text title = new Text("HIGH SCORE LEADERBOARD");
        title.setFont(Font.font("Orbitron", 40));
        title.setStyle("-fx-fill: #00ffff; -fx-effect: dropshadow(gaussian, black, 10, 0, 0, 0);");

        // NEW: Container cho các bảng điểm
        HBox scoreDisplayContainer = new HBox(30); // Giảm khoảng cách giữa 3 cột
        scoreDisplayContainer.setAlignment(Pos.CENTER);
        scoreDisplayContainer.setPadding(new Insets(0, 0, 50, 0));


        // Duyệt qua tất cả các chế độ để tạo bảng điểm tương ứng
        for (GameMode mode : GameMode.values()) {
            VBox modeScores = createScoreListDisplay(mode); // MODIFIED
            scoreDisplayContainer.getChildren().add(modeScores);
        }

        mainContainer.getChildren().addAll(title, scoreDisplayContainer); // MODIFIED

        Button backButton = createBackButton();

        root.getChildren().addAll(mainContainer, backButton);

        Scene menuScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(menuScene);
        stage.show();
    }

    private VBox createScoreListDisplay(GameMode mode) { // MODIFIED: Thêm tham số mode
        // Tiêu đề chế độ
        Text modeTitle = new Text(mode.getDisplayName().toUpperCase());
        modeTitle.setFont(Font.font("Orbitron", 22));
        // Màu tiêu đề khác nhau cho mỗi mode
        Color modeColor = switch (mode) {
            case EASY -> Color.LIMEGREEN;
            case NORMAL -> Color.LIGHTBLUE;
            case HARD -> Color.ORANGE;
        };
        modeTitle.setFill(modeColor);

        List<Integer> scores = loadHighScores(mode); // MODIFIED
        VBox listContainer = new VBox(5);
        listContainer.setAlignment(Pos.CENTER_LEFT);
        listContainer.setMinWidth(SCREEN_WIDTH * 0.2);
        listContainer.setMaxWidth(SCREEN_WIDTH * 0.25);

        for (int i = 0; i < scores.size(); i++) {
            int score = scores.get(i);
            int rank = i + 1;

            Text scoreEntry = new Text(String.format("%2d. %8d", rank, score));
            scoreEntry.setFont(Font.font("Monospace", 24));

            if (rank == 1) {
                scoreEntry.setFill(Color.YELLOW);
            } else if (rank <= 3) {
                scoreEntry.setFill(Color.LIGHTBLUE);
            } else {
                scoreEntry.setFill(Color.WHITE);
            }

            listContainer.getChildren().add(scoreEntry);
        }

        VBox wrapper = new VBox(10, modeTitle, listContainer); // MODIFIED: Thêm modeTitle
        wrapper.setAlignment(Pos.TOP_CENTER); // Đảm bảo căn chỉnh từ trên xuống
        // Thêm đường viền cho mỗi chế độ
        wrapper.setStyle("-fx-border-color: " + toWebHex(modeColor) + "; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: rgba(0, 0, 0, 0.3);");
        return wrapper;
    }

    // NEW: Hàm chuyển Color sang chuỗi HEX cho CSS
    private String toWebHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


    /**
     * Tạo nút BACK ở góc dưới trái.
     */
    private Button createBackButton() {
// ... (Logic tạo nút Back giữ nguyên) ...
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