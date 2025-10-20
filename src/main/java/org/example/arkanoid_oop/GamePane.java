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
    private Background background;

    // Vòng lặp game
    private AnimationTimer gameLoop;

    public GamePane(double width, double height) {
        this.screenWidth = width;
        this.screenHeight = height;

        setPrefSize(screenWidth, screenHeight);

        // Khởi tạo đối tượng Background
        background = new Background(screenWidth, screenHeight);
        getChildren().add(background);

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
     * Hàm này được gọi liên tục (khoảng 60 lần/giây)
     * để cập nhật toàn bộ logic game.
     */
    private void updateGame() {
    }

    /**
     * Xử lý khi phím được nhấn
     */
    public void handleKeyPressed(KeyCode code) {
    }

    /**
     * Xử lý khi phím được thả ra
     */
    public void handleKeyReleased(KeyCode code) {
    }
}