package com.example.werewolfclient.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.werewolfclient.R;

public class LoadingDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static LoadingDialogFragment newInstance(String message) {
        LoadingDialogFragment fragment = new LoadingDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate the layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_loading_dialouge, null);

        // Set the message
        TextView messageText = view.findViewById(R.id.loading_message);
        messageText.setText(getArguments().getString(ARG_MESSAGE, "Connecting..."));

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(view)
                .setCancelable(false); // Prevent manual dismissal

        return builder.create();
    }
}