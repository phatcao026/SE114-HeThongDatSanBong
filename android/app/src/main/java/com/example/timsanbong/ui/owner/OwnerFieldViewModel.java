package com.example.timsanbong.ui.owner;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.Field;
import com.example.timsanbong.data.model.FieldCreateRequest;
import com.example.timsanbong.data.model.FieldUpdateRequest;
import com.example.timsanbong.data.model.TimeSlot;
import com.example.timsanbong.data.model.TimeSlotCreateRequest;
import com.example.timsanbong.data.model.TimeSlotResponse;
import com.example.timsanbong.data.model.TimeSlotUpdateRequest;
import com.example.timsanbong.data.repository.OwnerFieldRepository;
import com.example.timsanbong.utils.RepositoryCallback;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OwnerFieldViewModel extends AndroidViewModel {
    private final OwnerFieldRepository repository = new OwnerFieldRepository();
    private final MutableLiveData<List<Field>> fields = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<List<TimeSlotResponse>> currentFieldAvailability = new MutableLiveData<>();
    private String lastLoadedDate;

    public OwnerFieldViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Field>> getFields() {
        return fields;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<List<TimeSlotResponse>> getCurrentFieldAvailability() {
        return currentFieldAvailability;
    }

    public void loadFields() {
        Calendar cal = Calendar.getInstance();
        String today = String.format(Locale.US, "%04d-%02d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        loadFields(today);
    }

    public void loadFields(String date) {
        this.lastLoadedDate = date;
        loading.setValue(true);
        repository.getOwnerFields(getApplication(), date, new RepositoryCallback<>() {
            @Override
            public void onSuccess(List<Field> data) {
                loading.setValue(false);
                fields.setValue(data == null ? Collections.emptyList() : data);
            }

            @Override
            public void onError(String error) {
                loading.setValue(false);
                message.setValue(error);
            }
        });
    }

    public void loadFieldAvailability(long fieldId, String date) {
        currentFieldAvailability.setValue(null);
        loading.setValue(true);
        repository.getFieldAvailability(getApplication(), fieldId, date, new RepositoryCallback<>() {
            @Override
            public void onSuccess(List<TimeSlotResponse> data) {
                loading.setValue(false);
                currentFieldAvailability.setValue(data);
            }

            @Override
            public void onError(String error) {
                loading.setValue(false);
                message.setValue(error);
            }
        });
    }

    public void createField(FieldCreateRequest request) {
        loading.setValue(true);
        repository.createField(getApplication(), request, fieldMutationCallback());
    }

    public void updateField(long fieldId, FieldUpdateRequest request) {
        loading.setValue(true);
        repository.updateField(getApplication(), fieldId, request, fieldMutationCallback());
    }

    public void deleteField(long fieldId) {
        loading.setValue(true);
        repository.deleteField(getApplication(), fieldId, fieldMutationCallback());
    }

    public void createTimeSlot(long fieldId, TimeSlotCreateRequest request) {
        loading.setValue(true);
        repository.createTimeSlot(getApplication(), fieldId, request, timeSlotMutationCallback());
    }

    public void updateTimeSlot(long fieldId, long slotId, TimeSlotUpdateRequest request) {
        loading.setValue(true);
        repository.updateTimeSlot(getApplication(), fieldId, slotId, request, timeSlotMutationCallback());
    }

    public void deleteTimeSlot(long fieldId, long slotId) {
        loading.setValue(true);
        repository.deleteTimeSlot(getApplication(), fieldId, slotId, timeSlotMutationCallback());
    }

    private RepositoryCallback<Field> fieldMutationCallback() {
        return new RepositoryCallback<>() {
            @Override
            public void onSuccess(Field data) {
                loading.setValue(false);
                message.setValue("Thành công.");
                loadFields(lastLoadedDate);
            }

            @Override
            public void onError(String error) {
                loading.setValue(false);
                message.setValue(error);
            }
        };
    }

    private RepositoryCallback<TimeSlot> timeSlotMutationCallback() {
        return new RepositoryCallback<>() {
            @Override
            public void onSuccess(TimeSlot data) {
                loading.setValue(false);
                message.setValue("Thành công.");
                loadFields(lastLoadedDate);
            }

            @Override
            public void onError(String error) {
                loading.setValue(false);
                message.setValue(error);
            }
        };
    }
}
