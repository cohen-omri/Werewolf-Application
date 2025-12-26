package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Activities.HomeFragment.user;
import static com.example.werewolfclient.Activities.Signup.maxLength;
import static com.example.werewolfclient.Activities.Signup.minLength;
import static com.example.werewolfclient.Objects.SocketManager.gson;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.werewolfclient.ClientObjects.LoginObj;
import com.example.werewolfclient.ClientObjects.Message;
import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.LoginObject;

public class EditUserFragment extends Fragment {

    private EditText usernameEdt, passwordEdt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_user, container, false);

        // Check if user is null (shouldn't happen since MainActivity ensures login)
        if (user == null) {
            getParentFragmentManager().popBackStack();
            return view;
        }

        // Initialize views
        usernameEdt = view.findViewById(R.id.usernameEdt);
        passwordEdt = view.findViewById(R.id.passwordEdt);
        Button updateBtn = view.findViewById(R.id.updateBtn);

        Button logoutBtn = view.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this::logoutClick);
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this::backClick);

        // Set initial values
        usernameEdt.setText(user.getData().get("name").toString());
        passwordEdt.setText(user.getData().get("password").toString());

        // Set up update button click listener
        updateBtn.setOnClickListener(v -> {
            String un = usernameEdt.getText().toString();
            String p = passwordEdt.getText().toString();

            if (un.isEmpty() || p.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all categories", Toast.LENGTH_SHORT).show();
            } else {
                LoginObj u = new LoginObj(un, p, 3);
                Thread t = new Thread(() -> updateUser(u));
                t.start();
            }
        });


        return view;
    }

    public void updateUser(LoginObj u) {

        u.setUuid(user.getUuid());
        System.out.println(u.getUuid());
        Object response = SocketManager.sendAndGetResponse(u);

        if (response == null) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Problem connecting to server", Toast.LENGTH_SHORT).show();
            });
            System.out.println("Problem connecting to server");
            return;
        }

        LoginObject obj = gson.fromJson(response.toString(), LoginObject.class);
        System.out.println(obj.toString());
        if (obj.isAck()) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Successfully updated user", Toast.LENGTH_SHORT).show();
                user.setData(obj.getData());
            });
        } else {
            requireActivity().runOnUiThread(() -> {
                switch (obj.getMsg()) {
                    case 3:
                        Toast.makeText(requireContext(), "Username must be longer than " + minLength + " characters", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(requireContext(), "Username must be shorter than " + maxLength + " characters", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(requireContext(), "Password must be longer than " + minLength + " characters", Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        Toast.makeText(requireContext(), "Password must be shorter than " + maxLength + " characters", Toast.LENGTH_SHORT).show();
                        break;
                    case 7:
                        Toast.makeText(requireContext(), "There already exists a user with this name", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(requireContext(), "There was an unexpected problem", Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        }
    }

    public void logoutClick(View view) {
        Thread t = new Thread(() -> {
            Message msg = new Message(4, 0, user.getUuid());
            Object response = SocketManager.sendAndGetResponse(msg);

            if (response == null) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Problem connecting to server", Toast.LENGTH_SHORT).show();
                });
                System.out.println("Problem connecting to server");
                return;
            }

            Message obj = gson.fromJson(response.toString(), Message.class);
            if (obj.getMsg() == 0) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Logging out", Toast.LENGTH_SHORT).show();
                    user = null;
                    Intent intent = new Intent(requireContext(), Welcome.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                });
            } else {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "There was an unexpected problem", Toast.LENGTH_SHORT).show();
                });
            }
        });
        t.start();
    }

    public void backClick(View view) {
        requireActivity().onBackPressed();
    }
}