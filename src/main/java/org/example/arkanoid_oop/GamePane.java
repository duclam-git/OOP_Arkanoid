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

    // THÊM: Tên file lưu High Score
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    // --- SINGLETON IMPLEMENTATION ---
    private static GamePane instance;
    // --------------------------------

    private Stage stage;

    private double screenWidth;
    private double screenHeight;

    // Đối tượng game
    private Background background;
    private Paddle paddle;
    private List<Ball> balls = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<Powerup> powerups = new ArrayList<>();
    private List<Teleporter> teleporters = new ArrayList<>();

    // (LASER)
    private boolean isLaserActive = false;
    private List<Laser> lasers = new ArrayList<>();
    private long laserEndTime = 0;
    private long lastShotTime = 0;
    private final long LASER_DURATION_NANO = 4_000_000_000L;
    private final long SHOT_INTERVAL_NANO = 800_000_000L;

    // (DOUBLE PADDLE)
    private boolean isDoublePaddleActive = false;
    private long doublePaddleEndTime = 0;
    private final long DOUBLE_PADDLE_DURATION_NANO = 15_000_000_000L; // Giữ lại 15 giây

    // (SHIELD)
    private boolean isShieldActive = false;
    private Rectangle shieldBar;

    // Trạng thái input
    private boolean goLeft = false;
    private boolean goRight = false;

    // Trạng thái game
    private boolean gameRunning = false;
    private int lives = 3;
    private int score = 0;
    private int level = 1;
    private int highscore = 0; // THÊM: Biến lưu High Score

    // Thêm một đối tượng Random
    private Random rand = new Random();

    // Đối tượng HUD
    private ImageView heartIcon;
    private Text livesText;
    private Text scoreText;
    private Text messageText;
    private Text levelText;
    private Text highscoreText; // THÊM: Đối tượng hiển thị High Score

    private AnimationTimer gameLoop;

    private AudioManager audio; // Thêm trường AudioManager

    // *** KHAI BÁO BIẾN MỚI ***
    private VBox gameOverOverlay;
    private Rectangle backdrop; // Backdrop làm tối màn hình

    // --- THAY ĐỔI: Chuyển constructor thành private ---
    private GamePane(double width, double height, Stage stage) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.stage = stage;

        setPrefSize(screenWidth, screenHeight);

        audio = AudioManager.getInstance();

        loadHighScore(); // GỌI HÀM LOAD HIGH SCORE KHI KHỞI ĐỘNG

        // Khởi tạo các đối tượng game
        background = new Background(screenWidth, screenHeight);
        getChildren().add(background);

        paddle = new Paddle(screenWidth, screenHeight);
        getChildren().add(paddle);

        spawnInitialBall();

        // Tạo gạch và thêm vào Pane
        createBricks();
        for (Brick brick : bricks) {
            getChildren().add(brick.getView());
        }

        createTeleporters();

        // Khởi tạo giao diện
        createHUD();
        createShieldBar();

        // KHỞI TẠO OVERLAY GAME OVER (ẨN BAN ĐẦU)
        createGameOverOverlay();

        // Bắt đầu vòng lặp game
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // CẬP NHẬT PADDLE LUÔN LUÔN CHẠY
                paddle.update(goLeft, goRight);

                if (gameRunning) {
                    updateGame(now);
                } else {
                    // Logic Ball dính Paddle
                    updateBallStuckToPaddle();
                }
            }
        };
        gameLoop.start();
    }

    // *** PHƯƠNG THỨC MỚI: Cập nhật bóng dính vào Paddle khi game chưa chạy ***
    private void updateBallStuckToPaddle() {
        if (!balls.isEmpty()) {
            Ball ball = balls.get(0);

            // LẤY VỊ TRÍ MỚI CỦA PADDLE VÀ ĐẶT VỊ TRÍ BÓNG
            double newBallX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2) - ball.getRadius();
            double newBallY = paddle.getLayoutY() - 15 - (ball.getRadius() * 2);

            ball.setLayoutX(newBallX);
            ball.setLayoutY(newBallY);
        }
    }


    // --- THÊM: Phương thức truy cập tĩnh (Static Factory Method) ---
    public static GamePane getInstance(double width, double height, Stage stage) {
        if (instance == null) {
            instance = new GamePane(width, height, stage);
        }
        return instance;
    }

    // *** PHƯƠNG THỨC MỚI: Reset Instance ***
    public static void resetInstance() {
        if (instance != null && instance.gameLoop != null) {
            instance.gameLoop.stop(); // Dừng vòng lặp cũ
        }
        instance = null;
    }

    private void createShieldBar() {
        double barHeight = 5;
        // Tạo Rectangle bao phủ toàn bộ chiều rộng, dày 5px, nằm ngay trên đáy map
        shieldBar = new Rectangle(0, screenHeight - barHeight, screenWidth, barHeight);
        shieldBar.setFill(Color.AQUA);
        shieldBar.setOpacity(0.7);
        shieldBar.setVisible(false);

        getChildren().add(shieldBar);
    }

    private void spawnInitialBall() {
        double ballStartX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2);
        double ballStartY = paddle.getLayoutY() - 15;
        Ball initialBall = new Ball(screenWidth, screenHeight, ballStartX, ballStartY);
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
                double y = offsetTop + r * (Brick.BRICK_HEIGHT + padding);

                Brick brick = null;
                double chance = rand.nextDouble();

                if (chance < 0.1) {
                    brick = new Explosive_brick(x, y);
                }
                else if (chance < 0.25) {
                    brick = new Powerup_brick(x, y);
                }
                else if (chance < 0.45) {
                    brick = new Hard_brick(x, y);
                }
                else {
                    brick = new Normal_brick(x, y);
                }

                bricks.add(brick);
            }
        }
    }

    private void createTeleporters() {
        // Xóa các Teleporter cũ
        for (Teleporter teleporter : teleporters) {
            getChildren().remove(teleporter);
        }
        teleporters.clear();

        // Xác định vùng an toàn
        // Vùng an toàn:
        // X: 50 -> screenWidth - 50 (Tránh cạnh màn hình)
        // Y: 250 -> screenHeight - 100;
        double minX = 50;
        double maxX = screenWidth - 50;
        double minY = 250;
        double maxY = screenHeight - 100;

        // Tạo vị trí ngẫu nhiên cho 2 cổng
        double teleporter1X = generateRandom(minX, maxX / 2);
        double teleporter1Y = generateRandom(minY, maxY);

        double teleporter2X = generateRandom(maxX / 2, maxX);
        double teleporter2Y = generateRandom(minY, maxY);

        // Khởi tạo và liên kết
        Teleporter teleporter1 = new Teleporter(teleporter1X, teleporter1Y);
        Teleporter teleporter2 = new Teleporter(teleporter2X, teleporter2Y);

        teleporter1.setPartner(teleporter2);
        teleporter2.setPartner(teleporter1);

        teleporters.add(teleporter1);
        teleporters.add(teleporter2);
        getChildren().addAll(teleporter1, teleporter2);
    }

    private void createHUD() {
        Image heartImage = new Image(getClass().getResourceAsStream("/images/Heart.png"));
        heartIcon = new ImageView(heartImage);
        heartIcon.setFitWidth(25); heartIcon.setFitHeight(25);
        heartIcon.setLayoutX(10); heartIcon.setLayoutY(10);
        livesText = new Text("x " + lives);
        livesText.setFont(Font.font("Arial", 20)); livesText.setFill(Color.WHITE);
        livesText.setLayoutX(40); livesText.setLayoutY(30);

        scoreText = new Text("Score: 0");
        scoreText.setFont(Font.font("Arial", 20)); scoreText.setFill(Color.WHITE);
        scoreText.setLayoutX(screenWidth - 120); scoreText.setLayoutY(30);

        // ĐIỀU CHỈNH VỊ TRÍ LEVEL VÀ THÊM HIGH SCORE
        levelText = new Text("Level: " + level);
        levelText.setFont(Font.font("Arial", 20)); levelText.setFill(Color.WHITE);
        // Đặt Level sang trái giữa
        levelText.setLayoutX(screenWidth / 2 - 120);
        levelText.setLayoutY(30);

        highscoreText = new Text("High Score: " + highscore); // KHỞI TẠO HIGH SCORE DÙNG GIÁ TRỊ ĐÃ LOAD
        highscoreText.setFont(Font.font("Arial", 20)); highscoreText.setFill(Color.WHITE);
        // Đặt High Score sang phải giữa
        highscoreText.setLayoutX(screenWidth / 2 + 20);
        highscoreText.setLayoutY(30);

        messageText = new Text("Press SPACE to Start");
        messageText.setFont(Font.font("Arial", 30)); messageText.setFill(Color.WHITE);
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setLayoutY(screenHeight / 2);

        // THÊM highscoreText VÀO PANE
        getChildren().addAll(heartIcon, livesText, scoreText, messageText, levelText, highscoreText);
    }

    // *** PHƯƠNG THỨC MỚI: Tạo màn hình Game Over ***
    private void createGameOverOverlay() {
        // Overlay mờ: Tách khỏi VBox và làm Node riêng
        backdrop = new Rectangle(screenWidth, screenHeight, Color.BLACK);
        backdrop.setOpacity(0.7);
        backdrop.setVisible(false); // Ẩn ban đầu

        Text gameOverTitle = new Text("GAME OVER");
        gameOverTitle.setFont(Font.font("Orbitron", 50));
        gameOverTitle.setFill(Color.RED);

        Text finalScoreText = new Text("Final Score: 0"); // Score sẽ được cập nhật sau
        finalScoreText.setFont(Font.font("Arial", 24));
        finalScoreText.setFill(Color.WHITE);

        Text highscoreNotice = new Text("");
        highscoreNotice.setFont(Font.font("Arial", 20));
        highscoreNotice.setFill(Color.YELLOW);
        highscoreNotice.setId("highscoreNotice"); // Dùng ID để truy cập và cập nhật sau

        // --- NÚT ---
        Button restartBtn = new Button("RESTART");
        Button quitBtn = new Button("QUIT");

        for (Button btn : new Button[]{restartBtn, quitBtn}) {
            btn.setPrefWidth(150);
            btn.setFont(Font.font("Orbitron", 18));
            btn.setStyle("""
                -fx-background-color: #00ffff;
                -fx-text-fill: black;
                -fx-border-color: #00ffff;
                -fx-border-width: 2;
                -fx-background-radius: 10;
                -fx-border-radius: 10;
            """);
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-background-color: #00ffff; -fx-text-fill: black; -fx-border-color: #00ffff; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;"
            ));
        }

        // --- SỰ KIỆN NÚT ---
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

        // VBox chính cho nội dung Game Over (không bao gồm backdrop)
        gameOverOverlay = new VBox(20, gameOverTitle, finalScoreText, highscoreNotice, buttonBox);
        gameOverOverlay.setAlignment(Pos.CENTER);
        gameOverOverlay.setPrefSize(screenWidth, screenHeight);

        // Thêm backdrop và overlay riêng biệt vào GamePane
        getChildren().add(backdrop);
        getChildren().add(gameOverOverlay);

        // Đảm bảo backdrop và overlay nằm trên cùng, và ẩn đi
        backdrop.toFront();
        gameOverOverlay.toFront();
        gameOverOverlay.setVisible(false);
    }

    // *** PHƯƠNG THỨC MỚI: Hiển thị màn hình Game Over ***
    public void showGameOverScreen() {
        gameRunning = false;
        audio.stopGameMusic();
        audio.play("game_over");

        // *** QUAN TRỌNG: Dừng Animation Timer khi game kết thúc ***
        gameLoop.stop();

        // Cập nhật High Score và thông báo
        // finalScoreText là phần tử thứ 2 (index 1) trong gameOverOverlay
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

        // Ẩn các đối tượng game (trừ background, HUD, và các overlay)
        for (Node node : getChildren()) {
            // Kiểm tra các Node cần ẩn để màn hình Game Over hiện rõ
            if (node != background && node != backdrop && node != gameOverOverlay) {
                node.setVisible(false);
            }
        }

        // *** HIỆN CẢ BACKDROP VÀ OVERLAY ***
        backdrop.setVisible(true); // Hiện backdrop làm tối
        gameOverOverlay.setVisible(true);
        gameOverOverlay.toFront();
    }

    // *** PHƯƠNG THỨC ĐÃ SỬA: Xử lý logic Restart Game ***
    public void restartGame() {
        // Ẩn màn hình Game Over và Backdrop
        gameOverOverlay.setVisible(false);
        backdrop.setVisible(false);

        // HIỆN LẠI CÁC THÀNH PHẦN CỐ ĐỊNH VÀ HUD

        // 1. Hiển thị HUD
        heartIcon.setVisible(true);
        livesText.setVisible(true);
        scoreText.setVisible(true);
        levelText.setVisible(true);
        highscoreText.setVisible(true);
        background.setVisible(true);

        // 2. Hiển thị Paddle
        paddle.setVisible(true);

        // 3. Hiển thị Teleporters
        for (Teleporter teleporter : teleporters) {
            teleporter.setVisible(true);
        }

        resetGame(); // Gọi lại logic reset game (đã có)
        audio.playGameMusic(); // Bắt đầu nhạc game

        // Đặt lại trạng thái game
        gameRunning = false;
        messageText.setVisible(true);

        // Bắt đầu lại Animation Timer
        gameLoop.start();
    }

    // *** PHƯƠNG THỨC CHÍNH CỦA VÒNG LẶP GAME ***
    private void updateGame(long now) {
        // paddle.update(goLeft, goRight); // Đã chuyển lên AnimationTimer.handle()
        updatePowerups();

        // --- KIỂM TRA HẾT HẠN POWERUP ---
        if (isDoublePaddleActive && now > doublePaddleEndTime) {
            paddle.setNormalLength();
            isDoublePaddleActive = false;
        }
        // ---------------------------------

        // Logic này chỉ chạy khi gameRunning = true
        updateLasers(now);

        for (Ball ball : balls) {
            ball.update();
        }
        checkCollisions(now);
    }

    private void updateLasers(long now) {
        // --- 1. KIỂM TRA HẾT HẠN LASER ---
        if (isLaserActive && now > laserEndTime) {
            isLaserActive = false;
        }

        // --- 2. XỬ LÝ BẮN LASER ---
        if (isLaserActive && now > lastShotTime + SHOT_INTERVAL_NANO) {
            shootLasers();
            audio.play("laser");
            lastShotTime = now;
        }

        // --- 3. CẬP NHẬT VỊ TRÍ LASER ---
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

        // Xóa laser cũ
        isLaserActive = false;
        for (Laser laser : lasers) {
            getChildren().remove(laser);
        }
        lasers.clear();

        // (RESET POWERUP)
        if (isDoublePaddleActive) {
            paddle.setNormalLength();
            isDoublePaddleActive = false;
        }
        isShieldActive = false;
        shieldBar.setVisible(false);

        // 1. Xóa vật phẩm (Powerups) còn sót lại
        for (Powerup powerup : powerups) {
            getChildren().remove(powerup);
        }
        powerups.clear();

        // 2. Đưa game về trạng thái PAUSED và đặt lại bóng về Paddle
        gameRunning = false;
        removeAndRespawnBall();
        paddle.reset();

        // 3. Tạo gạch mới
        createBricks();
        for (Brick brick : bricks) {
            getChildren().add(brick.getView());
        }

        // 4. Tạo teleporter mới
        createTeleporters();

        // 4. Cập nhật HUD
        levelText.setText("Level: " + level);
        messageText.setText("LEVEL CLEARED! Press SPACE to continue.");
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setVisible(true);
    }

    private void checkCollisions(long now) {
        // --- 1. KIỂM TRA VÀ XỬ LÝ BALL-PADDLE VÀ BALL RƠI XUỐNG ---
        Iterator<Ball> ballIt = balls.iterator();
        while (ballIt.hasNext()) {
            Ball ball = ballIt.next();

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

            // (Va chạm Bóng với Đáy)
            if (ball.getLayoutY() >= screenHeight) {

                // Xử lý Shield
                if (isShieldActive) {
                    isShieldActive = false;
                    shieldBar.setVisible(false);

                    ball.reverseDy();
                    ball.setLayoutY(screenHeight - ball.getRadius() * 2 - 1);

                    audio.play("hit");

                    continue;
                }

                // Nếu không có Shield, mất bóng
                getChildren().remove(ball);
                ballIt.remove();

                if (balls.isEmpty()) {
                    audio.play("lose");
                    loseLife();
                    return;
                }
            }
        }

        // --- 2. Xử lý CỔNG-BÓNG ---
        for (Ball ball : balls) {
            for (Teleporter teleporter : teleporters) {
                if (ball.getBoundsInParent().intersects(teleporter.getBoundsInParent()) && !teleporter.isOnCooldown(now)) {

                    teleporter.teleportBall(ball);
                    audio.play("tele");
                    break;
                }
            }
        }

        // --- 3. XỬ LÝ VA CHẠM LASER-BRICK ---
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
                    // Xóa Laser
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

        // --- 4. XỬ LÝ VA CHẠM BALL-BRICK ---
        if (balls.isEmpty() && bricksHitByLaser.isEmpty()) return;

        List<Brick> bricksToExplode = new ArrayList<>();

        for (Ball ball : balls) {
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

        // --- 5. XỬ LÝ GẠCH BỊ LASER PHÁ HỦY HOÀN TOÀN ---
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


        // --- 6. Xử lý các vụ nổ ---
        if (!bricksToExplode.isEmpty()) {
            for (Brick explosiveBrick : bricksToExplode) {
                if (explosiveBrick instanceof Explosive_brick) {
                    audio.play("explosion");
                    explode((Explosive_brick) explosiveBrick);
                }
            }
            scoreText.setText("Score: " + score);
        }

        // --- 7. Kiểm tra chuyển cấp độ ---
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

    /**
     * (CẬP NHẬT) Xử lý kích hoạt Powerup.
     */
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

        Ball sourceBall = balls.get(0);
        double ballX = sourceBall.getLayoutX() + sourceBall.getRadius();
        double ballY = sourceBall.getLayoutY() + sourceBall.getRadius();
        double speed = sourceBall.getSpeed();

        Ball newBall1 = new Ball(ballX, ballY, screenWidth, screenHeight, -speed * 1.5, -speed * 0.5);
        balls.add(newBall1);
        getChildren().add(newBall1);

        Ball newBall2 = new Ball(ballX, ballY, screenWidth, screenHeight, speed * 1.5, -speed * 0.5);
        balls.add(newBall2);
        getChildren().add(newBall2);
    }


    private void explode(Explosive_brick sourceExplosiveBrick) {
        int explosionRadius = 3;

        Rectangle explosionArea = new Rectangle((int)sourceExplosiveBrick.getX() - BRICK_WIDTH * (explosionRadius / 2),
                (int)sourceExplosiveBrick.getY() - BRICK_HEIGHT  * (explosionRadius / 2),
                BRICK_WIDTH * explosionRadius, BRICK_HEIGHT * explosionRadius);

        List<Brick> bricksToRemove = new ArrayList<>();

        for (Brick brick : bricks) {

            if (brick == sourceExplosiveBrick) {
                continue;
            }

            if (explosionArea.getBoundsInParent().intersects(brick.getView().getBoundsInParent())) {
                bricksToRemove.add(brick);
            }
        }

        for (Brick brick : bricksToRemove) {
            if (bricks.contains(brick)) {

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


    /**
     * (CẬP NHẬT) Xử lý mất mạng.
     */
    private void loseLife() {
        lives--;
        livesText.setText("x " + lives);
        gameRunning = false;

        // Xóa laser cũ
        isLaserActive = false;
        for (Laser laser : lasers) {
            getChildren().remove(laser);
        }
        lasers.clear();

        // (RESET POWERUP)
        if (isDoublePaddleActive) {
            paddle.setNormalLength();
            isDoublePaddleActive = false;
        }
        isShieldActive = false;
        shieldBar.setVisible(false);

        // *** THAY ĐỔI VỊ TRÍ PADDLE.RESET() ***
        if (lives > 0) {
            paddle.reset(); // Reset Paddle về giữa ngay lập tức, nếu game còn tiếp tục
        }

        // Ball sẽ spawn ở vị trí Paddle HIỆN TẠI (vừa được reset nếu lives > 0)
        removeAndRespawnBall();

        if (lives <= 0) {
            // HIỆN MÀN HÌNH GAME OVER MỚI
            showGameOverScreen();
        } else {
            messageText.setText("Press SPACE to Continue");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
            // Bỏ paddle.reset() cũ
        }
    }

    /**
     * Trả giá trị random từ min đến mã
     * @param min min
     * @param max mã
     * @return Số random
     */
    private double generateRandom(double min, double max) {
        return min + (rand.nextDouble() * (max - min));
    }

    /**
     * (CẬP NHẬT) Reset game, reset Powerup.
     */
    private void resetGame() {
        lives = 3;
        livesText.setText("x " + lives);
        score = 0;
        scoreText.setText("Score: 0");
        level = 1;
        levelText.setText("Level: " + level);

        // Xóa laser cũ
        isLaserActive = false;
        for (Laser laser : lasers) {
            getChildren().remove(laser);
        }
        lasers.clear();

        // (RESET POWERUP)
        if (isDoublePaddleActive) {
            paddle.setNormalLength();
            isDoublePaddleActive = false;
        }
        isShieldActive = false;
        shieldBar.setVisible(false);

        paddle.reset();

        removeAndRespawnBall();

        // Xóa gạch cũ
        for (Brick brick : bricks) {
            getChildren().remove(brick.getView());
        }
        bricks.clear();

        // Xóa vật phẩm cũ
        for (Powerup powerup : powerups) {
            getChildren().remove(powerup);
        }
        powerups.clear();

        // Tạo gạch mới
        createBricks();
        for (Brick brick : bricks) {
            getChildren().add(brick.getView());
        }

        messageText.setText("Press SPACE to Start");
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setVisible(true);

        gameRunning = false;
    }

    /**
     * (MỚI) Đọc High Score từ file.
     */
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
            // Đây là lỗi bình thường khi chạy lần đầu, không cần in lỗi quá nghiêm trọng
        } catch (Exception e) {
            System.out.println("Lỗi: Không thể tải highscore.");
            e.printStackTrace();
        }
    }

    /**
     * (MỚI) Lưu High Score vào file.
     */
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