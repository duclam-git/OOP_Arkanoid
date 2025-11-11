package org.example.arkanoid_oop;

import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.arkanoid_oop.Brick.*;
import org.example.arkanoid_oop.Entities.*;
import org.example.arkanoid_oop.Manager.BallTrailManager;
import org.example.arkanoid_oop.Brick.Impervious_brick;
import org.example.arkanoid_oop.Manager.AudioManager;
import org.example.arkanoid_oop.Menu.HighScoreMenu;
import org.example.arkanoid_oop.Menu.MainMenu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Collections;

import static org.example.arkanoid_oop.Brick.Brick.BRICK_HEIGHT;
import static org.example.arkanoid_oop.Brick.Brick.BRICK_WIDTH;

public class GamePane extends Pane {
    private static GamePane instance;
    private Stage stage;
    private double screenWidth, screenHeight;
    private Background background;
    private Paddle paddle;
    private List<Ball> balls = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<Powerup> powerups = new ArrayList<>();
    private List<Teleporter> teleporters = new ArrayList<>();
    private boolean isLaserActive = false;
    private List<Laser> lasers = new ArrayList<>();
    private long laserEndTime = 0;
    private long lastShotTime = 0;
    private final long LASER_DURATION_NANO = 4_000_000_000L;
    private final long SHOT_INTERVAL_NANO = 800_000_000L;
    private boolean isDoublePaddleActive = false;
    private long doublePaddleEndTime = 0;
    private final long DOUBLE_PADDLE_DURATION_NANO = 15_000_000_000L;
    private boolean isShieldActive = false;
    private Rectangle shieldBar;
    private boolean goLeft = false;
    private boolean goRight = false;
    private boolean gameRunning = false;
    private int lives = 3;
    private int score = 0;
    private int level = 1;
    private int highscore = 0;
    private Random rand = new Random();
    private List<ImageView> heartIcons = new ArrayList<>();
    private Text scoreText;
    private Text messageText;
    private Text levelText;
    private Text highscoreText;
    private AnimationTimer gameLoop;
    private AudioManager audio;
    private VBox gameOverOverlay;
    private Rectangle backdrop;

    // Pause denpendant
    private boolean isPaused = false;
    private VBox pauseOverlay;

    private BallTrailManager ballTrailManager;
    private long frameCount = 0;

    private GamePane(double width, double height, Stage stage) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.stage = stage;

        setPrefSize(screenWidth, screenHeight);
        audio = AudioManager.getInstance();
        loadHighScoreForHUD();

        // Khởi tạo các đối tượng game
        background = new Background(screenWidth, screenHeight);
        getChildren().add(background);

        paddle = new Paddle(screenWidth, screenHeight, audio.getSettings().getPaddleSkinPath()); //
        getChildren().add(paddle);

        spawnInitialBall();

        createBricks();
        for (Brick brick : bricks) {
            getChildren().add(brick.getView());
        }

        createTeleporters();
        createHUD();
        createShieldBar();
        createGameOverOverlay();

        createPauseOverlay();
        getChildren().add(pauseOverlay);
        pauseOverlay.toFront();

        ballTrailManager = new BallTrailManager(this, audio.getSettings().getBallSkinPath());

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frameCount++;
                paddle.update(goLeft, goRight);
                ballTrailManager.update();
                if (gameRunning && !isPaused) {
                    updateGame(now);
                } else if (!gameRunning && !isPaused) {
                    updateBallStuckToPaddle();
                }
                if(background != null) background.toBack();
            }
        };
        gameLoop.start();
    }

    private void loadHighScoreForHUD() {
        List<Integer> scores = HighScoreMenu.loadHighScores();
        this.highscore = scores.isEmpty() ? 0 : scores.get(0);
    }

    private void updateBallStuckToPaddle() {
        if (!balls.isEmpty()) {
            Ball ball = balls.get(0);
            double newBallX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2) - ball.getRadius();
            double newBallY = paddle.getLayoutY() - 15 - (ball.getRadius() * 2);
            ball.setLayoutX(newBallX);
            ball.setLayoutY(newBallY);
        }
    }

    public static GamePane getInstance(double width, double height, Stage stage) {
        if (instance == null) {
            instance = new GamePane(width, height, stage);
        }
        return instance;
    }

    public static void resetInstance() {
        if (instance != null && instance.gameLoop != null) {
            instance.gameLoop.stop();
        }
        instance = null;
    }

    private void createShieldBar() {
        double barHeight = 5;
        shieldBar = new Rectangle(0, screenHeight - barHeight, screenWidth, barHeight);
        shieldBar.setFill(Color.AQUA);
        shieldBar.setOpacity(0.7);
        shieldBar.setVisible(false);
        getChildren().add(shieldBar);
    }

    private void spawnInitialBall() {
        double ballStartX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2);
        double ballStartY = paddle.getLayoutY() - 15;
        Ball initialBall = new Ball(screenWidth, screenHeight, ballStartX, ballStartY, audio.getSettings().getBallSkinPath());
        balls.add(initialBall);
        getChildren().add(initialBall);
    }

    private void createBricks() {
        int brickRows = 7;
        int brickCols = 10;
        double padding = 5;
        double totalBrickWidth = (brickCols * BRICK_WIDTH) + ((brickCols - 1) * padding);
        double offsetLeft = (screenWidth - totalBrickWidth) / 2;
        double offsetTop = 50;
        for (int r = 0; r < brickRows; r++) {
            for (int c = 0; c < brickCols; c++) {
                double x = offsetLeft + c * (BRICK_WIDTH + padding);
                double y = offsetTop + r * (BRICK_HEIGHT + padding);
                Brick brick = null;
                double chance = rand.nextDouble();

                // 10% cơ hội cho gạch không thể phá hủy (chỉ từ màn 2 trở đi)
                if (chance < 0.1) {
                    brick = new Impervious_brick(x, y);
                }
                // 10% gạch nổ (0.1 -> 0.2)
                else if (chance < 0.2) {
                    brick = new Explosive_brick(x, y);
                }
                // 15% gạch powerup (0.2 -> 0.35)
                else if (chance < 0.35) {
                    brick = new Powerup_brick(x, y);
                }
                // 20% gạch cứng (0.35 -> 0.55)
                else if (chance < 0.55) {
                    brick = new Hard_brick(x, y);
                }
                // 45% gạch thường
                else {
                    brick = new Normal_brick(x, y);
                }

                bricks.add(brick);
            }
        }
    }

    private void createTeleporters() {
        for (Teleporter teleporter : teleporters) {
            getChildren().remove(teleporter);
        }
        teleporters.clear();
        double minX = 50, maxX = screenWidth - 50, minY = 250, maxY = screenHeight - 100;
        double teleporter1X = generateRandom(minX, maxX / 2);
        double teleporter1Y = generateRandom(minY, maxY);
        double teleporter2X = generateRandom(maxX / 2, maxX);
        double teleporter2Y = generateRandom(minY, maxY);
        Teleporter teleporter1 = new Teleporter(teleporter1X, teleporter1Y);
        Teleporter teleporter2 = new Teleporter(teleporter2X, teleporter2Y);
        teleporter1.setPartner(teleporter2);
        teleporter2.setPartner(teleporter1);
        teleporters.add(teleporter1);
        teleporters.add(teleporter2);
        getChildren().addAll(teleporter1, teleporter2);
    }

    private void createHUD() {
        heartIcons.clear();
        Image heartImage = null;
        try {
            heartImage = new Image(getClass().getResourceAsStream("/images/Heart.png"));
            if (heartImage.isError()) throw new NullPointerException("Lỗi tải Heart.png");
        } catch (Exception e) {
            System.err.println("Lỗi tải ảnh Heart.png: " + e.getMessage());
        }
        for (int i = 0; i < lives; i++) {
            ImageView heartView = new ImageView(heartImage);
            heartView.setFitWidth(25); heartView.setFitHeight(25);
            heartView.setLayoutX(10 + (i * 30));
            heartView.setLayoutY(10);
            heartIcons.add(heartView);
            getChildren().add(heartView);
        }
        scoreText = new Text("Score: 0");
        scoreText.setFont(Font.font("Arial", 20)); scoreText.setFill(Color.WHITE);
        scoreText.setLayoutX(screenWidth - 120); scoreText.setLayoutY(30);
        levelText = new Text("Level: " + level);
        levelText.setFont(Font.font("Arial", 20)); levelText.setFill(Color.WHITE);
        levelText.setLayoutX(screenWidth / 2 - 120); levelText.setLayoutY(30);

        highscoreText = new Text("High Score: " + highscore);

        highscoreText.setFont(Font.font("Arial", 20)); highscoreText.setFill(Color.WHITE);
        highscoreText.setLayoutX(screenWidth / 2 + 20); highscoreText.setLayoutY(30);
        messageText = new Text("Press SPACE to Start");
        messageText.setFont(Font.font("Arial", 30)); messageText.setFill(Color.WHITE);
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setLayoutY(screenHeight / 2);
        getChildren().addAll(scoreText, messageText, levelText, highscoreText);
    }

    private void createPauseOverlay() {
        Text pauseTitle = new Text("PAUSED");
        pauseTitle.setFont(Font.font("Orbitron", 50));
        pauseTitle.setFill(Color.YELLOW);

        Button resumeBtn = new Button("RESUME");
        Button quitBtn = new Button("QUIT TO MENU");

        for (Button btn : new Button[]{resumeBtn, quitBtn}) {
            btn.setPrefWidth(250);
            btn.setFont(Font.font("Orbitron", 20));
            btn.setStyle("-fx-background-color: #00ffff; -fx-text-fill: black; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #00ffff; -fx-text-fill: black; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;"));
        }

        resumeBtn.setOnAction(e -> {
            audio.play("click");
            hidePauseMenu();
        });

        quitBtn.setOnAction(e -> {
            audio.play("click");
            gameLoop.stop();
            audio.stopGameMusic();
            GamePane.resetInstance(); // Đảm bảo instance được reset
            MainMenu menu = new MainMenu(stage);
            menu.show();
        });

        VBox buttonBox = new VBox(15, resumeBtn, quitBtn);
        buttonBox.setAlignment(Pos.CENTER);

        pauseOverlay = new VBox(40, pauseTitle, buttonBox);
        pauseOverlay.setAlignment(Pos.CENTER);
        pauseOverlay.setPrefSize(screenWidth, screenHeight);
        pauseOverlay.setVisible(false);
    }

    public void showPauseMenu() {
        if (!gameRunning || isPaused) return;
        goLeft = false;
        goRight = false;

        isPaused = true;
        audio.stopGameMusic();
        audio.play("click");

        backdrop.setVisible(true);
        pauseOverlay.setVisible(true);

        backdrop.toFront();
        pauseOverlay.toFront();
    }

    public void hidePauseMenu() {
        if (!isPaused) return;

        isPaused = false;
        if (audio.isSoundEnabled()) {
            audio.playGameMusic();
        }

        backdrop.setVisible(false);
        pauseOverlay.setVisible(false);
        requestFocus();
    }

    private void createGameOverOverlay() {
        backdrop = new Rectangle(screenWidth, screenHeight, Color.BLACK);
        backdrop.setOpacity(0.7);
        backdrop.setVisible(false);
        getChildren().add(backdrop);

        Text gameOverTitle = new Text("GAME OVER");
        gameOverTitle.setFont(Font.font("Orbitron", 50)); gameOverTitle.setFill(Color.RED);
        Text finalScoreText = new Text("Final Score: 0");
        finalScoreText.setFont(Font.font("Arial", 24)); finalScoreText.setFill(Color.WHITE);
        Text highscoreNotice = new Text("");
        highscoreNotice.setFont(Font.font("Arial", 20));
        highscoreNotice.setFill(Color.YELLOW);
        highscoreNotice.setId("highscoreNotice");

        Button scoresBtn = new Button("RESTART");
        Button quitBtn = new Button("QUIT");

        for (Button btn : new Button[]{scoresBtn, quitBtn}) {
            btn.setPrefWidth(250);
            btn.setFont(Font.font("Orbitron", 18));
            btn.setStyle("-fx-background-color: #00ffff; -fx-text-fill: black; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #00ffff; -fx-text-fill: black; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;"));
        }

        VBox buttonBox = new VBox(10, scoresBtn, quitBtn);
        buttonBox.setAlignment(Pos.CENTER);
        gameOverOverlay = new VBox(20, gameOverTitle, finalScoreText, highscoreNotice, buttonBox);
        gameOverOverlay.setAlignment(Pos.CENTER);
        gameOverOverlay.setPrefSize(screenWidth, screenHeight);

        getChildren().add(gameOverOverlay);

        backdrop.toFront();
        gameOverOverlay.toFront();
        gameOverOverlay.setVisible(false);
    }

    public void showGameOverScreen() {
        gameRunning = false;
        audio.stopGameMusic();
        audio.play("game_over");
        gameLoop.stop();

        boolean isNewTop = HighScoreMenu.updateHighScores(score);
        loadHighScoreForHUD();
        highscoreText.setText("High Score: " + highscore);

        Text finalScoreText = (Text) gameOverOverlay.getChildren().get(1);
        Text highscoreNotice = (Text) gameOverOverlay.lookup("#highscoreNotice");
        finalScoreText.setText("Final Score: " + score);
        highscoreNotice.setText("");

        List<Integer> currentHighScores = HighScoreMenu.loadHighScores();
        boolean madeTopList = currentHighScores.contains(score);

        if (isNewTop) {
            highscoreNotice.setText("NEW TOP SCORE!");
        } else if (madeTopList) {
            int rank = currentHighScores.indexOf(score) + 1;
            highscoreNotice.setText("NEW HIGH SCORE (RANK " + rank + ")!");
        }

        for (Node node : getChildren()) {
            if (node != background && node != backdrop && node != gameOverOverlay && node != pauseOverlay) {
                node.setVisible(false);
            }
        }

        VBox buttonBox = (VBox) gameOverOverlay.getChildren().get(3);
        Button scoresBtn = (Button) buttonBox.getChildren().get(0);
        Button quitBtn = (Button) buttonBox.getChildren().get(1);

        scoresBtn.setText("VIEW SCORES");
        quitBtn.setText("QUIT TO MENU");

        scoresBtn.setOnAction(e -> {
            audio.play("click");
            gameOverOverlay.setVisible(false);
            backdrop.setVisible(false);

            GamePane.resetInstance();
            HighScoreMenu menu = new HighScoreMenu(stage);
            menu.show();
        });

        quitBtn.setOnAction(e -> {
            audio.play("click");
            gameLoop.stop();
            audio.stopGameMusic();
            GamePane.resetInstance();
            MainMenu menu = new MainMenu(stage);
            menu.show();
        });

        backdrop.setVisible(true);
        gameOverOverlay.setVisible(true);
        gameOverOverlay.toFront();
    }

    public void restartGame() {
        gameOverOverlay.setVisible(false);
        pauseOverlay.setVisible(false);
        backdrop.setVisible(false);
        for (ImageView icon : heartIcons) icon.setVisible(true);
        scoreText.setVisible(true);
        levelText.setVisible(true);
        highscoreText.setVisible(true);
        background.setVisible(true);
        paddle.setVisible(true);
        for (Teleporter teleporter : teleporters) teleporter.setVisible(true);

        resetGame();

        VBox buttonBox = (VBox) gameOverOverlay.getChildren().get(3);
        Button restartBtn = (Button) buttonBox.getChildren().get(0);
        Button quitBtn = (Button) buttonBox.getChildren().get(1);

        restartBtn.setText("RESTART");
        quitBtn.setText("QUIT");

        restartBtn.setOnAction(e -> {
            audio.play("click");
            restartGame();
        });
        quitBtn.setOnAction(e -> {
            audio.play("click");
            gameLoop.stop();
            audio.stopGameMusic();
            MainMenu menu = new MainMenu(stage);
            menu.show();
        });


        if (audio.isSoundEnabled()) audio.playGameMusic();
        gameRunning = false;
        isPaused = false;
        messageText.setVisible(true);
        gameLoop.start();
    }

    private double generateRandom(double min, double max) {
        return min + (rand.nextDouble() * (max - min));
    }

    private void resetGame() {
        lives = 3;
        for (ImageView icon : heartIcons) {
            icon.setVisible(true);
        }

        score = 0;
        scoreText.setText("Score: 0");
        level = 1;
        levelText.setText("Level: " + level);
        isLaserActive = false;
        for (Laser laser : lasers) getChildren().remove(laser);
        lasers.clear();
        if (isDoublePaddleActive) {
            paddle.setNormalLength();
            isDoublePaddleActive = false;
        }
        isShieldActive = false;
        shieldBar.setVisible(false);
        paddle.reset();

        ballTrailManager.clear();

        removeAndRespawnBall();
        for (Brick brick : bricks) getChildren().remove(brick.getView());
        bricks.clear();
        for (Powerup powerup : powerups) getChildren().remove(powerup);
        powerups.clear();
        createBricks();
        for (Brick brick : bricks) {
            getChildren().add(brick.getView());
        }

        createTeleporters();

        loadHighScoreForHUD();
        highscoreText.setText("High Score: " + highscore);

        VBox buttonBox = (VBox) gameOverOverlay.getChildren().get(3);
        Button restartBtn = (Button) buttonBox.getChildren().get(0);
        Button quitBtn = (Button) buttonBox.getChildren().get(1);

        restartBtn.setText("RESTART");
        quitBtn.setText("QUIT");

        restartBtn.setOnAction(e -> {
            audio.play("click");
            restartGame();
        });
        quitBtn.setOnAction(e -> {
            audio.play("click");
            gameLoop.stop();
            audio.stopGameMusic();
            MainMenu menu = new MainMenu(stage);
            menu.show();
        });

        messageText.setText("Press SPACE to Start");
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setVisible(true);
        gameRunning = false;
    }

    private void updateGame(long now) {
        updatePowerups();
        if (isDoublePaddleActive && now > doublePaddleEndTime) {
            paddle.setNormalLength();
            isDoublePaddleActive = false;
        }
        updateLasers(now);

        List<Ball> currentBalls = new ArrayList<>(balls);
        for (Ball ball : currentBalls) {
            if (balls.contains(ball)){
                ball.update();
                ballTrailManager.spawnTrail(ball, frameCount);
            }
        }
        checkCollisions(now);
    }
    private void updateLasers(long now) {
        if (isLaserActive && now > laserEndTime) {
            isLaserActive = false;
        }
        if (isLaserActive && now > lastShotTime + SHOT_INTERVAL_NANO) {
            shootLasers();
            audio.play("laser");
            lastShotTime = now;
        }
        Iterator<Laser> laserIt = lasers.iterator();
        while (laserIt.hasNext()) {
            Laser laser = laserIt.next();
            if (laser.update()) {
                getChildren().remove(laser);
                laserIt.remove();
            }
        }
    }
    private void shootLasers() {
        double paddleY = paddle.getLayoutY();
        double paddleWidth = paddle.getBoundsInLocal().getWidth();
        double paddleX = paddle.getLayoutX();
        double leftGunX = paddleX + 10;
        double rightGunX = paddleX + paddleWidth - 10;
        double gunY = paddleY;
        Laser laser1 = new Laser(leftGunX, gunY);
        lasers.add(laser1);
        getChildren().add(laser1);
        Laser laser2 = new Laser(rightGunX, gunY);
        lasers.add(laser2);
        getChildren().add(laser2);
    }
    private void updatePowerups() {
        Iterator<Powerup> it = powerups.iterator();
        while (it.hasNext()) {
            Powerup powerup = it.next();
            powerup.update();
            if (powerup.getLayoutY() > screenHeight) {
                getChildren().remove(powerup);
                it.remove();
            }
            if (paddle.getBoundsInParent().intersects(powerup.getBoundsInParent())) {
                activatePowerup(powerup.getType());
                getChildren().remove(powerup);
                audio.play("powerup");
                it.remove();
            }
        }
    }
    private void removeAndRespawnBall() {
        for (Ball ball : balls) {
            getChildren().remove(ball);
        }
        balls.clear();
        spawnInitialBall();
    }
    private void startNextLevel() {
        level++;
        isLaserActive = false;
        for (Laser laser : lasers) getChildren().remove(laser);
        lasers.clear();
        if (isDoublePaddleActive) {
            paddle.setNormalLength();
            isDoublePaddleActive = false;
        }
        isShieldActive = false;
        shieldBar.setVisible(false);
        for (Powerup powerup : powerups) getChildren().remove(powerup);
        powerups.clear();
        gameRunning = false;
        removeAndRespawnBall();
        paddle.reset();

        ballTrailManager.clear();

        for (Brick brick : bricks) {
            getChildren().remove(brick.getView());
        }
        // 2. Xóa danh sách đối tượng gạch để chuẩn bị cho level mới
        bricks.clear();

        createBricks();
        for (Brick brick : bricks) {
            getChildren().add(brick.getView());
        }
        createTeleporters();
        levelText.setText("Level: " + level);
        messageText.setText("LEVEL CLEARED! Press SPACE to continue.");
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setVisible(true);
    }

    private void checkCollisions(long now) {
        // Ball - Paddle
        Iterator<Ball> ballIt = new ArrayList<>(balls).iterator();
        while (ballIt.hasNext()) {
            Ball ball = ballIt.next();
            if (!balls.contains(ball)) continue;
            if (ball.getBoundsInParent().intersects(paddle.getBoundsInParent())) {
                audio.play("hit");
                if (ball.getDy() > 0) {
                    Bounds ballBounds = ball.getBoundsInParent();
                    Bounds paddleBounds = paddle.getBoundsInParent();
                    double hitPointX = ballBounds.getCenterX();
                    double paddleCenter = paddleBounds.getCenterX();
                    double paddleHalfWidth = paddleBounds.getWidth() / 2.0;
                    double relativeHit = (hitPointX - paddleCenter) / paddleHalfWidth;
                    double newDx = relativeHit * (ball.getSpeed() * 1.5);
                    double newDy = -ball.getSpeed();
                    ball.setDirection(newDx, newDy);
                }
            }
            // Ball - Border
            if (ball.getLayoutY() >= screenHeight) {
                if (isShieldActive) {
                    isShieldActive = false;
                    shieldBar.setVisible(false);
                    ball.reverseDy();
                    ball.setLayoutY(screenHeight - ball.getRadius() * 2 - 1);
                    audio.play("hit");
                    continue;
                }
                getChildren().remove(ball);
                balls.remove(ball);
                if (balls.isEmpty()) {
                    audio.play("lose");
                    loseLife();
                    return;
                }
            }
        }
        // Ball - Teleporters
        for (Ball ball : balls) {
            for (Teleporter teleporter : teleporters) {
                if (ball.getBoundsInParent().intersects(teleporter.getBoundsInParent()) && !teleporter.isOnCooldown(now)) {
                    teleporter.teleportBall(ball);
                    audio.play("tele");
                    break;
                }
            }
        }
        // Brick - Laser
        List<Brick> bricksHitByLaser = new ArrayList<>();
        Iterator<Laser> laserIt = lasers.iterator();
        while (laserIt.hasNext()) {
            Laser laser = laserIt.next();
            Iterator<Brick> brickIt = bricks.iterator();
            while (brickIt.hasNext()) {
                Brick brick = brickIt.next();
                Node brickView = brick.getView();
                if (laser.getBoundsInParent().intersects(brickView.getBoundsInParent())) {
                    boolean wasDestroyed = brick.onHit();
                    audio.play("brick");
                    getChildren().remove(laser);
                    laserIt.remove();
                    if (wasDestroyed) {
                        bricksHitByLaser.add(brick);
                    }
                    scoreText.setText("Score: " + score);
                    break;
                }
            }
        }
        if (balls.isEmpty() && bricksHitByLaser.isEmpty()) return;
        // Ball - Brick
        ballIt = new ArrayList<>(balls).iterator();
        while (ballIt.hasNext()) {
            Ball ball = ballIt.next();
            if (!balls.contains(ball)) continue;

            Iterator<Brick> brickIt = bricks.iterator();
            while (brickIt.hasNext()) {
                Brick brick = brickIt.next();
                Node brickView = brick.getView();
                if (ball.getBoundsInParent().intersects(brickView.getBoundsInParent())) {
                    Bounds ballBounds = ball.getBoundsInParent();
                    Bounds brickBounds = brickView.getBoundsInParent();
                    double overlapX = Math.max(ballBounds.getMinX(), brickBounds.getMinX());
                    double overlapY = Math.max(ballBounds.getMinY(), brickBounds.getMinY());
                    double maxOverlapX = Math.min(ballBounds.getMaxX(), brickBounds.getMaxX());
                    double maxOverlapY = Math.min(ballBounds.getMaxY(), brickBounds.getMaxY());
                    double overlapWidth = maxOverlapX - overlapX;
                    double overlapHeight = maxOverlapY - overlapY;

                    if (overlapWidth < overlapHeight) {
                        ball.reverseDx();
                    } else {
                        ball.reverseDy();
                    }
                    boolean wasDestroyed = brick.onHit();
                    audio.play("brick");
                    if (wasDestroyed) {
                        brickIt.remove();
                        getChildren().remove(brickView);
                        score += brick.getScoreValue();
                        if (brick instanceof Explosive_brick) {
                            explode((Explosive_brick) brick);
                        }
                        if (brick instanceof Powerup_brick) {
                            spawnPowerup((Powerup_brick) brick);
                            audio.play("powerup_spawn");
                        }
                    } else {
                        if (overlapWidth < overlapHeight) {
                            double push = (ballBounds.getCenterX() < brickBounds.getCenterX()) ? -(overlapWidth + 1) : (overlapWidth + 1);
                            ball.setLayoutX(ball.getLayoutX() + push);
                        } else {
                            double push = (ballBounds.getCenterY() < brickBounds.getCenterY()) ? -(overlapHeight + 1) : (overlapHeight + 1);
                            ball.setLayoutY(ball.getLayoutY() + push);
                        }
                    }
                    scoreText.setText("Score: " + score);
                    break;
                }
            }
        }
        for (Brick brick : bricksHitByLaser) {
            if (bricks.contains(brick)) {
                bricks.remove(brick);
                getChildren().remove(brick.getView());
                score += brick.getScoreValue();
                if (brick instanceof Explosive_brick) {
                    explode((Explosive_brick) brick);
                }
                if (brick instanceof Powerup_brick) {
                    spawnPowerup((Powerup_brick) brick);
                }
            }
            scoreText.setText("Score: " + score);
        }
        // Kiểm tra xem còn gạch KHÔNG BẤT TỬ nào còn lại không
        boolean hasDestroyableBricksLeft = false;
        for (Brick brick : bricks) {
            // Nếu tìm thấy bất kỳ viên gạch nào KHÔNG phải là Impervious_brick
            if (!(brick instanceof Impervious_brick)) {
                hasDestroyableBricksLeft = true;
                break;
            }
        }

// Nếu không còn gạch phá hủy được nào (chỉ còn lại gạch bất tử)
        if (!hasDestroyableBricksLeft) {
            audio.play("level_complete");
            startNextLevel();
        }
    }

    private void spawnPowerup(Powerup_brick brick) {
        double chance = rand.nextDouble();
        if (chance <= 0.5) {
            PowerupType type = brick.getPowerupType();
            double x = brick.getCenterX();
            double y = brick.getCenterY();
            Powerup powerup = new Powerup(x, y, type);
            powerups.add(powerup);
            getChildren().add(powerup);
        }
    }
    private void activatePowerup(PowerupType type) {
        long now = System.nanoTime();
        if (type == PowerupType.MULTI_BALL) {
            spawnMultiBall();
        }
        else if (type == PowerupType.LASER_PADDLE) {
            isLaserActive = true;
            laserEndTime = now + LASER_DURATION_NANO;
            lastShotTime = 0;
        }
        else if (type == PowerupType.DOUBLE_PADDLE) {
            paddle.setDoubleLength();
            isDoublePaddleActive = true;
            doublePaddleEndTime = now + DOUBLE_PADDLE_DURATION_NANO;
        }
        else if (type == PowerupType.SHIELD) {
            isShieldActive = true;
            shieldBar.setVisible(true);
        }
    }
    private void spawnMultiBall() {
        if (balls.isEmpty()) return;
        List<Ball> newBalls = new ArrayList<>();
        List<Ball> currentBallsSnapshot = new ArrayList<>(balls);
        String skinPath = audio.getSettings().getBallSkinPath();

        for (Ball sourceBall : currentBallsSnapshot) {
            if (balls.contains(sourceBall)) {
                double ballX = sourceBall.getLayoutX() + sourceBall.getRadius();
                double ballY = sourceBall.getLayoutY() + sourceBall.getRadius();
                double speed = sourceBall.getSpeed();
                Ball newBall1 = new Ball(ballX, ballY, screenWidth, screenHeight, -speed * 1.5, -speed * 0.5, skinPath);
                newBalls.add(newBall1);
                Ball newBall2 = new Ball(ballX, ballY, screenWidth, screenHeight, speed * 1.5, -speed * 0.5, skinPath);
                newBalls.add(newBall2);
            }
        }
        balls.addAll(newBalls);
        getChildren().addAll(newBalls);
    }


    private void explode(Explosive_brick sourceExplosiveBrick) {
        int explosionRadius = 3;
        Rectangle explosionArea = new Rectangle((int)sourceExplosiveBrick.getX() - BRICK_WIDTH * (explosionRadius / 2),
                (int)sourceExplosiveBrick.getY() - BRICK_HEIGHT  * (explosionRadius / 2),
                BRICK_WIDTH * explosionRadius, BRICK_HEIGHT * explosionRadius);
        List<Brick> bricksToRemove = new ArrayList<>();
        for (Brick brick : bricks) {
            if (brick == sourceExplosiveBrick) continue;
            if (explosionArea.getBoundsInParent().intersects(brick.getView().getBoundsInParent())) {
                bricksToRemove.add(brick);
            }
        }
        for (Brick brick : bricksToRemove) {

            if (brick instanceof Impervious_brick) {
                continue;
            }

            if (bricks.contains(brick)) {
                if (brick instanceof Hard_brick) {
                    boolean wasDestroyed = brick.onHit();
                    if (wasDestroyed) {
                        bricks.remove(brick);
                        getChildren().remove(brick.getView());
                        score += brick.getScoreValue();
                    }
                } else {
                    bricks.remove(brick);
                    getChildren().remove(brick.getView());
                    score += brick.getScoreValue();
                    if (brick instanceof Powerup_brick) {
                        spawnPowerup((Powerup_brick) brick);
                    }
                    if (brick instanceof Explosive_brick) {
                        explode((Explosive_brick) brick);
                    }
                }
            }
        }
    }

    private void loseLife() {
        lives--;
        if (lives >= 0 && lives < heartIcons.size()) {
            heartIcons.get(lives).setVisible(false);
        }

        gameRunning = false;
        isLaserActive = false;
        for (Laser laser : lasers) getChildren().remove(laser);
        lasers.clear();
        if (isDoublePaddleActive) {
            paddle.setNormalLength();
            isDoublePaddleActive = false;
        }
        isShieldActive = false;
        shieldBar.setVisible(false);

        ballTrailManager.clear();

        if (lives > 0) {
            paddle.reset();
        }

        removeAndRespawnBall();

        if (lives <= 0) {
            showGameOverScreen();
        } else {
            messageText.setText("Press SPACE to Continue");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
        }
    }

    private void loadHighScore() {
        try {
            File file = new File("highscore.txt");
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                if (scanner.hasNextInt()) {
                    this.highscore = scanner.nextInt();
                }
                scanner.close();
            }
        } catch (FileNotFoundException e) {
        } catch (Exception e) {
            System.out.println("Lỗi: Không thể tải highscore.");
            e.printStackTrace();
        }
    }

    public void handleKeyPressed(KeyCode code) {
        if (!isPaused) {
            if (code == KeyCode.A || code == KeyCode.LEFT) goLeft = true;
            if (code == KeyCode.D || code == KeyCode.RIGHT) goRight = true;
            if (code == KeyCode.SPACE) {
                if (!gameRunning && lives > 0 && !balls.isEmpty()) {
                    gameRunning = true;
                    messageText.setVisible(false);
                }
            }
            if (code == KeyCode.P || code == KeyCode.ESCAPE) {
                if (gameRunning) {
                    showPauseMenu();
                }
            }
            if (code == KeyCode.R) {
                if (!gameRunning) {
                    resetGame();
                }
            }
        }
    }
    public void handleKeyReleased(KeyCode code) {
        if (!isPaused) {
            if (code == KeyCode.A || code == KeyCode.LEFT) goLeft = false;
            if (code == KeyCode.D || code == KeyCode.RIGHT) goRight = false;
        }
    }
}