package com.example.timsanbong.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.FieldReviewResponse;
import com.example.timsanbong.utils.Resource;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class FieldReviewsActivity extends AppCompatActivity {

    private ImageView ivReviewBack;
    private TextView tvReviewTitleText;
    private ChipGroup chipGroupRatingFilter;
    private RecyclerView rvFieldReviews;
    private ProgressBar pbLoading;
    private TextView tvReviewsEmpty;

    private FieldViewModel fieldViewModel;
    private FieldReviewAdapter adapter;
    private long fieldId = -1;
    private List<FieldReviewResponse> allReviews = new ArrayList<>();
    private int currentFilterType = 0; // 0: All, 1: Positive, 2: Negative

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_reviews);

        fieldId = getIntent().getLongExtra("fieldId", -1);
        String fieldName = getIntent().getStringExtra("fieldName");

        initViews();
        setupListeners();
        setupRecyclerView();
        setupViewModel(fieldName);
    }

    private void initViews() {
        ivReviewBack = findViewById(R.id.ivReviewBack);
        tvReviewTitleText = findViewById(R.id.tvReviewTitleText);
        chipGroupRatingFilter = findViewById(R.id.chipGroupRatingFilter);
        rvFieldReviews = findViewById(R.id.rvFieldReviews);
        pbLoading = findViewById(R.id.pbLoading);
        tvReviewsEmpty = findViewById(R.id.tvReviewsEmpty);
    }

    private void setupListeners() {
        ivReviewBack.setOnClickListener(v -> finish());

        chipGroupRatingFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipFilterAll) {
                currentFilterType = 0;
            } else if (checkedId == R.id.chipFilterPositive) {
                currentFilterType = 1;
            } else if (checkedId == R.id.chipFilterNegative) {
                currentFilterType = 2;
            }
            applyFilter();
        });
    }

    private void setupRecyclerView() {
        adapter = new FieldReviewAdapter();
        rvFieldReviews.setLayoutManager(new LinearLayoutManager(this));
        rvFieldReviews.setAdapter(adapter);
    }

    private void setupViewModel(String fieldName) {
        if (fieldName != null && !fieldName.trim().isEmpty()) {
            tvReviewTitleText.setText("Đánh giá - " + fieldName.trim());
        }

        fieldViewModel = new ViewModelProvider(this).get(FieldViewModel.class);

        fieldViewModel.fieldReviewsState.observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.LOADING) {
                showLoadingState();
            } else if (resource.status == Resource.Status.SUCCESS) {
                allReviews.clear();
                if (resource.data != null) {
                    allReviews.addAll(resource.data);
                }
                showContentState();
                applyFilter();
            } else if (resource.status == Resource.Status.ERROR) {
                showErrorState(resource.message);
            }
        });

        if (fieldId != -1) {
            fieldViewModel.loadFieldReviews(fieldId);
        } else {
            showErrorState("Không tìm thấy thông tin sân");
        }
    }

    private void applyFilter() {
        List<FieldReviewResponse> filteredList = new ArrayList<>();
        for (FieldReviewResponse review : allReviews) {
            int rating = review.getRating() != null ? review.getRating() : 0;
            if (currentFilterType == 0) {
                filteredList.add(review);
            } else if (currentFilterType == 1 && rating >= 4) {
                filteredList.add(review);
            } else if (currentFilterType == 2 && rating <= 3) {
                filteredList.add(review);
            }
        }

        adapter.updateReviews(filteredList);
        rvFieldReviews.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);
        tvReviewsEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showLoadingState() {
        pbLoading.setVisibility(View.VISIBLE);
        rvFieldReviews.setVisibility(View.GONE);
        tvReviewsEmpty.setVisibility(View.GONE);
    }

    private void showContentState() {
        pbLoading.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        pbLoading.setVisibility(View.GONE);
        rvFieldReviews.setVisibility(View.GONE);
        tvReviewsEmpty.setVisibility(View.VISIBLE);
        tvReviewsEmpty.setText(message == null || message.trim().isEmpty() ? "Đã xảy ra lỗi khi tải đánh giá" : message);
    }
}
