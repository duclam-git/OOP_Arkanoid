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

    // Trạng thái input
    private boolean goLeft = false;
    private boolean goRight = false;

    // Trạng thái game
    private boolean gameRunning = false;
    private int lives = 3;
    private int score = 0;
    private int level = 1; // (MỚI) Biến theo dõi cấp độ

    // Thêm một đối tượng Random
    private Random rand = new Random();

    // Đối tượng HUD
    private ImageView heartIcon;
    private Text livesText;
    private Text scoreText;
    private Text messageText;
    private Text levelText; // (MỚI) Text hiển thị cấp độ

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

        // Khởi tạo giao diện
        createHUD();

        // Bắt đầu vòng lặp game
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
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
        // (MỚI) Tăng số lượng gạch
        int brickRows = 7;
        int brickCols = 10;
        double padding = 5; // Giảm padding để vừa nhiều gạch hơn

        // Tính toán lại để căn giữa
        double totalBrickWidth = (brickCols * BRICK_WIDTH) + ((brickCols - 1) * padding);
        double offsetLeft = (screenWidth - totalBrickWidth) / 2;
        double offsetTop = 50;

        for (int r = 0; r < brickRows; r++) {
            for (int c = 0; c < brickCols; c++) {
                double x = offsetLeft + c * (BRICK_WIDTH + padding);
                double y = offsetTop + r * (Brick.BRICK_HEIGHT + padding);

                Brick brick = null;

                // --- (MỚI) Logic tạo gạch ngẫu nhiên ---
                double chance = rand.nextDouble(); // Lấy một số ngẫu nhiên từ 0.0 đến 1.0

                if (chance < 0.1) { // 10% cơ hội là gạch nổ
                    brick = new Explosive_brick(x, y);
                }
                else if (chance < 0.25) { // 15% cơ hội là gạch vật phẩm
                    brick = new Powerup_brick(x, y);
                }
                else if (chance < 0.45) { // 20% cơ hội là gạch cứng
                    brick = new Hard_brick(x, y);
                }
                else { // 55% còn lại là gạch thường
                    brick = new Normal_brick(x, y);
                }

                bricks.add(brick);
            }
        }
    }

    /**
     * (CẬP NHẬT) Khởi tạo HUD, thêm Level Text.
     */
    private void createHUD() {
        // Lives
        Image heartImage = new Image(getClass().getResourceAsStream("/images/heart.png"));
        heartIcon = new ImageView(heartImage);
        heartIcon.setFitWidth(25); heartIcon.setFitHeight(25);
        heartIcon.setLayoutX(10); heartIcon.setLayoutY(10);
        livesText = new Text("x " + lives);
        livesText.setFont(Font.font("Arial", 20)); livesText.setFill(Color.WHITE);
        livesText.setLayoutX(40); livesText.setLayoutY(30);

        // Score
        scoreText = new Text("Score: 0");
        scoreText.setFont(Font.font("Arial", 20)); scoreText.setFill(Color.WHITE);
        scoreText.setLayoutX(screenWidth - 120); scoreText.setLayoutY(30);

        // (MỚI) Level Text - Đặt ở giữa Score và Lives
        levelText = new Text("Level: " + level);
        levelText.setFont(Font.font("Arial", 20)); levelText.setFill(Color.WHITE);
        // Tính toán vị trí X để căn giữa
        double levelTextWidth = 100; // Giả sử độ rộng cố định để căn giữa
        levelText.setLayoutX((screenWidth - levelTextWidth) / 2);
        levelText.setLayoutY(30);

        // Message
        messageText = new Text("Press SPACE to Start");
        messageText.setFont(Font.font("Arial", 30)); messageText.setFill(Color.WHITE);
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setLayoutY(screenHeight / 2);

        // Thêm Level Text vào Pane
        getChildren().addAll(heartIcon, livesText, scoreText, messageText, levelText);
    }

    // (Hàm updateGame() giữ nguyên)
    private void updateGame() {
        paddle.update(goLeft, goRight);
        updatePowerups(); // Luôn cập nhật vật phẩm rơi

        if (gameRunning) {
            for (Ball ball : balls) {
                ball.update();
            }
            checkCollisions();
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

    // (Hàm updatePowerups() giữ nguyên)
    private void updatePowerups() {
        Iterator<Powerup> it = powerups.iterator();
        while (it.hasNext()) {
            Powerup powerup = it.next();
            powerup.update(); // Làm vật phẩm rơi xuống

            if (powerup.getLayoutY() > screenHeight) {
                getChildren().remove(powerup);
                it.remove();
            }

            // (Va chạm Ván trượt với Vật phẩm)
            if (paddle.getBoundsInParent().intersects(powerup.getBoundsInParent())) {
                activatePowerup(powerup.getType());
                getChildren().remove(powerup);
                it.remove();
            }
        }
    }

    // (Hàm removeAndRespawnBall() giữ nguyên)
    private void removeAndRespawnBall() {
        // Xóa tất cả các bóng hiện có
        for (Ball ball : balls) {
            getChildren().remove(ball);
        }
        balls.clear();

        // Tạo lại bóng ban đầu, nó sẽ tự động bám theo Paddle khi gameRunning=false
        spawnInitialBall();
    }

    /**
     * (CẬP NHẬT) Thiết lập trạng thái và tạo gạch cho cấp độ tiếp theo, tăng Level.
     */
    private void startNextLevel() {
        level++; // (QUAN TRỌNG) Tăng cấp độ

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
        levelText.setText("Level: " + level); // Cập nhật Level Text
        messageText.setText("LEVEL CLEARED! Press SPACE to continue.");
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setVisible(true);
    }

    // (Hàm checkCollisions() giữ nguyên)
    private void checkCollisions() {

        // --- 1. KIỂM TRA VÀ XỬ LÝ BALL-PADDLE VÀ BALL RƠI XUỐNG ---
        Iterator<Ball> ballIt = balls.iterator();
        while (ballIt.hasNext()) {
            Ball ball = ballIt.next();

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

        // --- 2. XỬ LÝ VA CHẠM BALL-BRICK ---
        if (balls.isEmpty()) return;

        List<Brick> bricksToExplode = new ArrayList<>();

        for (Ball ball : balls) {
            Iterator<Brick> brickIt = bricks.iterator();
            while (brickIt.hasNext()) {
                Brick brick = brickIt.next();
                Node brickView = brick.getView();

                if (ball.getBoundsInParent().intersects(brickView.getBoundsInParent())) {

                    Bounds ballBounds = ball.getBoundsInParent();
                    Bounds brickBounds = brickView.getBoundsInParent();

                    //TÍNH TOÁN VÙNG CHỒNG
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


        if (!bricksToExplode.isEmpty()) {
            for (Brick explosiveBrick : bricksToExplode) {
                if (explosiveBrick instanceof Explosive_brick) {
                    explode((Explosive_brick) explosiveBrick);
                }
            }
            scoreText.setText("Score: " + score);
        }

        if (bricks.isEmpty()) {
            startNextLevel();
        }
    }

    // (Hàm spawnPowerup() giữ nguyên)
    private void spawnPowerup(Powerup_brick brick) {
        double chance = rand.nextDouble(); // Lấy một số ngẫu nhiên từ 0.0 đến 1.0

        if (chance <= 0.25) {
            PowerupType type = brick.getPowerupType();
            double x = brick.getCenterX();
            double y = brick.getCenterY();
            Powerup powerup = new Powerup(x, y, type);
            powerups.add(powerup);
            getChildren().add(powerup);
        }

    }

    // (Hàm activatePowerup() giữ nguyên)
    private void activatePowerup(PowerupType type) {
        if (type == PowerupType.MULTI_BALL) {
            System.out.println("KÍCH HOẠT MULTI-BALL!");
            spawnMultiBall();
        }
        else if (type == PowerupType.LASER_PADDLE) {
            System.out.println("KÍCH HOẠT LASER PADDLE!");
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
        int explosionRadius = 3; // Đường kính 3 ô gạch

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


    // (Hàm loseLife() giữ nguyên)
    private void loseLife() {
        lives--;
        livesText.setText("x " + lives);
        gameRunning = false;

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
     * (CẬP NHẬT) Reset game, đặt Level về 1.
     */
    private void resetGame() {
        lives = 3;
        livesText.setText("x " + lives);
        score = 0;
        scoreText.setText("Score: 0");
        level = 1; // (QUAN TRỌNG) Đặt lại Level
        levelText.setText("Level: " + level); // Cập nhật HUD

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