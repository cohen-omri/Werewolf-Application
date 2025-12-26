package com.example.werewolfclient.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.werewolfclient.R;

public class PopupDisplayCard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_display_card);

        // Get the views
        TextView popupText = findViewById(R.id.popup_text);
        ImageView popupImage = findViewById(R.id.popup_image);

        // Get data from the Intent
        String text = getIntent().getStringExtra("text");
        int imageResId = getIntent().getIntExtra("resId", R.drawable.ic_launcher_background);

        // Set the text and image
        if (text != null) {
            popupText.setText(text);
        }
        popupImage.setImageResource(imageResId);
    }

    public void backClick(View view) {
        finish();
    }
}