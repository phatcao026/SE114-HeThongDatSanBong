package com.example.timsanbong.ui.customer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.ChatMessage;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    // ── Views ────────────────────────────────────────────
    private View viewChatAvatarBg;
    private TextView tvChatInitials;
    private View viewChatOnlineDot;
    private TextView tvChatName;
    private TextView tvChatStatus;
    private RecyclerView rvMessages;
    private EditText etChatInput;
    private ImageView ivSend;

    // ── Data ─────────────────────────────────────────────
    private ChatMessageAdapter chatAdapter;
    private Conversation conversation;
    private ChatViewModel chatViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_chat);
        conversation = (Conversation) getIntent().getSerializableExtra(Constants.EXTRA_CONVERSATION);
        initViews();
        setupListeners();
        loadData();
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
        chatAdapter = new ChatMessageAdapter(new ArrayList<>());
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

        tvChatName.setText(conversation.getName());
        tvChatInitials.setText(conversation.getInitials());
        tvChatStatus.setText(conversation.isOnline()
                ? getString(R.string.chat_status_online)
                : getString(R.string.chat_status_recent));

        if (conversation.isOnline()) {
            viewChatOnlineDot.setVisibility(View.VISIBLE);
        }

        android.graphics.drawable.GradientDrawable avatarBg =
                (android.graphics.drawable.GradientDrawable) viewChatAvatarBg.getBackground().mutate();
        avatarBg.setColor(ContextCompat.getColor(this, R.color.primary));

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.messagesState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS && resource.data != null) {
                List<ChatMessage> messages = resource.data;
                if (messages.isEmpty()) {
                    messages = buildMockMessages();
                }
                for (ChatMessage msg : messages) {
                    chatAdapter.addMessage(msg);
                }
                scrollToBottom();
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                List<ChatMessage> messages = buildMockMessages();
                for (ChatMessage msg : messages) {
                    chatAdapter.addMessage(msg);
                }
                scrollToBottom();
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

        long convId = 1; // Assuming conversation object has getId as String, we parse or use default
        try {
            convId = Long.parseLong(conversation.getId());
        } catch (Exception ignored) {}
        
        chatViewModel.loadMessages(convId, 0, 50);
    }

    private void sendMessage() {
        String text = etChatInput.getText().toString().trim();
        if (text.isEmpty()) return;

        com.example.timsanbong.data.model.MessageRequest req = new com.example.timsanbong.data.model.MessageRequest();
        long convId = 1;
        try {
            convId = Long.parseLong(conversation.getId());
        } catch (Exception ignored) {}
        req.setConversationId(convId);
        req.setContent(text);
        
        chatViewModel.sendMessage(req);
    }

    private void scrollToBottom() {
        int count = chatAdapter.getItemCount();
        if (count > 0) rvMessages.scrollToPosition(count - 1);
    }

    private List<ChatMessage> buildMockMessages() {
        List<ChatMessage> list = new ArrayList<>();
        list.add(new ChatMessage(ChatMessage.SIDE_SYSTEM,
                "Bạn và " + (conversation != null ? conversation.getName() : "") + " đã kết nối qua Bắt kèo",
                ""));
        list.add(new ChatMessage(ChatMessage.SIDE_THEM,
                "Ê mình muốn xác nhận lịch ngày mai nhé", "16:00"));
        list.add(new ChatMessage(ChatMessage.SIDE_ME,
                "Ok, mình sẽ có mặt đúng giờ nhé!", "16:01"));
        list.add(new ChatMessage(ChatMessage.SIDE_THEM,
                "Vị trí sân ở đâu bạn nhỉ? Mình lần đầu đến Sân Thái Mỹ", "16:02"));
        list.add(new ChatMessage(ChatMessage.SIDE_ME,
                "Mình sẽ pin map cho. Đậu xe cổng phụ bên hông nhé", "16:03"));
        list.add(new ChatMessage(ChatMessage.SIDE_THEM,
                "Cảm ơn nhiều nha! Hẹn gặp ngày mai", "16:04"));
        return list;
    }
}
