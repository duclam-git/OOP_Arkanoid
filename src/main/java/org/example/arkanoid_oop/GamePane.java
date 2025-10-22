import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

/**
 * Lớp GamePane là màn chơi chính, chứa tất cả các đối tượng game.
 * Nó kế thừa Pane.
 */
public class GamePane extends Pane {

    private double screenWidth;
    private double screenHeight;

    // --- Các đối tượng game ---
    private Background background;
    private Paddle paddle; // (MỚI) Thêm đối tượng Paddle

    // --- Trạng thái game ---
    private boolean goLeft = false;   // (MỚI) Cờ di chuyển trái
    private boolean goRight = false;  // (MỚI) Cờ di chuyển phải

    // Vòng lặp game
    private AnimationTimer gameLoop;

    public GamePane(double width, double height) {
        this.screenWidth = width;
        this.screenHeight = height;

        setPrefSize(screenWidth, screenHeight);

        // --- 1. Tạo Nền ---
        background = new Background(screenWidth, screenHeight);
        getChildren().add(background); // Thêm Nền (ở lớp dưới cùng)

        // --- 2. Tạo Ván trượt (MỚI) ---
        paddle = new Paddle(screenWidth, screenHeight);
        getChildren().add(paddle); // Thêm Ván trượt (ở lớp trên)

        // --- 3. Khởi tạo Vòng lặp Game ---
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
        // (MỚI) Yêu cầu ván trượt tự cập nhật vị trí
        paddle.update(goLeft, goRight);
    }

    /**
     * (MỚI) Xử lý khi phím được nhấn
     */
    public void handleKeyPressed(KeyCode code) {
        if (code == KeyCode.A || code == KeyCode.LEFT) {
            goLeft = true;
        } else if (code == KeyCode.D || code == KeyCode.RIGHT) {
            goRight = true;
        }
    }

    /**
     * (MỚI) Xử lý khi phím được thả ra
     */
    public void handleKeyReleased(KeyCode code) {
        if (code == KeyCode.A || code == KeyCode.LEFT) {
            goLeft = false;
        } else if (code == KeyCode.D || code == KeyCode.RIGHT) {
            goRight = false;
        }
    }
}