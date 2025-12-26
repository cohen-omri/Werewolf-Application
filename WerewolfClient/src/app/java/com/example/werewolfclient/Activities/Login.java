package com.example.werewolfclient.Activities;

import static com.example.werewolfclient.Objects.SocketManager.gson;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.werewolfclient.ClientObjects.LoginObj;
import com.example.werewolfclient.Objects.SocketManager;
import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.LoginObject;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button button = findViewById(R.id.loginBtn);
        EditText username = findViewById(R.id.usernameEdt);
        EditText password = findViewById(R.id.passwordEdt);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                button.setClickable(false);
                String un = username.getText().toString();
                String p = password.getText().toString();

                if (un.isEmpty() || p.isEmpty())
                    Toast.makeText(Login.this, "Please fill all categories", Toast.LENGTH_SHORT).show();

                else {
                    LoginObj u = new LoginObj(un, p, 2);

                    Thread t = new Thread(() -> {
                        loginUser(u);
                    });
                    t.start();
                }
            }
        });
    }

    public void loginUser(LoginObj u) {

        Object response = SocketManager.sendAndGetResponse(u);
        if (response == null) {
            runOnUiThread(() -> {
                Toast.makeText(Login.this, "Problem connecting to server", Toast.LENGTH_SHORT).show();
            });
            System.out.println("Problem connecting to server");
            findViewById(R.id.loginBtn).setClickable(true);
            return;
        }

        LoginObject obj = gson.fromJson(response.toString(), LoginObject.class);
        System.out.println(obj.toString() + " --- " + obj.getUuid());
        if (obj.isAck()) {
            runOnUiThread(() -> {
                Toast.makeText(Login.this, "Succesfully logged in", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("userObject", obj);
                startActivity(intent);
                finish();
            });

        } else
            runOnUiThread(() -> {
                switch (obj.getMsg()) {
                    case -1:
                        Toast.makeText(Login.this, "There was an unexpected problem", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(Login.this, "This user does not exist", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(Login.this, "Password does not match the username", Toast.LENGTH_SHORT).show();
                        break;
                    case 8:
                        Toast.makeText(Login.this, "User is already connected", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(Login.this, "There was an unexpected problem", Toast.LENGTH_SHORT).show();
                        break;
                }

                findViewById(R.id.loginBtn).setClickable(true);
            });
    }

    public void signUpClick(View view) {
        Intent intent = new Intent(this, Signup.class);
        startActivity(intent);
        finish();
    }
}