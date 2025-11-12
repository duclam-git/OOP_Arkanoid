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

public class PaddleSkinMenu extends Menu {

    // Danh sách Skin Paddle có sẵn: Key=Tên hiển thị, Value=Đường dẫn ảnh
    private static final Map<String, String> PADDLE_SKINS = new LinkedHashMap<>() {{
        put("Tech", "/images/paddle.png");
        put("Hydro", "/images/paddle1.png");
        put("Aero", "/images/paddle2.png"); // Placeholder
        put("Pyro", "/images/paddle3.png"); // Placeholder
    }};

    private Text statusText;
    private ImageView previewPaddle;
    private TilePane skinTilePane;

    public PaddleSkinMenu(Stage stage) {
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

        // Cấu trúc chính: VBox chứa Preview và Selector
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
        Text title = new Text("PADDLE SKIN SELECTION");
        title.setFont(Font.font("Orbitron", 30));
        title.setStyle("-fx-fill: #00ffff; -fx-effect: dropshadow(gaussian, black, 10, 0, 0, 0);");

        // 2. ImageView Preview
        String currentSkinPath = audio.getSettings().getPaddleSkinPath();
        Image currentImage = new Image(getClass().getResource(currentSkinPath).toExternalForm());

        previewPaddle = new ImageView(currentImage);
        previewPaddle.setFitWidth(250); // Lớn hơn bình thường
        previewPaddle.setFitHeight(50);

        // 3. Status Text
        statusText = new Text("CURRENTLY SELECTED");
        statusText.setFont(Font.font("Orbitron", 18));
        statusText.setFill(javafx.scene.paint.Color.LIME);

        VBox box = new VBox(15, title, previewPaddle, statusText);
        box.setAlignment(Pos.CENTER);
        box.setMaxHeight(SCREEN_HEIGHT / 2.0);
        return box;
    }

    /**
     * Tạo khu vực Skin Selector ở nửa dưới màn hình.
     */
    private VBox createSkinSelectorArea() {
        // Sử dụng TilePane để sắp xếp các skin theo lưới/dòng
        skinTilePane = new TilePane();
        skinTilePane.setPrefColumns(2);
        skinTilePane.setHgap(30);
        skinTilePane.setVgap(30);
        skinTilePane.setAlignment(Pos.CENTER);

        String selectedSkinPath = audio.getSettings().getPaddleSkinPath();

        // Duyệt qua danh sách skins để tạo nút
        for (Map.Entry<String, String> entry : PADDLE_SKINS.entrySet()) {
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
        ImageView skinIcon = new ImageView(new Image(getClass().getResource(path).toExternalForm()));
        skinIcon.setFitWidth(150);
        skinIcon.setFitHeight(30);

        // 2. Nút Select/Selected
        Button selectBtn = new Button();
        updateSelectButton(selectBtn, path, selectedPath);

        // 3. Logic khi bấm nút
        selectBtn.setOnAction(e -> {
            audio.play("click");

            // Cập nhật Settings và Lưu
            audio.getSettings().setPaddleSkinPath(path);
            audio.saveSettings();

            // Cập nhật Preview
            previewPaddle.setImage(new Image(getClass().getResource(path).toExternalForm()));
            statusText.setText("CURRENTLY SELECTED");

            // Cập nhật trạng thái TẤT CẢ các nút
            updateAllSelectButtons(path);
        });

        // 4. Container cho mỗi skin
        VBox skinContainer = new VBox(5, new Text(name), skinIcon, selectBtn);
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
        // Duyệt qua tất cả các ô skin trong TilePane
        for (javafx.scene.Node node : skinTilePane.getChildren()) {
            if (node instanceof VBox) {
                VBox skinBox = (VBox) node;
                // Lấy ImageView và Button từ VBox con
                ImageView skinIcon = (ImageView) skinBox.getChildren().get(1);
                Button selectBtn = (Button) skinBox.getChildren().get(2);

                // Lấy đường dẫn từ ImageView (lưu ý: Image.getUrl() trả về full path)
                String pathUrl = skinIcon.getImage().getUrl();

                // Trích xuất path tương đối (ví dụ: "/images/paddle.png")
                String relativePath = null;
                for (String p : PADDLE_SKINS.values()) {
                    if (pathUrl.endsWith(p)) {
                        relativePath = p;
                        break;
                    }
                }

                if (relativePath != null) {
                    // Áp dụng logic cập nhật trạng thái
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