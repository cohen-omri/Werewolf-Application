package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Activities.HomeFragment.cardIds;
import static com.example.werewolfclient.Activities.HomeFragment.user;
import static com.example.werewolfclient.Objects.SocketManager.gson;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.werewolfclient.ClientObjects.Message;
import com.example.werewolfclient.ClientObjects.StringMessage;
import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.GameObject;
import com.example.werewolfclient.ServerObjects.LobbyObject;
import com.example.werewolfclient.ServerObjects.ResultsObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Results extends AppCompatActivity implements SocketManager.ServerMessageListener {

    private LinearLayout[] rows;
    private LinearLayout topCards, userRow;
    private HashMap<String, objects.CardType> userRoles;
    private ArrayList<String> players;
    private String votedUsername, lobbyCode;
    private Boolean isWin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_results);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve ResultsObject from arguments
        Intent intent = getIntent();
        ResultsObject results = (ResultsObject) intent.getSerializableExtra("resultsObject");

        userRoles = results.getUserRoles();
        players = results.getPlayers();
        votedUsername = results.getVotedUsername();
        lobbyCode = results.getCode();
        isWin = results.isWin();

        rows = new LinearLayout[]{findViewById(R.id.row1), findViewById(R.id.row2), findViewById(R.id.row3)};
        topCards = findViewById(R.id.topCardsView);
        userRow = findViewById(R.id.userRow);

        players.remove(user.getData().get("name"));
        distributePlayers();

        String text = votedUsername + " was:";
        int votedCardID = cardIds.get(userRoles.get(votedUsername).getType());
        showPopup(text, votedCardID);

        text = votedUsername + " was voted out...";
        showPopup(text, R.drawable.card_back);


        TextView winLose = findViewById(R.id.winLose);
        if (isWin)
            winLose.setText("You won!");
        else
            winLose.setText("You lost...");
    }

    @Override
    public void onMessageReceived(Object obj) {

        Message message = gson.fromJson(obj.toString(), Message.class);
        System.out.println("Results received message with action: " + message.getAction());

        if (message.getAction() == 5) {
            LobbyObject lobby = gson.fromJson(obj.toString(), LobbyObject.class);
            System.out.println("server responded: " + lobby );

            if (lobby.getMsg() == 0) {
                // Navigate to LobbyFragment
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("lobbyObject", lobby);
                startActivity(intent);
                 // Close Results after starting MainActivity
            }
            else {
                if (lobby.getMsg() == 1) {
                    Toast.makeText(this, "This code does not exist", Toast.LENGTH_SHORT).show();
                } else if (lobby.getMsg() == 2) {
                    Toast.makeText(this, "This lobby is already full", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("joinGame", true);
                startActivity(intent);
            }
            finish();
        }
    }

    private void distributePlayers() {
        // Top cards
        for (int i = 0 ; i < 3 ; i++) {
            System.out.println("print top card number " + i + ". user roles: " + userRoles);
            ImageView card = (ImageView)topCards.getChildAt(i);
            card.setImageResource(cardIds.get(userRoles.get(i + "").getType()));
        }

        // Player cards
        // Calculate how many players per row (max 4 per row)
        int totalPlayers = players.size();
        int playersPerRow = 4; // Maximum players per row

        View gridItem = LayoutInflater.from(this).inflate(R.layout.game_card, userRow, false);
        TextView usernameText = gridItem.findViewById(R.id.username_text);
        String username = user.getData().get("name").toString();
        usernameText.setText(username);

        // Get the ImageView and resize it
        ImageView userCardImage = gridItem.findViewById(R.id.card_image);
        userCardImage.setImageResource(cardIds.get(userRoles.get(username).getType())); // set opened card
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) userCardImage.getLayoutParams();
        params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, getResources().getDisplayMetrics());
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 131.5f, getResources().getDisplayMetrics());
        userCardImage.setLayoutParams(params);

        userRow.addView(gridItem);

        // Distribute players across rows
        int playerIndex = 0;
        for (LinearLayout currentRow : rows) {
            currentRow.removeAllViews(); // Clear existing views in each row

            // Determine how many players to place in this row
            int playersInRow = Math.min(totalPlayers - playerIndex, playersPerRow);
            if (playersInRow <= 0)
                break;

            // Add players to the current row
            for (int i = 0; i < playersInRow; i++) {
                // Inflate the grid item layout
                gridItem = LayoutInflater.from(this).inflate(R.layout.game_card, currentRow, false);

                userCardImage = gridItem.findViewById(R.id.card_image);
                userCardImage.setImageResource(cardIds.get(userRoles.get(players.get(playerIndex)).getType()));

                // Set the card username
                usernameText = gridItem.findViewById(R.id.username_text);
                usernameText.setText(players.get(playerIndex));

                // Add the view to the current row
                currentRow.addView(gridItem);
                gridItem.setTag("p " + playerIndex++); // p stands for players
            }
        }
    }

    private void showPopup(String text, int imageResId) {
        Intent intent = new Intent(this, PopupDisplayCard.class);
        intent.putExtra("text", text);
        intent.putExtra("resId", imageResId);
        startActivity(intent);
    }

    public void lobbyClick(View view) {
        new Thread(() -> {
            StringMessage codeMsg = new StringMessage(6, 1, user.getUuid(), lobbyCode);
            SocketManager.sendToServer(codeMsg);
        }).start();
    }


    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onStart() {
        super.onStart();
        SocketManager.setListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("Results onStop called");
    }
}