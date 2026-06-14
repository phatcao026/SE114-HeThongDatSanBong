package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.ui.profile.ProfileActivity;
import com.example.timsanbong.ui.profile.ProfileViewModel;
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
    private TextView tvUpcomingFieldName;
    private TextView tvUpcomingTime;
    private TextView tvHotMatchAvatar;
    private TextView tvHotMatchScore;
    private TextView tvHotMatchTeam;
    private TextView tvHotMatchTime;
    private TextView tvHotMatchLevel;
    private TextView tvHotMatchLocation;
    private MaterialCardView btnNotifications;
    private MaterialButton btnSearchNearby;
    private MaterialButton btnMatchmaking;
    private MaterialButton btnHotMatchAccept;
    private MaterialButton btnFloatingBook;
    private TextView tvHotSeeAll;
    private TextView tvSuggestedMore;
    private RecyclerView rvSuggestedFields;

    private FieldViewModel fieldViewModel;
    private BookingViewModel bookingViewModel;
    private ChatViewModel chatViewModel;
    private MatchViewModel matchViewModel;
    private ProfileViewModel profileViewModel;
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
        tvUpcomingFieldName = findViewById(R.id.tvUpcomingFieldName);
        tvUpcomingTime = findViewById(R.id.tvUpcomingTime);
        tvHotMatchAvatar = findViewById(R.id.tvHotMatchAvatar);
        tvHotMatchScore = findViewById(R.id.tvHotMatchScore);
        tvHotMatchTeam = findViewById(R.id.tvHotMatchTeam);
        tvHotMatchTime = findViewById(R.id.tvHotMatchTime);
        tvHotMatchLevel = findViewById(R.id.tvHotMatchLevel);
        tvHotMatchLocation = findViewById(R.id.tvHotMatchLocation);
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
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        bindSessionAvatar();
        observeProfile();
        observeBookings();
        observeConversations();
        observeMatches();
        observeFields();

        profileViewModel.loadProfile();
        bookingViewModel.loadMyBookings();
        chatViewModel.loadConversations();
        matchViewModel.loadMatchPosts(0, 50, null);
        fieldViewModel.loadFields();
    }

    private void bindSessionAvatar() {
        try {
            JSONObject userJson = new JSONObject(sessionManager.getUserJson());
            String fullName = userJson.optString("fullName", "User");
            if (!fullName.isEmpty()) {
                tvAvatar.setText(fullName.substring(0, 1).toUpperCase());
            }
        } catch (Exception e) {
            tvAvatar.setText("U");
        }
    }

    private void observeProfile() {
        profileViewModel.profileState.observe(this, resource -> {
            if (resource == null || resource.status != com.example.timsanbong.utils.Resource.Status.SUCCESS || resource.data == null) {
                return;
            }
            String fullName = resource.data.getFullName();
            if (fullName != null && !fullName.isEmpty()) {
                tvAvatar.setText(fullName.substring(0, 1).toUpperCase());
            }
            tvTrustScore.setText(String.valueOf(resource.data.getTrustScore()));
        });
    }

    private void observeBookings() {
        bookingViewModel.bookingsState.observe(this, resource -> {
            if (resource == null || resource.status != com.example.timsanbong.utils.Resource.Status.SUCCESS || resource.data == null) {
                tvUpcomingCount.setText("0");
                return;
            }

            List<Booking> upcoming = new ArrayList<>();
            for (Booking booking : resource.data) {
                if (isUpcomingBooking(booking)) {
                    upcoming.add(booking);
                }
            }
            tvUpcomingCount.setText(String.valueOf(upcoming.size()));
            if (upcoming.isEmpty()) {
                tvUpcomingFieldName.setText("");
                tvUpcomingTime.setText("");
            } else {
                Booking nextBooking = upcoming.get(0);
                tvUpcomingFieldName.setText(nextBooking.getFieldName());
                tvUpcomingTime.setText(formatBookingTime(nextBooking));
            }
        });
    }

    private void observeConversations() {
        chatViewModel.conversationsState.observe(this, resource -> {
            if (resource == null || resource.status != com.example.timsanbong.utils.Resource.Status.SUCCESS || resource.data == null) {
                tvMessageCount.setText("0");
                return;
            }
            int unreadCount = 0;
            for (Conversation conversation : resource.data) {
                unreadCount += conversation.getUnreadCount();
            }
            tvMessageCount.setText(String.valueOf(unreadCount));
        });
    }

    private void observeMatches() {
        matchViewModel.matchPostsState.observe(this, resource -> {
            if (resource == null || resource.status != com.example.timsanbong.utils.Resource.Status.SUCCESS || resource.data == null || resource.data.isEmpty()) {
                bindHotMatch(null);
                return;
            }
            bindHotMatch(resource.data.get(0));
        });
    }

    private void observeFields() {
        fieldViewModel.filteredFields.observe(this, fields -> {
            if (fields == null) return;
            List<SuggestedFieldItem> suggestedFields = new ArrayList<>();
            for (Field field : fields) {
                float rating = field.getAverageRating() != null ? field.getAverageRating().floatValue() : 0f;
                suggestedFields.add(new SuggestedFieldItem(
                        field.getId(),
                        field.getName(),
                        field.getImageUrl(),
                        field.getFieldType() != null ? field.getFieldType() : "San 5",
                        rating,
                        "",
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
    }

    private void bindHotMatch(MatchPost match) {
        boolean hasMatch = match != null;
        btnHotMatchAccept.setEnabled(hasMatch);
        tvHotMatchAvatar.setText(hasMatch ? match.getCaptainInitials() : "");
        tvHotMatchScore.setText(hasMatch ? String.valueOf(match.getTrustScore()) : "");
        tvHotMatchTeam.setText(hasMatch ? match.getTeam() : "");
        tvHotMatchTime.setText(hasMatch ? match.getDate() + " " + match.getTime() : "");
        tvHotMatchLevel.setText(hasMatch ? match.getLevel() : "");
        tvHotMatchLocation.setText(hasMatch ? match.getField() : "");
    }

    private boolean isUpcomingBooking(Booking booking) {
        String status = booking.getStatus() == null ? "" : booking.getStatus().toUpperCase();
        return !"CANCELLED".equals(status) && !"COMPLETED".equals(status);
    }

    private String formatBookingTime(Booking booking) {
        String date = booking.getBookingDate() != null ? booking.getBookingDate() : "";
        String start = booking.getStartTime();
        String end = booking.getEndTime();
        if (start.isEmpty() && end.isEmpty()) {
            return date;
        }
        return date + " " + start + "-" + end;
    }
}
