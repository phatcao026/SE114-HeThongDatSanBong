package com.example.timsanbong.ui.customer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.data.model.MatchPostRequest;
import com.example.timsanbong.data.model.PageResponse;
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

    public MatchViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadMatchPosts(int page, int size, String postType) {
        _matchPostsState.setValue(Resource.loading(null));
        matchRepository.getMatchPosts(getApplication(), page, size, postType, new RepositoryCallback<PageResponse<MatchPost>>() {
            @Override
            public void onSuccess(PageResponse<MatchPost> data) {
                _matchPostsState.postValue(Resource.success(data.getContent()));
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
}
