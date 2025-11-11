package org.example.arkanoid_oop.Brick;

import org.example.arkanoid_oop.model.Brick.Impervious_brick;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Impervious_brickTest {

    // Khởi tạo JavaFX Platform
    @BeforeAll
    static void initJfxRuntime() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    private Impervious_brick imperviousBrick;

    @BeforeEach
    void setUp() {
        imperviousBrick = new Impervious_brick(100, 100);
    }

    @Test
    void testInitialState() {
        assertFalse(imperviousBrick.isDestroyed(), "Gạch không nên bị phá hủy ban đầu.");
        assertEquals(0, imperviousBrick.getScoreValue(), "Giá trị điểm phải là 0.");
    }

    @Test
    void testOnHit_ShouldNeverBeDestroyed() {
        // Chạm lần 1
        boolean wasDestroyed1 = imperviousBrick.onHit();

        assertFalse(wasDestroyed1, "Lần chạm 1: onHit() phải trả về false.");
        assertFalse(imperviousBrick.isDestroyed(), "Lần chạm 1: isDestroyed() phải là false.");

        // Chạm lần 2
        boolean wasDestroyed2 = imperviousBrick.onHit();

        assertFalse(wasDestroyed2, "Lần chạm 2: onHit() phải trả về false.");
        assertFalse(imperviousBrick.isDestroyed(), "Lần chạm 2: isDestroyed() phải là false.");
    }
}