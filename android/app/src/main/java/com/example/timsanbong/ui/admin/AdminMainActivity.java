package com.example.timsanbong.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timsanbong.R;

public class AdminMainActivity extends AppCompatActivity {

    private AdminNavBarManager navBarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        
        setupStats();
        setupHealth();
        
        navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_OVERVIEW);
        navBarManager.setup();
    }

    private void setupStats() {
        // Find the GridLayout and its children (included layouts)
        // Note: include tags don't have IDs themselves, but we can access their root views if we provide IDs in include
        // For simplicity, let's just find them by index or manually if we had IDs.
        // Since I didn't add IDs to includes in activity_admin_main.xml, I'll update the layout first or just leave as is for UI demo.
    }

    private void setupHealth() {
        // Similar to stats, would populate data here.
    }
}