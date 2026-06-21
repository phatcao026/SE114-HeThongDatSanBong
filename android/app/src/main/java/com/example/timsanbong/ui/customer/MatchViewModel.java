package com.example.timsanbong.ui.customer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.MatchPostRequest;
import com.example.timsanbong.data.model.MatchRequestResponse;
import com.example.timsanbong.data.repository.MatchRepository;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;

import java.util.List;

public class MatchViewModel extends AndroidViewModel {

    private final MatchRepository matchRepository = new MatchRepository();

    private final MutableLiveData<Resource<List<MatchPost>>> _matchPostsState = new MutableLiveData<>();
    public LiveData<Resource<List<MatchPost>>> matchPostsState = _matchPostsState;

    private final MutableLiveData<Resource<MatchPost>> _createMatchState = new MutableLiveData<>();
    public LiveData<Resource<MatchPost>> createMatchState = _createMatchState;

    private final MutableLiveData<Resource<MatchRequestResponse>> _matchRequestState = new MutableLiveData<>();
    public LiveData<Resource<MatchRequestResponse>> matchRequestState = _matchRequestState;

    private final MutableLiveData<Resource<Void>> _deleteMatchState = new MutableLiveData<>();
    public LiveData<Resource<Void>> deleteMatchState = _deleteMatchState;

    public MatchViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadMatchPosts(int page, int size, String postType) {
        _matchPostsState.setValue(Resource.loading(null));
        matchRepository.getMatchPosts(getApplication(), postType, new RepositoryCallback<List<MatchPost>>() {
            @Override
            public void onSuccess(List<MatchPost> data) {
                _matchPostsState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _matchPostsState.postValue(Resource.error(message, null));
            }
        });
    }

    public void loadMyMatchPosts() {
        _matchPostsState.setValue(Resource.loading(null));
        matchRepository.getMyMatchPosts(getApplication(), new RepositoryCallback<List<MatchPost>>() {
            @Override
            public void onSuccess(List<MatchPost> data) {
                _matchPostsState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _matchPostsState.postValue(Resource.error(message, null));
            }
        });
    }

    public void createMatchPost(MatchPostRequest request) {
        _createMatchState.setValue(Resource.loading(null));
        matchRepository.createMatchPost(getApplication(), request, new RepositoryCallback<MatchPost>() {
            @Override
            public void onSuccess(MatchPost data) {
                _createMatchState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _createMatchState.postValue(Resource.error(message, null));
            }
        });
    }

    public void createMatchRequest(long postId, String message) {
        _matchRequestState.setValue(Resource.loading(null));
        matchRepository.createMatchRequest(getApplication(), postId, message, new RepositoryCallback<MatchRequestResponse>() {
            @Override
            public void onSuccess(MatchRequestResponse data) {
                _matchRequestState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _matchRequestState.postValue(Resource.error(message, null));
            }
        });
    }

    public void deleteMatchPost(long postId) {
        _deleteMatchState.setValue(Resource.loading(null));
        matchRepository.deleteMatchPost(getApplication(), postId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _deleteMatchState.postValue(Resource.success(null));
            }

            @Override
            public void onError(String message) {
                _deleteMatchState.postValue(Resource.error(message, null));
            }
        });
    }
}
