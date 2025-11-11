package org.example.arkanoid_oop.Menu;

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
import org.example.arkanoid_oop.BallSkinMenu;
import org.example.arkanoid_oop.GamePane;


public class MainMenu extends Menu {
    // Các thành phần Menu chính
    private VBox menuBoxContainer; // Chứa Title và các nút chính
    private Button debugButton;     // Nút chuyển sang Debug Menu

    // Các thành phần Menu Debug
    private HBox debugMenuBox; // Chứa 2 nút Debug lớn ở giữa
    private Button backButton; // Nút quay lại Menu chính

    private Image paddleImage;
    private Image ballImage;

    public MainMenu(Stage stage) {
        super(stage);

        // Tải ảnh một lần
        paddleImage = new Image(getClass().getResource("/images/paddle.png").toExternalForm());
        ballImage = new Image(getClass().getResource("/images/ball.png").toExternalForm());
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

        // --- KHỞI TẠO CÁC PHẦN TỬ ---

        // 1. Menu Chính (Tiêu đề + Nút START/OPTIONS/EXIT)
        menuBoxContainer = createMainMenuVBox();

        // 2. Nút Debug (Paddle nhỏ, góc dưới trái)
        debugButton = createDebugToggleButton();

        // 3. Menu Debug (2 nút icon lớn, giữa màn hình)
        debugMenuBox = createDebugMenuBox();

        // 4. Nút Back (Góc dưới trái)
        backButton = createBackButton();

        // --- CẤU HÌNH BAN ĐẦU (Menu Chính hiện, Debug ẩn) ---
        debugMenuBox.setVisible(false);
        backButton.setVisible(false);

        // Thêm tất cả vào root
        root.getChildren().addAll(menuBoxContainer, debugButton, debugMenuBox, backButton);

        // --- NHẠC NỀN LOOP ---
        if (audio.isSoundEnabled()) {
            audio.playMenuMusic();
        }

        // --- SỰ KIỆN CHUYỂN ĐỔI VIEW ---
        debugButton.setOnAction(e -> switchToDebugMenu());
        backButton.setOnAction(e -> switchToMainMenu());

        // --- SỰ KIỆN NÚT CHÍNH ---
        VBox mainButtonsBox = (VBox)menuBoxContainer.getChildren().get(1);
        Button startBtn = (Button)mainButtonsBox.getChildren().get(0);
        Button optionsBtn = (Button)mainButtonsBox.getChildren().get(1);
        Button exitBtn = (Button)mainButtonsBox.getChildren().get(2);

        startBtn.setOnAction(e -> {
            audio.play("click");
            audio.stopMenuMusic();
            // Fix lỗi
            if (audio.isSoundEnabled()) {
                audio.playGameMusic();
            }

            // *** GỌI RESET TRƯỚC KHI TẠO INSTANCE MỚI ***
            GamePane.resetInstance();

            GamePane gamePane = GamePane.getInstance(SCREEN_WIDTH, SCREEN_HEIGHT, stage);
            Scene scene = new Scene(gamePane, SCREEN_WIDTH, SCREEN_HEIGHT);
            scene.setOnKeyPressed(event -> gamePane.handleKeyPressed(event.getCode()));
            scene.setOnKeyReleased(event -> gamePane.handleKeyReleased(event.getCode()));
            stage.setTitle("Game Arkanoid (JavaFX)");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
            gamePane.requestFocus();
        });

        optionsBtn.setOnAction(e -> {
            audio.play("click");
            OptionsMenu options = new OptionsMenu(stage);
            options.show();
        });

        exitBtn.setOnAction(e -> {
            audio.play("click");
            stage.close();
        });

        Scene menuScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(menuScene);
        stage.show();
    }

    // ===================================
    // PHƯƠNG THỨC CHUYỂN ĐỔI VIEW
    // ===================================

    private void switchToDebugMenu() {
        audio.play("click");
        // Ẩn Menu chính
        menuBoxContainer.setVisible(false);
        debugButton.setVisible(false);

        // Hiện Menu Debug
        debugMenuBox.setVisible(true);
        backButton.setVisible(true);
    }

    private void switchToMainMenu() {
        audio.play("click");
        // Ẩn Menu Debug
        debugMenuBox.setVisible(false);
        backButton.setVisible(false);

        // Hiện Menu chính
        menuBoxContainer.setVisible(true);
        debugButton.setVisible(true);
    }

    // ===================================
    // PHƯƠNG THỨC TẠO THÀNH PHẦN
    // ===================================

    private VBox createMainMenuVBox() {
        // --- TIÊU ĐỀ ---
        Text title = new Text("");
        title.setFont(Font.font("Orbitron", 40));
        title.setStyle("-fx-fill: #00ffff; -fx-effect: dropshadow(gaussian, black, 10, 0, 0, 0);");

        // --- NÚT CHÍNH ---
        Button startBtn = new Button("START");
        Button optionsBtn = new Button("OPTIONS");
        Button exitBtn = new Button("EXIT");

        for (Button btn : new Button[]{startBtn, optionsBtn, exitBtn}) {
            btn.setPrefWidth(200);
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
        VBox mainButtonsBox = new VBox(20, startBtn, optionsBtn, exitBtn);
        mainButtonsBox.setAlignment(Pos.CENTER);

        VBox menuBox = new VBox(20, title, mainButtonsBox);
        menuBox.setAlignment(Pos.CENTER);
        return menuBox;
    }

    private Button createDebugToggleButton() {
        ImageView debugIcon = new ImageView(paddleImage);
        debugIcon.setFitWidth(30);
        debugIcon.setFitHeight(30);

        Button btn = new Button();
        btn.setGraphic(debugIcon);
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 5;");

        StackPane.setAlignment(btn, Pos.BOTTOM_LEFT);
        StackPane.setMargin(btn, new Insets(0, 0, 10, 10));

        return btn;
    }

    /**
     * Tạo Menu Debug với 2 nút icon lớn ở giữa màn hình.
     */
    private HBox createDebugMenuBox() {
        // Nút 1 (hình paddle)
        ImageView paddleView = new ImageView(paddleImage);
        paddleView.setFitWidth(80);
        paddleView.setFitHeight(80);
        Button paddleDebugBtn = new Button();
        paddleDebugBtn.setGraphic(paddleView);
        paddleDebugBtn.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-border-color: yellow; -fx-border-width: 3; -fx-padding: 15;");

        paddleDebugBtn.setOnAction(e -> {
            audio.play("click");
            // GỌI MÀN HÌNH CHỌN SKIN PADDLE MỚI
            PaddleSkinMenu skinMenu = new PaddleSkinMenu(stage);
            skinMenu.show();
        });

        // Nút 2 (hình ball)
        ImageView ballView = new ImageView(ballImage);
        ballView.setFitWidth(80);
        ballView.setFitHeight(80);
        Button ballDebugBtn = new Button();
        ballDebugBtn.setGraphic(ballView);
        ballDebugBtn.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-border-color: yellow; -fx-border-width: 3; -fx-padding: 15;");

        ballDebugBtn.setOnAction(e -> {
            audio.play("click");
            // GỌI MÀN HÌNH CHỌN SKIN BALL MỚI
            BallSkinMenu ballMenu = new BallSkinMenu(stage);
            ballMenu.show();
        });

        // Đặt 2 nút vào HBox ở giữa màn hình
        HBox hbox = new HBox(50, paddleDebugBtn, ballDebugBtn);
        hbox.setAlignment(Pos.CENTER);

        return hbox;
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

        StackPane.setAlignment(backBtn, Pos.BOTTOM_LEFT);
        StackPane.setMargin(backBtn, new Insets(0, 0, 10, 10));

        return backBtn;
    }
}