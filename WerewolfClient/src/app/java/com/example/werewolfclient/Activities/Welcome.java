package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Objects.SocketManager.ip;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Thread(() -> {
            try {
                URL url = new URL("https://checkip.amazonaws.com"/*"https://api.ipify.org"*/);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String ipp = in.readLine();
                ip.set(ipp);
                Log.d("IP", "Public IP: " + ipp);
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(1000);
                SocketManager.initializeSocket();
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to connect to server", Toast.LENGTH_LONG).show());
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t.start();

    }

    protected void onDestroy() {
        super.onDestroy();
        SocketManager.closeSocket();
    }

    public void signUpClick(View view) {
        Intent intent = new Intent(this, Signup.class);
        startActivity(intent);
    }

    public void logInClick(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

}