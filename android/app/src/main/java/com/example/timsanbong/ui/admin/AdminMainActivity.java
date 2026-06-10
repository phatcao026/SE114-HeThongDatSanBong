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
        
        findViewById(R.id.cvAdminAvatar).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });
        
        navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_OVERVIEW);
        navBarManager.setup();
    }

    private void setupStats() {
        View gmv = findViewById(R.id.cardGmv);
        ((ImageView) gmv.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_admin_wallet);
        ((TextView) gmv.findViewById(R.id.tvStatLabel)).setText("Doanh thu (GMV)");
        ((TextView) gmv.findViewById(R.id.tvStatValue)).setText("42.850.000đ");
        ((TextView) gmv.findViewById(R.id.tvTrendValue)).setText("+12.5%");

        View users = findViewById(R.id.cardUsers);
        ((ImageView) users.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_admin_users);
        ((TextView) users.findViewById(R.id.tvStatLabel)).setText("Người dùng");
        ((TextView) users.findViewById(R.id.tvStatValue)).setText("1.248");
        ((TextView) users.findViewById(R.id.tvTrendValue)).setText("+8.3%");

        View fields = findViewById(R.id.cardFields);
        ((ImageView) fields.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_admin_dashboard);
        ((TextView) fields.findViewById(R.id.tvStatLabel)).setText("Sân bóng");
        ((TextView) fields.findViewById(R.id.tvStatValue)).setText("156");
        ((TextView) fields.findViewById(R.id.tvTrendValue)).setText("+2.1%");

        View bookings = findViewById(R.id.cardBookings);
        ((ImageView) bookings.findViewById(R.id.ivStatIcon)).setImageResource(R.drawable.ic_bolt);
        ((TextView) bookings.findViewById(R.id.tvStatLabel)).setText("Lượt đặt");
        ((TextView) bookings.findViewById(R.id.tvStatValue)).setText("3.420");
        ((TextView) bookings.findViewById(R.id.tvTrendValue)).setText("+5.7%");
    }

    private void setupHealth() {
    }
}