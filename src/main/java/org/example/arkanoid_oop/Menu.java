package org.example.arkanoid_oop;

import javafx.stage.Stage;

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
