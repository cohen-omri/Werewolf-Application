package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Activities.HomeFragment.cardIds;
import static com.example.werewolfclient.Objects.SocketManager.gson;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.werewolfclient.R;

import java.util.ArrayList;

public class RulesFragment extends DialogFragment {

    private TextView subtitleTV, contentTV;
    private Button jumpBtn1, jumpBtn2;
    private ImageView img;
    private ImageButton backBtn, forwardBtn;
    private int currPage;
    private ArrayList<String> content;
    private ArrayList<Integer> imageIds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rules, container, false);

        // Initialize views using the inflated view
        subtitleTV = view.findViewById(R.id.subTitle);
        contentTV = view.findViewById(R.id.currRules);
        jumpBtn1 = view.findViewById(R.id.jumpBtn1);
        jumpBtn2 = view.findViewById(R.id.jumpBtn2);
        img = view.findViewById(R.id.currImage);
        backBtn = view.findViewById(R.id.backArrow);
        forwardBtn = view.findViewById(R.id.forwardArrow);

        // Set up back button click listener to dismiss the dialog
        ImageView backIcon = view.findViewById(R.id.backBtn);
        backIcon.setOnClickListener(v -> dismiss());

        // Set up other click listeners for buttons
        jumpBtn1.setOnClickListener(this::jumpTo);
        jumpBtn2.setOnClickListener(this::jumpTo);
        backBtn.setOnClickListener(this::arrowClick);
        forwardBtn.setOnClickListener(this::arrowClick);

        // Initialize data and UI
        currPage = 1;
        setUpRules();
        arrowClick(backBtn);

        // set onClick linsteners
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this::backClick);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // Set the dialog to be translucent with a darkened background
                window.setBackgroundDrawableResource(android.R.color.transparent);
                // Optionally, adjust the width/height of the dialog
                window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Remove the default title bar
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void arrowClick(View view) {
        if (view.getId() == forwardBtn.getId()) {
            currPage++;
            if (currPage == 1)
                changeBtnState(backBtn, true);
            if (currPage == content.size() - 1)
                changeBtnState(forwardBtn, false);
        } else {
            currPage--;
            if (currPage == content.size() - 2)
                changeBtnState(forwardBtn, true);
            if (currPage == 0)
                changeBtnState(backBtn, false);
        }

        String[] text = content.get(currPage).split("#");
        subtitleTV.setText(text[0]);
        contentTV.setText(text[1]);
        img.setImageResource(imageIds.get(currPage));

        switch (currPage) {
            case 0:
                jumpBtn1.setVisibility(View.VISIBLE);
                jumpBtn2.setVisibility(View.VISIBLE);
                jumpBtn1.setText("Game structure");
                jumpBtn2.setText("Roles");
                break;
            case 4:
                jumpBtn1.setVisibility(View.VISIBLE);
                jumpBtn2.setVisibility(View.VISIBLE);
                jumpBtn1.setText("Introduction");
                jumpBtn2.setText("Roles");
                break;
            case 10:
                jumpBtn1.setVisibility(View.VISIBLE);
                jumpBtn2.setVisibility(View.VISIBLE);
                jumpBtn1.setText("Introduction");
                jumpBtn2.setText("Game structure");
                break;
            default:
                jumpBtn1.setVisibility(View.GONE);
                jumpBtn2.setVisibility(View.GONE);
                break;
        }
    }

    public void jumpTo(View view) {
        Button btn = (Button) view;
        String jump = (String) btn.getText();

        switch (jump) {
            case "Introduction":
                currPage = 0;
                break;
            case "Game structure":
                currPage = 4;
                break;
            case "Roles":
                currPage = 10;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + jump);
        }

        currPage--;
        arrowClick(forwardBtn);
        changeBtnState(backBtn, true);
    }

    public void changeBtnState(ImageButton btn, Boolean changeTo) {
        btn.setEnabled(changeTo);
        if (changeTo)
            btn.setVisibility(View.VISIBLE);
        else
            btn.setVisibility(View.INVISIBLE);
    }

    public void setUpRules() {
        content = new ArrayList<>();
        imageIds = new ArrayList<>();
        String str;

        // page 0
        str = "Welcome to Werewolf! \nAn adaptation of the popular game \"One Night Ultimate Werewolf\". " +
                "\n\nIn the next pages we have the game's rules, for you to learn and understand. \nSo let's get started!#" +
                "Click the arrows to go between pages. If you wish to jump ahead, Click these buttons:";
        content.add(str);
        imageIds.add(R.drawable.logo);

        // page 1
        str = "Introduction:#" +
                "A small town is where you've always lived. The people of the town, although diverse and different from one another, have always lived together in peace.\n" +
                "Yet recently, every full moon people have been disappearing... Suspicions arise between the folk of one of them being the mighty beast - a werewolf. " +
                "In order to live - the werewolves must disguise themselves, and in order to stop them - the townies must unite!  \n\n" +
                "In Werewolf, each player takes on the role of a Villager, a Werewolf, or a special character. " +
                "It's your job to figure out who the werewolves are and to kill at least one of them in order to win... " +
                "Unless you've become a Werewolf yourself!";
        content.add(str);
        imageIds.add(R.drawable.logo);

        // page 2
        str = "Introduction:#" +
                "Unlike many other games, the real fun in One Night is figuring out exactly what happened over that one night," +
                " where many of the players performed some sort of special action.\n\n" +
                "You’ll need to figure out what team you’re on " +
                "(because your role card might have been switched with another role card), and then figure out what teams the other players are on.\n" +
                "At the end of each game, you’ll vote for a player who isn’t on your team; the player that receives the most votes is \"killed\".\n" +
                "If a Werewolf is killed, everyone on the village team wins. If no Werewolves are killed, the Werewolf team wins.";
        content.add(str);
        imageIds.add(R.drawable.logo);

        // page 3
        str = "Navigating the screen:#" +
                "On the top right corner:\n" +
                "Click the question mark icon to read the game's rules - where you currently are" +
                "Click the trophy icon to see your achievements in the game  \uD83C\uDFC6\n" +
                "Click the person icon to edit your profile and log out of it  \uD83D\uDC64\n\n" +
                "From the home screen you can create a game or join one.";
        content.add(str);
        imageIds.add(R.drawable.logo);

        // page 4
        str = "Game Structure:#" +
                "After joining a lobby, you will be able to talk to your friends using a chat, " +
                "and choose which roles you want to have in your game.\n\n" +
                "In the actual game, there are a few stages: Night, Discussion, Voting and Game Ending.\n" +
                "You can learn about each stage in the next few pages.";
        content.add(str);
        imageIds.add(R.drawable.lobby_screen);

        // page 6
        str = "Night:#" +
                "Night - during the night each role will wake up at their turn, perform their action and go back to sleep." +
                "The roles will include viewing and switching other people's and your own cards, and divide into 3 teams - " +
                "you can learn about each role's action in the Role section.";
        content.add(str);
        imageIds.add(R.drawable.game_screen);

        // page 7
        str = "Discussion:#" +
                "After the night phase, players discuss amongst themselves who they believe the Werewolves are.\n" +
                "Werewolves might want to claim to be a different role so that they don't die. " +
                "Because certain roles change other players' cards, some players will believe they are one role, " +
                "when they are actually a different one. After the night phase, your role is the card " +
                "you have after any switching done, which may be different than your original role. " +
                "No one can look at any cards after the night phase.\n" +
                "After a few minutes of discussion, players vote.";
        content.add(str);
        imageIds.add(R.drawable.ic_forum);

        // page 8
        str = "Voting:#" +
                "The player with the most votes dies and reveals their card. " +
                "In case of a tie, one of the players will be randomly selected to die.\n" +
                "If no player receives more than one vote, no one dies. " +
                "One way the players might achieve this is if everyone votes for the person following them " +
                "on the player list, with each player receiving one vote (decide this as a group before you vote).";
        content.add(str);
        imageIds.add(R.drawable.ic_vote);

        // page 9
        str = "Game Ending:#" +
                "After just one night and one day...\n" +
                "The village team wins:\n" +
                "1) If at least one Werewolf dies.\n" +
                "2) If no one is a Werewolf and no one dies. It is possible for no one to be a Werewolf if " +
                "the \"force werewolf\" option has been disabled, and all Werewolf cards are in the center.\n" +
                "The werewolf team only wins if at least one player is a Werewolf and no Werewolves are killed.\n" +
                "If you are playing with the Tanner, there are special rules regarding who wins; see the Tanner's " +
                "role description for details.\n\n" +
                "After the reveal, players will return to the lobby, to play another game :)";
        content.add(str);
        imageIds.add(R.drawable.ic_trophy);

        // page 10
        str = "In the case of disconecction:#" +
                "After joining a game you may not quit.\n" +
                "Yet if a player has connection errors or has to leave for some reason, their card will become " +
                "inactive during the night. They may return to the game and players may interact with their card again.";
        content.add(str);
        imageIds.add(R.drawable.ic_bad_wifi);

        // page 11
        str = "Roles:#" +
                "The game's roles divide into three teams: Village team, Werewolf team and Tanner team.\n" +
                "The Village team's goal is to find the werewolves and vote one of them out. The Werewolf team's goal is to survive " +
                "the night, securing themselves a position within the village - if anyone but the Werewolves is voted out, they win. " +
                "The tanner's goal is to be voted out.\n\n" +
                "In the following pages you can learn about each role in detail.";
        content.add(str);
        imageIds.add(R.drawable.ic_happy_sad);

        // Add role cards starting from page 12
        imageIds.addAll(cardIds);

        str = "Doppelganger:#" +
                "The Doppelganger is a fairly complicated card, because she takes on the role and team of whatever card she views.\n" +
                "If you haven't played with most of the other roles yet, skip this section for now... It will make a lot more sense then.\n\n" +
                "The Doppelganger wakes up before the other roles. At night, she looks at (but does not switch) one other player's card and " +
                "does the following based on what she sees:\n" +
                "Villager, Tanner, Hunter: She is now that role and does nothing else at night.\n" +
                "Werewolf or Mason: She wakes up with the other Werewolves or Masons when they are called. She is on the werewolf team if she " +
                "views a werewolf, and on the village team if sh views a Mason.\n" +
                "Minion or Insomniac: She wakes up with the other Minion or Insomniac and performes her new role. If she viewed a minion, the " +
                "Werewolves' cards will be shown, and if she viewed the Insomniac her own card will show (at the end of the game).\n" +
                "Seer, Robber, Troublemaker, Drunk: She immediately does that role's action, and does not wake up again with the " +
                "original role when called.\n\n" +
                "If the Doppelgänger card gets swapped to another player during the Night phase, that player's role becomes whatever card the Doppelgänger viewed.\n" +
                "If the Doppelgänger card is in the center and is then given to an active player, then the card is a simple Villager team card, as no card was viewed by a Doppelgänger.";
        content.add(str);

        str = "Werewolf:#" +
                "At night, all werewolves open their eyes and look for other werewolves. If no one else opens their eyes, " +
                "the other Werewolves are in the center or were mixed out. Werewolves are on the werewolf team." +
                "\nLone Wolf option: If there is only one Werewolf, the Werewolf may view one center card. This is extremely beneficial " +
                "to a Werewolf who doesn't have a partner, providing a useful tool for decieving the rest of the players.";
        content.add(str);

        str = "Minion:#" +
                "Immediately following the Werewolf phase at night, the Minion wakes up and sees who the Werewolves are. " +
                "During this phase, all Werewolves' cards are shown, so the Minion can see who they are. The Werewolves don’t know who " +
                "the Minion is. If the Minion dies and no Werewolves die, the Werewolves (and the Minion) win. " +
                "If no players are Werewolves, the Minion wins as long as one other player (not the Minion) dies. " +
                "This role can be a very powerful ally for the werewolf team. This Minion is on the werewolf team.";
        content.add(str);

        str = "Mason:#" +
                "When using the Masons always put both Masons in the game. " +
                "The Mason wakes up at night and looks for the other Mason. If the Mason doesn’t see another Mason, " +
                "it means the other Mason card is in the center, or was mixed out. Masons are on the village team.";
        content.add(str);

        str = "Seer:#" +
                "At night, the Seer may look either at one other player’s card or at two of the center cards, " +
                "but does not move them. The Seer is on the village team.";
        content.add(str);

        str = "Robber:#" +
                "At night, the Robber may choose to rob a card from another player, switching their roles. " +
                "Then the Robber looks at his new card. The player who receives the Robber card is on the village team. " +
                "The Robber is on the team of the card he takes, however, he does not do the action of his new role at night. " +
                "If the Robber chooses not to rob a card from another player, he remains the Robber and is on the village team.";
        content.add(str);

        str = "Troublemaker:#" +
                "At night, the Troublemaker may switch the cards of two other players without looking at those cards. " +
                "The players who receive a different card are now the role (and team) of their new card, even though they don’t know what " +
                "role that is until the end of the game. The Troublemaker is on the village team.";
        content.add(str);

        str = "Drunk:#" +
                "The Drunk is so drunk that he doesn’t remember his role. " +
                "When it comes time to wake up at night, he must exchange his Drunk card for any card in the center, " +
                "but he does not look at it. The Drunk is now the new role in front of him (even though he doesn’t know " +
                "what that new role is) and is on that team.";
        content.add(str);

        str = "Insomniac:#" +
                "The Insomniac wakes up and looks at her card (to see if it has changed). " +
                "Only use the Insomniac if the Robber and/or the Troublemaker are in the game. " +
                "The Insomniac is on the village team.";
        content.add(str);

        str = "Hunter:#" +
                "The Hunter does not wake up during the night. If the Hunter dies, the player he voted for dies instead." +
                "(regardless of how many votes his target receives). The Hunter is on the village team.";
        content.add(str);

        str = "Tanner:#" +
                "The Tanner does not wake up during the night. He hates his job so much that he wants to die. " +
                "The Tanner only wins if he dies. If the Tanner dies and no Werewolves die, the Werewolves do not win. " +
                "The Tanner is considered a member of the village (but is not on their team), so if the Tanner dies " +
                "when all werewolves are in the center, the village team loses. " +
                "The Tanner is not on the werewolf or the villager team.";
        content.add(str);

        str = "Villager:#" +
                "The Villager does not wake up during the night. He has no special abilities, but he is definitely not a werewolf. " +
                "Players may often claim to be a Villager. The Villager is on the village team.";
        content.add(str);
    }

    public void backClick(View view) {
        // Check if this fragment is shown as a dialog (from an activity)
        if (getShowsDialog()) {
            dismiss(); // Simply dismiss the dialog, return to the host activity
        } else {
            // If shown as a regular fragment (in MainActivity), use onBackPressed
            requireActivity().onBackPressed();
        }
    }
}