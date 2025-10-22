import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Lớp GamePane là màn chơi chính, chứa tất cả các đối tượng game.
 * Nó kế thừa Pane.
 */
public class GamePane extends Pane {

    private double screenWidth;
    private double screenHeight;

    // --- Các đối tượng game ---
    private Background background;
    private Paddle paddle;
    private Ball ball;

    // --- Trạng thái game ---
    private boolean goLeft = false;
    private boolean goRight = false;
    private boolean gameRunning = false;

    // --- Giao diện (HUD) ---
    private Text messageText; // Thông báo "Press SPACE to Start"

    // Vòng lặp game
    private AnimationTimer gameLoop;

    public GamePane(double width, double height) {
        this.screenWidth = width;
        this.screenHeight = height;

        setPrefSize(screenWidth, screenHeight);

        // --- 1. Tạo Nền ---
        background = new Background(screenWidth, screenHeight);
        getChildren().add(background); // Thêm Nền (ở lớp dưới cùng)

        // --- 2. Tạo Ván trượt ---
        paddle = new Paddle(screenWidth, screenHeight);
        getChildren().add(paddle); // Thêm Ván trượt (ở lớp trên)

        // --- 3. Tạo Quả bóng ---
        // Đặt bóng ngay trên giữa ván trượt
        double ballStartX = paddle.getLayoutX() + (paddle.getBoundsInLocal().getWidth() / 2);
        double ballStartY = paddle.getLayoutY() - 15; // Cách ván trượt 15px
        ball = new Ball(screenWidth, screenHeight, ballStartX, ballStartY);
        getChildren().add(ball); // Thêm Bóng vào Pane

        // --- 4. Tạo Thông báo ---
        messageText = new Text("Press SPACE to Start");
        messageText.setFont(Font.font("Arial", 30));
        messageText.setFill(Color.WHITE);
        // Canh giữa
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setLayoutY(screenHeight / 2);
        getChildren().add(messageText);

        // --- 5. Khởi tạo Vòng lặp Game ---
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
            }
        };
        gameLoop.start();
    }

    /**
     * Hàm này được gọi liên tục (khoảng 60 lần/giây)
     * để cập nhật toàn bộ logic game.
     */
    private void updateGame() {
        // Cập nhật ván trượt (luôn di chuyển được)
        paddle.update(goLeft, goRight);

        if (gameRunning) {
            // (A) Game ĐANG CHẠY: Bóng di chuyển tự do
            ball.update();
            checkCollisions(); // Kiểm tra va chạm
        } else {
            // (B) Game CHƯA CHẠY: Bóng đi theo ván trượt
            // Tính vị trí X mới cho bóng (luôn ở giữa ván trượt)
            double newBallX = paddle.getLayoutX()
                    + (paddle.getBoundsInLocal().getWidth() / 2)
                    - ball.getRadius();

            // Tính vị trí Y mới cho bóng (luôn ở ngay trên ván trượt)
            double newBallY = paddle.getLayoutY()
                    - 15 // Khoảng cách
                    - (ball.getRadius() * 2); // Trừ chiều cao quả bóng

            // Cập nhật vị trí bóng
            ball.setLayoutX(newBallX);
            ball.setLayoutY(newBallY);
        }
    }

    /**
     * Kiểm tra các va chạm
     */
    private void checkCollisions() {
        // --- Va chạm Bóng với Ván trượt ---
        if (ball.getBoundsInParent().intersects(paddle.getBoundsInParent())) {

            if (ball.getLayoutY() + (ball.getRadius() * 2) - ball.getDy() <= paddle.getLayoutY()) {
                ball.reverseDy(); // Đảo hướng dọc
            }
        }

        // --- Va chạm Bóng với Đáy (Game Over đơn giản) ---
        if (ball.getLayoutY() >= screenHeight) {
            gameRunning = false; // Dừng game

            // Hiện thông báo thua
            messageText.setText("GAME OVER! Press R to Restart");
            messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
            messageText.setVisible(true);
        }
    }

    /**
     * Hàm reset game
     */
    private void resetGame() {
        paddle.reset();
        // ball.reset() sẽ tự động đặt bóng về vị trí ban đầu
        // và logic "bám theo" trong updateGame() sẽ lo phần còn lại
        ball.reset();

        messageText.setText("Press SPACE to Start");
        messageText.setLayoutX((screenWidth - messageText.getLayoutBounds().getWidth()) / 2);
        messageText.setVisible(true);

        gameRunning = false;

        // (Sẽ thêm logic tạo lại gạch ở bước sau)
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

        // Bắt đầu game
        if (code == KeyCode.SPACE) {
            if (!gameRunning) {
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