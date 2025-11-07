package org.example.arkanoid_oop;

import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;

/**
 * AudioManager: Quản lý âm thanh cho game Arkanoid.
 * - Dùng AudioClip cho sound effect ngắn → mượt, không delay.
 * - Có cooldown cho các hiệu ứng phát nhiều lần liên tiếp.
 */
public class AudioManager {

    private static AudioManager instance;

    // AudioClip đã load sẵn
    private final Map<String, AudioClip> clips = new HashMap<>();

    // Cooldown cho các sound effect (ms)
    private final Map<String, Long> lastPlayTime = new HashMap<>();
    private final long DEFAULT_COOLDOWN_MS = 50; // 50ms giữa các lần phát cùng 1 sound

    private boolean soundEnabled = true;

    private AudioManager() {
        loadSounds();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Load tất cả file âm thanh từ resources/sounds/
     * File nên là .wav ngắn cho sound effect.
     */
    private void loadSounds() {
        addClip("hit", "/sounds/hit.wav");
        addClip("brick", "/sounds/brick_break.wav");
        addClip("powerup", "/sounds/powerup.wav");
        addClip("laser", "/sounds/laser.wav");
        addClip("game_over", "/sounds/game_over.wav");
        addClip("explosion", "/sounds/explosion.wav");
        addClip("powerup_spawn", "/sounds/powerup_spawn.wav");
        addClip("level_complete", "/sounds/level_complete.wav");
    }

    private void addClip(String name, String path) {
        try {
            AudioClip clip = new AudioClip(getClass().getResource(path).toExternalForm());
            clips.put(name, clip);
        } catch (Exception e) {
            System.err.println("Không load được âm thanh: " + path);
        }
    }

    /**
     * Phát âm thanh với cooldown mặc định.
     */
    public void play(String name) {
        play(name, DEFAULT_COOLDOWN_MS);
    }

    /**
     * Phát âm thanh với cooldown tùy chỉnh.
     * @param name Tên âm thanh đã load
     * @param cooldownMs Thời gian tối thiểu giữa các lần play liên tiếp
     */
    public void play(String name, long cooldownMs) {
        if (!soundEnabled) return;

        AudioClip clip = clips.get(name);
        if (clip == null) return;

        long now = System.currentTimeMillis();
        long lastTime = lastPlayTime.getOrDefault(name, 0L);

        if (now - lastTime >= cooldownMs) {
            clip.play();
            lastPlayTime.put(name, now);
        }
    }

    /** Bật / tắt âm thanh */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}
