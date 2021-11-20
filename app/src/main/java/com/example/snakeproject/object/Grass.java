package com.example.snakeproject.object;

import android.graphics.Bitmap;
import android.graphics.Point;

//Celda del Tablero
public class Grass extends Point {

    private Bitmap bm;
    private int w, h;

    public Grass(Bitmap bm, int x, int y, int w, int h) {
        this.bm = bm;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
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

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }
}
