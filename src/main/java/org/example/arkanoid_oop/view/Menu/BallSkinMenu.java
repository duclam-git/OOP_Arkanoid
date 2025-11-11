package org.example.arkanoid_oop.view.Menu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

public class BallSkinMenu extends Menu {

    // Danh sách Skin Ball có sẵn: Key=Tên hiển thị, Value=Đường dẫn ảnh
    private static final Map<String, String> BALL_SKINS = new LinkedHashMap<>() {{
        put("Tech", "/images/ball.png");
        put("Cryo", "/images/ball1.png"); // Cần thêm file
        put("Geo", "/images/ball2.png"); // Cần thêm file
        put("Electro", "/images/ball3.png"); // Cần thêm file
    }};

    private Text statusText;
    private ImageView previewBall;
    private TilePane skinTilePane;

    public BallSkinMenu(Stage stage) {
        super(stage);
    }

    @Override
    public void show() {
        // --- ẢNH NỀN ---
        Image bgImage = new Image(getClass().getResourceAsStream("/images/concept.png"));
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(SCREEN_WIDTH);
        bgView.setFitHeight(SCREEN_HEIGHT);
        bgView.setPreserveRatio(false);

        StackPane root = new StackPane();
        root.getChildren().add(bgView);

        // --- Cấu trúc chính: VBox chứa Preview và Selector ---
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));

        // 1. Nửa trên: Preview
        VBox previewArea = createPreviewArea();

        // 2. Nửa dưới: Skin Selector
        VBox selectorArea = createSkinSelectorArea();

        mainContainer.getChildren().addAll(previewArea, selectorArea);

        // 3. Nút Back
        Button backButton = createBackButton();

        root.getChildren().addAll(mainContainer, backButton);

        Scene menuScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(menuScene);
        stage.show();
    }

    /**
     * Tạo khu vực Preview ở nửa trên màn hình.
     */
    private VBox createPreviewArea() {
        // 1. Tiêu đề
        Text title = new Text("BALL SKIN SELECTION");
        title.setFont(Font.font("Orbitron", 30));
        title.setStyle("-fx-fill: #00ffff; -fx-effect: dropshadow(gaussian, black, 10, 0, 0, 0);");

        // 2. ImageView Preview (Ball có kích thước bằng nhau)
        String currentSkinPath = audio.getSettings().getBallSkinPath();
        Image currentImage = new Image(getClass().getResourceAsStream(currentSkinPath));

        previewBall = new ImageView(currentImage);
        previewBall.setFitWidth(100);
        previewBall.setFitHeight(100);

        // 3. Status Text
        statusText = new Text("CURRENTLY SELECTED");
        statusText.setFont(Font.font("Orbitron", 18));
        statusText.setFill(javafx.scene.paint.Color.LIME);

        VBox box = new VBox(15, title, previewBall, statusText);
        box.setAlignment(Pos.CENTER);
        box.setMaxHeight(SCREEN_HEIGHT / 2.0);
        return box;
    }

    /**
     * Tạo khu vực Skin Selector ở nửa dưới màn hình.
     */
    private VBox createSkinSelectorArea() {
        skinTilePane = new TilePane();
        skinTilePane.setPrefColumns(4);
        skinTilePane.setHgap(30);
        skinTilePane.setVgap(30);
        skinTilePane.setAlignment(Pos.CENTER);

        String selectedSkinPath = audio.getSettings().getBallSkinPath();

        for (Map.Entry<String, String> entry : BALL_SKINS.entrySet()) {
            String skinName = entry.getKey();
            String skinPath = entry.getValue();

            VBox skinBox = createSkinButton(skinName, skinPath, selectedSkinPath);
            skinTilePane.getChildren().add(skinBox);
        }

        VBox selectorBox = new VBox(skinTilePane);
        selectorBox.setAlignment(Pos.CENTER);
        return selectorBox;
    }

    /**
     * Tạo một ô chọn skin hoàn chỉnh (Ảnh + Nút Select/Selected).
     */
    private VBox createSkinButton(String name, String path, String selectedPath) {
        // 1. Ảnh Skin nhỏ
        ImageView skinIcon = new ImageView(new Image(getClass().getResourceAsStream(path)));
        skinIcon.setFitWidth(50);
        skinIcon.setFitHeight(50);

        // 2. Nút Select/Selected
        Button selectBtn = new Button();
        updateSelectButton(selectBtn, path, selectedPath);

        // 3. Logic khi bấm nút
        selectBtn.setOnAction(e -> {
            audio.play("click");

            audio.getSettings().setBallSkinPath(path);
            audio.saveSettings();

            // Cập nhật Preview
            previewBall.setImage(new Image(getClass().getResourceAsStream(path)));
            statusText.setText("CURRENTLY SELECTED");

            // Cập nhật trạng thái TẤT CẢ các nút
            updateAllSelectButtons(path);
        });

        // 4. Container cho mỗi skin
        VBox skinContainer = new VBox(5, new Text(name), skinIcon, selectBtn);
        skinContainer.setUserData(path); // LƯU TRỮ PATH
        skinContainer.setAlignment(Pos.CENTER);
        skinContainer.setStyle("-fx-border-color: rgba(0, 255, 255, 0.5); -fx-border-width: 1; -fx-padding: 5;");

        return skinContainer;
    }

    /**
     * Cập nhật Text và Style của một nút Select.
     */
    private void updateSelectButton(Button btn, String path, String selectedPath) {
        if (path.equals(selectedPath)) {
            btn.setText("SELECTED");
            btn.setDisable(true);
            btn.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-padding: 5 15; -fx-font-weight: bold;");
        } else {
            btn.setText("SELECT");
            btn.setDisable(false);
            btn.setStyle("-fx-background-color: blue; -fx-text-fill: white; -fx-padding: 5 15;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #00ffff; -fx-text-fill: black; -fx-padding: 5 15;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: blue; -fx-text-fill: white; -fx-padding: 5 15;"));
        }
    }

    /**
     * Cập nhật trạng thái của TẤT CẢ các nút sau khi một nút được chọn.
     */
    private void updateAllSelectButtons(String newlySelectedPath) {
        for (javafx.scene.Node node : skinTilePane.getChildren()) {
            if (node instanceof VBox) {
                VBox skinBox = (VBox) node;
                Button selectBtn = (Button) skinBox.getChildren().get(2);

                String relativePath = (String) skinBox.getUserData();

                if (relativePath != null) {
                    updateSelectButton(selectBtn, relativePath, newlySelectedPath);
                }
            }
        }
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