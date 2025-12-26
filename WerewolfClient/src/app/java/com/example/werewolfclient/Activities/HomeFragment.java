package com.example.werewolfclient.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.LoginObject;
import com.example.werewolfclient.ClientObjects.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {

    public static User user;
    public static List<Integer> cardIds;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize cardIds and user if they arent Initialized
        if (cardIds == null) {
            cardIds = new ArrayList<>();
            cardIds.add(R.drawable.card_dopplegenger);
            cardIds.add(R.drawable.card_werewolf);
            cardIds.add(R.drawable.card_minion);
            cardIds.add(R.drawable.card_mason);
            cardIds.add(R.drawable.card_seer);
            cardIds.add(R.drawable.card_robber);
            cardIds.add(R.drawable.card_troublemaker);
            cardIds.add(R.drawable.card_drunk);
            cardIds.add(R.drawable.card_insomniac);
            cardIds.add(R.drawable.card_hunter);
            cardIds.add(R.drawable.card_tanner);
            cardIds.add(R.drawable.card_villager);
        }

        if (user == null) {
            LoginObject loginObject = (LoginObject) getArguments().getSerializable("loginObject");
            user = new User(loginObject.getUuid(), loginObject.getData());
            updateData();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button createGameBtn = view.findViewById(R.id.createGameBtn);
        createGameBtn.setOnClickListener(this::createGame);
        Button joinGameBtn = view.findViewById(R.id.joinGameBtn);
        joinGameBtn.setOnClickListener(this::joinGame);
        Button adminBtn = view.findViewById(R.id.adminBtn);
        adminBtn.setOnClickListener(this::adminClick);
        if ((boolean) user.getData().get("type"))
            adminBtn.setVisibility(View.VISIBLE);
        else
            adminBtn.setVisibility(View.INVISIBLE);

        return view;
    }

    /**
     * Fix data to account for changes after a game
     */
    public static void updateData() {
        HashMap<String, Object> data = user.getData();
        List<?> rolesData = (List<?>) data.get("characters"); // Use wildcard to avoid direct cast
        List<Integer> newRolesData = new ArrayList<>();
        for (Object value : rolesData) {
            if (value instanceof Double) {
                newRolesData.add(((Double) value).intValue());
            } else if (value instanceof Integer) {
                newRolesData.add((Integer) value); // Safe cast to Integer
            } else {
                newRolesData.add(Integer.valueOf(value.toString())); // Fallback for other types
            }
        }
        data.put("characters", newRolesData);

        // Handle other numeric fields safely
        data.put("games", convertToInt(data.get("games")));
        data.put("wins", convertToInt(data.get("wins")));
        data.put("wins-town", convertToInt(data.get("wins-town")));
        data.put("wins-tanner", convertToInt(data.get("wins-tanner")));
        data.put("wins-werewolf", convertToInt(data.get("wins-werewolf")));

        user.setData(data);
    }

    // Helper method to safely convert Object to Integer
    private static Integer convertToInt(Object value) {
        if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value != null) {
            try {
                return Integer.valueOf(value.toString());
            } catch (NumberFormatException e) {
                return 0; // Default value if conversion fails
            }
        }
        return 0; // Default value if null
    }

    public void createGame(View view) {
        CreateGameFragment fragment = new CreateGameFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void joinGame(View view) {
        JoinGameFragment fragment = new JoinGameFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void adminClick(View view) {

        AdminPageFragment fragment = new AdminPageFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Disable the phone's back button
     */
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to disable back button
            }
        });
    }
}