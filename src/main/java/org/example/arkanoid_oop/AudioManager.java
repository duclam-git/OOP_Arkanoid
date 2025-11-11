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

    public GameSettings getSettings() {
        return settings;
    }

    private static final String SETTINGS_FILE = "gamesettings.ser"; // Tên file settings của bạn

    // AudioClip pool cho SFX
    private final Map<String, List<AudioClip>> clipPools = new HashMap<>();
    private final Random rand = new Random();

    // MediaPlayer cho nhạc nền
    private MediaPlayer menuMusic;
    private MediaPlayer gameMusic;

    private AudioManager() {
        loadSettings(); // Tải settings trước khi preloadSounds
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
        // "menu_music" và "game_bg" sẽ được quản lý bởi MediaPlayer riêng, không cần pool
    }

    /** Load một loại âm thanh và tạo pool (Fix: Dùng getResourceAsStream) */
    private void loadClip(String name, String path, int poolSize) {
        List<AudioClip> list = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            try {
                // SỬA: Kiểm tra null trước khi gọi toExternalForm
                // HOẶC TỐT HƠN LÀ TRUYỀN THẲNG Stream cho AudioClip nếu nó hỗ trợ (nhưng AudioClip chỉ nhận URL)
                // Vậy nên, chúng ta phải kiểm tra URL.
                java.net.URL resourceUrl = getClass().getResource(path);
                if (resourceUrl != null) {
                    AudioClip clip = new AudioClip(resourceUrl.toExternalForm());
                    clip.setVolume(0.8);
                    list.add(clip);
                } else {
                    System.err.println("❌ Không tìm thấy resource âm thanh: " + path);
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tải AudioClip " + path + ": " + e.getMessage());
            }
        }
        clipPools.put(name, list);
    }

    /** Phát âm thanh SFX theo tên */
    public void play(String name) {
        if (!settings.isSoundEnabled()) return;
        List<AudioClip> list = clipPools.get(name);
        if (list == null || list.isEmpty()) {
            System.err.println("Cảnh báo: Không có âm thanh cho '" + name + "' hoặc pool rỗng.");
            return;
        }

        AudioClip clip = list.get(rand.nextInt(list.size()));
        clip.play();
    }

    // ============================
    // NHẠC NỀN MENU
    // ============================
    public void playMenuMusic() {
        if (!settings.isSoundEnabled()) return; // Kiểm tra enable trước
        stopMenuMusic(); // dừng nhạc cũ nếu có
        try {
            java.net.URL resourceUrl = getClass().getResource("/sounds/menu_music.wav");
            if (resourceUrl != null) {
                Media media = new Media(resourceUrl.toExternalForm());
                menuMusic = new MediaPlayer(media);
                menuMusic.setCycleCount(MediaPlayer.INDEFINITE); // loop vô hạn
                menuMusic.setVolume(0.5);
                menuMusic.play();
            } else {
                System.err.println("❌ Không tìm thấy nhạc nền menu: /sounds/menu_music.wav");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi phát nhạc nền menu: " + e.getMessage());
        }
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
        if (!settings.isSoundEnabled()) return; // Kiểm tra enable trước
        stopGameMusic();
        try {
            java.net.URL resourceUrl = getClass().getResource("/sounds/game_bg.wav");
            if (resourceUrl != null) {
                Media media = new Media(resourceUrl.toExternalForm());
                gameMusic = new MediaPlayer(media);
                gameMusic.setCycleCount(MediaPlayer.INDEFINITE);
                gameMusic.setVolume(0.5);
                gameMusic.play();
            } else {
                System.err.println("❌ Không tìm thấy nhạc nền game: /sounds/game_bg.wav");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi phát nhạc nền game: " + e.getMessage());
        }
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
        } else {
            // Nếu bật âm thanh lại, bạn có thể muốn tự động chơi lại nhạc nền tương ứng
            // Hiện tại chưa có logic để biết nhạc nào đang chạy, có thể bổ sung sau
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

            // THÊM LOGIC KIỂM TRA PADDLE SKIN PATH SAU KHI TẢI
            if (settings.getPaddleSkinPath() == null || !isValidSkinPath(settings.getPaddleSkinPath())) {
                settings.setPaddleSkinPath("/images/paddle.png"); // Đặt lại mặc định hợp lệ
                saveSettings(); // Lưu lại cài đặt đã sửa
            }

            // THÊM LOGIC KIỂM TRA BALL SKIN PATH
            if (settings.getBallSkinPath() == null || !isValidSkinPath(settings.getBallSkinPath())) {
                settings.setBallSkinPath("/images/ball.png"); // Mặc định Ball
                saveSettings();
            }

            // Nếu tải thành công, cần áp dụng trạng thái âm thanh ngay lập tức
            if (!settings.isSoundEnabled()) {
                stopMenuMusic(); // Đảm bảo đã dừng nhạc nếu âm thanh bị tắt
                stopGameMusic();
            }
        } catch (FileNotFoundException e) {
            System.out.println("File settings không tồn tại. Tạo settings mới.");
            settings = new GameSettings();
            saveSettings(); // Lưu cài đặt mới
        } catch (Exception e) {
            System.err.println("Lỗi khi tải settings: " + e.getMessage() + ". Tạo settings mới.");
            settings = new GameSettings();
            saveSettings(); // Lưu cài đặt mới
        }
    }

    // Phương thức kiểm tra đường dẫn skin có hợp lệ không
    private boolean isValidSkinPath(String path) {
        try {
            return getClass().getResource(path) != null; // Chỉ cần kiểm tra resource có tồn tại không
        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra đường dẫn skin " + path + ": " + e.getMessage());
            return false;
        }
    }

    /** Lưu settings hiện tại ra file. */
    void saveSettings() {
        try (
                FileOutputStream fileOut = new FileOutputStream(SETTINGS_FILE);
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)
        ) {
            objectOut.writeObject(settings);
            // System.out.println("Lưu settings thành công."); // Có thể bỏ comment này khi debug xong
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu settings: " + e.getMessage());
        }
    }
}