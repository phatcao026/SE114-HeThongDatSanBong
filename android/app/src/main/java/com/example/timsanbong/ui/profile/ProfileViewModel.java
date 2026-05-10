package com.example.timsanbong.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.User;
import com.example.timsanbong.data.repository.UserRepository;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;
import com.example.timsanbong.utils.SessionManager;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository = new UserRepository();
    private final SessionManager sessionManager;

    private final MutableLiveData<Resource<User>> _profileState = new MutableLiveData<>();
    public LiveData<Resource<User>> profileState = _profileState;

    private final MutableLiveData<Boolean> _logoutEvent = new MutableLiveData<>();
    public LiveData<Boolean> logoutEvent = _logoutEvent;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.sessionManager = new SessionManager(application);
    }

    public void loadProfile() {
        _profileState.setValue(Resource.loading(null));
        userRepository.getMyProfile(getApplication(), new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User data) {
                _profileState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _profileState.postValue(Resource.error(message, null));
            }
        });
    }

    public void logout() {
        sessionManager.clearSession();
        ApiClient.reset();
        _logoutEvent.setValue(true);
    }
}
