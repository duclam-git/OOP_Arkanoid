package org.example.arkanoid_oop.model.Brick;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ExplosiveBrick extends Brick {

    public ExplosiveBrick(double x, double y) {
        super(x, y);

        Image img = new Image(getClass().getResourceAsStream("/images/explosive_brick.png"));
        this.view = new ImageView(img);

        this.view.setFitWidth(BRICK_WIDTH);
        this.view.setFitHeight(BRICK_HEIGHT);
        this.view.setLayoutX(x);
        this.view.setLayoutY(y);

        this.scoreValue = 10;
    }

    @Override
    public boolean onHit() {
        setDestroyed(true);
        return true;
    }

    public double getCenterX() {
        return view.getLayoutX() + BRICK_WIDTH / 2;
    }

    public double getCenterY() {
        return view.getLayoutY() + BRICK_HEIGHT / 2;
    }
}