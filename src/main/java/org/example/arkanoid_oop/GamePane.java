import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Lớp GamePane là màn chơi chính, chứa tất cả các đối tượng game.
 * Nó kế thừa Pane.
 */
public class GamePane extends Pane {

    private double screenWidth;
    private double screenHeight;

    // Các đối tượng game
    private Background background;
    private Paddle paddle;
    private Ball ball;

    // Trạng thái game
    private boolean goLeft = false;
    private boolean goRight = false;
    private boolean gameRunning = false;

    private int lives = 3; // Bắt đầu với 3 mạng
    private ImageView heartIcon; // Icon trái tim
    private Text livesText; // Chữ "x 3"
    private Text messageText; // Thông báo "Press SPACE to Start"
    private Text scoreText; // (Chúng ta thêm luôn placeholder cho điểm số)

    // Vòng lặp game
    private AnimationTimer gameLoop;

    public GamePane(double width, double height) {
        this.screenWidth = width;
        this.screenHeight = height;

        setPrefSize(screenWidth, screenHeight);

        // Tạo nền
        background = new Background(screenWidth, screenHeight);
        getChildren().add(background);

        // Tạo Ván trượt
        paddle = new Paddle(screenWidth, screenHeight);
        getChildren().add(paddle);

        // Tạo Quả bóng
        double ballStartX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2);
        double ballStartY = paddle.getLayoutY() - 15;
        ball = new Ball(screenWidth, screenHeight, ballStartX, ballStartY);
        getChildren().add(ball);

        // Tạo HUD (Giao diện)
        createHUD();

        // Khởi tạo Vòng lặp Game
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
            }
        };
        gameLoop.start();
    }

    /**
     * Tạo tất cả văn bản và icon trên màn hình
     */
    private void createHUD() {
        // Icon Trái tim
        Image heartImage = new Image(getClass().getResourceAsStream("/images/heart.png"));
        heartIcon = new ImageView(heartImage);
        heartIcon.setFitWidth(25); // Đặt kích thước
        heartIcon.setFitHeight(25);
        heartIcon.setLayoutX(10); // Đặt vị trí (góc trên bên trái)
        heartIcon.setLayoutY(10);

        // Chữ hiển thị số mạng
        livesText = new Text("x " + lives);
        livesText.setFont(Font.font("Arial", 20));
        livesText.setFill(Color.WHITE);
        livesText.setLayoutX(40); // Ngay bên cạnh trái tim
        livesText.setLayoutY(30);

        // Chữ hiển thị điểm (sẽ dùng sau) và để ở góc trên bên phải
        scoreText = new Text("Score: 0");
        scoreText.setFont(Font.font("Arial", 20));
        scoreText.setFill(Color.WHITE);
        scoreText.setLayoutX(screenWidth - 120);
        scoreText.setLayoutY(30);

        // Thông báo giữa màn hình
        messageText = new Text("Press SPACE to Start");
        messageText.setFont(Font.font("Arial", 30));
        messageText.setFill(Color.WHITE);
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setLayoutY(screenHeight / 2);

        // Thêm tất cả vào Pane
        getChildren().addAll(heartIcon, livesText, scoreText, messageText);
    }

    /**
     * Hàm này được gọi liên tục (khoảng 60 lần/giây)
     * để cập nhật toàn bộ logic game.
     */
    private void updateGame() {
        // Cập nhật ván trượt (luôn di chuyển được)
        paddle.update(goLeft, goRight);

        if (gameRunning) {
            // Game ĐANG CHẠY: Bóng di chuyển tự do
            ball.update();
            checkCollisions(); // Kiểm tra va chạm
        } else {
            // Game CHƯA CHẠY: Bóng đi theo ván trượt
            double newBallX = paddle.getLayoutX()
                    + (paddle.getBoundsInLocal().getWidth() / 2)
                    - ball.getRadius();

            double newBallY = paddle.getLayoutY()
                    - 15 // Khoảng cách
                    - (ball.getRadius() * 2); // Trừ chiều cao quả bóng

            ball.setLayoutX(newBallX);
            ball.setLayoutY(newBallY);
        }
    }

    /**
     * (CẬP NHẬT) Kiểm tra các va chạm
     */
    private void checkCollisions() {
        // Va chạm Bóng với Ván trượt
        if (ball.getBoundsInParent().intersects(paddle.getBoundsInParent())) {
            if (ball.getLayoutY() + (ball.getRadius() * 2) - ball.getDy() <= paddle.getLayoutY()) {
                ball.reverseDy(); // Đảo hướng dọc
            }
        }

        // Va chạm Bóng với Đáy
        if (ball.getLayoutY() >= screenHeight) {
            loseLife(); // Gọi hàm mất mạng
        }
    }

    /**
     * Xử lý logic khi mất một mạng
     */
    private void loseLife() {
        lives--; // Trừ 1 mạng
        livesText.setText("x " + lives); // Cập nhật chữ
        gameRunning = false; // Dừng game

        if (lives <= 0) {
            // Game Over
            messageText.setText("GAME OVER! Press R to Restart");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
        } else {
            // Còn mạng, tiếp tục
            messageText.setText("Press SPACE to Continue");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);

            // Reset vị trí paddle và ball
            paddle.reset();
            ball.reset();
        }
    }

    /**
     * Hàm reset game (khi nhấn R)
     */
    private void resetGame() {
        // Đặt lại mạng
        lives = 3;
        livesText.setText("x " + lives);

        // Đặt lại điểm (sẽ dùng sau)
        // score = 0;
        // scoreText.setText("Score: 0");

        paddle.reset();
        ball.reset();

        messageText.setText("Press SPACE to Start");
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setVisible(true);

        gameRunning = false;
    }

    /**
     * Xử lý khi phím được nhấn
     */
    public void handleKeyPressed(KeyCode code) {
        // Di chuyển
        if (code == KeyCode.A || code == KeyCode.LEFT) {
            goLeft = true;
        } else if (code == KeyCode.D || code == KeyCode.RIGHT) {
            goRight = true;
        }

        // Bắt đầu game / Tiếp tục
        if (code == KeyCode.SPACE) {
            // (CẬP NHẬT) Chỉ chạy nếu game chưa chạy VÀ còn mạng
            if (!gameRunning && lives > 0) {
                gameRunning = true;
                messageText.setVisible(false); // Ẩn thông báo
            }
        }

        // Chơi lại
        if (code == KeyCode.R) {
            if (!gameRunning) {
                resetGame();
            }
        }
    }

    /**
     * Xử lý khi phím được thả ra
     */
    public void handleKeyReleased(KeyCode code) {
        if (code == KeyCode.A || code == KeyCode.LEFT) {
            goLeft = false;
        } else if (code == KeyCode.D || code == KeyCode.RIGHT) {
            goRight = false;
        }
    }
}