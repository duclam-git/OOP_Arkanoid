package org.example.arkanoid_oop.model.Brick;

import javafx.scene.image.ImageView;

public abstract class Brick {
    public static final int BRICK_WIDTH = 60;
    public static final int BRICK_HEIGHT = 20;

    protected double x;
    protected double y;
    protected ImageView view;
    protected int scoreValue;
    protected boolean destroyed = false;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public ImageView getView() {
        return view;
    }

    public void setView(ImageView view) {
        this.view = view;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(int scoreValue) {
        this.scoreValue = scoreValue;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public Brick(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public abstract boolean onHit();
}
