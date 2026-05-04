package com.example.fieldbooking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fieldbooking.R;
import com.example.fieldbooking.adapter.FieldAdapter;
import com.example.fieldbooking.model.Field;
import com.example.fieldbooking.network.ApiClient;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvFields;
    private FieldAdapter adapter;
    private TextInputEditText etSearch;
    private ImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvFields = findViewById(R.id.rvFields);
        etSearch = findViewById(R.id.etSearch);
        ivProfile = findViewById(R.id.ivProfile);

        rvFields.setLayoutManager(new LinearLayoutManager(this));

        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    loadFields();
                } else {
                    searchFields(keyword);
                }
            }
        });

        loadFields();
    }

    private void loadFields() {
        ApiClient.getService(this).getFields().enqueue(new Callback<List<Field>>() {
            @Override
            public void onResponse(Call<List<Field>> call, Response<List<Field>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showFields(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Field>> call, Throwable t) {
                Toast.makeText(MainActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchFields(String keyword) {
        ApiClient.getService(this).searchFields(keyword).enqueue(new Callback<List<Field>>() {
            @Override
            public void onResponse(Call<List<Field>> call, Response<List<Field>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showFields(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Field>> call, Throwable t) {}
        });
    }

    private void showFields(List<Field> fields) {
        adapter = new FieldAdapter(fields, field -> {
            Intent intent = new Intent(this, FieldDetailActivity.class);
            intent.putExtra("fieldId", field.getId());
            startActivity(intent);
        });
        rvFields.setAdapter(adapter);
    }
}
