package org.example.arkanoid_oop;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;
import java.util.*;

/**
 * AudioManager - quản lý toàn bộ âm thanh trong game
 * -------------------------------------------------
 * 1. Âm thanh ngắn (SFX) dùng AudioClip pool → phát trùng nhau, mượt
 * 2. Nhạc nền (menu / game) dùng MediaPlayer → loop liên tục
 */
public class AudioManager {

    private static AudioManager instance;

    private GameSettings settings;
    private static final String SETTINGS_FILE = "gamesettings.ser";

    // AudioClip pool cho SFX
    private final Map<String, List<AudioClip>> clipPools = new HashMap<>();
    private final Random rand = new Random();

    // MediaPlayer cho nhạc nền
    private MediaPlayer menuMusic;
    private MediaPlayer gameMusic;

    private AudioManager() {
        loadSettings();
        preloadSounds();
    }

    /** Singleton */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /** Load toàn bộ âm thanh vào pool */
    private void preloadSounds() {
        loadClip("hit", "/sounds/hit.wav", 5);
        loadClip("brick", "/sounds/brick_break.wav", 5);
        loadClip("laser", "/sounds/laser.wav", 4);
        loadClip("powerup", "/sounds/powerup.wav", 3);
        loadClip("explosion", "/sounds/explosion.wav", 3);
        loadClip("game_over", "/sounds/game_over.wav", 1);
        loadClip("level_complete", "/sounds/level_complete.wav", 1);
        loadClip("powerup_spawn", "/sounds/powerup_spawn.wav", 2);
        loadClip("lose", "/sounds/lose.wav", 2);
        loadClip("tele", "/sounds/teleport.wav", 2);
        loadClip("click", "/sounds/click.wav", 2);
        loadClip("menu_music", "/sounds/menu_music.wav", 1); // vẫn dùng pool cho click nhạc ngắn, nhưng nhạc loop dùng MediaPlayer
        loadClip("game_bg", "/sounds/game_bg.wav", 1);
    }

    /** Load một loại âm thanh và tạo pool */
    private void loadClip(String name, String path, int poolSize) {
        List<AudioClip> list = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            try {
                AudioClip clip = new AudioClip(getClass().getResource(path).toExternalForm());
                clip.setVolume(0.8);
                list.add(clip);
            } catch (Exception e) {
                System.err.println("❌ Không tải được âm thanh: " + path);
            }
        }
        clipPools.put(name, list);
    }

    /** Phát âm thanh SFX theo tên */
    public void play(String name) {
        if (!settings.isSoundEnabled()) return;
        List<AudioClip> list = clipPools.get(name);
        if (list == null || list.isEmpty()) return;

        AudioClip clip = list.get(rand.nextInt(list.size()));
        clip.play();
    }

    // ============================
    // NHẠC NỀN MENU
    // ============================
    public void playMenuMusic() {
        stopMenuMusic(); // dừng nhạc cũ nếu có
        Media media = new Media(getClass().getResource("/sounds/menu_music.wav").toExternalForm());
        menuMusic = new MediaPlayer(media);
        menuMusic.setCycleCount(MediaPlayer.INDEFINITE); // loop vô hạn
        menuMusic.setVolume(0.5);
        menuMusic.play();
    }

    public void stopMenuMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();
            menuMusic = null;
        }
    }

    // ============================
    // NHẠC NỀN GAME
    // ============================
    public void playGameMusic() {
        stopGameMusic();
        Media media = new Media(getClass().getResource("/sounds/game_bg.wav").toExternalForm());
        gameMusic = new MediaPlayer(media);
        gameMusic.setCycleCount(MediaPlayer.INDEFINITE);
        gameMusic.setVolume(0.5);
        gameMusic.play();
    }

    public void stopGameMusic() {
        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.dispose();
            gameMusic = null;
        }
    }
    public void setSoundEnabled(boolean enabled) {
        settings.setSoundEnabled(enabled);
        saveSettings();
        if (!enabled) {
            stopMenuMusic();
            stopGameMusic();
        }
    }

    public boolean isSoundEnabled() {
        return settings.isSoundEnabled();
    }

    // ============================
    // LOGIC LOAD/SAVE SETTINGS
    // ============================

    /** Tải settings từ file. Nếu thất bại, dùng settings mặc định. */
    private void loadSettings() {
        try (
                FileInputStream fileIn = new FileInputStream(SETTINGS_FILE);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn)
        ) {
            settings = (GameSettings) objectIn.readObject();
            // Nếu tải thành công, cần áp dụng trạng thái âm thanh ngay lập tức
            if (!settings.isSoundEnabled()) {
                stopMenuMusic();
                stopGameMusic();
            }
            System.out.println("Tải settings thành công.");
        } catch (FileNotFoundException e) {
            System.out.println("File settings không tồn tại.");
            settings = new GameSettings();
        } catch (Exception e) {
            System.err.println("Lỗi khi tải settings: " + e.getMessage());
            settings = new GameSettings();
        }
    }

    /** Lưu settings hiện tại ra file. */
    private void saveSettings() {
        try (
                FileOutputStream fileOut = new FileOutputStream(SETTINGS_FILE);
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)
        ) {
            objectOut.writeObject(settings);
            System.out.println("Lưu settings thành công.");
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu settings: " + e.getMessage());
        }
    }
}

