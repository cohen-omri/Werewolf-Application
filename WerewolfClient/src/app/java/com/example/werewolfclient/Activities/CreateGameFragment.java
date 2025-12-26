package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Activities.HomeFragment.user;
import static com.example.werewolfclient.Objects.SocketManager.gson;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.werewolfclient.ClientObjects.LobbyObj;
import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.LobbyObject;

public class CreateGameFragment extends Fragment {

    private Spinner[] spinners;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_game, container, false);

        // Check if user is null (shouldn't happen since MainActivity ensures login)
        if (user == null) {
            getParentFragmentManager().popBackStack();
            return view;
        }

        // Initialize spinners
        spinners = new Spinner[]{
                view.findViewById(R.id.dropdown1),
                view.findViewById(R.id.dropdown2),
                view.findViewById(R.id.dropdown3),
                view.findViewById(R.id.dropdown4)
        };

        // Array of options for each dropdown
        String[][] options = {
                {"6", "8", "10", "12", "13"},  // Options for max participants
                {"10 sec", "20 sec", "30 sec", "45 sec", "1 min"},  // Options for action time
                {"1 min", "3 min", "5 min", "7 min", "10 min"},  // Options for discussion time
                {"10 sec", "20 sec", "30 sec", "1 min"}   // Options for voting time
        };

        // Default selections (positions in the arrays)
        int[] defaultPositions = {2, 1, 2, 1};

        for (int i = 0; i < spinners.length; i++) {
            // Set up the adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.custom_spinner, options[i]);
            adapter.setDropDownViewResource(R.layout.custom_spinner_item);
            spinners[i].setAdapter(adapter);

            spinners[i].setSelection(defaultPositions[i]);
            spinners[i].setDropDownWidth(Spinner.LayoutParams.MATCH_PARENT);
        }

        // Set click listeners
        View createGameBtn = view.findViewById(R.id.createGameBtn);
        createGameBtn.setOnClickListener(this::createGame);

        ImageView backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this::backClick);

        return view;
    }

    public void createGame(View view) {
        int maxParticipants = Integer.parseInt((String) spinners[0].getSelectedItem());
        int actionTime = parseTimeToSeconds((String) spinners[1].getSelectedItem());
        int discussionTime = parseTimeToSeconds((String) spinners[2].getSelectedItem());
        int votingTime = parseTimeToSeconds((String) spinners[3].getSelectedItem());

        Thread t = new Thread(() -> {
            LobbyObj settings = new LobbyObj(user.getUuid(), maxParticipants, actionTime, discussionTime, votingTime);
            Object response = SocketManager.sendAndGetResponse(settings);

            if (response == null) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Problem connecting to server", Toast.LENGTH_SHORT).show();
                });
                System.out.println("Problem connecting to server");
                return;
            }

            LobbyObject lobby = gson.fromJson(response.toString(), LobbyObject.class);
            switch (lobby.getMsg()) {
                case 0:
                    // Navigate to LobbyFragment
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("lobbyObject", lobby);
                    LobbyFragment lobbyFragment = new LobbyFragment();
                    lobbyFragment.setArguments(bundle);

                    requireActivity().runOnUiThread(() -> {
                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, lobbyFragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    break;
                default:
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "There was an unexpected problem", Toast.LENGTH_SHORT).show();
                    });
                    break;
            }
        });
        t.start();
    }

    private int parseTimeToSeconds(String timeString) {
        // Split the string to separate the number and unit (e.g., "5 sec" -> ["5", "sec"])
        String[] parts = timeString.split(" ");
        int value = Integer.parseInt(parts[0]);

        if (parts[1].equals("min")) {
            return value * 60; // Convert minutes to seconds
        } else {
            return value; // Already in seconds
        }
    }

    public void backClick(View view) {
        getParentFragmentManager().popBackStack();
    }
}