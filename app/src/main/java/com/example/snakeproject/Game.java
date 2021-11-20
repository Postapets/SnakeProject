package com.example.snakeproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;
import android.graphics.Canvas;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.snakeproject.object.Apple;
import com.example.snakeproject.object.Grass;
import com.example.snakeproject.object.PartSnake;
import com.example.snakeproject.object.Player;
import com.example.snakeproject.object.Snake;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Game extends View {

    public static int DELAY = 200;

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    public static int size;

    // Cantidad de celdas visibles y disponibles en el Game
    private int w = 12, h = 17;

    private Bitmap bmGrassLight, bmGrassDark, bmSprites, bmOpSprites, bmApple;
    private Grass[][] grass = new Grass[h][w];

    private Snake snake, opponentSnake = null;
    private Apple apple;

    private Handler handler;
    private Runnable runnable;

    private boolean move = false;
    private float mx, my;

    public static boolean isPlaying = false;
    public static int initialMove = 0;
    public static int score = 0;
    public static int bestScore = 0;

    private Context context;

    private QuerySnapshot infoGame;
    private int modGame;
    private boolean host = true;
    private String roomCode;

    public static ListenerRegistration opponent;

    public Game(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.infoGame = null;
        loadBest(context);
    }

    public void init() {
        Game.size = 90 * SCREEN_WIDTH / 1080;

        // Estilo de Celda con fondo Claro
        bmGrassLight = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bmGrassLight.eraseColor(Color.parseColor("#B5835E"));

        // Estilo de Celda con fondo Oscuro
        bmGrassDark = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bmGrassDark.eraseColor(Color.parseColor("#A87854"));

        // Sprites dispuestos para el juego.
        bmSprites = BitmapFactory.decodeResource(this.getResources(), host ? R.drawable.sprites1 : R.drawable.sprites2);
        bmSprites = Bitmap.createScaledBitmap(bmSprites, 5 * size, 4 * size, true);

        bmOpSprites = BitmapFactory.decodeResource(this.getResources(), host ? R.drawable.sprites2_tr : R.drawable.sprites_tr);
        bmOpSprites = Bitmap.createScaledBitmap(bmOpSprites, 5 * size, 4 * size, true);

        bmApple = BitmapFactory.decodeResource(this.getResources(), R.drawable.manzana);
        bmApple = Bitmap.createScaledBitmap(bmApple, size, size, true);

        reset(0);

        //Inicialización del hilo
        handler = new Handler();
        runnable = () -> invalidate();
    }

    public void setIcons() {
        if (modGame == 0) {
            MainActivity.imgview1.setImageResource(R.drawable.manzana);
            MainActivity.imgview2.setImageResource(R.drawable.insignia);
        }
        if (modGame == 1) {
            MainActivity.imgview1.setImageResource(host ? R.drawable.snake : R.drawable.snake2);
            MainActivity.imgview2.setImageResource(host ? R.drawable.snake2 : R.drawable.snake);
        }
    }

    public void reset(int game) {
        modGame = game;
        opponentSnake = null;
        snake = null;
        System.gc();

        // Adaptación de los estilos en la matriz
        boolean color = true;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                grass[i][j] = new Grass((color) ? bmGrassLight : bmGrassDark, j * size + SCREEN_WIDTH / 2 - (w / 2) * size, i * size + 50 * SCREEN_HEIGHT / 1920, size, size);
                color = !color;
            }
            color = !color;
        }

        snake = new Snake(bmSprites, grass[9][6].getX(), grass[9][6].getY(), 4);

        if (modGame == 0) {
            //Creación del Snake con posición inicial en el centro del juego

            score = 0;
            MainActivity.txtScore1.setText(score + "");
        }

        if (modGame == 1 && infoGame != null) {
            MainActivity.txtScore1.setText("0");
            MainActivity.txtScore2.setText("0");

            //Creación del Snake con posición inicial en el centro del juego
            for (DocumentSnapshot player : infoGame) {
                if (host) {
                    if (player.getId().equals("1")) {
                        snake.setScore(((Long) player.get("score")).intValue());
                    } else {
                        setSnapshotOpponent("2");
                    }
                } else {
                    if (player.getId().equals("2")) {
                        snake.setScore(((Long) player.get("score")).intValue());
                    } else {
                        setSnapshotOpponent("1");
                    }
                }
            }
            initialMove = 0;
        }

        //Creación del Apple con posición inicial random
        Point random = randomApple();
        apple = new Apple(bmApple, grass[random.y][random.x].getX(), grass[random.y][random.x].getY());

        setIcons();
    }

    private void setSnapshotOpponent(String code) {
        // Snapshopt listener de cambios de la sala

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("rooms").document(roomCode).collection("players").document(code);
        opponent = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                // Si existen errores

                if (e != null) {
                    Log.w(MainActivity.TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists() && snapshot.getData().get("d").equals("F")) {
                    gameOver();
                }

                // Si existe un cambio
                if (snapshot != null && snapshot.exists() && snapshot.getData().get("body") != null) {

                    int x = ((Long) snapshot.getData().get("x")).intValue();
                    int y = ((Long) snapshot.getData().get("y")).intValue();
                    opponentSnake = new Snake(bmOpSprites, (List<Map>) snapshot.getData().get("body"));
                    MainActivity.txtScore2.setText(String.valueOf(snapshot.getData().get("score")));

                    Log.d(MainActivity.TAG, "Current data: " + snapshot.getData());
                } else {
                    Log.d(MainActivity.TAG, "Current data: null");
                }
            }
        });
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (modGame == 1 && initialMove == 1) {
            DELAY = 200;
            initialMove = 2;
        }

        if (size == 0) {
            return;
        }

        canvas.drawColor(context.getResources().getColor(R.color.green_bg));

        // Dibujado del Background
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                Grass current = grass[i][j];
                canvas.drawBitmap(current.getBm(), current.getX(), current.getY(), null);
            }
        }

        PartSnake head = snake.getBody().get(0);

        if (isPlaying) {

            snake.updateMovement();
            head = snake.getBody().get(0);

            // Validar si la cabeza sale del tablero de juego
            if (head.getX() < grass[0][0].getX()
                    || head.getY() < grass[0][0].getY()
                    || head.getY() + size > grass[h - 1][w - 1].getY() + size
                    || head.getX() + size > grass[h - 1][w - 1].getX() + size) {
                if (modGame == 1) {
                    resetMultiplayer();
                }
                gameOver();
            }

            //Validar si la cabeza toca alguna parte del cuerpo
            for (int i = 1; i < snake.getBody().size(); i++) {
                if (head.getrBody().intersect(snake.getBody().get(i).getrBody())) {
                    if (modGame == 1) {
                        resetMultiplayer();
                    }
                    gameOver();
                }
            }
        }


        if (opponentSnake != null) {
            opponentSnake.drawSnake(canvas);
        }
        // Dibujado del Snake y del Apple
        snake.drawSnake(canvas);
        apple.draw(canvas);

        // Validar que la serpiente se alimenta
        if (head.getrBody().intersect(apple.getR())) {

            if (modGame == 1 && isPlaying) {
                snake.setScore(snake.getScore() + 1);

                DocumentReference docRef = FirebaseFirestore.getInstance().collection("rooms").document(roomCode).collection("players").document(host ? "1" : "2");
                docRef.update("d", snake.direction(),
                        "score", snake.getScore(),
                        "x", Math.max((snake.getBody().get(0).getX() / Game.size), 0),
                        "y", Math.max(((snake.getBody().get(0).getY() - 27) / Game.size), 0),
                        "body", snake.getBodyMap()
                );
            }


            // Setear una nueva posición a una nueva manzana
            Point random = randomApple();
            apple.reset(grass[random.y][random.x].getX(), grass[random.y][random.x].getY());

            //Añadir una nueva parte
            snake.addPart();
            score++;

            if (modGame == 0) {
                MainActivity.txtScore1.setText(score + "");
                if (score > bestScore) {
                    bestScore = score;
                    saveBest();
                    MainActivity.txtScore2.setText(bestScore + "");
                }
            }
            if (modGame == 1) {
                MainActivity.txtScore1.setText(String.valueOf(snake.getScore()));
            }

        }

        if (modGame == 1 && initialMove == 0) {
            DELAY = 3000;
            initialMove = 1;
            this.snake.setMoveRight(true);
            isPlaying = true;
        }


        //Delay del movimiento de la Snake
        handler.postDelayed(runnable, DELAY);
    }

    // Guardar la información de la mejor puntuación de partida obtenida
    private void loadBest(@NonNull Context context) {
        SharedPreferences sp = context.getSharedPreferences(MainActivity.GAME_SETTINGS, Context.MODE_PRIVATE);
        if (sp != null) {
            bestScore = sp.getInt(MainActivity.BEST_SCORE, 0);
        }
    }

    // Storage interno de la información de puntuación de partida
    private void saveBest() {
        SharedPreferences sp = context.getSharedPreferences(MainActivity.GAME_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(MainActivity.BEST_SCORE, bestScore);
        editor.apply();
    }

    // Finalizar juego y mostrar Dialog
    private void gameOver() {
        isPlaying = false;
        if (modGame == 0) {
            MainActivity.txtDialogBest.setText(bestScore + "");
            MainActivity.txtDialogScore.setText(score + "");
            MainActivity.dialogScore.show();
        } else {
            opponent.remove();
            FirebaseFirestore.getInstance().collection("rooms").document(roomCode)
                    .update("state", "Partida lista");
            MainActivity.hostScore.setText(host ? MainActivity.txtScore1.getText() : MainActivity.txtScore2.getText());
            MainActivity.invitedScore.setText(host ? MainActivity.txtScore2.getText() : MainActivity.txtScore1.getText());
            MainActivity.INSTANCE.setSnapshotRoom(roomCode);
            MainActivity.dialogGame.show();
        }
    }

    private void resetMultiplayer() {
        FirebaseFirestore.getInstance().collection("rooms").document(roomCode).collection("players")
                .document(host ? "1" : "2").update("d", "F");
    }

    // Generar posición aleatoria de la manzana
    private Point randomApple() {
        try {
            Point point = new Point();
            Random r = new Random();
            point.y = r.nextInt(h - 1);
            point.x = r.nextInt(w - 1);

            Grass current = grass[point.y][point.x];
            // Algoritmo para verificar que la nueva posición de la manzana no tenga una parte del cuerpo de la serpiente
            Rect rect = new Rect(current.getX(), current.getY(), current.getX() + size, current.getY() + size);
            boolean check = true;

            while (check) {
                check = false;
                for (int i = 0; i < snake.getBody().size(); i++) {
                    if (rect.intersect(snake.getBody().get(i).getrBody())) {
                        System.out.println("intersectó");
                        check = true;
                        point.y = r.nextInt(h - 1);
                        point.x = r.nextInt(w - 1);
                        System.out.println("ciclo:" + point);
                        current = grass[point.y][point.x];
                        rect = new Rect(current.getX(), current.getY(), current.getX() + size, current.getY() + size);
                    } else {
                        check = false;
                    }
                }
            }
            return point;
        } catch (Exception e) {
            e.printStackTrace();
            return new Point(0, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int a = event.getActionMasked();
        switch (a) {
            // Cuando realiza un touch
            case MotionEvent.ACTION_MOVE: {
                if (move == false) {
                    mx = event.getX();
                    my = event.getY();
                    move = true;
                } else {
                    // Si el movimiento es hacia la izquierda
                    if (mx - event.getX() > 100 && !snake.isMoveRight()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMoveLeft(true);
                        isPlaying = true;
                        // Si el movimiento es hacia la derecha
                    } else if (event.getX() - mx > 100 && !snake.isMoveLeft()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMoveRight(true);
                        isPlaying = true;
                        // Si el movimiento es hacia abajo
                    } else if (event.getY() - my > 100 && !snake.isMoveUp()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMoveDown(true);
                        isPlaying = true;
                        // Si el movimiento es hacia arriba
                    } else if (my - event.getY() > 100 && !snake.isMoveDown()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMoveUp(true);
                        isPlaying = true;
                    }
                }
                break;
            }
            // Cuando suelta el touch
            case MotionEvent.ACTION_UP: {
                mx = 0;
                my = 0;
                move = false;
                break;
            }
        }
        return true;
    }

    public QuerySnapshot getInfoGame() {
        return infoGame;
    }

    public void setInfoGame(QuerySnapshot infoGame, String roomCode, boolean host) {
        this.infoGame = infoGame;
        this.host = host;
        this.roomCode = roomCode;
    }

    public boolean isHost() {
        return host;
    }

    public void setHost(boolean host) {
        this.host = host;
    }
}
