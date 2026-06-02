package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.NavBarManager;

import java.util.ArrayList;
import java.util.List;

public class MatchmakingActivity extends AppCompatActivity {

    // ── Views ────────────────────────────────────────────
    private TextView tabAll, tabOpponent, tabMember, tabSuggested;
    private TextView tvMatchCount;
    private RecyclerView rvMatches;
    private TextView tvEmptyMatches;

    // ── Data ─────────────────────────────────────────────
    private MatchAdapter matchAdapter;
    private List<MatchPost> allMatches;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matchmaking);
        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        tabAll = findViewById(R.id.tabAll);
        tabOpponent = findViewById(R.id.tabOpponent);
        tabMember = findViewById(R.id.tabMember);
        tabSuggested = findViewById(R.id.tabSuggested);
        tvMatchCount = findViewById(R.id.tvMatchCount);
        rvMatches = findViewById(R.id.rvMatches);
        tvEmptyMatches = findViewById(R.id.tvEmptyMatches);

        rvMatches.setLayoutManager(new LinearLayoutManager(this));
        matchAdapter = new MatchAdapter(new ArrayList<>(), new MatchAdapter.OnMatchActionListener() {
            @Override
            public void onAccept(MatchPost match, int position) {
                match.setAccepted(true);
                matchAdapter.updateMatches(getFilteredList(currentTab));
            }

            @Override
            public void onChat(MatchPost match) {
                Conversation conv = new Conversation(
                        match.getId(),
                        match.getTeam(),
                        match.getCaptainInitials(),
                        "Bắt đầu cuộc trò chuyện…",
                        "Vừa xong",
                        0,
                        match.getTypeLabel() + " • " + match.getField(),
                        false);
                Intent intent = new Intent(MatchmakingActivity.this, ChatActivity.class);
                intent.putExtra(Constants.EXTRA_CONVERSATION, conv);
                startActivity(intent);
            }

            @Override
            public void onCardClick(MatchPost match) {
                Intent intent = new Intent(MatchmakingActivity.this, MatchDetailActivity.class);
                intent.putExtra(Constants.EXTRA_MATCH, match);
                startActivity(intent);
            }
        });
        rvMatches.setAdapter(matchAdapter);

        new NavBarManager(this, NavBarManager.ITEM_MATCH).setup();
    }

    private void setupListeners() {
        View.OnClickListener tabClick = v -> {
            int id = v.getId();
            if (id == R.id.tabAll) selectTab(0);
            else if (id == R.id.tabOpponent) selectTab(1);
            else if (id == R.id.tabMember) selectTab(2);
            else selectTab(3);
        };
        tabAll.setOnClickListener(tabClick);
        tabOpponent.setOnClickListener(tabClick);
        tabMember.setOnClickListener(tabClick);
        tabSuggested.setOnClickListener(tabClick);

        findViewById(R.id.ivNotifications).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));
    }

    private void loadData() {
        allMatches = buildMockMatches();
        selectTab(0);
    }

    private void selectTab(int tab) {
        currentTab = tab;
        TextView[] tabs = {tabAll, tabOpponent, tabMember, tabSuggested};
        for (int i = 0; i < tabs.length; i++) {
            boolean active = i == tab;
            tabs[i].setBackgroundResource(active ? R.drawable.bg_segment_active : android.R.color.transparent);
            tabs[i].setTextColor(getColor(active ? R.color.text_on_primary : R.color.text_secondary));
        }

        List<MatchPost> filtered = getFilteredList(tab);
        matchAdapter.updateMatches(filtered);
        tvMatchCount.setText(String.format(getString(R.string.match_count), filtered.size()));
        tvEmptyMatches.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rvMatches.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private List<MatchPost> getFilteredList(int tab) {
        List<MatchPost> result = new ArrayList<>();
        for (MatchPost m : allMatches) {
            if (tab == 0) result.add(m);
            else if (tab == 1 && m.getType().equals(MatchPost.TYPE_FIND_OPPONENT)) result.add(m);
            else if (tab == 2 && m.getType().equals(MatchPost.TYPE_FIND_MEMBER)) result.add(m);
            else if (tab == 3 && m.getTrustScore() >= 90) result.add(m);
        }
        return result;
    }

    private List<MatchPost> buildMockMatches() {
        List<MatchPost> list = new ArrayList<>();
        list.add(new MatchPost("1", "Bão Đông FC", "Nguyễn Văn A", "BĐ", 92, "Amateur",
                MatchPost.TYPE_FIND_OPPONENT, "Tìm đối",
                "Sân Thái Mỹ - Q1", "Thứ 7 - 14/06", "18:00-19:30", "420.000đ/người",
                "Tụi mình cần 1 đội khoảng 7 người, level trung bình, chơi giao hữu thân thiện nhé!",
                "3/7", "2 phút", true));
        list.add(new MatchPost("2", "Cá Sấu United", "Trần Thị B", "CU", 78, "Intermediate",
                MatchPost.TYPE_FIND_MEMBER, "Tìm cầu thủ",
                "Sân Trần Bình - Bình Thạnh", "CN - 15/06", "06:00-07:30", "380.000đ/người",
                "Cần 3 cầu thủ chạy cánh, ưu tiên có kinh nghiệm thi đấu phong trào.",
                "4/11", "15 phút", false));
        list.add(new MatchPost("3", "Thủ Đức All-Stars", "Lê Văn C", "TĐ", 96, "Amateur",
                MatchPost.TYPE_FIND_OPPONENT, "Tìm đối",
                "Sân Thủ Đức Sport - Thủ Đức", "Thứ 7 - 14/06", "15:00-16:30", "500.000đ/người",
                "Đội chúng mình toàn dân Thủ Đức, chơi đều đặn cuối tuần. Mời đội bạn thử sức!",
                "5/7", "30 phút", true));
        list.add(new MatchPost("4", "FC Hậu Vệ", "Phạm Quốc D", "HV", 85, "Amateur",
                MatchPost.TYPE_FIND_OPPONENT, "Tìm đối",
                "Sân Bình Dương FC - Bình Dương", "Thứ 6 - 13/06", "20:00-21:30", "350.000đ/người",
                "Cần đối thủ cho buổi tập tối thứ 6, mọi level đều welcome!",
                "6/7", "1 giờ", false));
        return list;
    }
}
