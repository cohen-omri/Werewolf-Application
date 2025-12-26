package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Activities.HomeFragment.cardIds;
import static com.example.werewolfclient.Activities.HomeFragment.user;
import static com.example.werewolfclient.Objects.SocketManager.gson;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.werewolfclient.ClientObjects.LobbyObj;
import com.example.werewolfclient.ClientObjects.Message;
import com.example.werewolfclient.ClientObjects.StringMessage;
import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.AdminObject;
import com.example.werewolfclient.ServerObjects.LobbyObject;

import java.util.ArrayList;

public class AdminPageFragment extends DialogFragment {

    private Spinner gameDropdown;
    private Button viewGameBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_page, container, false);

        // Set up back button click listener to dismiss the dialog
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this::backClick);

        viewGameBtn = view.findViewById(R.id.viewGameBtn);
        viewGameBtn.setOnClickListener(this::viewGame);
        viewGameBtn.setClickable(true);

        gameDropdown = view.findViewById(R.id.gameDropdown);

        Thread t = new Thread(() -> {
            Message msg = new Message(20, 0, user.getUuid());
            Object response = SocketManager.sendAndGetResponse(msg);

            if (response == null) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Problem connecting to server", Toast.LENGTH_SHORT).show();
                });
                System.out.println("Problem connecting to server");
                return;
            }

            StringMessage message = gson.fromJson(response.toString(), StringMessage.class);
            String[] codes = message.getString().split(";");

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.custom_spinner, codes);
                adapter.setDropDownViewResource(R.layout.custom_spinner_item);
                gameDropdown.setAdapter(adapter);
                gameDropdown.setDropDownWidth(Spinner.LayoutParams.MATCH_PARENT);
            });
        });
        t.start();


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

    public void viewGame (View view) {
        viewGameBtn.setClickable(false);
        String code = (String) gameDropdown.getSelectedItem();

        Thread t = new Thread(() -> {
            StringMessage msg = new StringMessage(19, 0, user.getUuid(), code);
            Object response = SocketManager.sendAndGetResponse(msg);

            if (response == null) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Problem connecting to server", Toast.LENGTH_SHORT).show();
                });
                System.out.println("Problem connecting to server");
                return;
            }

            AdminObject adminObject = gson.fromJson(response.toString(), AdminObject.class);

            Bundle bundle = new Bundle();
            bundle.putSerializable("adminObject", adminObject);
            ActiveGameFragment fragment = new ActiveGameFragment();
            fragment.setArguments(bundle);

            requireActivity().runOnUiThread(() -> {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });
        });
        t.start();
    }

    public void backClick(View view) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }
}