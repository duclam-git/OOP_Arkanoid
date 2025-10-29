package org.example.arkanoid;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.example.arkanoid.model.Ball;
import org.example.arkanoid.model.Paddle;
import java.util.List;
import java.util.ArrayList;
import org.example.arkanoid.model.Brick;
import java.util.ArrayList;
import java.util.List;

public class GameScene {
    private final Scene scene;
    private final Paddle paddle;
    private final Ball ball;
    private final List<Brick> bricks = new ArrayList<>();
    private final Label scoreLabel = new Label();
    private int score = 0;

    private boolean leftPressed = false;
    private boolean rightPressed = false;

    public GameScene(int width, int height) {
        Group root = new Group();
        scene = new Scene(root, width, height);

        // Background
        Image bg = new Image(getClass().getResource("/org/example/arkanoid/image/background.jpg").toExternalForm());
        ImageView bgView = new ImageView(bg);
        bgView.setFitWidth(width);
        bgView.setFitHeight(height);
        root.getChildren().add(bgView);

        // Paddle
        paddle = new Paddle(width / 2 - 50, height - 60,
                "/org/example/arkanoid/image/paddle.png",120,20);
        root.getChildren().add(paddle.getNode());

        // Ball
        ball = new Ball(width / 2 - 10, height - 80,
                "/org/example/arkanoid/image/ball.png",20,20);
        root.getChildren().add(ball.getNode());

        // Bricks
        int rows = 5;
        int cols = 13;
        double brickWidth = 50;
        double brickHeight = 20;
        double startX = 35;
        double startY = 50;
        double gap = 5;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Brick brick = new Brick(
                        startX + col * (brickWidth + gap),
                        startY + row * (brickHeight + gap),
                        "/org/example/arkanoid/image/brick.png",
                        brickWidth, brickHeight
                );
                bricks.add(brick);
                root.getChildren().add(brick.getNode());
            }
        }

        // Score label
        scoreLabel.setText("Score: 0");
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font(20));
        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(10);
        root.getChildren().add(scoreLabel);

        // Input
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT, A -> leftPressed = true;
                case RIGHT, D -> rightPressed = true;
                case SPACE -> ball.launch();
            }
        });

        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case LEFT, A -> leftPressed = false;
                case RIGHT, D -> rightPressed = false;
            }
        });

        scene.setOnMousePressed(e -> ball.launch());
    }

    public Scene getScene() {
        return scene;
    }

    public void startGame() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Paddle di chuyển
                if (leftPressed) paddle.moveLeft();
                if (rightPressed) paddle.moveRight();

                // Ball cập nhật
                ball.updatePosition(scene.getWidth(), scene.getHeight(), paddle, bricks);

                // Cập nhật điểm
                score = (int) bricks.stream().filter(Brick::isDestroyed).count();
                scoreLabel.setText("Score: " + score);
            }
        };
        timer.start();

        // Đặt focus để nhận input
        scene.getRoot().requestFocus();
    }
}
