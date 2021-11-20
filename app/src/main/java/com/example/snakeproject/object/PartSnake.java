package com.example.snakeproject.object;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

import com.example.snakeproject.Game;

public class PartSnake extends Point {
    private Bitmap bm;

    //Creación de rectángulos para lógica de intersección
    private Rect rBody, rTop, rBottom, rRight, rLeft;
    private int id;

    public PartSnake(Bitmap bm, int x, int y, int id) {
        this.bm = bm;
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        this.bm = bm;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    //Crear Rectangulo superior
    public Rect getrTop() {
        return new Rect(this.x, this.y - 10 * Game.SCREEN_HEIGHT / 1920, this.x + Game.size, this.y);
    }

    public void setrTop(Rect rTop) {
        this.rTop = rTop;
    }

    //Crear Rectangulo inferior
    public Rect getrBottom() {
        return new Rect(this.x, this.y + Game.size, this.x + Game.size, this.y + Game.size + 10 * Game.SCREEN_HEIGHT / 1920);
    }

    public void setrBottom(Rect rBottom) {
        this.rBottom = rBottom;
    }

    //Crear Rectangulo a la derecha
    public Rect getrRight() {
        return new Rect(this.x + Game.size, this.y, this.x + Game.size + 10 * Game.SCREEN_WIDTH / 1080, this.y + Game.size);
    }

    public void setrRight(Rect rRight) {
        this.rRight = rRight;
    }

    //Crear Rectangulo a la izquierda
    public Rect getrLeft() {
        return new Rect(this.x - 10 * Game.SCREEN_WIDTH / 1080, this.y, this.x, this.y + Game.size);
    }

    public void setrLeft(Rect rLeft) {
        this.rLeft = rLeft;
    }

    //Crear Rectangulo de la parte del cuerpo
    public Rect getrBody() {
        return new Rect(this.x, this.y, this.x + Game.size, this.y + Game.size);
    }

    public void setrBody(Rect rBody) {
        this.rBody = rBody;
    }
}
