package org.example.arkanoid_oop;

public abstract class MovableObject extends GameObject {
    protected double dx;
    protected double dy;

    public void move(double fps) {
        x += dx / fps;
        y += dy / fps;
    }

    public abstract void checkCollision();
}
