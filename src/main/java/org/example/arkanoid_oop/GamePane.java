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
import org.example.arkanoid_oop.Entities.BallTrailManager; // (MỚI) Import lớp hiệu ứng

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static org.example.arkanoid_oop.Brick.Brick.BRICK_HEIGHT;
import static org.example.arkanoid_oop.Brick.Brick.BRICK_WIDTH;

/**
 * GamePane là màn chơi chính, chứa tất cả các đối tượng game
 * và quản lý vòng lặp (game loop) chính.
 */
public class GamePane extends Pane {

    // (Các biến của bạn giữ nguyên)
    private static final String HIGH_SCORE_FILE = "highscore.txt";
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

    // --- (MỚI) Thêm các biến cho hiệu ứng đuôi ---
    private BallTrailManager ballTrailManager; // Đối tượng quản lý đuôi
    private long frameCount = 0; // Bộ đếm khung hình
    // ---------------------------------------------

    private GamePane(double width, double height, Stage stage) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.stage = stage;

        setPrefSize(screenWidth, screenHeight);
        audio = AudioManager.getInstance();
        loadHighScore();

        // Khởi tạo các đối tượng game
        background = new Background(screenWidth, screenHeight);
        getChildren().add(background);

        paddle = new Paddle(screenWidth, screenHeight);
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

        // (MỚI) Khởi tạo trình quản lý đuôi
        ballTrailManager = new BallTrailManager(this); // 'this' chính là Pane

        // Bắt đầu vòng lặp game
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // (MỚI) Tăng bộ đếm khung hình
                frameCount++;

                paddle.update(goLeft, goRight);

                // (MỚI) Cập nhật hiệu ứng đuôi (luôn chạy)
                ballTrailManager.update();

                if (gameRunning) {
                    updateGame(now);
                } else {
                    updateBallStuckToPaddle();
                }

                // (MỚI) Đẩy nền xuống dưới cùng (để đuôi hiện trên nền)
                if(background != null) background.toBack();
            }
        };
        gameLoop.start();
    }

    // (Hàm updateBallStuckToPaddle() giữ nguyên)
    private void updateBallStuckToPaddle() {
        if (!balls.isEmpty()) {
            Ball ball = balls.get(0);
            double newBallX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2) - ball.getRadius();
            double newBallY = paddle.getLayoutY() - 15 - (ball.getRadius() * 2);
            ball.setLayoutX(newBallX);
            ball.setLayoutY(newBallY);
        }
    }


    // (Hàm getInstance() giữ nguyên)
    public static GamePane getInstance(double width, double height, Stage stage) {
        if (instance == null) {
            instance = new GamePane(width, height, stage);
        }
        return instance;
    }

    // (Hàm resetInstance() giữ nguyên)
    public static void resetInstance() {
        if (instance != null && instance.gameLoop != null) {
            instance.gameLoop.stop();
        }
        instance = null;
    }

    // (Hàm createShieldBar() giữ nguyên)
    private void createShieldBar() {
        double barHeight = 5;
        shieldBar = new Rectangle(0, screenHeight - barHeight, screenWidth, barHeight);
        shieldBar.setFill(Color.AQUA);
        shieldBar.setOpacity(0.7);
        shieldBar.setVisible(false);
        getChildren().add(shieldBar);
    }

    // (Hàm spawnInitialBall() giữ nguyên)
    private void spawnInitialBall() {
        double ballStartX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2);
        double ballStartY = paddle.getLayoutY() - 15;
        Ball initialBall = new Ball(screenWidth, screenHeight, ballStartX, ballStartY);
        balls.add(initialBall);
        getChildren().add(initialBall);
    }

    // (Hàm createBricks() giữ nguyên)
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
                if (chance < 0.1) brick = new Explosive_brick(x, y);
                else if (chance < 0.25) brick = new Powerup_brick(x, y);
                else if (chance < 0.45) brick = new Hard_brick(x, y);
                else brick = new Normal_brick(x, y);
                bricks.add(brick);
            }
        }
    }

    // (Hàm createTeleporters() giữ nguyên)
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

    // (Hàm createHUD() giữ nguyên - đã sửa để dùng 3 icon trái tim)
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

    // (Hàm createGameOverOverlay(), showGameOverScreen() giữ nguyên)
    private void createGameOverOverlay() {
        backdrop = new Rectangle(screenWidth, screenHeight, Color.BLACK);
        backdrop.setOpacity(0.7); backdrop.setVisible(false);
        Text gameOverTitle = new Text("GAME OVER");
        gameOverTitle.setFont(Font.font("Orbitron", 50)); gameOverTitle.setFill(Color.RED);
        Text finalScoreText = new Text("Final Score: 0");
        finalScoreText.setFont(Font.font("Arial", 24)); finalScoreText.setFill(Color.WHITE);
        Text highscoreNotice = new Text("");
        highscoreNotice.setFont(Font.font("Arial", 20));
        highscoreNotice.setFill(Color.YELLOW);
        highscoreNotice.setId("highscoreNotice");
        Button restartBtn = new Button("RESTART");
        Button quitBtn = new Button("QUIT");
        for (Button btn : new Button[]{restartBtn, quitBtn}) {
            btn.setPrefWidth(150);
            btn.setFont(Font.font("Orbitron", 18));
            btn.setStyle("-fx-background-color: #00ffff; -fx-text-fill: black; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #00ffff; -fx-text-fill: black; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;"));
        }
        quitBtn.setOnAction(e -> {
            audio.play("click");
            gameLoop.stop();
            audio.stopGameMusic();
            MainMenu menu = new MainMenu(stage);
            menu.show();
        });
        restartBtn.setOnAction(e -> {
            audio.play("click");
            restartGame();
        });
        VBox buttonBox = new VBox(10, restartBtn, quitBtn);
        buttonBox.setAlignment(Pos.CENTER);
        gameOverOverlay = new VBox(20, gameOverTitle, finalScoreText, highscoreNotice, buttonBox);
        gameOverOverlay.setAlignment(Pos.CENTER);
        gameOverOverlay.setPrefSize(screenWidth, screenHeight);
        getChildren().add(backdrop);
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
        Text finalScoreText = (Text) gameOverOverlay.getChildren().get(1);
        Text highscoreNotice = (Text) gameOverOverlay.lookup("#highscoreNotice");
        finalScoreText.setText("Final Score: " + score);
        highscoreNotice.setText("");
        if (score > highscore) {
            highscore = score;
            highscoreText.setText("High Score: " + highscore);
            saveHighScore();
            highscoreNotice.setText("NEW HIGH SCORE!");
        }
        for (Node node : getChildren()) {
            if (node != background && node != backdrop && node != gameOverOverlay) {
                node.setVisible(false);
            }
        }
        backdrop.setVisible(true);
        gameOverOverlay.setVisible(true);
        gameOverOverlay.toFront();
    }

    // (Hàm restartGame() giữ nguyên)
    public void restartGame() {
        gameOverOverlay.setVisible(false);
        backdrop.setVisible(false);
        for (ImageView icon : heartIcons) icon.setVisible(true);
        scoreText.setVisible(true);
        levelText.setVisible(true);
        highscoreText.setVisible(true);
        background.setVisible(true);
        paddle.setVisible(true);
        for (Teleporter teleporter : teleporters) teleporter.setVisible(true);

        resetGame(); // Gọi logic reset

        if (audio.isSoundEnabled()) audio.playGameMusic();
        gameRunning = false;
        messageText.setVisible(true);
        gameLoop.start();
    }

    /**
     * (CẬP NHẬT) Vòng lặp game chính, gọi spawnTrail
     */
    private void updateGame(long now) {
        updatePowerups();
        if (isDoublePaddleActive && now > doublePaddleEndTime) {
            paddle.setNormalLength();
            isDoublePaddleActive = false;
        }
        updateLasers(now);

        // (CẬP NHẬT) Duyệt và tạo đuôi
        List<Ball> currentBalls = new ArrayList<>(balls); // Duyệt trên bản sao
        for (Ball ball : currentBalls) {
            if (!balls.contains(ball)) continue; // Bỏ qua nếu bóng đã bị xóa

            ball.update();
            // (MỚI) Gọi hàm tạo đuôi
            ballTrailManager.spawnTrail(ball, frameCount);
        }
        checkCollisions(now);
    }

    // (Các hàm updateLasers, shootLasers, updatePowerups, removeAndRespawnBall, startNextLevel, checkCollisions, spawnPowerup, activatePowerup, spawnMultiBall, explode... giữ nguyên)
    // ... (Giữ nguyên các hàm này) ...
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

        ballTrailManager.clear(); // (MỚI) Xóa đuôi cũ

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
        for (Ball ball : balls) {
            for (Teleporter teleporter : teleporters) {
                if (ball.getBoundsInParent().intersects(teleporter.getBoundsInParent()) && !teleporter.isOnCooldown(now)) {
                    teleporter.teleportBall(ball);
                    audio.play("tele");
                    break;
                }
            }
        }
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
        List<Brick> bricksToExplode = new ArrayList<>();
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
                    boolean wasDestroyed = brick.onHit();
                    audio.play("brick");
                    if (overlapWidth < overlapHeight) {
                        ball.reverseDx();
                    } else {
                        ball.reverseDy();
                    }
                    if (wasDestroyed) {
                        brickIt.remove();
                        getChildren().remove(brickView);
                        score += brick.getScoreValue();
                        if (brick instanceof Explosive_brick) {
                            bricksToExplode.add(brick);
                        }
                        if (brick instanceof Powerup_brick) {
                            spawnPowerup((Powerup_brick) brick);
                            audio.play("powerup_spawn");
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
                    bricksToExplode.add(brick);
                }
                if (brick instanceof Powerup_brick) {
                    spawnPowerup((Powerup_brick) brick);
                }
            }
            scoreText.setText("Score: " + score);
        }
        if (!bricksToExplode.isEmpty()) {
            for (Brick explosiveBrick : bricksToExplode) {
                if (explosiveBrick instanceof Explosive_brick) {
                    audio.play("explosion");
                    explode((Explosive_brick) explosiveBrick);
                }
            }
            scoreText.setText("Score: " + score);
        }
        if (bricks.isEmpty()) {
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

        for (Ball sourceBall : currentBallsSnapshot) {
            if (balls.contains(sourceBall)) {
                double ballX = sourceBall.getLayoutX() + sourceBall.getRadius();
                double ballY = sourceBall.getLayoutY() + sourceBall.getRadius();
                double speed = sourceBall.getSpeed();
                Ball newBall1 = new Ball(ballX, ballY, screenWidth, screenHeight, -speed * 1.5, -speed * 0.5);
                newBalls.add(newBall1);
                Ball newBall2 = new Ball(ballX, ballY, screenWidth, screenHeight, speed * 1.5, -speed * 0.5);
                newBalls.add(newBall2);
            }
        }
        balls.addAll(newBalls);
        getChildren().addAll(newBalls);
    }
    // (Hàm explode() giữ nguyên)
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


    /**
     * (CẬP NHẬT) Sửa lại logic mất mạng
     */
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

        // (MỚI) Xóa đuôi cũ
        ballTrailManager.clear();

        if (lives > 0) {
            paddle.reset(); // Reset Paddle về giữa
        }

        // Xóa bóng cũ và spawn bóng mới
        removeAndRespawnBall();

        if (lives <= 0) {
            showGameOverScreen();
        } else {
            messageText.setText("Press SPACE to Continue");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
        }
    }

    // (Hàm generateRandom() giữ nguyên)
    private double generateRandom(double min, double max) {
        return min + (rand.nextDouble() * (max - min));
    }

    /**
     * (CẬP NHẬT) Sửa lại logic reset
     */
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

        // (MỚI) Xóa đuôi cũ
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

        messageText.setText("Press SPACE to Start");
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setVisible(true);
        gameRunning = false;
    }

    // (Hàm loadHighScore(), saveHighScore() giữ nguyên)
    private void loadHighScore() {
        try {
            File file = new File(HIGH_SCORE_FILE);
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
    private void saveHighScore() {
        try {
            PrintWriter writer = new PrintWriter(HIGH_SCORE_FILE);
            writer.print(this.highscore);
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Lỗi: Không thể lưu highscore.");
            e.printStackTrace();
        }
    }

    // (Hàm handleKeyPressed(), handleKeyReleased() giữ nguyên)
    public void handleKeyPressed(KeyCode code) {
        if (code == KeyCode.A || code == KeyCode.LEFT) goLeft = true;
        if (code == KeyCode.D || code == KeyCode.RIGHT) goRight = true;
        if (code == KeyCode.SPACE) {
            if (!gameRunning && lives > 0 && !balls.isEmpty()) {
                gameRunning = true;
                messageText.setVisible(false);
            }
        }
        if (code == KeyCode.R) {
            if (!gameRunning) {
                resetGame();
            }
        }
    }
    public void handleKeyReleased(KeyCode code) {
        if (code == KeyCode.A || code == KeyCode.LEFT) goLeft = false;
        if (code == KeyCode.D || code == KeyCode.RIGHT) goRight = false;
    }
}