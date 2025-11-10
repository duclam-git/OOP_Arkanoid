package org.example.arkanoid_oop;

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

public class OptionsMenu extends Menu {
    // Thành phần Menu
    private VBox optionsBoxContainer;
    private Button backButton;
    private Button toggleSoundButton;

    public OptionsMenu(Stage stage) {
        super(stage);
    }

    @Override
    public void show() {
        // --- ẢNH NỀN ---
        Image bgImage = new Image(getClass().getResource("/images/mainMenu.png").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(SCREEN_WIDTH);
        bgView.setFitHeight(SCREEN_HEIGHT);
        bgView.setPreserveRatio(false);

        StackPane root = new StackPane();
        root.getChildren().add(bgView);

        // --- KHỞI TẠO CÁC PHẦN TỬ ---

        // 1. Menu Tùy chọn (Tiêu đề + Nút)
        optionsBoxContainer = createOptionsVBox();

        // 2. Nút Back (Dưới cùng)
        backButton = createBackButton();

        // Thêm tất cả vào root
        root.getChildren().addAll(optionsBoxContainer, backButton);

        // --- SỰ KIỆN NÚT ---

        // Truy cập nút Sound Toggle trong VBox
        VBox mainButtonsBox = (VBox)optionsBoxContainer.getChildren().get(1);
        toggleSoundButton = (Button)mainButtonsBox.getChildren().get(0);

        // Cập nhật text ban đầu
        updateSoundButtonText();

        // Logic Toggle Sound
        toggleSoundButton.setOnAction(e -> {
            audio.play("click");
            boolean current = audio.isSoundEnabled();
            audio.setSoundEnabled(!current);
            updateSoundButtonText();

            if (audio.isSoundEnabled()) {
                audio.playMenuMusic();
            } else {
                audio.stopMenuMusic();
            }
        });

        // Logic Back
        backButton.setOnAction(e -> {
            audio.play("click");
            MainMenu menu = new MainMenu(stage);
            menu.show();
        });

        Scene menuScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(menuScene);
        stage.show();
    }

    // ===================================
    // PHƯƠNG THỨC TẠO THÀNH PHẦN
    // ===================================

    /**
     * Tạo VBox chứa Tiêu đề và các nút tùy chọn chính.
     */
    private VBox createOptionsVBox() {
        // --- TIÊU ĐỀ ---
        Text title = new Text("GAME OPTIONS");
        title.setFont(Font.font("Orbitron", 40));
        title.setStyle("-fx-fill: #00ffff; -fx-effect: dropshadow(gaussian, black, 10, 0, 0, 0);");

        // --- NÚT CHÍNH ---
        // Nút này được khởi tạo ở đây nhưng logic được thêm vào trong show()
        Button soundBtn = new Button("SOUND: ON/OFF");
        soundBtn.setId("soundToggle"); // Đặt ID để truy cập dễ hơn

        // Áp dụng style và hiệu ứng chuột như MainMenu
        for (Button btn : new Button[]{soundBtn}) {
            btn.setPrefWidth(250);
            btn.setFont(Font.font("Orbitron", 20));
            btn.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.6);
                -fx-text-fill: #00ffff;
                -fx-border-color: #00ffff;
                -fx-border-width: 2;
                -fx-background-radius: 10;
                -fx-border-radius: 10;
            """);
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-background-color: #00ffff; -fx-text-fill: black; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-background-color: rgba(0, 0, 0, 0.6); -fx-text-fill: #00ffff; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;"
            ));
        }

        // --- MENU BOX ---
        VBox mainButtonsBox = new VBox(20, soundBtn);
        mainButtonsBox.setAlignment(Pos.CENTER);

        VBox menuBox = new VBox(40, title, mainButtonsBox);
        menuBox.setAlignment(Pos.CENTER);
        return menuBox;
    }

    /**
     * Tạo nút back ở góc dưới.
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

        StackPane.setAlignment(backBtn, Pos.BOTTOM_LEFT);
        StackPane.setMargin(backBtn, new Insets(0, 0, 10, 10));

        return backBtn;
    }

    /**
     * Cập nhật chữ trên nút Sound Toggle dựa trên cài đặt.
     */
    private void updateSoundButtonText() {
        if (toggleSoundButton != null) {
            if (audio.isSoundEnabled()) {
                toggleSoundButton.setText("SOUND: ON");
            } else {
                toggleSoundButton.setText("SOUND: OFF");
            }
        }
    }
}