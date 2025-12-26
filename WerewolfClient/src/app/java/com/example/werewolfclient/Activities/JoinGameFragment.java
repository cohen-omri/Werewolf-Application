package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Activities.HomeFragment.user;
import static com.example.werewolfclient.Objects.SocketManager.gson;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.werewolfclient.ClientObjects.StringMessage;
import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.LobbyObject;

public class JoinGameFragment extends Fragment {

    private EditText codeEdt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_game, container, false);

        // Check if user is null (shouldn't happen since MainActivity ensures login)
        if (user == null) {
            getParentFragmentManager().popBackStack();
            return view;
        }

        // Initialize views
        codeEdt = view.findViewById(R.id.codeEdt);

        // Set click listeners
        View joinGameBtn = view.findViewById(R.id.joinGameBtn);
        joinGameBtn.setOnClickListener(this::joinGame);

        ImageView backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this::backClick);

        return view;
    }

    public void joinGame(View view) {
        String code = codeEdt.getText().toString();
        if (code.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a code", Toast.LENGTH_SHORT).show();
            return;
        }

        Thread t = new Thread(() -> {
            StringMessage codeMsg = new StringMessage(6, 0, user.getUuid(), code);
            Object response = SocketManager.sendAndGetResponse(codeMsg);

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
                case 1:
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "This code does not exist", Toast.LENGTH_SHORT).show();
                    });
                    break;
                case 2:
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "This lobby is already full", Toast.LENGTH_SHORT).show();
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

    public void backClick(View view) {
        getParentFragmentManager().popBackStack();
    }
}