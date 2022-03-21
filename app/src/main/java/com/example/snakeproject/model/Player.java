package com.example.snakeproject.model;

import android.graphics.Point;

public class Player extends Point{
    private int score;
    private String d;

    public Player() {
    }

    public Player(int score, int x, int y, String d) {
        this.x = x;
        this.y = y;
        this.score = score;
        this.d = d;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
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

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }
}
