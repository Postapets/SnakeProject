package com.example.snakeproject.object;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.snakeproject.Game;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Snake {

    // Principal Bitmap
    private Bitmap bm;

    // Bitmaps de Cabeza, Cuerpo y Cola
    private Bitmap bmHeadDown, bmHeadLeft, bmHeadRight, bmHeadUp;
    private Bitmap bmBodyVertical, bmBodyHorizontal;
    private Bitmap bmBodyBottomLeft, bmBodyBottomRight, bmBodyTopLeft, bmBodyTopRight;
    private Bitmap bmTailDown, bmTailRight, bmTailLeft, bmTailUp;

    // Cuerpo de la Snake como lista de PartSnake
    private ArrayList<PartSnake> body = new ArrayList<>();

    // Cantidad de elementos del cuerpo
    private int length;

    // Booleans para el control del movimiento
    private boolean moveUp, moveLeft, moveRight, moveDown;

    private int id, score;

    public Snake(Bitmap bm, int x, int y, int length) {
        this.bm = bm;
        this.length = length;

        this.setBitmaps(bm);

        // Al inicializar se setea que la snake comience hacia la derecha
        moveRight = true;

        // Partes del cuerpo en dirección de la derecha
        body.add(new PartSnake(bmHeadRight, x, y, 1));
        for (int i = 1; i < length - 1; i++) {
            this.body.add(new PartSnake(bmBodyHorizontal, this.body.get(i - 1).getX() - Game.size, y, 13));
        }
        body.add(new PartSnake(bmTailRight, body.get(length - 2).getX() - Game.size, body.get(length - 2).getY(), 5));
    }

    public Snake(Bitmap bm, List<Map> parts) {
        this.bm = bm;
        this.length = parts.size();
        this.setBitmaps(bm);
        Bitmap b = null;
        for (Map el : parts) {
            switch (((Long) el.get("bitmap")).intValue()) {
                case 1:
                    b = bmHeadRight;
                    break;
                case 2:
                    b = bmHeadDown;
                    break;
                case 3:
                    b = bmHeadLeft;
                    break;
                case 4:
                    b = bmHeadUp;
                    break;
                case 5:
                    b = bmTailRight;
                    break;
                case 6:
                    b = bmTailDown;
                    break;
                case 7:
                    b = bmTailLeft;
                    break;
                case 8:
                    b = bmTailUp;
                    break;
                case 9:
                    b = bmBodyBottomRight;
                    break;
                case 10:
                    b = bmBodyBottomLeft;
                    break;
                case 11:
                    b = bmBodyTopLeft;
                    break;
                case 12:
                    b = bmBodyTopRight;
                    break;
                case 13:
                    b = bmBodyHorizontal;
                    break;
                case 14:
                    b = bmBodyVertical;
                    break;
            }

            this.body.add(new PartSnake(b, ((Long) el.get("x")).intValue(), ((Long) el.get("y")).intValue(), ((Long) el.get("bitmap")).intValue()));
        }
    }

    public void setBitmaps(Bitmap bm) {
        int w = bm.getWidth() / 5;
        int h = bm.getHeight() / 4;
        // Elementos de la primera fila de los sprites
        bmBodyBottomRight = Bitmap.createBitmap(bm, 0, 0, w, h);
        bmBodyHorizontal = Bitmap.createBitmap(bm, w, 0, w, h);
        bmBodyBottomLeft = Bitmap.createBitmap(bm, 2 * w, 0, w, h);
        bmHeadUp = Bitmap.createBitmap(bm, 3 * w, 0, w, h);
        bmHeadRight = Bitmap.createBitmap(bm, 4 * w, 0, w, h);

        // Elementos de la segunda fila de los sprites
        bmBodyTopRight = Bitmap.createBitmap(bm, 0, h, w, h);
        bmBodyVertical = Bitmap.createBitmap(bm, 2 * w, h, w, h);
        bmHeadLeft = Bitmap.createBitmap(bm, 3 * w, h, w, h);
        bmHeadDown = Bitmap.createBitmap(bm, 4 * w, h, w, h);

        // Elementos de la tercera fila de los sprites
        bmBodyTopLeft = Bitmap.createBitmap(bm, 2 * w, 2 * h, w, h);
        bmTailUp = Bitmap.createBitmap(bm, 3 * w, 2 * h, w, h);
        bmTailRight = Bitmap.createBitmap(bm, 4 * w, 2 * h, w, h);

        // Elementos de la cuarta fila de los sprites
        bmTailLeft = Bitmap.createBitmap(bm, 3 * w, 3 * h, w, h);
        bmTailDown = Bitmap.createBitmap(bm, 4 * w, 3 * h, w, h);
    }

    public void addPart() {
        PartSnake tail = this.body.get(length - 1);
        this.length += 1;

        // Si la dirección era derecha
        if (tail.getBm() == bmTailRight) {
            this.body.add(new PartSnake(bmTailRight, tail.getX() - Game.size, tail.getY(), 5));

            // Si la dirección era abajo
        } else if (tail.getBm() == bmTailDown) {
            this.body.add(new PartSnake(bmTailDown, tail.getX(), tail.getY() - Game.size, 6));

            // Si la dirección era izquierda
        } else if (tail.getBm() == bmTailLeft) {
            this.body.add(new PartSnake(bmTailLeft, tail.getX() + Game.size, tail.getY(), 7));

            // Si la dirección era arriba
        } else if (tail.getBm() == bmTailUp) {
            this.body.add(new PartSnake(bmTailUp, tail.getX(), tail.getY() + Game.size, 8));
        }

    }

    public void updateMovement() {

        // Desplazamiento del cuerpo
        for (int i = length - 1; i > 0; i--) {
            body.get(i).setX(body.get(i - 1).getX());
            body.get(i).setY(body.get(i - 1).getY());
        }

        // Movimientos de la Cabeza
        if (moveRight) {
            body.get(0).setX(body.get(0).getX() + Game.size);
            body.get(0).setBm(bmHeadRight);
            body.get(0).setId(1);
        } else if (moveDown) {
            body.get(0).setY(body.get(0).getY() + Game.size);
            body.get(0).setBm(bmHeadDown);
            body.get(0).setId(2);
        } else if (moveLeft) {
            body.get(0).setX(body.get(0).getX() - Game.size);
            body.get(0).setBm(bmHeadLeft);
            body.get(0).setId(3);
        } else if (moveUp) {
            body.get(0).setY(body.get(0).getY() - Game.size);
            body.get(0).setBm(bmHeadUp);
            body.get(0).setId(4);
        }

        // Movimientos del Cuerpo
        for (int i = 1; i < length - 1; i++) {

            // Validación intersección izquierda
            boolean inter_left_next = body.get(i).getrLeft().intersect(body.get(i + 1).getrBody());
            boolean inter_left_prev = body.get(i).getrLeft().intersect(body.get(i - 1).getrBody());

            // Validación intersección derecha
            boolean inter_right_next = body.get(i).getrRight().intersect(body.get(i + 1).getrBody());
            boolean inter_right_prev = body.get(i).getrRight().intersect(body.get(i - 1).getrBody());

            // Validación intersección arriba
            boolean inter_top_next = body.get(i).getrTop().intersect(body.get(i + 1).getrBody());
            boolean inter_top_prev = body.get(i).getrTop().intersect(body.get(i - 1).getrBody());

            // Validación intersección abajo
            boolean inter_bottom_next = body.get(i).getrBottom().intersect(body.get(i + 1).getrBody());
            boolean inter_bottom_prev = body.get(i).getrBottom().intersect(body.get(i - 1).getrBody());

            // Dibujar sprites de cambio de dirección

            // Si la parte del cuerpo intersecta otra parte siguiente a la derecha y una parte previa abajo
            // o si la parte del cuerpo intersecta otra parte siguiente abajo y una parte previa a la derecha
            if (inter_right_next && inter_bottom_prev || inter_bottom_next && inter_right_prev) {
                body.get(i).setBm(bmBodyBottomRight);
                body.get(i).setId(9);

                // Si la parte del cuerpo intersecta otra parte siguiente a la izquierda y una parte previa abajo
                // o si la parte del cuerpo intersecta otra parte siguiente abajo y una parte previa a la izquierda
            } else if (inter_left_next && inter_bottom_prev || inter_bottom_next && inter_left_prev) {
                body.get(i).setBm(bmBodyBottomLeft);
                body.get(i).setId(10);

                // Si la parte del cuerpo intersecta otra parte siguiente a la izquierda y una parte previa arriba
                // o si la parte del cuerpo intersecta otra parte siguiente arriba y una parte previa a la izquierda
            } else if (inter_left_next && inter_top_prev || inter_top_next && inter_left_prev) {
                body.get(i).setBm(bmBodyTopLeft);
                body.get(i).setId(11);

                // Si la parte del cuerpo intersecta otra parte siguiente a la derecha y una parte previa arriba
                // o si la parte del cuerpo intersecta otra parte siguiente arriba y una parte previa a la derecha
            } else if (inter_right_next && inter_top_prev || inter_top_next && inter_right_prev) {
                body.get(i).setBm(bmBodyTopRight);
                body.get(i).setId(12);

                // Si la parte del cuerpo intersecta otra parte siguiente a la derecha y una parte previa a la izquierda
                // o si la parte del cuerpo intersecta otra parte siguiente a la izquierda y una parte previa a la derecha
            } else if (inter_right_next && inter_left_prev || inter_left_next && inter_right_prev) {
                body.get(i).setBm(bmBodyHorizontal);
                body.get(i).setId(13);

                // Si la parte del cuerpo intersecta otra parte siguiente arriba y una parte previa a la abajo
                // o si la parte del cuerpo intersecta otra parte siguiente abajo y una parte previa a la arriba
            } else if (inter_top_next && inter_bottom_prev || inter_bottom_next && inter_top_prev) {
                body.get(i).setBm(bmBodyVertical);
                body.get(i).setId(14);
            } else {
                // Si el movimiento es horizontal
                if (moveRight || moveLeft) {
                    body.get(i).setBm(bmBodyHorizontal);
                    body.get(i).setId(13);

                    // Si el movimiento es vertical
                } else if (moveUp || moveDown) {
                    body.get(i).setBm(bmBodyVertical);
                    body.get(i).setId(14);
                }
            }
        }

        Rect beforeTail = body.get(length - 2).getrBody();

        // Movimientos de la Cola
        // Si la cola intersecta con la anterior parte a la derecha
        if (body.get(length - 1).getrRight().intersect(beforeTail)) {
            body.get(length - 1).setBm(bmTailRight);
            body.get(length - 1).setId(5);

            // Si la cola intersecta con la anterior parte abajo
        } else if (body.get(length - 1).getrBottom().intersect(beforeTail)) {
            body.get(length - 1).setBm(bmTailDown);
            body.get(length - 1).setId(6);

            // Si la cola intersecta con la anterior parte a la izquierda
        } else if (body.get(length - 1).getrLeft().intersect(beforeTail)) {
            body.get(length - 1).setBm(bmTailLeft);
            body.get(length - 1).setId(7);

            // Si la cola intersecta con la anterior parte arriba
        } else if (body.get(length - 1).getrTop().intersect(beforeTail)) {
            body.get(length - 1).setBm(bmTailUp);
            body.get(length - 1).setId(8);
        }
    }

    //Dibujar la serpiente en el Canvas
    public void drawSnake(Canvas canvas) {
        for (int i = length - 1; i >= 0; i--) {
            canvas.drawBitmap(body.get(i).getBm(), body.get(i).getX(), body.get(i).getY(), null);
        }
    }

    //Set Movimientos en false
    public void movements() {
        this.moveRight = false;
        this.moveDown = false;
        this.moveLeft = false;
        this.moveUp = false;
    }

    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        this.bm = bm;
    }

    public Bitmap getBmHeadDown() {
        return bmHeadDown;
    }

    public void setBmHeadDown(Bitmap bmHeadDown) {
        this.bmHeadDown = bmHeadDown;
    }

    public Bitmap getBmHeadLeft() {
        return bmHeadLeft;
    }

    public void setBmHeadLeft(Bitmap bmHeadLeft) {
        this.bmHeadLeft = bmHeadLeft;
    }

    public Bitmap getBmHeadRight() {
        return bmHeadRight;
    }

    public void setBmHeadRight(Bitmap bmHeadRight) {
        this.bmHeadRight = bmHeadRight;
    }

    public Bitmap getBmHeadUp() {
        return bmHeadUp;
    }

    public void setBmHeadUp(Bitmap bmHeadUp) {
        this.bmHeadUp = bmHeadUp;
    }

    public Bitmap getBmBodyVertical() {
        return bmBodyVertical;
    }

    public void setBmBodyVertical(Bitmap bmBodyVertical) {
        this.bmBodyVertical = bmBodyVertical;
    }

    public Bitmap getBmBodyHorizontal() {
        return bmBodyHorizontal;
    }

    public void setBmBodyHorizontal(Bitmap bmBodyHorizontal) {
        this.bmBodyHorizontal = bmBodyHorizontal;
    }

    public Bitmap getBmBodyBottomLeft() {
        return bmBodyBottomLeft;
    }

    public void setBmBodyBottomLeft(Bitmap bmBodyBottomLeft) {
        this.bmBodyBottomLeft = bmBodyBottomLeft;
    }

    public Bitmap getBmBodyBottomRight() {
        return bmBodyBottomRight;
    }

    public void setBmBodyBottomRight(Bitmap bmBodyBottomRight) {
        this.bmBodyBottomRight = bmBodyBottomRight;
    }

    public Bitmap getBmBodyTopLeft() {
        return bmBodyTopLeft;
    }

    public void setBmBodyTopLeft(Bitmap bmBodyTopLeft) {
        this.bmBodyTopLeft = bmBodyTopLeft;
    }

    public Bitmap getBmBodyTopRight() {
        return bmBodyTopRight;
    }

    public void setBmBodyTopRight(Bitmap bmBodyTopRight) {
        this.bmBodyTopRight = bmBodyTopRight;
    }

    public Bitmap getBmTailUp() {
        return bmTailUp;
    }

    public void setBmTailUp(Bitmap bmTailUp) {
        this.bmTailUp = bmTailUp;
    }

    public Bitmap getBmTailDown() {
        return bmTailDown;
    }

    public void setBmTailDown(Bitmap bmTailDown) {
        this.bmTailDown = bmTailDown;
    }

    public Bitmap getBmTailRight() {
        return bmTailRight;
    }

    public void setBmTailRight(Bitmap bmTailRight) {
        this.bmTailRight = bmTailRight;
    }

    public Bitmap getBmTailLeft() {
        return bmTailLeft;
    }

    public void setBmTailLeft(Bitmap bmTailLeft) {
        this.bmTailLeft = bmTailLeft;
    }

    public ArrayList<PartSnake> getBody() {
        return body;
    }

    public List<Map> getBodyMap() {
        List<Map> r = new ArrayList<Map>();
        for (PartSnake part : body) {
            Map<String, Object> element = new HashMap<>();
            element.put("x", part.getX());
            element.put("y", part.getY());
            element.put("bitmap", part.getId());
            r.add(element);
        }
        return r;
    }

    public void setBody(ArrayList<PartSnake> body) {
        this.body = body;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isMoveLeft() {
        return moveLeft;
    }

    public void setMoveLeft(boolean moveLeft) {
        this.movements();
        this.moveLeft = moveLeft;
    }

    public boolean isMoveRight() {
        return moveRight;
    }

    public void setMoveRight(boolean moveRight) {
        this.movements();
        this.moveRight = moveRight;
    }

    public boolean isMoveUp() {
        return moveUp;
    }

    public void setMoveUp(boolean moveUp) {
        this.movements();
        this.moveUp = moveUp;
    }

    public boolean isMoveDown() {
        return moveDown;
    }

    public void setMoveDown(boolean moveDown) {
        this.movements();
        this.moveDown = moveDown;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


    public String direction() {
        if (moveDown) {
            return "D";
        }
        if (moveRight) {
            return "R";
        }
        if (moveLeft) {
            return "L";
        }
        if (moveUp) {
            return "U";
        }
        return "N";
    }

}
