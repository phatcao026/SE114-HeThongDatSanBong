package com.example.timsanbong.ui.customer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.ChatMessage;
import com.example.timsanbong.data.model.Conversation;
import com.example.timsanbong.data.model.MessageRequest;
import com.example.timsanbong.data.repository.ChatRepository;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository = new ChatRepository();

    private final MutableLiveData<Resource<List<Conversation>>> _conversationsState = new MutableLiveData<>();
    public LiveData<Resource<List<Conversation>>> conversationsState = _conversationsState;

    private final MutableLiveData<Resource<List<ChatMessage>>> _messagesState = new MutableLiveData<>();
    public LiveData<Resource<List<ChatMessage>>> messagesState = _messagesState;

    private final MutableLiveData<Resource<ChatMessage>> _sendMessageState = new MutableLiveData<>();
    public LiveData<Resource<ChatMessage>> sendMessageState = _sendMessageState;

    public ChatViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadConversations() {
        _conversationsState.setValue(Resource.loading(null));
        chatRepository.getConversations(getApplication(), new RepositoryCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> data) {
                _conversationsState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _conversationsState.postValue(Resource.error(message, null));
            }
        });
    }

    public void loadMessages(long conversationId, int page, int size) {
        _messagesState.setValue(Resource.loading(null));
        chatRepository.getMessages(getApplication(), conversationId, new RepositoryCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> data) {
                _messagesState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _messagesState.postValue(Resource.error(message, null));
            }
        });
    }

    public void sendMessage(MessageRequest request) {
        _sendMessageState.setValue(Resource.loading(null));
        chatRepository.sendMessage(getApplication(), request, new RepositoryCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage data) {
                _sendMessageState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _sendMessageState.postValue(Resource.error(message, null));
            }
        });
    }
}
