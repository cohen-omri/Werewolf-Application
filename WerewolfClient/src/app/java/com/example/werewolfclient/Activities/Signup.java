package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Objects.SocketManager.gson;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.werewolfclient.ClientObjects.LoginObj;
import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.SignupObject;

public class Signup extends AppCompatActivity {

    // min max length
    static int minLength = 4;
    static int maxLength = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);


        Button button = findViewById(R.id.signupBtn);
        EditText username = findViewById(R.id.usernameEdt);
        EditText password = findViewById(R.id.passwordEdt);
        EditText cpassword = findViewById(R.id.cPasswordEdt);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String un = username.getText().toString();
                String p = password.getText().toString();
                String cp = cpassword.getText().toString();

                if (un.isEmpty() || p.isEmpty() || cp.isEmpty())
                    Toast.makeText(Signup.this, "Please fill all categories", Toast.LENGTH_SHORT).show();

                else if (!p.equals(cp))
                    Toast.makeText(Signup.this, "Plese confirm your password", Toast.LENGTH_LONG).show();

                else {
                    LoginObj u = new LoginObj(un, p, 1);

                    Thread t = new Thread(() -> {
                        signupUser(u);
                    });
                    t.start();
                }
            }
        });
    }

    public void signupUser(LoginObj u) {

        Object response = SocketManager.sendAndGetResponse(u);
        if (response == null) {
            runOnUiThread(() -> {
                Toast.makeText(Signup.this, "Problem connecting to server", Toast.LENGTH_SHORT).show();
            });
            System.out.println("Problem connecting to server");
            return;
        }

        SignupObject obj = gson.fromJson(response.toString(), SignupObject.class);
        System.out.println(obj.toString());
        if (obj.isAck()) {
            runOnUiThread(() -> {
                Toast.makeText(Signup.this, "Succesfully signed up", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
            });

        }

        else
            runOnUiThread(() -> {
                switch (obj.getMsg()) {
                    case 1:
                        Toast.makeText(Signup.this, "Username must be longer than " + minLength + " characters", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(Signup.this, "Username must be shorter than " + maxLength + " characters", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(Signup.this, "Password must be longer than " + minLength + " characters", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(Signup.this, "Password must be shorter than " + maxLength + " characters", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(Signup.this, "There already exists a user with this name", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(Signup.this, "There was an unexpected problem", Toast.LENGTH_SHORT).show();
                        break;
                }
            });
    }
    public void logInClick(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }
}