package org.example.arkanoid_oop.view.Menu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.arkanoid_oop.model.util.GameSettings.GameMode;

public class ModeSelectionMenu extends Menu {

    private Text statusText;
    private GameMode selectedMode;
    private VBox buttonBox;

    public ModeSelectionMenu(Stage stage) {
        super(stage);
        this.selectedMode = audio.getSettings().getGameMode();
    }

    @Override
    public void show() {
        // --- ẢNH NỀN ---
        Image bgImage = new Image(getClass().getResource("/images/concept.png").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(SCREEN_WIDTH);
        bgView.setFitHeight(SCREEN_HEIGHT);
        bgView.setPreserveRatio(false);

        StackPane root = new StackPane();
        root.getChildren().add(bgView);

        // --- Cấu trúc chính: VBox chứa Tiêu đề và Nút chọn ---
        VBox mainContainer = new VBox(40);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));

        // 1. Tiêu đề
        Text title = new Text("GAME MODE SELECTION");
        title.setFont(Font.font("Orbitron", 40));
        title.setStyle("-fx-fill: #00ffff; -fx-effect: dropshadow(gaussian, black, 10, 0, 0, 0);");

        // 2. Khu vực chọn Mode
        buttonBox = createModeSelectorArea();

        // 3. Status Text
        statusText = new Text();
        updateStatusText(selectedMode);
        statusText.setFont(Font.font("Orbitron", 18));
        statusText.setFill(javafx.scene.paint.Color.LIME);

        mainContainer.getChildren().addAll(title, buttonBox, statusText);

        // 4. Nút Back
        Button backButton = createBackButton();

        root.getChildren().addAll(mainContainer, backButton);

        Scene menuScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(menuScene);
        stage.show();
    }

    private VBox createModeSelectorArea() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(250);

        for (GameMode mode : GameMode.values()) {
            Button modeBtn = createModeButton(mode);
            box.getChildren().add(modeBtn);
        }

        return box;
    }

    private Button createModeButton(GameMode mode) {
        String buttonText = mode.getDisplayName().toUpperCase() + " (" + mode.getInitialLives() + " MẠNG)";
        Button btn = new Button(buttonText);
        btn.setPrefWidth(250);
        btn.setFont(Font.font("Orbitron", 20));

        updateModeButtonStyle(btn, mode);

        btn.setOnAction(e -> {
            audio.play("click");
            selectedMode = mode;
            audio.getSettings().setGameMode(selectedMode);
            audio.saveSettings();
            updateAllModeButtons(selectedMode);
            updateStatusText(selectedMode);
        });
        return btn;
    }

    private void updateModeButtonStyle(Button btn, GameMode mode) {
        // Lấy chế độ đang được lưu trong settings để so sánh
        GameMode currentSelectedMode = audio.getSettings().getGameMode();
        boolean isSelected = (mode == currentSelectedMode);

        // 1. Style chuẩn (Đã lấy từ MainMenu.java - Nền đen, Viền xanh neon)
        String baseStyle = """
            -fx-background-color: rgba(0, 0, 0, 0.6);
            -fx-text-fill: #00ffff;
            -fx-border-color: #00ffff;
            -fx-border-width: 2;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
        """;
        // 2. Style Hover
        String hoverStyle = "-fx-background-color: #00ffff; -fx-text-fill: black; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;";
        // 3. Style Selected (Green)
        String selectedStyle = "-fx-background-color: #00ff00; -fx-text-fill: black; -fx-border-color: #00ff00; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;";

        if (isSelected) {
            // Nút đang được chọn: Màu xanh lá cây và bị vô hiệu hóa
            btn.setStyle(selectedStyle);
            btn.setDisable(true);
            btn.setOnMouseEntered(e -> {}); // Xóa hiệu ứng hover
            btn.setOnMouseExited(e -> {});
        } else {
            // Nút chưa được chọn: Style mặc định và cho phép chọn
            btn.setStyle(baseStyle);
            btn.setDisable(false);
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        }
    }

    private void updateAllModeButtons(GameMode newlySelectedMode) {
        // Chỉ cần lặp qua tất cả các nút và gọi lại updateModeButtonStyle
        for (javafx.scene.Node node : buttonBox.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;

                // Lấy GameMode của nút đó từ tên hiển thị (logic đã được sử dụng trước đó)
                String buttonText = btn.getText();
                GameMode mode = null;
                if (buttonText.contains("EASY")) mode = GameMode.EASY;
                else if (buttonText.contains("NORMAL")) mode = GameMode.NORMAL;
                else if (buttonText.contains("HARD")) mode = GameMode.HARD;

                if (mode != null) {
                    // Gọi updateModeButtonStyle để áp dụng style SELECTED/DEFAULT
                    updateModeButtonStyle(btn, mode);
                }
            }
        }
    }

    private void updateStatusText(GameMode mode) {
        statusText.setText("CHẾ ĐỘ HIỆN TẠI: " + mode.getDisplayName().toUpperCase() + " (" + mode.getInitialLives() + " MẠNG)");
    }

    /**
     * Tạo nút BACK ở góc dưới trái.
     */
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