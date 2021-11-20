package com.example.snakeproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.snakeproject.object.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // TAG LOGCAT
    public static final String TAG = "firestore";

    // STORAGE VARIABLES
    public static final String GAME_SETTINGS = "games";
    public static final String BEST_SCORE = "best";

    public static MainActivity INSTANCE;
    public static Dialog dialogScore, dialogRooms, dialogGame;
    public static TextView txtScore1, txtScore2, txtDialogScore, txtDialogBest, hostScore, invitedScore;
    public static ImageView imgview1, imgview2;
    public Game game;

    // LIST ITEMS
    public ListView list;
    public List<Rlist> listRooms = new ArrayList();

    // NECCESARY VARIABLES
    public Activity context = this;
    public String roomCode;
    public boolean host = false;
    public DocumentSnapshot gameInfo;
    public Rlist match;
    public int numRooms = 0;
    public int numPlayers = 0;
    public AdapterRoomList adapter;

    // STOP SNAPSHOTS
    public static ListenerRegistration registration, room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SET SCREEN SIZES
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        setContentView(R.layout.activity_main);

        INSTANCE = this;

        game = findViewById(R.id.game);

        // Revisar dimensionamiento de la pantalla para setear medidas del tablero de juego
        game.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                MainActivity.this.game.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Game.SCREEN_WIDTH = MainActivity.this.game.getMeasuredWidth();
                Game.SCREEN_HEIGHT = MainActivity.this.game.getMeasuredHeight();
                MainActivity.this.game.init();
            }
        });

        txtScore1 = findViewById(R.id.txtScore);
        txtScore2 = findViewById(R.id.txtBest);
        imgview1 = findViewById(R.id.icon1);
        imgview2 = findViewById(R.id.icon2);
        initialDialog();
    }

    // Desplegar ventana dialog
    public void initialDialog() {
        int bestScore = 0;
        // Tomar en cuenta anterior partida
        SharedPreferences sp = this.getSharedPreferences(GAME_SETTINGS, Context.MODE_PRIVATE);
        if (sp != null) {
            bestScore = sp.getInt(BEST_SCORE, 0);
        }

        MainActivity.txtScore2.setText(bestScore + "");

        dialogScore = new Dialog(this);
        dialogScore.setContentView(R.layout.dialog_start);
        txtDialogScore = dialogScore.findViewById(R.id.txt_dialog_score);
        txtDialogBest = dialogScore.findViewById(R.id.txt_dialog_best_score);
        txtDialogBest.setText(bestScore + "");
        dialogScore.setCanceledOnTouchOutside(false);

        Button rl_start = dialogScore.findViewById(R.id.rl_start);
        // Cuando pulse el botón de start resetear el game.
        rl_start.setOnClickListener(v -> {
            game.setHost(true);
            game.init();
            dialogScore.dismiss();
        });

        Button rl_multiplayer = dialogScore.findViewById(R.id.rl_multiplayer);

        // Cuando pulse el botón de start multiplayer mostrar ventana de lista de salas.
        rl_multiplayer.setOnClickListener(v -> {
            dialogRooms();
        });

        dialogScore.show();
    }

    // Desplegar ventana rooms
    public void dialogRooms() {
        host = false;

        dialogRooms = new Dialog(this);
        dialogRooms.setContentView(R.layout.activity_rooms);

        list = dialogRooms.findViewById(R.id.list_rooms);
        listRooms = new ArrayList();
        adapter = new AdapterRoomList(context, listRooms);
        list.setAdapter(adapter);

        // Snapshot listener collections rooms
        registration = FirebaseFirestore.getInstance().collection("rooms")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        // Si existe un error

                        if (e != null) {
                            Log.e(TAG, "onEvent: ", e);
                            return;
                        }

                        listRooms.clear();

                        // Si existen colecciones
                        if (queryDocumentSnapshots != null) {
                            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                            numRooms = snapshotList.size();
                            for (DocumentSnapshot snapshot : snapshotList) {
                                FirebaseFirestore.getInstance().collection("rooms").
                                        document(snapshot.getId()).collection("players").
                                        get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            // Añadir nuevo elemento al ListView
                                            if (findItem(snapshot.getId()) == null) {
                                                match = new Rlist();
                                                match.setCod(snapshot.getId());
                                                match.setState((String) snapshot.getData().get("state"));
                                                match.setPlayers(((Long) snapshot.getData().get("numPlayers")).intValue());
                                                listRooms.add(match);
                                            }
                                            if (numRooms == 1) {
                                                adapter.setItem(listRooms);
                                                adapter.notifyDataSetChanged();
                                                itemTouch();
                                            } else {
                                                numRooms = numRooms - 1;
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                            }
                        } else {
                            Log.e(TAG, "onEvent: query snapshot was null");
                        }
                    }
                });

        Button rl_new_room = dialogRooms.findViewById(R.id.rl_new_room);

        // Escucha evento onclick de botón "Nueva Partida"
        rl_new_room.setOnClickListener(v -> {
            registration.remove();  // Eliminar snapshot listener
            String id = FirebaseFirestore.getInstance().collection("rooms").document().getId();
            Map<String, Object> fields = new HashMap<>();
            fields.put("state", "En espera");
            fields.put("numPlayers", 0);

            // Insertar nuevo documento de sala
            FirebaseFirestore.getInstance().collection("rooms").document(id)
                    .set(fields)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            host = true;
                            dialogGame(id); // Mostrar diálogo de juego
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        });

        dialogRooms.show();
        Window window = dialogRooms.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    }

    public void itemTouch() {
        // Identificar item pulsado
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Si existen dos jugadores
                if (listRooms.get(position).getPlayers() == 2) {
                    // Mostrar alerta de capacidad
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Capacidad de jugadores alcanzada").setTitle("Alert Dialog");
                    builder.setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                // Si existe un sólo jugador
                if (listRooms.get(position).getPlayers() == 1) {
                    host = false;
                    registration.remove();
                    dialogGame(listRooms.get(position).getCod());
                }
            }
        });
    }

    // Desplegar ventana de juego
    public void dialogGame(String code) {
        dialogRooms.dismiss();
        dialogScore.dismiss();
        roomCode = code; // Guardar código de la partida

        dialogGame = new Dialog(this);
        dialogGame.setCanceledOnTouchOutside(false);
        dialogGame.setContentView(R.layout.dialog_room);

        hostScore = dialogGame.findViewById(R.id.hostScore);
        invitedScore = dialogGame.findViewById(R.id.invitedScore);

        TextView txtState = dialogGame.findViewById(R.id.txt_rol);
        txtState.setText(host ? "Host" : "Inv");

        FirebaseFirestore.getInstance().collection("rooms").document(roomCode).
                collection("players").document(host ? "1" : "2").set(new Player(0, 0, 0, "N"))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference docRef = FirebaseFirestore.getInstance().collection("rooms").document(roomCode);
                        docRef.update("state", host ? "En espera" : "Partida lista", "numPlayers", host ? 1 : 2);
                        numPlayers = 1;
                        setSnapshotRoom(roomCode);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

        dialogGame.show();

        // Comenzar partida multijugador
        Button start = dialogGame.findViewById(R.id.room_start);

        if (host) {
            start.setVisibility(View.VISIBLE);
            start.setOnClickListener(v -> {
                if (numPlayers == 1) {
                    // Mostrar alerta de capacidad
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Falta un jugador para comenzar").setTitle("Alert Dialog");
                    builder.setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                if (numPlayers == 2) {
                    System.out.println("Está jugando");
                    DocumentReference docRef = FirebaseFirestore.getInstance().collection("rooms").document(roomCode);
                    docRef.update("state", "Jugando");
                }

            });
        } else {
            start.setVisibility(View.GONE);
        }

        // Eliminar partida si se sale
        Button exit = dialogGame.findViewById(R.id.room_exit);
        exit.setOnClickListener(v -> {
            alertDialogDelete();
        });

        dialogGame.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == 1) {
                    alertDialogDelete();
                }
                return true;
            }
        });

    }

    public void setSnapshotRoom(String code) {
        // Snapshopt listener de cambios de la sala
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("rooms").document(code);
        room = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {


                System.out.println("Actualización en snapshotRoom");
                // Si existen errores
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (snapshot.getData() == null) {

                    // Mostrar alerta de capacidad
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Partida finalizada").setTitle("Alert Dialog");
                    builder.setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            room.remove();
                            dialogGame.dismiss();
                            dialogScore.show();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                // Si existe un cambio
                if (snapshot != null && snapshot.exists()) {

                    FirebaseFirestore.getInstance().collection("rooms").document(roomCode).collection("players")
                            .document("1").update("score",0,"d", "N");
                    FirebaseFirestore.getInstance().collection("rooms").document(roomCode).collection("players")
                            .document("2").update("score",0,"d", "N");

                    if (snapshot.getData().get("state").equals("Jugando")) {
                        FirebaseFirestore.getInstance().collection("rooms").document(roomCode)
                                .collection("players").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> players) {
                                dialogScore.dismiss();
                                dialogGame.dismiss();
                                room.remove();
                                game.setInfoGame(players.getResult(), roomCode, host);
                                game.init();
                                game.reset(1);
                            }
                        });
                    }else{
                        // Cambio nombre de la sala
                        TextView txtCode = dialogGame.findViewById(R.id.txt_room_play_code);
                        txtCode.setText(snapshot.getId().substring(0, 4));

                        // Cambio estado de la sala

                        TextView txtState = dialogGame.findViewById(R.id.txt_state);
                        txtState.setText((String) snapshot.getData().get("state"));

                        numPlayers = ((Long) snapshot.getData().get("numPlayers")).intValue();
                    }

                    Log.d(TAG, "Current data: " + snapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }


    public void alertDialogDelete() {
        // Mostrar alerta de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(host ? "¿Deseas finalizar la sala?" : "¿Deseas salir de la sala?").setTitle("Alert Dialog");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if (host) {
                    deleteRoom();
                } else {
                    exitRoom();
                }
                dialogGame.dismiss();
                game.setInfoGame(null, null, false);
                MainActivity.this.game.init();
                game.reset(0);
                dialogScore.show();
                room.remove();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void exitRoom() {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("rooms").document(roomCode);
        docRef.update("state", "En espera", "numPlayers", 1);

        FirebaseFirestore.getInstance().collection("rooms").document(roomCode)
                .collection("players").document("2").delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    public void deleteRoom() {

        FirebaseFirestore.getInstance().collection("rooms").document(roomCode)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseFirestore.getInstance().collection("rooms").document(roomCode).collection("players").document("1").delete();
                        FirebaseFirestore.getInstance().collection("rooms").document(roomCode).collection("players").document("2").delete();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    private Rlist findItem(String code) {
        for(Rlist item : listRooms) {
            if(item.getCod().equals(code)) {
                return item;
            }
        }
        return null;
    }

}