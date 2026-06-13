package com.example.timsanbong.ui.customer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.AppNotification;
import com.example.timsanbong.data.model.PageResponse;
import com.example.timsanbong.data.repository.NotificationRepository;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;

import java.util.List;

public class NotificationViewModel extends AndroidViewModel {

    private final NotificationRepository notificationRepository = new NotificationRepository();

    private final MutableLiveData<Resource<List<AppNotification>>> _notificationsState = new MutableLiveData<>();
    public LiveData<Resource<List<AppNotification>>> notificationsState = _notificationsState;

    private final MutableLiveData<Resource<Void>> _markReadState = new MutableLiveData<>();
    public LiveData<Resource<Void>> markReadState = _markReadState;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadNotifications(int page, int size) {
        _notificationsState.setValue(Resource.loading(null));
        notificationRepository.getNotifications(getApplication(), page, size, new RepositoryCallback<PageResponse<AppNotification>>() {
            @Override
            public void onSuccess(PageResponse<AppNotification> data) {
                _notificationsState.postValue(Resource.success(data.getContent()));
            }

            @Override
            public void onError(String message) {
                _notificationsState.postValue(Resource.error(message, null));
            }
        });
    }

    public void markAsRead(long id) {
        _markReadState.setValue(Resource.loading(null));
        notificationRepository.markNotificationRead(getApplication(), id, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _markReadState.postValue(Resource.success(null));
            }

            @Override
            public void onError(String message) {
                _markReadState.postValue(Resource.error(message, null));
            }
        });
    }

    public void markAllAsRead() {
        _markReadState.setValue(Resource.loading(null));
        notificationRepository.markAllNotificationsRead(getApplication(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _markReadState.postValue(Resource.success(null));
                loadNotifications(0, 50);
            }

            @Override
            public void onError(String message) {
                _markReadState.postValue(Resource.error(message, null));
            }
        });
    }
}
