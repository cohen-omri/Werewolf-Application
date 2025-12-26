package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Activities.HomeFragment.user;
import static com.example.werewolfclient.Objects.SocketManager.gson;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.werewolfclient.ClientObjects.Message;
import com.example.werewolfclient.ClientObjects.StringMessage;
import com.example.werewolfclient.Objects.ChatMessage;
import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.GameObject;
import com.example.werewolfclient.ServerObjects.ResultsObject;

import java.util.ArrayList;

public class Vote extends AppCompatActivity implements SocketManager.ServerMessageListener {

    private GameObject game;
    private RadioGroup radioGroup;
    private TextView amountOfVotes, timerText;
    private ArrayList<String> players;
    private boolean isVoted;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vote);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left + 20, systemBars.top + 20, systemBars.right + 20, systemBars.bottom + 20);
            return insets;
        });

        // Retrieve GameObject from arguments
        Intent intent = getIntent();
        game = (GameObject) intent.getSerializableExtra("gameObject");

        players = game.getPlayers();
        players.remove(user.getData().get("name"));

        amountOfVotes = findViewById(R.id.amountOfVotes);
        amountOfVotes.setText("0/" + (game.getPlayers().size()+1) + " voted");
        isVoted = false;
        timerText = findViewById(R.id.timerText);

        radioGroup = findViewById(R.id.radioGroup);
        for (String username : players) {
            // Create a new RadioButton for each username
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(username);
            radioButton.setTextColor(getResources().getColor(R.color.black));
            radioButton.setTextSize(20);
            radioButton.setPadding(10, 10, 10, 10); // Add padding for better spacing
            radioButton.setGravity(android.view.Gravity.CENTER); // Center the text
            radioButton.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));

            // Add the RadioButton to the RadioGroup
            radioGroup.addView(radioButton);
        }

        startTimer();
    }

    @Override
    public void onMessageReceived(Object obj) {

        Message message = gson.fromJson(obj.toString(), Message.class);
        System.out.println("Vote received message with action: " + message.getAction());

        switch (message.getAction()) {

            case 17: // a person voted
                amountOfVotes.setText(message.getMsg() + "/" + (game.getPlayers().size()+1) + " voted"); // msg = amount of votes
                break;

            case 18:
                ResultsObject results = gson.fromJson(obj.toString(), ResultsObject.class);;
                Intent intent = new Intent(this, Results.class);
                intent.putExtra("resultsObject", results);
                startActivity(intent);
                finish();
                break;

            default:
                runOnUiThread(() -> {
                    Toast.makeText(Vote.this, "Unexpected message action: " + message.getAction(), Toast.LENGTH_SHORT).show();
                });
                break;
        }
    }

    public void voteClick (View view) {
        if (isVoted) {
            Toast.makeText(this, "You have already voted", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedButton = findViewById(selectedId);
            String selectedUsername = selectedButton.getText().toString();

            Toast.makeText(this, "Voted for: " + selectedUsername, Toast.LENGTH_SHORT).show();
            Thread t = new Thread(() -> {
                StringMessage msg = new StringMessage(17, 1, user.getUuid(), selectedUsername);
                SocketManager.sendToServer(msg);
            });
            t.start();

            radioGroup.setClickable(false);
            isVoted = true;
        } else {
            Toast.makeText(this, "Please select a player to vote for", Toast.LENGTH_SHORT).show();
        }
    }

    public void startTimer() {
        // Cancel any existing timer to avoid overlapping timers
        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(game.getActionTime() * 1000, 1000) {
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

    public void rulesClick(View view) {
        RulesFragment rulesFragment = new RulesFragment();
        rulesFragment.show(getSupportFragmentManager(), "rules_dialog");
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
        System.out.println("Vote onStop called");
    }
}