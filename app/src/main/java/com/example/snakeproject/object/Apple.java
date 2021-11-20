package com.example.snakeproject.object;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

import com.example.snakeproject.Game;

public class Apple extends Point {
    private Bitmap bm;
    private Rect r;

    public Apple(Bitmap bm, int x, int y) {
        this.x = x;
        this.y = y;
        this.bm = bm;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bm, x, y, null);
    }

    public void reset(int nx, int ny) {
        this.x = nx;
        this.y = ny;
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

    public Rect getR() {
        return new Rect(this.x, this.y, this.x + Game.size, this.y + Game.size);
    }

    public void setR(Rect r) {
        this.r = r;
    }
}
