package com.example.timsanbong.ui.customer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MessageRequest;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.SessionManager;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private View viewChatAvatarBg;
    private TextView tvChatInitials;
    private View viewChatOnlineDot;
    private TextView tvChatName;
    private TextView tvChatStatus;
    private RecyclerView rvMessages;
    private EditText etChatInput;
    private ImageView ivSend;

    private ChatMessageAdapter chatAdapter;
    private Conversation conversation;
    private ChatViewModel chatViewModel;
    private com.example.timsanbong.service.WebSocketService webSocketService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_chat);
        conversation = (Conversation) getIntent().getSerializableExtra(Constants.EXTRA_CONVERSATION);
        initViews();
        setupListeners();
        setupWebSocket();
        loadData();
    }

    private void setupWebSocket() {
        webSocketService = com.example.timsanbong.service.WebSocketService.getInstance(this);
        webSocketService.connect(this);
        webSocketService.setCallback((destination, body) -> {
            if (destination.contains("/topic/conversations/" + getConversationId())) {
                try {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    com.example.timsanbong.data.model.ChatMessage message = 
                            gson.fromJson(body, com.example.timsanbong.data.model.ChatMessage.class);
                    
                    runOnUiThread(() -> {
                        if (message.getSenderId() != new SessionManager(this).getUserId()) {
                            chatAdapter.addMessage(message);
                            scrollToBottom();
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e("ChatActivity", "Error parsing websocket message", e);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketService != null) {
            webSocketService.setCallback(null);
        }
    }

    private void initViews() {
        viewChatAvatarBg = findViewById(R.id.viewChatAvatarBg);
        tvChatInitials = findViewById(R.id.tvChatInitials);
        viewChatOnlineDot = findViewById(R.id.viewChatOnlineDot);
        tvChatName = findViewById(R.id.tvChatName);
        tvChatStatus = findViewById(R.id.tvChatStatus);
        rvMessages = findViewById(R.id.rvMessages);
        etChatInput = findViewById(R.id.etChatInput);
        ivSend = findViewById(R.id.ivSend);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        chatAdapter = new ChatMessageAdapter(new ArrayList<>(), new SessionManager(this).getUserId());
        rvMessages.setAdapter(chatAdapter);

        findViewById(R.id.ivChatBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        ivSend.setOnClickListener(v -> sendMessage());
        etChatInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivSend.setAlpha(s.length() > 0 ? 1f : 0.4f);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        ivSend.setAlpha(0.4f);
    }

    private void loadData() {
        if (conversation == null) {
            finish();
            return;
        }

        try {
            String json = new SessionManager(this).getUserJson();
            if (json != null) {
                String currentUserName = new org.json.JSONObject(json).optString("fullName");
                if (!currentUserName.isEmpty()) {
                    conversation.setCurrentUserName(currentUserName);
                }
            }
        } catch (Exception ignored) {}

        tvChatName.setText(conversation.getName());
        tvChatInitials.setText(conversation.getInitials());
        tvChatStatus.setText(conversation.isOnline()
                ? getString(R.string.chat_status_online)
                : getString(R.string.chat_status_recent));
        viewChatOnlineDot.setVisibility(conversation.isOnline() ? View.VISIBLE : View.GONE);

        android.graphics.drawable.GradientDrawable avatarBg =
                (android.graphics.drawable.GradientDrawable) viewChatAvatarBg.getBackground().mutate();
        avatarBg.setColor(ContextCompat.getColor(this, R.color.primary));

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.messagesState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS && resource.data != null) {
                chatAdapter.updateMessages(resource.data);
                scrollToBottom();
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                chatAdapter.updateMessages(new ArrayList<>());
            }
        });

        chatViewModel.sendMessageState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS && resource.data != null) {
                chatAdapter.addMessage(resource.data);
                etChatInput.setText("");
                scrollToBottom();
            }
        });

        chatViewModel.loadMessages(getConversationId(), 0, 50);
    }

    private void sendMessage() {
        String text = etChatInput.getText().toString().trim();
        if (text.isEmpty()) return;

        MessageRequest request = new MessageRequest(getConversationId(), text);
        chatViewModel.sendMessage(request);
    }

    private long getConversationId() {
        try {
            return Long.parseLong(conversation.getId());
        } catch (Exception ignored) {
            return -1;
        }
    }

    private void scrollToBottom() {
        rvMessages.post(() -> {
            int count = chatAdapter.getItemCount();
            if (count > 0) {
                rvMessages.scrollToPosition(count - 1);
            }
        });
    }
}
