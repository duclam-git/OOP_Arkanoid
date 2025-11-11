package org.example.arkanoid_oop.Brick;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Normal_brickTest {

    // Khởi tạo JavaFX Platform
    @BeforeAll
    static void initJfxRuntime() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    private Normal_brick normalBrick;

    @BeforeEach
    void setUp() {
        normalBrick = new Normal_brick(100, 100);
    }

    @Test
    void testInitialState() {
        assertFalse(normalBrick.isDestroyed(), "Gạch thường không nên bị phá hủy ban đầu.");
        assertEquals(10, normalBrick.getScoreValue(), "Giá trị điểm phải là 10.");
    }

    @Test
    void testOnHit_ShouldBeDestroyedImmediately() {
        // Lần chạm 1
        boolean wasDestroyed = normalBrick.onHit();

        assertTrue(wasDestroyed, "Lần chạm 1: onHit() phải trả về true.");
        assertTrue(normalBrick.isDestroyed(), "Lần chạm 1: isDestroyed() phải là true.");
    }
}