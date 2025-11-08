package org.example.arkanoid_oop;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


public class MainMenu {
    private Stage stage;
    private AudioManager audio;

    private MediaPlayer bgMusic;

    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;


    public MainMenu(Stage stage) {
        this.stage = stage;
        this.audio = AudioManager.getInstance();
    }

    public void show() {
        // --- ẢNH NỀN ---
        Image bgImage = new Image(getClass().getResource("/images/concept.png").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(800);
        bgView.setFitHeight(600);
        bgView.setPreserveRatio(false);

        StackPane root = new StackPane();
        root.getChildren().add(bgView);

        // --- TIÊU ĐỀ ---
        Text title = new Text("ARKANOID: QUANTUM RIFT");
        title.setFont(Font.font("Orbitron", 40));
        title.setStyle("-fx-fill: #00ffff; -fx-effect: dropshadow(gaussian, black, 10, 0, 0, 0);");

        // --- NÚT ---
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
                    "-fx-background-color: #00ffff; -fx-text-fill: black; -fx-border-color: #00ffff; -fx-border-width: 2;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-background-color: rgba(0, 0, 0, 0.6); -fx-text-fill: #00ffff; -fx-border-color: #00ffff; -fx-border-width: 2;"
            ));
        }

        // --- MENU BOX ---
        VBox menuBox = new VBox(20, title, startBtn, optionsBtn, exitBtn);
        menuBox.setAlignment(Pos.CENTER);

        root.getChildren().add(menuBox);

        // --- NHẠC NỀN LOOP ---
        AudioManager audio = AudioManager.getInstance();
        audio.playMenuMusic();

        // --- SỰ KIỆN NÚT ---
        startBtn.setOnAction(e -> {
            audio.play("click");          // hiệu ứng click
            audio.stopMenuMusic();              // dừng nhạc nền trước khi vào game
            audio.playGameMusic();

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
         //   new OptionsMenu(stage).show(); // nếu có class OptionsMenu
        });

        exitBtn.setOnAction(e -> {
            audio.play("click");
            stage.close();
        });

        Scene menuScene = new Scene(root, 800, 600);
        stage.setScene(menuScene);
        stage.show();
    }
}
