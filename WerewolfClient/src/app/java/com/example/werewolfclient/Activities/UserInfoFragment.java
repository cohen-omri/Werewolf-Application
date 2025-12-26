package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Activities.HomeFragment.cardIds;
import static com.example.werewolfclient.Activities.HomeFragment.user;
import static com.example.werewolfclient.Objects.SocketManager.gson;


import static objects.CardType.roleFromInt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.werewolfclient.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserInfoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);

        ImageButton backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this::backClick);

        // Check if user is null (shouldn't happen since MainActivity ensures login)
        if (user == null) {
            getParentFragmentManager().popBackStack();
            return view;
        }

        // Get data from user object
        HashMap<String, Object> uData = user.getData();

        List<Integer> rolesData = (ArrayList<Integer>) uData.get("characters"); // Array of 12
        // 0 - dopplegenger, 1 - werewolf, 2 - minion, 3 - mason, 4 - seer, 5 - robber, 6 - troublemaker, 7 - drunk, 8 - insomniac, 9 - hunter, 10 - tanner, 11 - villager

        String data = "Games played: " + uData.get("games") + "\nGames won: " + uData.get("wins") + " / " +
                uData.get("games") + "\n\nWins as town team: " + uData.get("wins-town") +
                "\nWins as werewolf team: " + uData.get("wins-werewolf") +
                "\nWins as tanner: " + uData.get("wins-tanner");
        TextView winDataTV = view.findViewById(R.id.winData);
        winDataTV.setText(data);

        GridLayout roleGrid = view.findViewById(R.id.roleGrid);

        for (int i = 0; i < cardIds.size(); i++) {
            ImageButton role = new ImageButton(requireContext(), null, android.R.attr.borderlessButtonStyle);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = (int) (192 * 0.9);
            params.height = (int) (263 * 0.9);
            params.setMargins(7, 7, 7, 7);
            role.setLayoutParams(params);

            role.requestLayout();
            role.setScaleType(ImageView.ScaleType.CENTER_CROP);
            role.setImageResource(cardIds.get(i));
            role.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_rounded));
            role.setClipToOutline(true);

            role.setId(i);
            role.setOnClickListener(v -> {
                ImageButton roleBtn = (ImageButton) v;
                int numOfPlays = rolesData.get(roleBtn.getId());
                String roleName = roleFromInt(roleBtn.getId()).toString();
                Toast.makeText(requireContext(), "You have played as a " + roleName + " " + numOfPlays + " times", Toast.LENGTH_SHORT).show();
            });

            roleGrid.addView(role);
        }

        return view;
    }

    public void backClick(View view) {
        requireActivity().onBackPressed();
    }
}