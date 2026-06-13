package com.example.timsanbong.ui.customer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.InvitationResponse;
import com.example.timsanbong.data.model.TeamRequest;
import com.example.timsanbong.data.model.TeamResponse;
import com.example.timsanbong.data.repository.TeamRepository;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;

import java.util.List;

public class TeamViewModel extends AndroidViewModel {

    private final TeamRepository teamRepository = new TeamRepository();

    private final MutableLiveData<Resource<List<TeamResponse>>> _teamsState = new MutableLiveData<>();
    public LiveData<Resource<List<TeamResponse>>> teamsState = _teamsState;
    
    private final MutableLiveData<Resource<List<InvitationResponse>>> _invitationsState = new MutableLiveData<>();
    public LiveData<Resource<List<InvitationResponse>>> invitationsState = _invitationsState;

    private final MutableLiveData<Resource<TeamResponse>> _createTeamState = new MutableLiveData<>();
    public LiveData<Resource<TeamResponse>> createTeamState = _createTeamState;

    public TeamViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadMyTeams() {
        _teamsState.setValue(Resource.loading(null));
        teamRepository.getMyTeams(getApplication(), new RepositoryCallback<List<TeamResponse>>() {
            @Override
            public void onSuccess(List<TeamResponse> data) {
                _teamsState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _teamsState.postValue(Resource.error(message, null));
            }
        });
    }

    public void loadInvitations() {
        _invitationsState.setValue(Resource.loading(null));
        teamRepository.getInvitations(getApplication(), new RepositoryCallback<List<InvitationResponse>>() {
            @Override
            public void onSuccess(List<InvitationResponse> data) {
                _invitationsState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _invitationsState.postValue(Resource.error(message, null));
            }
        });
    }

    public void createTeam(TeamRequest request) {
        _createTeamState.setValue(Resource.loading(null));
        teamRepository.createTeam(getApplication(), request, new RepositoryCallback<TeamResponse>() {
            @Override
            public void onSuccess(TeamResponse data) {
                _createTeamState.postValue(Resource.success(data));
                loadMyTeams();
            }

            @Override
            public void onError(String message) {
                _createTeamState.postValue(Resource.error(message, null));
            }
        });
    }
}
