package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.NavBarManager;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    // ── Views ────────────────────────────────────────────
    private EditText etSearch;
    private RecyclerView rvConversations;
    private TextView tvEmptyMessages;

    // ── Data ─────────────────────────────────────────────
    private ConversationAdapter conversationAdapter;
    private List<Conversation> allConversations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        initViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearchConversation);
        rvConversations = findViewById(R.id.rvConversations);
        tvEmptyMessages = findViewById(R.id.tvEmptyMessages);

        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        conversationAdapter = new ConversationAdapter(new ArrayList<>(), conv -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(Constants.EXTRA_CONVERSATION, conv);
            startActivity(intent);
        });
        rvConversations.setAdapter(conversationAdapter);

        new NavBarManager(this, NavBarManager.ITEM_MESSAGES).setup();
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterConversations(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadData() {
        allConversations = buildMockConversations();
        showConversations(allConversations);
    }

    private void filterConversations(String query) {
        if (query.isEmpty()) {
            showConversations(allConversations);
            return;
        }
        List<Conversation> filtered = new ArrayList<>();
        String lower = query.toLowerCase();
        for (Conversation c : allConversations) {
            if (c.getName().toLowerCase().contains(lower)
                    || c.getLastMessage().toLowerCase().contains(lower)) {
                filtered.add(c);
            }
        }
        showConversations(filtered);
    }

    private void showConversations(List<Conversation> list) {
        conversationAdapter.updateConversations(list);
        tvEmptyMessages.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        rvConversations.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private List<Conversation> buildMockConversations() {
        List<Conversation> list = new ArrayList<>();
        list.add(new Conversation("1", "Bão Đông FC", "BĐ",
                "Thứ 7 nào sân Thái Mỹ nhé!", "2 phút", 3,
                "Tìm đối • Sân Thái Mỹ Q1", true));
        list.add(new Conversation("2", "Sân Trần Bình", "TB",
                "Còn slot 18:00 chủ nhật không bạn?", "15 phút", 0,
                null, false));
        list.add(new Conversation("3", "Phạm Quốc Khánh", "PK",
                "Ok mình sẽ tới sớm hơn", "1 giờ", 0,
                null, false));
        return list;
    }
}
