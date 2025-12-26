package com.example.werewolfclient.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.werewolfclient.R;
import com.example.werewolfclient.ServerObjects.LobbyObject;
import com.example.werewolfclient.ServerObjects.LoginObject;
import com.example.werewolfclient.ServerObjects.ResultsObject;

public class MainActivity extends AppCompatActivity {

    static Fragment lastFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load HomeFragment by default or LobbyFragment if resultsObject is present
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                if (intent.hasExtra("userObject")) {
                    LoginObject loginObject = (LoginObject) intent.getSerializableExtra("userObject");
                    HomeFragment homeFragment = new HomeFragment();
                    Bundle args = new Bundle();

                    args.putSerializable("loginObject", loginObject);
                    homeFragment.setArguments(args);
                    loadFragment(homeFragment);
                    lastFragment = homeFragment;
                }
                else if (intent.hasExtra("lobbyObject")) {
                    LobbyObject lobbyObject = (LobbyObject) intent.getSerializableExtra("lobbyObject");
                    LobbyFragment lobbyFragment = new LobbyFragment();
                    Bundle args = new Bundle();

                    args.putSerializable("lobbyObject", lobbyObject);
                    lobbyFragment.setArguments(args);
                    loadFragment(lobbyFragment);
                }
                else if (intent.hasExtra("joinGame"))
                    loadFragment(new JoinGameFragment());
            } else {
                Intent welcomeIntent = new Intent(this, Welcome.class);
                startActivity(welcomeIntent);
                finish();
            }
        }
    }

    // Navbar click handlers
    public void rulesClick(View view) {
        loadFragment(new RulesFragment());
    }

    public void uInfoClick(View view) {
        loadFragment(new UserInfoFragment());
    }

    public void editUserClick(View view) {
        loadFragment(new EditUserFragment());
    }

    // Helper method to load a fragment
    private void loadFragment(Fragment fragment) {
        lastFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit(); // Removed addToBackStack(null) as per your previous request
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof RulesFragment ||
                currentFragment instanceof UserInfoFragment ||
                currentFragment instanceof EditUserFragment)
            loadFragment(lastFragment);
        else
            super.onBackPressed();

    }
}