package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Activities.HomeFragment.cardIds;
import static com.example.werewolfclient.Activities.HomeFragment.updateData;
import static com.example.werewolfclient.Activities.HomeFragment.user;
import static com.example.werewolfclient.Objects.SocketManager.gson;

import android.content.Intent;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.werewolfclient.ClientObjects.Message;
import com.example.werewolfclient.ClientObjects.StringMessage;
import com.example.werewolfclient.ClientObjects.User;
import com.example.werewolfclient.Objects.ChatMessage;
import com.example.werewolfclient.Objects.MessageAdapter;
import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.AdminObject;
import com.example.werewolfclient.ServerObjects.GameObject;
import com.example.werewolfclient.ServerObjects.LobbyObject;
import com.example.werewolfclient.ServerObjects.LoginObject;

import java.util.ArrayList;
import java.util.List;

public class ActiveGameFragment extends Fragment implements SocketManager.ServerMessageListener {

    private TextView stateTv, userListTv, numOfParticipants;
    private List<ImageButton> cardBtnList;
    private AdminObject adminObject;
    private LobbyObject lobby;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active_game, container, false);

        System.out.println(user.toString());

        // Check if user or lobby is null (shouldn't happen since MainActivity ensures login and lobby is passed)
        if (user == null || getArguments() == null) {
            getParentFragmentManager().popBackStack();
            return view;
        }

        // Retrieve LobbyObject from arguments
        adminObject = (AdminObject) getArguments().getSerializable("adminObject");
        lobby = adminObject.getLobby();

        if (lobby == null) {
            getParentFragmentManager().popBackStack();
            return view;
        }

        // Initialize views
        userListTv = view.findViewById(R.id.userListTv);
        numOfParticipants = view.findViewById(R.id.numOfParticipants);

        TextView tv = view.findViewById(R.id.lobbyTv);
        tv.setText("Lobby code: " + lobby.getCode());
        stateTv = view.findViewById(R.id.stateTv);
        stateTv.setText("Game state: " + adminObject.getState());

        // Role grid setup
        cardBtnList = new ArrayList<>();
        GridLayout roleGrid = view.findViewById(R.id.roleGrid);
        ImageButton temp;
        int id = 0;

        for (int i = 0; i < cardIds.size(); i++) {
            temp = setupRoleBtn(i, id++);
            roleGrid.addView(temp);
            cardBtnList.add(temp);
            if (i == 1 || i == 3 || i == 11) { // werewolf or mason (appears 2 times)
                temp = setupRoleBtn(i, id++);
                roleGrid.addView(temp);
                cardBtnList.add(temp);
            }
            if (i == 11) { // villager (appears 3 times)
                temp = setupRoleBtn(i, id++);
                roleGrid.addView(temp);
                cardBtnList.add(temp);
            }
        }

        // User list setup
        List<String> userList = lobby.getPlayers();
        String userString = "· " + userList.get(0);
        for (int i = 1; i < userList.size(); i++) {
            userString += "\n· " + userList.get(i);
        }
        userListTv.setText(userString);

        // Show number of users
        numOfParticipants.setText(userList.size() + " Participants");

        // Set up click listeners
        ImageButton backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(this::backClick);

        return view;
    }

    @Override
    public void onMessageReceived(Object obj) {
        Message message = gson.fromJson(obj.toString(), Message.class);
        System.out.println("ActiveGame received message with action: " + message.getAction());

        switch (message.getAction()) {

            case 7: // New participant
                StringMessage newParticipantMsg = gson.fromJson(obj.toString(), StringMessage.class);
                String newParticipant = newParticipantMsg.getString();
                requireActivity().runOnUiThread(() -> addParticipant(newParticipant));
                break;

            case 8: // Remove participant
                StringMessage removeParticipantMsg = gson.fromJson(obj.toString(), StringMessage.class);
                String uToRemove = removeParticipantMsg.getString();
                requireActivity().runOnUiThread(() -> removeParticipant(uToRemove));
                break;

            case 10: // Role toggled
                Message roleMsg = gson.fromJson(obj.toString(), Message.class);
                int roleId = roleMsg.getMsg();
                if (roleId != -1) {
                    System.out.println("toggle role: " + roleId);
                    ImageButton roleBtn = cardBtnList.get(roleId);
                    requireActivity().runOnUiThread(() -> {
                        roleBtn.setTag(!(Boolean) roleBtn.getTag());
                        toggleRole(roleBtn);
                    });
                }
                break;

            case 19: // Change state
                adminObject = gson.fromJson(obj.toString(), AdminObject.class);
                stateTv.setText("Game state: " + adminObject.getState());
                break;

            default:
                requireActivity().runOnUiThread(() -> { Toast.makeText(requireContext(), "Unexpected message action: " + message.getAction(), Toast.LENGTH_SHORT).show(); });
                break;
        }
    }

    private void addParticipant(String username) {
        lobby.getPlayers().add(username);
        userListTv.setText(userListTv.getText() + "\n· " + username);
        numOfParticipants.setText(lobby.getPlayers().size() + " Participants");
    }

    private void removeParticipant(String username) {
        lobby.getPlayers().remove(username);
        String userList = userListTv.getText().toString();
        userList = userList.replace("· " + username + "\n", ""); // in case the username is first in the list
        userList = userList.replace("\n· " + username, ""); // in case the username is at the middle of the list
        userListTv.setText(userList);
        numOfParticipants.setText(lobby.getPlayers().size() + " Participants");
    }

    public ImageButton setupRoleBtn(int i, int id) {
        ImageButton role = new ImageButton(requireContext(), null, android.R.attr.borderlessButtonStyle);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = (int) (192 * 0.6);
        params.height = (int) (263 * 0.6);
        params.setMargins(0, 7, 7, 7);
        role.setLayoutParams(params);

        role.requestLayout();
        role.setScaleType(ImageView.ScaleType.CENTER_CROP);
        role.setImageResource(cardIds.get(i));
        role.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_rounded));
        role.setClipToOutline(true);

        role.setTag(lobby.getActiveRoles().get(id));
        toggleRole(role);
        role.setId(id);

        return role;
    }

    private void toggleRole(ImageButton role) {
        if ((boolean) role.getTag()) {
            role.clearColorFilter();
            role.setAlpha(1.0f);
            return;
        }

        float[] matrix = new float[] {
                0.5f, 0f, 0f, 0f, 0f,
                0f, 0.5f, 0f, 0f, 0f,
                0f, 0f, 0.5f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
        };
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        role.setColorFilter(filter);
        role.setAlpha(0.5f);
    }



    @Override
    public void onError(Exception e) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), "Error receiving message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStart() {
        System.out.println("LobbyFragment onStart called");
        super.onStart();
        SocketManager.setListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("LobbyFragment onStop called");
    }

    public void backClick(View view) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AdminPageFragment())
                .commit();
    }
}