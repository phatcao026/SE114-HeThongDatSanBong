package com.example.timsanbong.ui.customer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.FieldFilter;
import com.example.timsanbong.data.repository.FieldRepository;
import com.example.timsanbong.utils.RepositoryCallback;
import com.example.timsanbong.utils.Resource;
import com.example.timsanbong.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FieldViewModel extends AndroidViewModel {

    private final FieldRepository fieldRepository = new FieldRepository();
    private final SessionManager sessionManager;

    private final MutableLiveData<Resource<List<Field>>> _fieldsState = new MutableLiveData<>();
    public LiveData<Resource<List<Field>>> fieldsState = _fieldsState;

    private final MutableLiveData<Resource<Field>> _fieldDetailState = new MutableLiveData<>();
    public LiveData<Resource<Field>> fieldDetailState = _fieldDetailState;

    private final MutableLiveData<List<Field>> _filteredFields = new MutableLiveData<>();
    public LiveData<List<Field>> filteredFields = _filteredFields;

    private final MutableLiveData<String> _userDisplayName = new MutableLiveData<>();
    public LiveData<String> userDisplayName = _userDisplayName;

    private List<Field> allFields = new ArrayList<>();
    private String currentKeyword = "";
    private FieldFilter currentFilter = new FieldFilter();

    public FieldViewModel(@NonNull Application application) {
        super(application);
        this.sessionManager = new SessionManager(application);
        loadUserDisplayName();
    }

    public void loadFields() {
        _fieldsState.setValue(Resource.loading(null));
        fieldRepository.getFields(getApplication(), new RepositoryCallback<List<Field>>() {
            @Override
            public void onSuccess(List<Field> data) {
                allFields = data;
                _fieldsState.postValue(Resource.success(data));
                applyFilters();
            }

            @Override
            public void onError(String message) {
                _fieldsState.postValue(Resource.error(message, null));
            }
        });
    }

    public void loadFieldDetail(long fieldId) {
        _fieldDetailState.setValue(Resource.loading(null));
        fieldRepository.getFieldById(getApplication(), fieldId, new RepositoryCallback<Field>() {
            @Override
            public void onSuccess(Field data) {
                _fieldDetailState.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                _fieldDetailState.postValue(Resource.error(message, null));
            }
        });
    }

    public void setSearchKeyword(String keyword) {
        currentKeyword = keyword.trim();
        applyFilters();
    }

    public void setFilter(FieldFilter filter) {
        currentFilter = filter;
        applyFilters();
    }

    public FieldFilter getCurrentFilter() {
        return currentFilter;
    }

    private void applyFilters() {
        List<Field> result = new ArrayList<>(allFields);

        if (!currentKeyword.isEmpty()) {
            List<Field> searched = new ArrayList<>();
            for (Field f : result) {
                if (f.getName().toLowerCase().contains(currentKeyword.toLowerCase()) ||
                        f.getAddress().toLowerCase().contains(currentKeyword.toLowerCase())) {
                    searched.add(f);
                }
            }
            result = searched;
        }

        if (currentFilter.fieldType != null) {
            List<Field> typed = new ArrayList<>();
            for (Field f : result) {
                if (currentFilter.fieldType.equals(f.getFieldType())) typed.add(f);
            }
            result = typed;
        }

        if (currentFilter.priceRange != 0) {
            List<Field> priced = new ArrayList<>();
            for (Field f : result) {
                boolean matches = false;
                if (currentFilter.priceRange == 1) matches = f.getPricePerHour() < 200000;
                else if (currentFilter.priceRange == 2) matches = f.getPricePerHour() >= 200000 && f.getPricePerHour() <= 300000;
                else if (currentFilter.priceRange == 3) matches = f.getPricePerHour() > 300000;

                if (matches) priced.add(f);
            }
            result = priced;
        }

        if (currentFilter.availableOnly) {
            List<Field> available = new ArrayList<>();
            for (Field f : result) {
                if (f.isAvailable()) available.add(f);
            }
            result = available;
        }

        _filteredFields.postValue(result);
    }

    public boolean isFilterActive() {
        return currentFilter.isActive();
    }

    private void loadUserDisplayName() {
        String userJson = sessionManager.getUserJson();
        if (userJson != null && !userJson.isEmpty()) {
            try {
                JSONObject json = new JSONObject(userJson);
                _userDisplayName.setValue(json.optString("fullName", "User"));
            } catch (JSONException e) {
                _userDisplayName.setValue("User");
            }
        } else {
            _userDisplayName.setValue("User");
        }
    }
}
