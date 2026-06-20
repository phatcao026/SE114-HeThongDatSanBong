package com.example.timsanbong.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
// removed unused import Toast

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.chat.ChatCreateRequest;
import com.example.timsanbong.data.chat.ChatMessage;
import com.example.timsanbong.data.chat.ChatResponse;
import com.example.timsanbong.network.ChatApiService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Bottom sheet that shows a simple chat UI and communicates with the backend via Retrofit.
 * Create with ChatBotBottomSheetFragment.newInstance(baseUrl, jwt, sessionId) where jwt may be null
 * and sessionId may be null (a new session will be generated client-side if missing).
 */
public class ChatBotBottomSheetFragment extends BottomSheetDialogFragment {
    private static final String ARG_BASE_URL = "arg_base_url";
    private static final String ARG_JWT = "arg_jwt";
    private static final String ARG_SESSION_ID = "arg_session_id";

    private String baseUrl;
    private String jwtToken;
    private String sessionId;

    private ChatAdapter adapter;
    private RecyclerView rvChat;
    private EditText etChatMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;

    public static ChatBotBottomSheetFragment newInstance(@NonNull String baseUrl, @Nullable String jwtToken, @Nullable String sessionId) {
        ChatBotBottomSheetFragment f = new ChatBotBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BASE_URL, baseUrl);
        args.putString(ARG_JWT, jwtToken);
        args.putString(ARG_SESSION_ID, sessionId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle a = getArguments();
        if (a != null) {
            baseUrl = a.getString(ARG_BASE_URL);
            jwtToken = a.getString(ARG_JWT);
            sessionId = a.getString(ARG_SESSION_ID);
        }
        if (TextUtils.isEmpty(baseUrl)) {
            baseUrl = "https://example.com"; // fallback, caller should provide real base url
        }
        if (TextUtils.isEmpty(sessionId)) {
            sessionId = UUID.randomUUID().toString();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_bot, container, false);
        rvChat = v.findViewById(R.id.rvChatMessages);
        etChatMessage = v.findViewById(R.id.etChatMessage);
        btnSend = v.findViewById(R.id.btnSendChat);
        progressBar = v.findViewById(R.id.progressChatLoading);

        adapter = new ChatAdapter();
        rvChat.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvChat.setAdapter(adapter);

        // Handle keyboard action send
        etChatMessage.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        btnSend.setOnClickListener(view -> sendMessage());

        // initial welcome message
        adapter.addMessage(new ChatMessage("Xin chào! Tôi có thể giúp gì cho bạn hôm nay?", false, System.currentTimeMillis()));
        scrollToBottom();

        return v;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSend.setEnabled(!loading);
        etChatMessage.setEnabled(!loading);
    }

    private void sendMessage() {
        final String raw = etChatMessage.getText() == null ? "" : etChatMessage.getText().toString().trim();
        if (TextUtils.isEmpty(raw)) return;

        // Add user message immediately
        ChatMessage userMsg = new ChatMessage(raw, true, System.currentTimeMillis());
        adapter.addMessage(userMsg);
        scrollToBottom();
        etChatMessage.setText("");

        // Prepare request
        ChatCreateRequest req = new ChatCreateRequest(raw, sessionId);

        setLoading(true);

        // Build Retrofit and send request
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ChatApiService api = retrofit.create(ChatApiService.class);
        String bearer = jwtToken == null ? "" : "Bearer " + jwtToken;
        Call<ChatResponse> call = api.ask(bearer, req);

        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse cr = response.body();
                    if (!TextUtils.isEmpty(cr.getSessionId())) {
                        // update local session id if backend returned a managed one
                        sessionId = cr.getSessionId();
                    }
                    String reply = cr.getReply() == null ? "(Không có phản hồi)" : cr.getReply();
                    adapter.addMessage(new ChatMessage(reply, false, System.currentTimeMillis()));
                } else {
                    adapter.addMessage(new ChatMessage("Xin lỗi, xảy ra lỗi khi xử lý yêu cầu. Vui lòng thử lại.", false, System.currentTimeMillis()));
                }
                scrollToBottom();
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                setLoading(false);
                adapter.addMessage(new ChatMessage("Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng và thử lại.", false, System.currentTimeMillis()));
                scrollToBottom();
            }
        });
    }

    private void scrollToBottom() {
        if (rvChat == null || adapter == null) return;
        rvChat.post(() -> rvChat.smoothScrollToPosition(adapter.getItemCount() - 1));
    }
}


