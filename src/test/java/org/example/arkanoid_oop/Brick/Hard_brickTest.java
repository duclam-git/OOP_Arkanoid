package org.example.arkanoid_oop.Brick;

import org.example.arkanoid_oop.model.Brick.Hard_brick;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// *LƯU Ý QUAN TRỌNG VỀ JAVA-FX*:
// Các lớp khởi tạo JavaFX components (như Hard_brick) cần JavaFX Runtime.
// Để test này chạy, bạn có thể cần thêm dependency TestFX (hoặc tương đương)
// HOẶC gọi javafx.application.Platform.startup(() -> {});
// Trong ví dụ này, chúng ta chỉ tập trung vào logic onHit()

public class Hard_brickTest {

    private Hard_brick hardBrick;

    // Chuẩn bị môi trường JavaFX (cần thiết vì Hard_brick sử dụng Image/ImageView)
    // Đây là cách đơn giản nhất để khởi tạo JavaFX Platform cho testing.
    @BeforeAll
    static void initJfxRuntime() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Runtime đã khởi tạo, không làm gì
        }
    }

    @BeforeEach
    void setUp() {
        // Khởi tạo một viên gạch mới trước mỗi test
        hardBrick = new Hard_brick(100, 100);
    }

    @Test
    void testInitialState() {
        // 1. Kiểm tra trạng thái ban đầu
        assertFalse(hardBrick.isDestroyed(), "Gạch không nên bị phá hủy ban đầu.");
        assertEquals(25, hardBrick.getScoreValue(), "Giá trị điểm phải là 25.");
    }

    @Test
    void testOnHit_FirstTime() {
        // 2. Chạm lần 1
        boolean wasDestroyed = hardBrick.onHit();

        assertFalse(wasDestroyed, "Lần chạm 1: onHit() phải trả về false (chưa phá hủy).");
        assertFalse(hardBrick.isDestroyed(), "Lần chạm 1: isDestroyed() phải là false.");

        // Kiểm tra logic thay đổi hình ảnh (Không kiểm tra trực tiếp đối tượng Image,
        // nhưng xác nhận rằng phương thức onHit đã được gọi)
    }

    @Test
    void testOnHit_SecondTime() {
        // 3. Chạm lần 2 (cần 2 lần)
        hardBrick.onHit(); // Lần 1
        boolean wasDestroyed = hardBrick.onHit(); // Lần 2

        assertTrue(wasDestroyed, "Lần chạm 2: onHit() phải trả về true (đã phá hủy).");
        assertTrue(hardBrick.isDestroyed(), "Lần chạm 2: isDestroyed() phải là true.");
    }

    @Test
    void testOnHit_SubsequentHits() {
        // 4. Chạm thêm sau khi đã phá hủy
        hardBrick.onHit();
        hardBrick.onHit(); // Đã phá hủy

        boolean wasDestroyed = hardBrick.onHit(); // Lần 3

        assertTrue(wasDestroyed, "Lần chạm sau khi phá hủy vẫn nên trả về true.");
        assertTrue(hardBrick.isDestroyed(), "isDestroyed() vẫn phải là true.");
    }
}