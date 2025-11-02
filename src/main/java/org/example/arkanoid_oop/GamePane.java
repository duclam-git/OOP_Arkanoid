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
    // (SỬA LỖI) Chuyển thành danh sách Ball
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

    // (MỚI) Thêm một đối tượng Random
    private Random rand = new Random();

    // Đối tượng HUD
    private ImageView heartIcon;
    private Text livesText;
    private Text scoreText;
    private Text messageText;

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

        // (SỬA LỖI) Gọi hàm khởi tạo bóng đầu tiên
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

    /**
     * (MỚI) Tạo và thêm quả bóng đầu tiên vào danh sách.
     */
    private void spawnInitialBall() {
        double ballStartX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2);
        double ballStartY = paddle.getLayoutY() - 15;
        Ball initialBall = new Ball(screenWidth, screenHeight, ballStartX, ballStartY);
        balls.add(initialBall);
        getChildren().add(initialBall);
    }

    /**
     * (CẬP NHẬT) Tạo lưới gạch ngẫu nhiên
     */
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
        messageText = new Text("Press SPACE to Start");
        messageText.setFont(Font.font("Arial", 30)); messageText.setFill(Color.WHITE);
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setLayoutY(screenHeight / 2);
        getChildren().addAll(heartIcon, livesText, scoreText, messageText);
    }

    /**
     * (CẬP NHẬT) Cập nhật trạng thái game, xử lý nhiều bóng.
     */
    private void updateGame() {
        paddle.update(goLeft, goRight);
        updatePowerups(); // Luôn cập nhật vật phẩm rơi

        if (gameRunning) {
            // (SỬA LỖI) Lặp qua tất cả các bóng
            for (Ball ball : balls) {
                ball.update();
            }
            checkCollisions();
        } else {
            // (SỬA LỖI) Code bóng bám theo ván trượt (chỉ áp dụng cho quả bóng đầu tiên)
            if (!balls.isEmpty()) {
                Ball ball = balls.get(0);
                double newBallX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2) - ball.getRadius();
                double newBallY = paddle.getLayoutY() - 15 - (ball.getRadius() * 2);
                ball.setLayoutX(newBallX);
                ball.setLayoutY(newBallY);
            }
        }
    }

    // (Hàm updatePowerups() được cập nhật để xử lý va chạm ở đây)
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

    /**
     * (CẬP NHẬT LỚN) Xử lý tất cả va chạm, đặc biệt là nhiều bóng và góc nảy mượt.
     */
    private void checkCollisions() {

        // --- 1. KIỂM TRA VÀ XỬ LÝ BALL-PADDLE VÀ BALL RƠI XUỐNG ---
        Iterator<Ball> ballIt = balls.iterator();
        while (ballIt.hasNext()) {
            Ball ball = ballIt.next();

            // (Va chạm Bóng với Ván trượt) - (CẢI THIỆN: Dùng tính toán góc nảy)
            if (ball.getBoundsInParent().intersects(paddle.getBoundsInParent())) {

                // Đảm bảo bóng nảy lên (chỉ xử lý khi bóng đang đi xuống)
                if (ball.getDy() > 0) {

                    Bounds ballBounds = ball.getBoundsInParent();
                    Bounds paddleBounds = paddle.getBoundsInParent();

                    // Tính vị trí tương đối của điểm chạm trên Paddle (-1.0 là rìa trái, 1.0 là rìa phải)
                    double hitPointX = ballBounds.getCenterX();
                    double paddleCenter = paddleBounds.getCenterX();
                    double paddleHalfWidth = paddleBounds.getWidth() / 2.0;

                    double relativeHit = (hitPointX - paddleCenter) / paddleHalfWidth;

                    // Thiết lập hướng nảy mới (Dx max = 1.5 * Tốc độ, Dy luôn âm)
                    // Tốc độ ngang càng lớn nếu chạm càng gần rìa

                    double newDx = relativeHit * (ball.getSpeed() * 1.5); // Nhân 1.5 để tăng độ nhạy
                    double newDy = -ball.getSpeed();

                    ball.setDirection(newDx, newDy);

                }
            }

            // (Va chạm Bóng với Đáy)
            if (ball.getLayoutY() >= screenHeight) {
                getChildren().remove(ball);
                ballIt.remove();

                if (balls.isEmpty()) {
                    // Nếu không còn bóng nào, mất một mạng
                    loseLife();
                    return; // Dừng ngay lập tức
                }
            }
        }

        // --- 2. XỬ LÝ VA CHẠM BALL-BRICK (Chỉ xử lý nếu còn bóng) ---
        if (balls.isEmpty()) return;

        // (MỚI) Tạo danh sách tạm thời để xử lý gạch nổ
        List<Brick> bricksToExplode = new ArrayList<>();

        // (SỬA LỖI) Lặp qua các bóng, rồi mới lặp qua gạch
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

                    // Tính Chiều rộng và Chiều cao của vùng chồng
                    double overlapWidth = maxOverlapX - overlapX;
                    double overlapHeight = maxOverlapY - overlapY;

                    boolean wasDestroyed = brick.onHit(); // Gạch bị va chạm

                    // Xác định hướng nảy
                    if (overlapWidth < overlapHeight) {
                        ball.reverseDx();
                    } else {
                        ball.reverseDy();
                    }

                    if (wasDestroyed) {
                        // 1. Xóa gạch bị bóng đập
                        brickIt.remove();
                        getChildren().remove(brickView);
                        score += brick.getScoreValue();

                        // 2. Kiểm tra xem có phải gạch nổ không
                        if (brick instanceof Explosive_brick) {
                            // Thêm vào danh sách chờ nổ
                            bricksToExplode.add(brick);
                        }

                        // 3. Kiểm tra xem có phải gạch vật phẩm không
                        if (brick instanceof Powerup_brick) {
                            spawnPowerup((Powerup_brick) brick);
                        }
                    }

                    scoreText.setText("Score: " + score);

                    // Mỗi quả bóng chỉ phá 1 gạch mỗi khung hình
                    break;
                }
            }
        }


        // (MỚI) Xử lý các vụ nổ
        if (!bricksToExplode.isEmpty()) {
            for (Brick explosiveBrick : bricksToExplode) {
                // ép kiểu an toàn
                if (explosiveBrick instanceof Explosive_brick) {
                    explode((Explosive_brick) explosiveBrick);
                }
            }
            // Cập nhật lại điểm số sau tất cả các vụ nổ
            scoreText.setText("Score: " + score);
        }

        // Kiểm tra thắng
        if (bricks.isEmpty()) {
            gameRunning = false;
            messageText.setText("YOU WIN! Press R to Restart");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
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

    /**
     * (CẬP NHẬT) Xử lý kích hoạt Powerup.
     * @param type Loại Powerup được kích hoạt.
     */
    private void activatePowerup(PowerupType type) {
        if (type == PowerupType.MULTI_BALL) {
            System.out.println("KÍCH HOẠT MULTI-BALL!");
            spawnMultiBall(); // (MỚI) Triển khai logic Multi-Ball
        }
        else if (type == PowerupType.LASER_PADDLE) {
            System.out.println("KÍCH HOẠT LASER PADDLE!");
            // (Logic ván trượt bắn sẽ ở đây)
        }
    }

    /**
     * (MỚI) Triển khai logic Multi-Ball: Tạo thêm 2 quả bóng.
     */
    private void spawnMultiBall() {
        if (balls.isEmpty()) return; // Không thể nhân bản nếu không có bóng nào

        Ball sourceBall = balls.get(0); // Lấy quả bóng đầu tiên để làm mốc vị trí
        double ballX = sourceBall.getLayoutX() + sourceBall.getRadius();
        double ballY = sourceBall.getLayoutY() + sourceBall.getRadius();
        double speed = sourceBall.getSpeed(); // Lấy tốc độ cơ sở

        // Bóng 1: Hướng X trái mạnh, Y lên
        Ball newBall1 = new Ball(ballX, ballY, screenWidth, screenHeight, -speed * 1.5, -speed * 0.5);
        balls.add(newBall1);
        getChildren().add(newBall1);

        // Bóng 2: Hướng X phải mạnh, Y lên
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

        // (MỚI) Tạo danh sách tạm thời để tránh lỗi
        List<Brick> bricksToRemove = new ArrayList<>();

        // Duyệt qua TẤT CẢ gạch còn lại
        for (Brick brick : bricks) {

            // Bỏ qua chính viên gạch vừa nổ (nó đã bị xóa rồi)
            if (brick == sourceExplosiveBrick) {
                continue;
            }

            // Kiểm tra xem gạch có nằm trong vùng nổ không
            if (explosionArea.getBoundsInParent().intersects(brick.getView().getBoundsInParent())) {
                bricksToRemove.add(brick);
            }
        }

        // (MỚI) Bây giờ mới thực hiện phá hủy
        for (Brick brick : bricksToRemove) {
            // Kiểm tra xem gạch có còn trong danh sách chính không
            // (vì một vụ nổ khác có thể đã xóa nó)
            if (bricks.contains(brick)) {

                // Phá hủy gạch (Hard_brick sẽ bị phá ngay lập tức)
                bricks.remove(brick);
                getChildren().remove(brick.getView());
                score += brick.getScoreValue();

                // (MỚI) Nếu một gạch vật phẩm bị nổ, nó vẫn rơi ra vật phẩm!
                if (brick instanceof Powerup_brick) {
                    spawnPowerup((Powerup_brick) brick);
                }

                // (MỚI) Nếu một gạch nổ khác bị nổ, nó cũng sẽ nổ! (Nổ dây chuyền)
                if (brick instanceof Explosive_brick) {
                    // Gọi đệ quy
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

        // (SỬA LỖI) Xóa tất cả các bóng còn lại
        for (Ball ball : balls) {
            getChildren().remove(ball);
        }
        balls.clear();

        if (lives <= 0) {
            messageText.setText("GAME OVER! Press R to Restart");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
        } else {
            messageText.setText("Press SPACE to Continue");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
            paddle.reset();
            spawnInitialBall(); // Tạo lại bóng
        }
    }

    /**
     * (CẬP NHẬT) Reset game.
     */
    private void resetGame() {
        lives = 3;
        livesText.setText("x " + lives);
        score = 0;
        scoreText.setText("Score: 0");

        paddle.reset();

        // Xóa bóng cũ
        for (Ball ball : balls) {
            getChildren().remove(ball);
        }
        balls.clear();
        spawnInitialBall(); // Tạo lại bóng

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
            // (SỬA LỖI) Chỉ cho phép bắt đầu nếu có ít nhất 1 quả bóng
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