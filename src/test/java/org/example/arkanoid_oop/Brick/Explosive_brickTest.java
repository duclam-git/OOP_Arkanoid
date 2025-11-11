package org.example.arkanoid_oop.Brick;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Explosive_brickTest {

    // Khởi tạo JavaFX Platform
    @BeforeAll
    static void initJfxRuntime() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    private Explosive_brick explosiveBrick;

    @BeforeEach
    void setUp() {
        explosiveBrick = new Explosive_brick(100, 100);
    }

    @Test
    void testInitialState() {
        assertFalse(explosiveBrick.isDestroyed(), "Gạch nổ không nên bị phá hủy ban đầu.");
        assertEquals(10, explosiveBrick.getScoreValue(), "Giá trị điểm phải là 10.");
    }

    @Test
    void testOnHit_ShouldBeDestroyedImmediately() {
        // Lần chạm 1
        boolean wasDestroyed = explosiveBrick.onHit();

        assertTrue(wasDestroyed, "Lần chạm 1: onHit() phải trả về true.");
        assertTrue(explosiveBrick.isDestroyed(), "Lần chạm 1: isDestroyed() phải là true.");

        // *LƯU Ý*: Logic kích hoạt vụ nổ được xử lý trong GamePane và không được test ở đây.
    }

    @Test
    void testGetCenterCoordinates() {
        // BRICK_WIDTH = 60, BRICK_HEIGHT = 20 (từ Brick.java)
        // x = 100, y = 100
        assertEquals(130.0, explosiveBrick.getCenterX(), "Tâm X phải được tính đúng (x + width/2).");
        assertEquals(110.0, explosiveBrick.getCenterY(), "Tâm Y phải được tính đúng (y + height/2).");
    }
}