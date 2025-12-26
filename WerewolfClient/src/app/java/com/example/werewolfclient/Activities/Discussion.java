package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Activities.HomeFragment.user;
import static com.example.werewolfclient.Objects.SocketManager.gson;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.werewolfclient.ClientObjects.Message;
import com.example.werewolfclient.ClientObjects.StringMessage;
import com.example.werewolfclient.Objects.ChatMessage;
import com.example.werewolfclient.Objects.MessageAdapter;
import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.GameObject;
import com.example.werewolfclient.ServerObjects.LobbyObject;

import java.util.ArrayList;
import java.util.List;

public class Discussion extends AppCompatActivity implements SocketManager.ServerMessageListener {

    private TextView userListTv, timerText;
    private EditText chatEdt;
    private Button readyButton;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<ChatMessage> messageList;
    private GameObject game;
    private ArrayList<String> players;
    private LinearLayout[] rows;
    private LinearLayout userRow;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_discussion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        chatEdt = findViewById(R.id.chatEdt);
        recyclerView = findViewById(R.id.recyclerView);
        userListTv = findViewById(R.id.userListTv);
        timerText = findViewById(R.id.timerText);
        readyButton = findViewById(R.id.readyButton);
        rows = new LinearLayout[]{findViewById(R.id.row1), findViewById(R.id.row2), findViewById(R.id.row3)};
        userRow = findViewById(R.id.userRow);

        // Retrieve GameObject from arguments
        Intent intent = getIntent();
        game = (GameObject) intent.getSerializableExtra("gameObject");

        // User list setup
        List<String> userList = game.getPlayers();
        String userString = "· " + userList.get(0);
        for (int i = 1; i < userList.size(); i++) {
            userString += "\n· " + userList.get(i);
        }
        userListTv.setText(userString);

        // Set up RecyclerView for chat
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        messageList = new ArrayList<ChatMessage>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);

        players = game.getPlayers();
        players.remove(user.getData().get("name"));
        distributePlayers();

        TextView sendMsgBtn = findViewById(R.id.sendMsgBtn);
        sendMsgBtn.setOnClickListener(this::sendMsg);

        startTimer();
    }

    public void onMessageReceived(Object obj) {
        Message message = gson.fromJson(obj.toString(), Message.class);
        System.out.println("Discussion received message with action: " + message.getAction());

        switch (message.getAction()) {
            case 8: // Remove participant
                StringMessage removeParticipantMsg = gson.fromJson(obj.toString(), StringMessage.class);
                String uToRemove = removeParticipantMsg.getString();
                runOnUiThread(() -> removeParticipant(uToRemove));
                break;

            case 9: // Chat message
                StringMessage sMsg = gson.fromJson(obj.toString(), StringMessage.class);
                ChatMessage chatMsg = gson.fromJson(sMsg.getString(), ChatMessage.class);
                runOnUiThread(() -> displayMsg(chatMsg.getUsername(), chatMsg.getText()));
                break;

            case 16: // start vote
                if (message.getMsg() == -1)
                    runOnUiThread(() -> {
                        Toast.makeText(Discussion.this, "Only the game admin can start the vote", Toast.LENGTH_SHORT).show(); });
                else
                    startVote();
                break;

            default:
                runOnUiThread(() -> {
                    Toast.makeText(Discussion.this, "Unexpected message action: " + message.getAction(), Toast.LENGTH_SHORT).show();
                });
                break;
        }
    }

    private void distributePlayers() {
        // Calculate how many players per row (max 4 per row)
        int totalPlayers = players.size();
        int playersPerRow = 4; // Maximum players per row

        View gridItem = LayoutInflater.from(this).inflate(R.layout.game_card, userRow, false);
        TextView usernameText = gridItem.findViewById(R.id.username_text);
        usernameText.setText(user.getData().get("name").toString());
        gridItem.setClickable(false);

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

                // Set the card username
                usernameText = gridItem.findViewById(R.id.username_text);
                usernameText.setText(players.get(playerIndex));

                // Get the ImageView and resize it
                ImageView userCardImage = gridItem.findViewById(R.id.card_image);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) userCardImage.getLayoutParams();
                params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
                params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 43.83f, getResources().getDisplayMetrics());
                userCardImage.setLayoutParams(params);
                gridItem.setClickable(false);

                // Add the view to the current row
                currentRow.addView(gridItem);
                gridItem.setTag("p " + playerIndex++); // p stands for players
            }
        }

        findViewById(R.id.topCard0).setClickable(false);
        findViewById(R.id.topCard1).setClickable(false);
        findViewById(R.id.topCard2).setClickable(false);
    }

    public void sendMsg(View view) {
        String text = chatEdt.getText().toString();
        if (text.isEmpty()) {
            return;
        }

        Thread t = new Thread(() -> {
            StringMessage msg = new StringMessage(9, 0, user.getUuid(), text);
            SocketManager.sendToServer(msg);
        });
        t.start();
        chatEdt.setText(null);
    }

    private void displayMsg(String username, String text) {
        messageList.add(new ChatMessage(username, text));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    private void removeParticipant(String username) {
        String userList = userListTv.getText().toString();
        userList = userList.replace("· " + username + "\n", ""); // in case the username is first in the list
        userList = userList.replace("\n· " + username, ""); // in case the username is at the middle of the list
        userListTv.setText(userList);

        Toast.makeText(this, username + " left the game :(", Toast.LENGTH_SHORT).show();
    }

    public void readyClick(View view) {
        readyButton.setClickable(false);
        Thread t = new Thread(() -> {
            Message msg = new Message(16, 0, user.getUuid());
            SocketManager.sendToServer(msg);
        });
        t.start();
    }

    public void startTimer() {
        // Cancel any existing timer to avoid overlapping timers
        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(game.getDiscussionTime() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                // Format as MM:SS
                long minutes = secondsRemaining / 60;
                long seconds = secondsRemaining % 60;
                String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                timerText.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
            }
        };
        timer.start();
    }

    public void startVote() {
        Intent intent = new Intent(this, Vote.class);
        intent.putExtra("gameObject", game);
        startActivity(intent);
        finish();
    }

    @Override
    public void onError(Exception e) {
        // Handle error if needed
    }

    @Override
    public void onStart() {
        super.onStart();
        SocketManager.setListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("Discussion onStop called");
    }

    public void rulesClick(View view) {
        RulesFragment rulesFragment = new RulesFragment();
        rulesFragment.show(getSupportFragmentManager(), "rules_dialog");
    }
}