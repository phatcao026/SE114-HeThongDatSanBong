package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.ui.profile.ProfileActivity;
import com.example.timsanbong.utils.NavBarManager;
import com.example.timsanbong.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvAvatar;
    private TextView tvUpcomingCount;
    private TextView tvTrustScore;
    private TextView tvMessageCount;
    private TextView tvHotMatchAvatar;
    private TextView tvHotMatchScore;
    private MaterialCardView btnNotifications;
    private MaterialButton btnSearchNearby;
    private MaterialButton btnMatchmaking;
    private MaterialButton btnHotMatchAccept;
    private MaterialButton btnFloatingBook;
    private TextView tvHotSeeAll;
    private TextView tvSuggestedMore;
    private RecyclerView rvSuggestedFields;
    private FieldViewModel fieldViewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);
        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tvAvatar);
        tvUpcomingCount = findViewById(R.id.tvUpcomingCount);
        tvTrustScore = findViewById(R.id.tvTrustScore);
        tvMessageCount = findViewById(R.id.tvMessageCount);
        tvHotMatchAvatar = findViewById(R.id.tvHotMatchAvatar);
        tvHotMatchScore = findViewById(R.id.tvHotMatchScore);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnSearchNearby = findViewById(R.id.btnSearchNearby);
        btnMatchmaking = findViewById(R.id.btnMatchmaking);
        btnHotMatchAccept = findViewById(R.id.btnHotMatchAccept);
        btnFloatingBook = findViewById(R.id.btnFloatingBook);
        tvHotSeeAll = findViewById(R.id.tvHotSeeAll);
        tvSuggestedMore = findViewById(R.id.tvSuggestedMore);
        rvSuggestedFields = findViewById(R.id.rvSuggestedFields);

        rvSuggestedFields.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        new NavBarManager(this, NavBarManager.ITEM_HOME).setup();
    }

    private void setupListeners() {
        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));
        tvAvatar.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
        btnSearchNearby.setOnClickListener(v ->
                startActivity(new Intent(this, FindPitchActivity.class)));
        btnMatchmaking.setOnClickListener(v ->
                startActivity(new Intent(this, MatchmakingActivity.class)));
        btnHotMatchAccept.setOnClickListener(v ->
                startActivity(new Intent(this, MatchmakingActivity.class)));
        btnFloatingBook.setOnClickListener(v ->
                startActivity(new Intent(this, FindPitchActivity.class)));
        tvHotSeeAll.setOnClickListener(v ->
                startActivity(new Intent(this, MatchmakingActivity.class)));
        tvSuggestedMore.setOnClickListener(v ->
                startActivity(new Intent(this, FindPitchActivity.class)));
    }

    private void loadData() {
        sessionManager = new SessionManager(this);
        fieldViewModel = new ViewModelProvider(this).get(FieldViewModel.class);

        try {
            JSONObject userJson = new JSONObject(sessionManager.getUserJson());
            String fullName = userJson.optString("fullName", "User");
            if (!fullName.isEmpty()) {
                tvAvatar.setText(fullName.substring(0, 1).toUpperCase());
            }
        } catch (Exception e) {
            tvAvatar.setText("U");
        }

        tvUpcomingCount.setText("2");
        tvTrustScore.setText("98");
        tvMessageCount.setText("1");
        tvHotMatchAvatar.setText("A");
        tvHotMatchScore.setText("4.5");

        fieldViewModel.filteredFields.observe(this, fields -> {
            if (fields == null) return;
            List<SuggestedFieldItem> suggestedFields = new ArrayList<>();
            for (Field field : fields) {
                suggestedFields.add(new SuggestedFieldItem(
                        field.getId(),
                        field.getName(),
                        field.getImageUrl(),
                        field.getFieldType() != null ? field.getFieldType() : "Sân 5",
                        4.5f,
                        "2.0km",
                        field.isAvailable()
                ));
            }
            SuggestedFieldAdapter suggestedFieldAdapter = new SuggestedFieldAdapter(suggestedFields, item -> {
                Intent intent = new Intent(MainActivity.this, FieldDetailActivity.class);
                intent.putExtra("fieldId", item.getId());
                startActivity(intent);
            });
            rvSuggestedFields.setAdapter(suggestedFieldAdapter);
        });

        fieldViewModel.loadFields();
    }
}
