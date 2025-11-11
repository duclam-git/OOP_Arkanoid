package org.example.arkanoid_oop.Menu;

import javafx.stage.Stage;
import org.example.arkanoid_oop.Manager.AudioManager;

public abstract class Menu {
    protected static final int SCREEN_WIDTH = 800;
    protected static final int SCREEN_HEIGHT = 600;

    protected Stage stage;
    protected AudioManager audio;

    public Menu(Stage stage) {
        this.stage = stage;
        this.audio = AudioManager.getInstance();
    }

    public abstract void show();
}
