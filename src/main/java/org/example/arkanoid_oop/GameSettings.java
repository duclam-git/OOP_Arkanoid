package org.example.arkanoid_oop;

import java.io.Serializable;

public class GameSettings implements Serializable {
    // Serial version UID: Dùng để kiểm tra tương thích khi deserialize
    private static final long serialVersionUID = 1L;

    private boolean soundEnabled = true;

    public GameSettings() {
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
}