import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;

    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 20;
    private static final int PADDLE_SPEED = 10;

    // Biến để theo dõi trạng thái phím
    private boolean goLeft = false;
    private boolean goRight = false;

    @Override
    public void start(Stage primaryStage) {

        Pane root = new Pane();
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Đặt màu nền cho màn chơi
        scene.setFill(Color.BLACK);

        // Tạo thanh trượt
        Rectangle paddle = new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT, Color.WHITE);

        // Vị trí ban đầu của thanh trượt
        paddle.setLayoutX((SCREEN_WIDTH - PADDLE_WIDTH) / 2.0);
        paddle.setLayoutY(SCREEN_HEIGHT - PADDLE_HEIGHT - 10);

        // Thêm thanh trượt vào màn chơi
        root.getChildren().add(paddle);

        // Xử lí các phím nhận vào
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.A) {
                goLeft = true;
            } else if (event.getCode() == KeyCode.D) {
                goRight = true;
            }
        });

        // Khi phím được thả ra
        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.A) {
                goLeft = false;
            } else if (event.getCode() == KeyCode.D) {
                goRight = false;
            }
        });

        // Vòng lặp chính của game
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Di chuyển thanh trượt

                // Lấy vị trí x hiện tại
                double x = paddle.getLayoutX();

                // Di chuyển dựa trên biến cờ (flag)
                if (goLeft) {
                    x -= PADDLE_SPEED;
                }
                if (goRight) {
                    x += PADDLE_SPEED;
                }

                // Giới hạn của thanh trượt (để không vị văng ra ngoài màn hình game)
                if (x < 0) {
                    x = 0; // Chạm biên trái
                }
                // Biên phải = chiều rộng màn hình - chiều rộng thanh trượt
                if (x > (SCREEN_WIDTH - PADDLE_WIDTH)) {
                    x = SCREEN_WIDTH - PADDLE_WIDTH;
                }

                // Cập nhật vị trí mới cho thanh trượt
                paddle.setLayoutX(x);

            }
        };

        // Bắt đầu vòng lặp game
        gameLoop.start();

        // Hiển thị cửa sổ
        primaryStage.setTitle("Game Arkanoid (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Không cho phép thay đổi kích thước
        primaryStage.show();
    }

    // Hàm main để khởi chạy ứng dụng JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}