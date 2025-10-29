import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random; // (MỚI) Thêm thư viện Random

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
    private Ball ball; // Tạm thời vẫn là 1 quả bóng

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

        double ballStartX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2);
        double ballStartY = paddle.getLayoutY() - 15;
        ball = new Ball(screenWidth, screenHeight, ballStartX, ballStartY);
        getChildren().add(ball);

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
     * (CẬP NHẬT) Tạo lưới gạch ngẫu nhiên
     */
    private void createBricks() {
        // (MỚI) Tăng số lượng gạch
        int brickRows = 7;
        int brickCols = 10;
        double padding = 5; // Giảm padding để vừa nhiều gạch hơn

        // Tính toán lại để căn giữa
        double totalBrickWidth = (brickCols * Brick.BRICK_WIDTH) + ((brickCols - 1) * padding);
        double offsetLeft = (screenWidth - totalBrickWidth) / 2;
        double offsetTop = 50;

        for (int r = 0; r < brickRows; r++) {
            for (int c = 0; c < brickCols; c++) {
                double x = offsetLeft + c * (Brick.BRICK_WIDTH + padding);
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

    // (Hàm updateGame() giữ nguyên)
    private void updateGame() {
        paddle.update(goLeft, goRight);
        updatePowerups(); // Luôn cập nhật vật phẩm rơi

        if (gameRunning) {
            ball.update();
            checkCollisions();
        } else {
            // (Code bóng bám theo ván trượt)
            double newBallX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2) - ball.getRadius();
            double newBallY = paddle.getLayoutY() - 15 - (ball.getRadius() * 2);
            ball.setLayoutX(newBallX);
            ball.setLayoutY(newBallY);
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
        }
    }

    // (CẬP NHẬT) Xử lý tất cả va chạm
    private void checkCollisions() {
        // (Va chạm Bóng với Ván trượt)
        if (ball.getBoundsInParent().intersects(paddle.getBoundsInParent())) {
            if (ball.getLayoutY() + (ball.getRadius() * 2) - ball.getDy() <= paddle.getLayoutY()) {
                ball.reverseDy();
            }
        }

        // (Va chạm Bóng với Đáy)
        if (ball.getLayoutY() >= screenHeight) {
            loseLife();
            return; // Dừng ngay lập tức
        }

        // (Va chạm Ván trượt với Vật phẩm)
        Iterator<Powerup> powerupIt = powerups.iterator();
        while (powerupIt.hasNext()) {
            Powerup powerup = powerupIt.next();
            if (paddle.getBoundsInParent().intersects(powerup.getBoundsInParent())) {
                activatePowerup(powerup.getType());
                getChildren().remove(powerup);
                powerupIt.remove();
            }
        }

        // --- Va chạm Bóng với Gạch ---
        // (MỚI) Tạo danh sách tạm thời để xử lý gạch nổ
        List<Brick> bricksToExplode = new ArrayList<>();

        Iterator<Brick> brickIt = bricks.iterator();
        while (brickIt.hasNext()) {
            Brick brick = brickIt.next();
            Node brickView = brick.getView();

            if (ball.getBoundsInParent().intersects(brickView.getBoundsInParent())) {

                boolean wasDestroyed = brick.onHit(); // Gạch bị va chạm
                ball.reverseDy(); // Bóng nảy ra

                if (wasDestroyed) {
                    // (SỬA LỖI) Xử lý logic PHIÊN BẢN GẠCH BỊ PHÁ HỦY

                    // 1. Xóa gạch bị bóng đập
                    brickIt.remove();
                    getChildren().remove(brickView);
                    score += brick.getScoreValue();

                    // 2. Kiểm tra xem có phải gạch nổ không
                    if (brick instanceof Explosive_brick) {
                        // Thêm vào danh sách chờ nổ
                        bricksToExplode.add(brick);
                    }

                    // 3. (SỬA LỖI) Kiểm tra xem có phải gạch vật phẩm không
                    if (brick instanceof Powerup_brick) {
                        spawnPowerup((Powerup_brick) brick);
                    }
                }

                scoreText.setText("Score: " + score);

                // Chỉ xử lý 1 va chạm mỗi khung hình
                break;
            }
        }

        // (MỚI) Xử lý các vụ nổ (sau khi vòng lặp va chạm chính kết thúc)
        // Điều này tránh lỗi ConcurrentModificationException
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

        // Kiểm tra thắng (sau khi tất cả gạch đã nổ)
        if (bricks.isEmpty()) {
            gameRunning = false;
            messageText.setText("YOU WIN! Press R to Restart");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
        }
    }

    // (Hàm spawnPowerup() giữ nguyên)
    private void spawnPowerup(Powerup_brick brick) {
        PowerupType type = brick.getPowerupType();
        double x = brick.getCenterX();
        double y = brick.getCenterY();
        Powerup powerup = new Powerup(x, y, type);
        powerups.add(powerup);
        getChildren().add(powerup);
    }

    // (Hàm activatePowerup() giữ nguyên)
    private void activatePowerup(PowerupType type) {
        if (type == PowerupType.MULTI_BALL) {
            System.out.println("KÍCH HOẠT MULTI-BALL!");
            // (Logic nhân bóng sẽ ở đây)
        }
        else if (type == PowerupType.LASER_PADDLE) {
            System.out.println("KÍCH HOẠT LASER PADDLE!");
            // (Logic ván trượt bắn sẽ ở đây)
        }
    }


    /**
     * (CẬP NHẬT) Xử lý logic Gạch Nổ
     * @param sourceExplosiveBrick Viên gạch gây ra vụ nổ
     */
    private void explode(Explosive_brick sourceExplosiveBrick) {
        double x = sourceExplosiveBrick.getCenterX();
        double y = sourceExplosiveBrick.getCenterY();
        double explosionRadius = 100; // Bán kính 100px

        Circle explosionArea = new Circle(x, y, explosionRadius);

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


    // (Hàm loseLife() giữ nguyên)
    private void loseLife() {
        lives--;
        livesText.setText("x " + lives);
        gameRunning = false;
        if (lives <= 0) {
            messageText.setText("GAME OVER! Press R to Restart");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
        } else {
            messageText.setText("Press SPACE to Continue");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
            paddle.reset();
            ball.reset();
        }
    }

    // (Hàm resetGame() giữ nguyên)
    private void resetGame() {
        lives = 3;
        livesText.setText("x " + lives);
        score = 0;
        scoreText.setText("Score: 0");

        paddle.reset();
        ball.reset();

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
            if (!gameRunning && lives > 0) {
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