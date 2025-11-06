package org.example.arkanoid_oop;

import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.arkanoid_oop.Brick.*;
import org.example.arkanoid_oop.Entities.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.example.arkanoid_oop.Brick.Brick.BRICK_HEIGHT;
import static org.example.arkanoid_oop.Brick.Brick.BRICK_WIDTH;

/**
 * GamePane là màn chơi chính, chứa tất cả các đối tượng game
 * và quản lý vòng lặp (game loop) chính.
 */
public class GamePane extends Pane {

    private double screenWidth;
    private double screenHeight;

    // Đối tượng game
    private Background background;
    private Paddle paddle;
    private List<Ball> balls = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<Powerup> powerups = new ArrayList<>();
    private List<Teleporter> teleporters = new ArrayList<>();

    // (MỚI CHO LASER)
    private boolean isLaserActive = false;
    private List<Laser> lasers = new ArrayList<>(); // Danh sách quản lý các tia laser đang hoạt động
    private long laserEndTime = 0; // Thời điểm laser hết hạn (nanoTime)
    private long lastShotTime = 0; // Thời điểm bắn laser gần nhất (nanoTime)
    private final long LASER_DURATION_NANO = 4_000_000_000L; // 4 giây
    private final long SHOT_INTERVAL_NANO = 800_000_000L; // 0.8 giây (0.5 giây trong code gốc, điều chỉnh lại theo giá trị đã định nghĩa)

    // Trạng thái input
    private boolean goLeft = false;
    private boolean goRight = false;

    // Trạng thái game
    private boolean gameRunning = false;
    private int lives = 3;
    private int score = 0;
    private int level = 1;

    // Thêm một đối tượng Random
    private Random rand = new Random();

    // Đối tượng HUD
    private ImageView heartIcon;
    private Text livesText;
    private Text scoreText;
    private Text messageText;
    private Text levelText;

    private AnimationTimer gameLoop;

    public GamePane(double width, double height) {
        this.screenWidth = width;
        this.screenHeight = height;

        setPrefSize(screenWidth, screenHeight);

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

        createTeleporters(); // Giữ lại hàm tạo Teleporter

        // Khởi tạo giao diện
        createHUD();

        // Bắt đầu vòng lặp game
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame(now); // Truyền thời gian hiện tại vào
            }
        };
        gameLoop.start();
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

    /**
     * Hàm createTeleporters() (Đã giải quyết xung đột, giữ lại phiên bản thêm Teleporter)
     */
    private void createTeleporters() {
        // Vị trí cố định cho cổng 1
        double teleporter1X = 100;
        double teleporter1Y = 400;

        // Vị trí cho cổng 2
        double teleporter2X = screenWidth - 100;
        double teleporter2Y = 400;

        Teleporter teleporter1 = new Teleporter(teleporter1X, teleporter1Y);
        Teleporter teleporter2 = new Teleporter(teleporter2X, teleporter2Y);

        // Thiết lập liên kết
        teleporter1.setPartner(teleporter2);
        teleporter2.setPartner(teleporter1);

        // Thêm vào danh sách và Pane
        teleporters.add(teleporter1);
        teleporters.add(teleporter2);
        getChildren().addAll(teleporter1, teleporter2);
    }

    // (Hàm createHUD() giữ nguyên)
    private void createHUD() {
        Image heartImage = new Image(getClass().getResourceAsStream("/images/heart.png"));
        heartIcon = new ImageView(heartImage);
        heartIcon.setFitWidth(25); heartIcon.setFitHeight(25);
        heartIcon.setLayoutX(10); heartIcon.setLayoutY(10);
        livesText = new Text("x " + lives);
        livesText.setFont(Font.font("Arial", 20)); livesText.setFill(Color.WHITE);
        livesText.setLayoutX(40); livesText.setLayoutY(30);

        scoreText = new Text("Score: 0");
        scoreText.setFont(Font.font("Arial", 20)); scoreText.setFill(Color.WHITE);
        scoreText.setLayoutX(screenWidth - 120); scoreText.setLayoutY(30);

        levelText = new Text("Level: " + level);
        levelText.setFont(Font.font("Arial", 20)); levelText.setFill(Color.WHITE);
        double levelTextWidth = 100;
        levelText.setLayoutX((screenWidth - levelTextWidth) / 2);
        levelText.setLayoutY(30);

        messageText = new Text("Press SPACE to Start");
        messageText.setFont(Font.font("Arial", 30)); messageText.setFill(Color.WHITE);
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setLayoutY(screenHeight / 2);

        getChildren().addAll(heartIcon, livesText, scoreText, messageText, levelText);
    }

    /**
     * (CẬP NHẬT) Cập nhật trạng thái game, xử lý laser.
     */
    private void updateGame(long now) {
        paddle.update(goLeft, goRight);
        updatePowerups();

        if (gameRunning) {

            // Xử lý Laser (MỚI)
            updateLasers(now);

            for (Ball ball : balls) {
                ball.update();
            }
            checkCollisions(now); // Truyền thời gian hiện tại
        } else {
            if (!balls.isEmpty()) {
                Ball ball = balls.get(0);
                double newBallX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2) - ball.getRadius();
                double newBallY = paddle.getLayoutY() - 15 - (ball.getRadius() * 2);
                ball.setLayoutX(newBallX);
                ball.setLayoutY(newBallY);
            }
        }
    }

    /**
     * (MỚI) Cập nhật trạng thái Laser và bắn đạn.
     * @param now Thời gian hiện tại (nanoTime).
     */
    private void updateLasers(long now) {
        // --- 1. KIỂM TRA HẾT HẠN LASER ---
        if (isLaserActive && now > laserEndTime) {
            isLaserActive = false;
        }

        // --- 2. XỬ LÝ BẮN LASER ---
        if (isLaserActive && now > lastShotTime + SHOT_INTERVAL_NANO) {
            shootLasers();
            lastShotTime = now;
        }

        // --- 3. CẬP NHẬT VỊ TRÍ LASER ---
        Iterator<Laser> laserIt = lasers.iterator();
        while (laserIt.hasNext()) {
            Laser laser = laserIt.next();

            // Cập nhật vị trí và kiểm tra nếu ra khỏi màn hình
            if (laser.update()) {
                getChildren().remove(laser);
                laserIt.remove();
            }
        }
    }

    /**
     * (MỚI) Bắn hai tia laser từ hai bên Paddle.
     */
    private void shootLasers() {
        double paddleY = paddle.getLayoutY();
        double paddleWidth = paddle.getBoundsInLocal().getWidth();
        double paddleX = paddle.getLayoutX();

        // Vị trí bắn laser bên trái (cách mép trái 10px)
        double leftGunX = paddleX + 10;

        // Vị trí bắn laser bên phải (cách mép phải 10px)
        double rightGunX = paddleX + paddleWidth - 10;

        // Cùng vị trí Y (đỉnh của paddle)
        double gunY = paddleY;

        // Tạo laser trái
        Laser laser1 = new Laser(leftGunX, gunY);
        lasers.add(laser1);
        getChildren().add(laser1);

        // Tạo laser phải
        Laser laser2 = new Laser(rightGunX, gunY);
        lasers.add(laser2);
        getChildren().add(laser2);
    }

    // (Hàm updatePowerups() giữ nguyên)
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
                it.remove();
            }
        }
    }

    // (Hàm removeAndRespawnBall() giữ nguyên)
    private void removeAndRespawnBall() {
        for (Ball ball : balls) {
            getChildren().remove(ball);
        }
        balls.clear();
        spawnInitialBall();
    }

    // (Hàm startNextLevel() cập nhật)
    private void startNextLevel() {
        level++;

        // Xóa laser cũ (MỚI)
        isLaserActive = false;
        for (Laser laser : lasers) {
            getChildren().remove(laser);
        }
        lasers.clear();

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

        // 4. Cập nhật HUD
        levelText.setText("Level: " + level);
        messageText.setText("LEVEL CLEARED! Press SPACE to continue.");
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setVisible(true);
    }

    /**
     * (CẬP NHẬT LỚN) Xử lý tất cả va chạm, thêm logic Laser-Brick và Teleporter.
     */
    private void checkCollisions(long now) {
        // --- 1. KIỂM TRA VÀ XỬ LÝ BALL-PADDLE VÀ BALL RƠI XUỐNG ---
        Iterator<Ball> ballIt = balls.iterator();
        while (ballIt.hasNext()) {
            Ball ball = ballIt.next();

            // ... (Ball-Paddle collision logic giữ nguyên)
            if (ball.getBoundsInParent().intersects(paddle.getBoundsInParent())) {

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
                getChildren().remove(ball);
                ballIt.remove();

                if (balls.isEmpty()) {
                    loseLife();
                    return;
                }
            }
        }

        // --- 2. Xử lý CỔNG-BÓNG --- (Giữ lại logic Teleporter)
        for (Ball ball : balls) {
            for (Teleporter teleporter : teleporters) {
                // Kiểm tra va chạm và không trong thời gian cooldown
                if (ball.getBoundsInParent().intersects(teleporter.getBoundsInParent()) && !teleporter.isOnCooldown(now)) {

                    teleporter.teleportBall(ball);
                    // Dịch chuyển khỏi cổng, chuyển sang bóng tiếp theo
                    break;
                }
            }
        }

        // --- 3. XỬ LÝ VA CHẠM LASER-BRICK (MỚI) --- (Đánh số lại)
        List<Brick> bricksHitByLaser = new ArrayList<>();
        Iterator<Laser> laserIt = lasers.iterator();

        while (laserIt.hasNext()) {
            Laser laser = laserIt.next();
            Iterator<Brick> brickIt = bricks.iterator(); // Loop over remaining bricks

            while (brickIt.hasNext()) {
                Brick brick = brickIt.next();
                Node brickView = brick.getView();

                if (laser.getBoundsInParent().intersects(brickView.getBoundsInParent())) {

                    boolean wasDestroyed = brick.onHit(); // Gạch mất 1 máu

                    // Xóa Laser
                    getChildren().remove(laser);
                    laserIt.remove();

                    if (wasDestroyed) {
                        // Lưu vào danh sách để xử lý nổ/xóa sau
                        bricksHitByLaser.add(brick);
                    }

                    scoreText.setText("Score: " + score);
                    break; // Laser chỉ chạm một gạch
                }
            }
        }

        // --- 4. XỬ LÝ VA CHẠM BALL-BRICK --- (Đánh số lại)
        if (balls.isEmpty() && bricksHitByLaser.isEmpty()) return; // Thoát nếu không có gì để va chạm

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
                        }
                    }

                    scoreText.setText("Score: " + score);
                    break;
                }
            }
        }

        // --- 5. XỬ LÝ GẠCH BỊ LASER PHÁ HỦY HOÀN TOÀN --- (Đánh số lại)
        // Xử lý các gạch mà laser đã bắn vỡ (vì laser không tự xóa gạch khỏi list bricks)
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


        // --- 6. Xử lý các vụ nổ (Explosion logic giữ nguyên) --- (Đánh số lại)
        if (!bricksToExplode.isEmpty()) {
            for (Brick explosiveBrick : bricksToExplode) {
                if (explosiveBrick instanceof Explosive_brick) {
                    explode((Explosive_brick) explosiveBrick);
                }
            }
            scoreText.setText("Score: " + score);
        }

        // --- 7. Kiểm tra chuyển cấp độ --- (Đánh số lại)
        if (bricks.isEmpty()) {
            startNextLevel();
        }
    }

    // (Hàm spawnPowerup() giữ nguyên)
    private void spawnPowerup(Powerup_brick brick) {
        double chance = rand.nextDouble();

        if (chance <= 0.25) {
            PowerupType type = brick.getPowerupType();
            double x = brick.getCenterX();
            double y = brick.getCenterY();
            Powerup powerup = new Powerup(x, y, type);
            powerups.add(powerup);
            getChildren().add(powerup);
        }

    }

    /**
     * (CẬP NHẬT) Xử lý kích hoạt Powerup, thêm logic Laser.
     */
    private void activatePowerup(PowerupType type) {
        if (type == PowerupType.MULTI_BALL) {
            System.out.println("KÍCH HOẠT MULTI-BALL!");
            spawnMultiBall();
        }
        else if (type == PowerupType.LASER_PADDLE) {
            System.out.println("KÍCH HOẠT LASER PADDLE! (4 giây)");

            // Kích hoạt Laser
            isLaserActive = true;
            // Thiết lập thời điểm kết thúc
            laserEndTime = System.nanoTime() + LASER_DURATION_NANO;
            // Đặt thời điểm bắn đầu tiên ngay lập tức
            lastShotTime = 0;
        }
    }

    // (Hàm spawnMultiBall() giữ nguyên)
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


    // (Hàm explode() giữ nguyên)
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
     * (CẬP NHẬT) Xử lý mất mạng, thêm dọn dẹp Laser.
     */
    private void loseLife() {
        lives--;
        livesText.setText("x " + lives);
        gameRunning = false;

        // Xóa laser cũ (MỚI)
        isLaserActive = false;
        for (Laser laser : lasers) {
            getChildren().remove(laser);
        }
        lasers.clear();

        removeAndRespawnBall();

        if (lives <= 0) {
            messageText.setText("GAME OVER! Press R to Restart");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
        } else {
            messageText.setText("Press SPACE to Continue");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
            paddle.reset();
        }
    }

    /**
     * (CẬP NHẬT) Reset game, thêm dọn dẹp Laser.
     */
    private void resetGame() {
        lives = 3;
        livesText.setText("x " + lives);
        score = 0;
        scoreText.setText("Score: 0");
        level = 1;
        levelText.setText("Level: " + level);

        // Xóa laser cũ (MỚI)
        isLaserActive = false;
        for (Laser laser : lasers) {
            getChildren().remove(laser);
        }
        lasers.clear();

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

    // (Hàm handleKeyPressed và handleKeyReleased giữ nguyên)
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