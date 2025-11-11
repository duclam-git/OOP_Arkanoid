// Thay thế toàn bộ nội dung của GameSettings.java bằng:
package org.example.arkanoid_oop;

import java.io.Serializable;

public class GameSettings implements Serializable {

    // NEW: Enum for Game Mode
    public enum GameMode {
        EASY(3, "Easy"), // 3 Mạng
        NORMAL(2, "Normal"), // 2 Mạng
        HARD(1, "Hard"); // 1 Mạng

        private final int initialLives;
        private final String displayName;

        GameMode(int initialLives, String displayName) {
            this.initialLives = initialLives;
            this.displayName = displayName;
        }

        public int getInitialLives() {
            return initialLives;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Serial version UID: Dùng để kiểm tra tương thích khi deserialize
    private static final long serialVersionUID = 1L;

    private boolean soundEnabled = true;
    private String paddleSkinPath = "/images/paddle.png";
    private String ballSkinPath = "/images/ball.png";
    // NEW: Default to EASY mode
    private GameMode gameMode = GameMode.EASY;

    public GameSettings() {
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public String getPaddleSkinPath() {
        return paddleSkinPath;
    }

    public void setPaddleSkinPath(String paddleSkinPath) {
        this.paddleSkinPath = paddleSkinPath;
    }

    public String getBallSkinPath() {
        return ballSkinPath;
    }

    public void setBallSkinPath(String ballSkinPath) {
        this.ballSkinPath = ballSkinPath;
    }

    // NEW: Getter and Setter for GameMode
    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
}