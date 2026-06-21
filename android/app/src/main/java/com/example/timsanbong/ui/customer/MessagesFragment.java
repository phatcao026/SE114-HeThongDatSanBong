package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {

    private EditText etSearch;
    private RecyclerView rvConversations;
    private TextView tvEmptyMessages;

    private ConversationAdapter conversationAdapter;
    private List<Conversation> allConversations = new ArrayList<>();
    private ChatViewModel chatViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_customer_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
        loadData();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearchConversation);
        rvConversations = view.findViewById(R.id.rvConversations);
        tvEmptyMessages = view.findViewById(R.id.tvEmptyMessages);

        // Handle bottom navigation visibility depending on hosting activity
        View bottomNav = view.findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            if (getActivity() instanceof CustomerMainActivity) {
                bottomNav.setVisibility(View.GONE);
            } else {
                bottomNav.setVisibility(View.VISIBLE);
                new com.example.timsanbong.utils.NavBarManager(getActivity(), com.example.timsanbong.utils.NavBarManager.ITEM_MESSAGES).setup();
            }
        }

        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        conversationAdapter = new ConversationAdapter(new ArrayList<>(), conv -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra(Constants.EXTRA_CONVERSATION, conv);
            startActivity(intent);
        });
        rvConversations.setAdapter(conversationAdapter);
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
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.conversationsState.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == com.example.timsanbong.utils.Resource.Status.SUCCESS && resource.data != null) {
                allConversations = resource.data;
                showConversations(allConversations);
            } else if (resource.status == com.example.timsanbong.utils.Resource.Status.ERROR) {
                allConversations = new ArrayList<>();
                showConversations(allConversations);
            }
        });
        chatViewModel.loadConversations();
    }

    private void filterConversations(String query) {
        if (query.isEmpty()) {
            showConversations(allConversations);
            return;
        }

        List<Conversation> filtered = new ArrayList<>();
        String lower = query.toLowerCase();
        for (Conversation conversation : allConversations) {
            String name = conversation.getName();
            String lastMsg = conversation.getLastMessage();
            if ((name != null && name.toLowerCase().contains(lower))
                    || (lastMsg != null && lastMsg.toLowerCase().contains(lower))) {
                filtered.add(conversation);
            }
        }
        showConversations(filtered);
    }

    private void showConversations(List<Conversation> conversations) {
        String currentUserName = null;
        try {
            String json = new SessionManager(requireContext()).getUserJson();
            if (json != null) {
                currentUserName = new org.json.JSONObject(json).optString("fullName");
            }
        } catch (Exception ignored) {}

        if (currentUserName != null) {
            for (Conversation c : conversations) {
                c.setCurrentUserName(currentUserName);
            }
        }

        conversationAdapter.updateConversations(conversations);
        tvEmptyMessages.setVisibility(conversations.isEmpty() ? View.VISIBLE : View.GONE);
        rvConversations.setVisibility(conversations.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
