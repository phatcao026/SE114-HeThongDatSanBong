package com.example.timsanbong.ui.customer;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.utils.NavBarManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

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
    private SuggestedFieldAdapter suggestedFieldAdapter;
    private NavBarManager navBarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        navBarManager = new NavBarManager(this, NavBarManager.ITEM_HOME);
        navBarManager.setup();
    }

    private void setupListeners() {
        btnNotifications.setOnClickListener(v -> showToast(R.string.notifications_title));
        btnSearchNearby.setOnClickListener(v -> showToast(R.string.action_search_nearby));
        btnMatchmaking.setOnClickListener(v -> showToast(R.string.matchmaking_title));
        btnHotMatchAccept.setOnClickListener(v -> showToast(R.string.action_accept_match));
        btnFloatingBook.setOnClickListener(v -> showToast(R.string.toast_select_field));
        tvHotSeeAll.setOnClickListener(v -> showToast(R.string.action_view_all));
        tvSuggestedMore.setOnClickListener(v -> showToast(R.string.action_view_more));
    }

    private void loadData() {
        tvAvatar.setText(getString(R.string.mock_avatar_initials));
        tvUpcomingCount.setText(getString(R.string.mock_upcoming_count));
        tvTrustScore.setText(getString(R.string.mock_trust_score));
        tvMessageCount.setText(getString(R.string.mock_message_count));
        tvHotMatchAvatar.setText(getString(R.string.mock_hot_match_initials));
        tvHotMatchScore.setText(getString(R.string.mock_hot_match_score));

        List<SuggestedFieldItem> suggestedFields = new ArrayList<>();
        suggestedFields.add(new SuggestedFieldItem(
                1,
                getString(R.string.mock_field_name_1),
                "https://via.placeholder.com/300",
                getString(R.string.suggested_field_type_7),
                4.8f,
                "2.3km",
                true));
        suggestedFields.add(new SuggestedFieldItem(
                2,
                getString(R.string.mock_field_name_2),
                "https://via.placeholder.com/300",
                getString(R.string.suggested_field_type_5),
                4.6f,
                "3.1km",
                false));
        suggestedFields.add(new SuggestedFieldItem(
                3,
                getString(R.string.mock_field_name_3),
                "https://via.placeholder.com/300",
                getString(R.string.suggested_field_type_7),
                4.9f,
                "1.2km",
                true));

        suggestedFieldAdapter = new SuggestedFieldAdapter(suggestedFields, item ->
                showToast(R.string.toast_select_field));
        rvSuggestedFields.setAdapter(suggestedFieldAdapter);
    }

    private void showToast(int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }
}
