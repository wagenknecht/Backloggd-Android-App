package de.wagenknecht.backloggd;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        // Optional: Add a back button to the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Optional: Handle the action bar's back button press
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}