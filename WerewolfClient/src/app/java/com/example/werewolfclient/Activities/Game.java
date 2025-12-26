package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Activities.HomeFragment.cardIds;
import static com.example.werewolfclient.Activities.HomeFragment.user;
import static com.example.werewolfclient.Objects.SocketManager.gson;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
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

import com.example.werewolfclient.ClientObjects.Message;
import com.example.werewolfclient.ClientObjects.StringMessage;
import com.example.werewolfclient.Objects.ChatMessage;
import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.GameObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import objects.CardType;

public class Game extends AppCompatActivity implements SocketManager.ServerMessageListener {

    private LinearLayout[] rows;
    private LinearLayout topCards, userRow;
    private TextView timerText, turnText, roleText, actionText;
    private ArrayList<String> players;
    private GameObject game;
    private int playerIndex, playerRole, clickCounter, doppelgangerActionCounter;
    private String prevClickUsername;
    private ArrayList<ImageView> cardsToHide;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        game = (GameObject) getIntent().getSerializableExtra("gameObject");
        playerRole = game.getCard().getType();

        String text = "Your role is:";
        int resId = cardIds.get(playerRole);

        showPopup(text, resId);

        // setup properties
        timerText = findViewById(R.id.timerText);
        turnText = findViewById(R.id.turnText);
        turnText.setVisibility(View.INVISIBLE);
        roleText = findViewById(R.id.roleText);
        actionText = findViewById(R.id.actionText);

        rows = new LinearLayout[]{findViewById(R.id.row1), findViewById(R.id.row2), findViewById(R.id.row3)};
        topCards = findViewById(R.id.topCardsView);
        userRow = findViewById(R.id.userRow);

        cardsToHide = new ArrayList<>();
        clickCounter = 0;
        if (playerRole == CardType.DOPPLEGANGER.getType())
            doppelgangerActionCounter = 0; // used to check if doppelganger finished action
        else
            doppelgangerActionCounter = 2; // no if will be activated

        players = game.getPlayers();
        playerIndex = players.indexOf(user.getData().get("name"));
        players.remove(user.getData().get("name"));
        distributePlayers();

        // Set click listener for rules icon
        ImageView rulesIcon = findViewById(R.id.rules);
        rulesIcon.setOnClickListener(this::rulesClick);
    }

    @Override
    public void onMessageReceived(Object obj) {
        Message message = gson.fromJson(obj.toString(), Message.class);
        System.out.println("Game received message with action: " + message.getAction());

        switch (message.getAction()) {
            case 8: // Remove participant
                StringMessage removeParticipantMsg = gson.fromJson(obj.toString(), StringMessage.class);
                String uToRemove = removeParticipantMsg.getString();
                runOnUiThread(() -> Toast.makeText(this, uToRemove + " left the game :(", Toast.LENGTH_SHORT).show());
                players.remove(uToRemove);
                break;
            case 12:
                runOnUiThread(() -> setTurn(message.getMsg()));
                break;
            case 13: // show card
                StringMessage sMsg = gson.fromJson(obj.toString(), StringMessage.class);
                System.out.println("show card of player:" + sMsg.getString());
                showCard(sMsg.getString(), sMsg.getMsg());
                break;
            case 15: // go to discussion
                players.add(playerIndex, user.getData().get("name").toString());
                Intent intent = new Intent(this, Discussion.class);
                intent.putExtra("gameObject", game);
                startActivity(intent);
                finish();
                break;
            default:
                Toast.makeText(this, "Unexpected message action: " + message.getAction(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showPopup(String text, int imageResId) {
        Intent intent = new Intent(this, PopupDisplayCard.class);
        intent.putExtra("text", text);
        intent.putExtra("resId", imageResId);
        startActivity(intent);
    }

    private void distributePlayers() {
        // Calculate how many players per row (max 4 per row)
        int totalPlayers = players.size();
        int playersPerRow = 4; // Maximum players per row

        View gridItem = LayoutInflater.from(this).inflate(R.layout.game_card, userRow, false);
        TextView usernameText = gridItem.findViewById(R.id.username_text);
        usernameText.setText(user.getData().get("name").toString());

        // Get the ImageView and resize it
        ImageView userCardImage = gridItem.findViewById(R.id.card_image);
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

                // Set the card username
                usernameText = gridItem.findViewById(R.id.username_text);
                usernameText.setText(players.get(playerIndex));
                // Setup the card's onClick and set it as unclickable
                gridItem.setOnClickListener(this::cardClick);
                gridItem.setClickable(false);

                // Add the view to the current row
                currentRow.addView(gridItem);
                gridItem.setTag("p " + playerIndex++); // p stands for players
            }
        }
        // disable top cards
        setCardsEnabled("top", false);
    }

    @SuppressLint("SetTextI18n")
    public void setTurn(int turn) {
        if (doppelgangerActionCounter == 1)
            playerRole = 0; // if doppelganger saw seer, robber, troublemaker or drunk, reset the role so the player won't wake up again

        // Cancel any existing timer before starting a new one
        if (timer != null) {
            timer.cancel();
        }

        switch (turn) {
            case 0:
                roleText.setText("Doppelganger");
                actionText.setText("Wake up and look at another player's card. You are now that role.\n" +
                        "If you viewed the Seer, Robber, Troublemaker or Drunk card, do your action now.\n" +
                        "If you are now a Minion, wake up with the other minion.");
                startTimer();
                if (playerRole == 0) {
                    turnText.setVisibility(View.VISIBLE);
                    setCardsEnabled("players", true);
                }
                break;

            case 1:
                hideCards();
                turnText.setVisibility(View.INVISIBLE);
                startTimer();

                roleText.setText("Werewolves");
                actionText.setText("Wake up and look for other Werewolves.\n" +
                        "If there is only one werewolf, you may look at a card from the top.");
                if (playerRole == 1)
                    turnText.setVisibility(View.VISIBLE);
                break;

            case 2:
                hideCards();
                turnText.setVisibility(View.INVISIBLE);
                startTimer();

                roleText.setText("Minion");
                actionText.setText("Wake up, and look for the Werewolves.");
                if (playerRole == 2)
                    turnText.setVisibility(View.VISIBLE);
                break;

            case 3:
                hideCards();
                turnText.setVisibility(View.INVISIBLE);
                startTimer();

                roleText.setText("Masons");
                actionText.setText("Wake up and look for the other Masons");
                if (playerRole == 3)
                    turnText.setVisibility(View.VISIBLE);
                break;

            case 4:
                hideCards();
                turnText.setVisibility(View.INVISIBLE);
                startTimer();

                roleText.setText("Seer");
                actionText.setText("Wake up. You may look at another player's card, or two of the top cards.");
                if (playerRole == 4) {
                    turnText.setVisibility(View.VISIBLE);
                    setCardsEnabled("all", true);
                }
                startTimer();
                break;

            case 5:
                hideCards();
                turnText.setVisibility(View.INVISIBLE);
                startTimer();

                roleText.setText("Robber");
                actionText.setText("Wake up. You may exchange your card with another player's card, and then view your new card.");
                if (playerRole == 5) {
                    turnText.setVisibility(View.VISIBLE);
                    setCardsEnabled("players", true);
                }
                startTimer();
                break;

            case 6:
                hideCards();
                turnText.setVisibility(View.INVISIBLE);
                startTimer();

                roleText.setText("Troublemaker");
                actionText.setText("Wake up. You may exchange cards between two other players.");
                if (playerRole == 6) {
                    turnText.setVisibility(View.VISIBLE);
                    setCardsEnabled("players", true);
                }
                startTimer();
                break;

            case 7:
                hideCards();
                turnText.setVisibility(View.INVISIBLE);
                startTimer();

                roleText.setText("Drunk");
                actionText.setText("Wake up, and exchange your card with a card from the top.");
                if (playerRole == 7) {
                    turnText.setVisibility(View.VISIBLE);
                    setCardsEnabled("top", true);
                }
                startTimer();
                break;

            case 8:
                hideCards();
                turnText.setVisibility(View.INVISIBLE);
                startTimer();

                roleText.setText("Insomniac");
                actionText.setText("Wake up, and look at your card.");
                if (playerRole == 8)
                    turnText.setVisibility(View.VISIBLE);
                startTimer();
                break;

            default:
                actionText.setText("Unexpected turn ");
                break;
        }
    }

    public void cardClick(View view) {
        setCardsEnabled("all", false);
        String[] tag = view.getTag().toString().split(" "); // divide into: 0 = t(top)/p(player), 1 = index
        System.out.println("role " + playerRole + " clicked: " + view.getTag().toString());

        Thread t;

        switch (playerRole) {
            case 0:
                String toSend = ((TextView) (view.findViewById(R.id.username_text))).getText().toString();
                t = new Thread(() -> { // doppelganger choosing a card
                    StringMessage msg = new StringMessage(13, 1, user.getUuid(), toSend);
                    SocketManager.sendToServer(msg);
                });
                t.start();
                break;
            case 1: // a lone wolf clicked one of the top cards
                t = new Thread(() -> {
                    StringMessage msg = new StringMessage(13, 0, user.getUuid(), tag[1]);
                    SocketManager.sendToServer(msg);
                });
                t.start();
                break;

            case 4: // seer
                String toSend1;
                if (tag[0].equals("p")) { // player click - only one player
                    toSend1 = ((TextView) (view.findViewById(R.id.username_text))).getText().toString();
                    setCardsEnabled("players", false);
                } else { // top cardClick - two cards
                    toSend1 = tag[1];
                    if (clickCounter == 0) {
                        setCardsEnabled("players", false);
                        setCardsEnabled("top", true);
                        view.setClickable(false);
                        clickCounter++;
                    } else {
                        setCardsEnabled("all", false);
                        clickCounter = 0;
                    }
                }
                t = new Thread(() -> {
                    StringMessage msg = new StringMessage(13, 0, user.getUuid(), toSend1);
                    SocketManager.sendToServer(msg);
                });
                t.start();
                doppelgangerActionCounter++; // used to know if doppelganger-seer did their action
                break;

            case 5: // robber
                // show the clicked card
                String toSend2 = ((TextView) (view.findViewById(R.id.username_text))).getText().toString();
                t = new Thread(() -> {
                    StringMessage msg = new StringMessage(13, 0, user.getUuid(), toSend2);
                    SocketManager.sendToServer(msg);
                });
                t.start();
                // switch cards
                String toSend3 = toSend2.concat(";" + user.getData().get("name"));
                Thread t1 = new Thread(() -> {
                    StringMessage msg = new StringMessage(14, 0, user.getUuid(), toSend3);
                    SocketManager.sendToServer(msg);
                });
                t1.start();
                doppelgangerActionCounter++; // used to know if doppelganger-robber did their action
                break;

            case 6: // troublemaker
                if (clickCounter == 0) {
                    setCardsEnabled("players", true);
                    view.setClickable(false);
                    prevClickUsername = ((TextView) (view.findViewById(R.id.username_text))).getText().toString();
                    clickCounter++;
                } else {
                    setCardsEnabled("all", false);

                    String toSend4 = prevClickUsername + ";" + ((TextView) (view.findViewById(R.id.username_text))).getText().toString();
                    t = new Thread(() -> {
                        StringMessage msg = new StringMessage(14, 0, user.getUuid(), toSend4);
                        SocketManager.sendToServer(msg);
                    });
                    t.start();
                    clickCounter = 0;
                }
                doppelgangerActionCounter++; // used to know if doppelganger-troublemaker did their action
                break;

            case 7:
                String toSend5 = tag[1] + ";" + user.getData().get("name");
                t = new Thread(() -> {
                    StringMessage msg = new StringMessage(14, 0, user.getUuid(), toSend5);
                    SocketManager.sendToServer(msg);
                });
                t.start();
                doppelgangerActionCounter++; // used to know if doppelganger-drunk did their action
                break;
        }
    }

    /***
     * @param whichCards "top", "players", "all"
     */
    public void setCardsEnabled(String whichCards, boolean b) {
        System.out.println("set " + whichCards + " cards as " + b);
        if (whichCards.equals("top") || whichCards.equals("all"))
            for (int i = 0; i < topCards.getChildCount(); i++) {
                View card = topCards.getChildAt(i);
                card.setClickable(b);
            }

        if (whichCards.equals("players") || whichCards.equals("all"))
            for (LinearLayout currentRow : rows)
                for (int i = 0; i < currentRow.getChildCount(); i++) {
                    View card = currentRow.getChildAt(i);
                    card.setClickable(b);
                }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SocketManager.setListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel the timer to prevent it from running in the background
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        System.out.println("Game onStop called");
    }

    public void showCard(String cards, int roleToShow) {
        if (cards.equals("")) {
            if (playerRole == 2) { // player is minion without werewolves
                Toast.makeText(Game.this, "There are no werewolves", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(Game.this, "You are the only " + CardType.roleFromInt(playerRole), Toast.LENGTH_SHORT).show();

            if (playerRole == 1) // player is a lone wolf
                setCardsEnabled("top", true);
            return;
        }

        String[] usernames = cards.split(";"); // separates usernames in case there are multiple cards to show
        System.out.println("amount of usernames: " + usernames.length + ", show card " + roleToShow);
        for (String username : usernames) {
            if (username.equals("0") || username.equals("1") || username.equals("2")) {
                ImageView topCard = (ImageView) topCards.getChildAt(Integer.parseInt(username));
                topCard.setImageResource(cardIds.get(roleToShow));

                cardsToHide.add(topCard);
                break;
            } else if (playerRole == 8) { // insomniac
                View gridItem = userRow.getChildAt(0);
                ImageView cardImage = gridItem.findViewById(R.id.card_image);
                cardImage.setImageResource(cardIds.get(roleToShow));

                cardsToHide.add(cardImage);
                break;
            }

            int index = players.indexOf(username);
            int row = (int) (index / 4);
            View gridItem = rows[row].getChildAt(index - row * 4);
            ImageView cardImage = gridItem.findViewById(R.id.card_image);
            cardImage.setImageResource(cardIds.get(roleToShow));

            cardsToHide.add(cardImage);
        }

        if (playerRole == 0) { // doppelganger
            if (roleToShow == 4 || roleToShow == 5 || roleToShow == 6 || roleToShow == 7)
                // if doppelganger saw seer, robber, troublemaker or drunk
                new android.os.Handler().postDelayed(() -> {
                    playerRole = roleToShow;
                    setTurn(roleToShow);
                    roleText.setText("Doppelganger - " + CardType.roleFromInt(roleToShow).toString());
                }, 1500);
            if (roleToShow == 1 || roleToShow == 2 || roleToShow == 8) // if doppelganger saw insomniac
                playerRole = roleToShow;
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

    public void hideCards() {
        for (ImageView iv : cardsToHide)
            iv.setImageResource(R.drawable.card_back);
        cardsToHide = new ArrayList<>();
    }

    @Override
    public void onError(Exception e) {
        // Handle error if needed
    }

    public void rulesClick(View view) {
        RulesFragment rulesFragment = new RulesFragment();
        rulesFragment.show(getSupportFragmentManager(), "rules_dialog");
    }
}