package com.example.timsanbong.ui.admin;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.OpponentReviewResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAuditActivity extends AppCompatActivity {

    private RecyclerView rvAudit;
    private View tabPending, tabProcessed;
    private View indicatorPending, indicatorProcessed;
    private TextView tvPendingLabel, tvProcessedLabel, tvPendingBadge, tvProcessedBadge;
    private final List<OpponentReviewResponse> currentReports = new ArrayList<>();
    private AuditAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_audit);

        initViews();
        setupTabs();
        loadData(true);

        findViewById(R.id.cvAdminAvatar).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });

        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_AUDIT);
        navBarManager.setup();
    }

    private void initViews() {
        rvAudit = findViewById(R.id.rvAdminAudit);
        rvAudit.setLayoutManager(new LinearLayoutManager(this));
        rvAudit.setNestedScrollingEnabled(false);

        tabPending = findViewById(R.id.tabPending);
        tabProcessed = findViewById(R.id.tabProcessed);
        indicatorPending = findViewById(R.id.indicatorPending);
        indicatorProcessed = findViewById(R.id.indicatorProcessed);
        tvPendingLabel = findViewById(R.id.tvTabPendingLabel);
        tvProcessedLabel = findViewById(R.id.tvTabProcessedLabel);
        tvPendingBadge = findViewById(R.id.tvPendingBadge);
        tvProcessedBadge = findViewById(R.id.tvProcessedBadge);

        adapter = new AuditAdapter(currentReports);
        rvAudit.setAdapter(adapter);
    }

    private void setupTabs() {
        tabPending.setOnClickListener(v -> {
            updateTabUI(true);
            loadData(true);
        });

        tabProcessed.setOnClickListener(v -> {
            updateTabUI(false);
            loadData(false);
        });
    }

    private void updateTabUI(boolean pending) {
        indicatorPending.setVisibility(pending ? View.VISIBLE : View.INVISIBLE);
        indicatorProcessed.setVisibility(pending ? View.INVISIBLE : View.VISIBLE);
        
        tvPendingLabel.setTextColor(ContextCompat.getColor(this, pending ? R.color.primary_dark : R.color.text_hint));
        tvPendingLabel.setTypeface(null, pending ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        
        tvProcessedLabel.setTextColor(ContextCompat.getColor(this, pending ? R.color.text_hint : R.color.primary_dark));
        tvProcessedLabel.setTypeface(null, pending ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);
    }

    private void loadData(boolean pending) {
        if (pending) {
            ApiClient.getService(this).getAdminFairplayPending().enqueue(new Callback<List<OpponentReviewResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<OpponentReviewResponse>> call, @NonNull Response<List<OpponentReviewResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentReports.clear();
                        currentReports.addAll(response.body());
                        adapter.notifyDataSetChanged();
                        tvPendingBadge.setText(String.valueOf(currentReports.size()));
                    } else {
                        Toast.makeText(AdminAuditActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<OpponentReviewResponse>> call, @NonNull Throwable t) {
                    Toast.makeText(AdminAuditActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });

            // Also update processed badge count in background
            ApiClient.getService(this).getAdminFairplayProcessed().enqueue(new Callback<List<OpponentReviewResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<OpponentReviewResponse>> call, @NonNull Response<List<OpponentReviewResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        tvProcessedBadge.setText(String.valueOf(response.body().size()));
                    }
                }
                @Override public void onFailure(@NonNull Call<List<OpponentReviewResponse>> call, @NonNull Throwable t) {}
            });
        } else {
            ApiClient.getService(this).getAdminFairplayProcessed().enqueue(new Callback<List<OpponentReviewResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<OpponentReviewResponse>> call, @NonNull Response<List<OpponentReviewResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentReports.clear();
                        currentReports.addAll(response.body());
                        adapter.notifyDataSetChanged();
                        tvProcessedBadge.setText(String.valueOf(currentReports.size()));
                    } else {
                        Toast.makeText(AdminAuditActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<OpponentReviewResponse>> call, @NonNull Throwable t) {
                    Toast.makeText(AdminAuditActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });

            // Also update pending badge count in background
            ApiClient.getService(this).getAdminFairplayPending().enqueue(new Callback<List<OpponentReviewResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<OpponentReviewResponse>> call, @NonNull Response<List<OpponentReviewResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        tvPendingBadge.setText(String.valueOf(response.body().size()));
                    }
                }
                @Override public void onFailure(@NonNull Call<List<OpponentReviewResponse>> call, @NonNull Throwable t) {}
            });
        }
    }

    static class AuditAdapter extends RecyclerView.Adapter<AuditAdapter.ViewHolder> {
        private final List<OpponentReviewResponse> reports;
        AuditAdapter(List<OpponentReviewResponse> reports) { this.reports = reports; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_audit, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OpponentReviewResponse report = reports.get(position);

            String title;
            int colorInt;
            int bgColorInt;

            // Map rating type to Vietnamese labels and colors
            String ratingType = report.getRatingType() != null ? report.getRatingType() : "";
            switch (ratingType) {
                case "GOOD":
                    title = "Thi đấu đẹp / Thân thiện";
                    colorInt = Color.parseColor("#38A169"); // Green
                    bgColorInt = Color.parseColor("#F0FFF4");
                    break;
                case "NO_SHOW":
                    title = "Bùng kèo / Hủy phút chót";
                    colorInt = Color.parseColor("#E53E3E"); // Red
                    bgColorInt = Color.parseColor("#FFF5F5");
                    break;
                case "BAD_BEHAVIOR":
                    title = "Hành vi xấu / Bạo lực";
                    colorInt = Color.parseColor("#DD6B20"); // Orange
                    bgColorInt = Color.parseColor("#FFFAF0");
                    break;
                default:
                    title = "Đánh giá Fairplay";
                    colorInt = Color.parseColor("#D69E2E"); // Amber
                    bgColorInt = Color.parseColor("#FFFFF0");
                    break;
            }

            holder.tvTitle.setText(title);
            holder.tvTime.setText(formatDateTime(report.getCreatedAt()));
            holder.tvPriority.setText(report.getStatus());
            holder.tvCategory.setText("Fairplay");
            holder.tvContent.setText(report.getComment() != null && !report.getComment().isEmpty()
                    ? report.getComment() : "(Không có nội dung nhận xét)");
            holder.tvReporter.setText(String.format("Từ: %s -> %s",
                    report.getReviewerName() != null ? report.getReviewerName() : "N/A",
                    report.getRevieweeName() != null ? report.getRevieweeName() : "N/A"));

            holder.ivIcon.setImageResource(R.drawable.ic_admin_shield);
            holder.ivIcon.setImageTintList(ColorStateList.valueOf(colorInt));
            holder.cvIcon.setCardBackgroundColor(bgColorInt);
            holder.tvPriority.setTextColor(colorInt);
            holder.tvPriority.setBackgroundTintList(ColorStateList.valueOf(bgColorInt));
            holder.vAccent.setBackgroundColor(colorInt);

            boolean isProcessed = !"PENDING".equals(report.getStatus());
            if (isProcessed) {
                holder.btnBlock.setVisibility(View.GONE);
                holder.btnIgnore.setVisibility(View.GONE);
            } else {
                holder.btnBlock.setVisibility(View.VISIBLE);
                holder.btnIgnore.setVisibility(View.VISIBLE);

                holder.btnBlock.setOnClickListener(v -> resolveReport(report.getId(), "APPROVED", v));
                holder.btnIgnore.setOnClickListener(v -> resolveReport(report.getId(), "DISMISSED", v));
            }
        }

        private String formatDateTime(String isoString) {
            if (isoString == null || isoString.isEmpty()) return "";
            try {
                // Handle format like 2026-06-21T20:41:18.269744 or 2026-06-21 20:41:18
                String cleanDate = isoString.replace("T", " ").split("\\.")[0];
                java.text.SimpleDateFormat inputSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
                java.text.SimpleDateFormat outputSdf = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", java.util.Locale.US);
                java.util.Date date = inputSdf.parse(cleanDate);
                return outputSdf.format(date);
            } catch (Exception e) {
                return isoString; // Fallback to raw string if parsing fails
            }
        }

        private void resolveReport(Long id, String action, View v) {
            if (action.equals("APPROVED")) {
                showDecisionDialog(id, v);
            } else {
                sendDecision(id, false, 0, v);
            }
        }

        private void showDecisionDialog(Long id, View v) {
            android.widget.LinearLayout layout = new android.widget.LinearLayout(v.getContext());
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            android.widget.RadioGroup rgType = new android.widget.RadioGroup(v.getContext());
            rgType.setOrientation(android.widget.RadioGroup.HORIZONTAL);
            
            android.widget.RadioButton rbMinus = new android.widget.RadioButton(v.getContext());
            rbMinus.setText("Trừ điểm");
            rbMinus.setId(View.generateViewId());
            rbMinus.setChecked(true);

            android.widget.RadioButton rbPlus = new android.widget.RadioButton(v.getContext());
            rbPlus.setText("Cộng điểm");
            rbPlus.setId(View.generateViewId());

            rgType.addView(rbMinus);
            rgType.addView(rbPlus);

            android.widget.EditText etPoints = new android.widget.EditText(v.getContext());
            etPoints.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            etPoints.setHint("Số điểm (ví dụ: 10)");

            layout.addView(rgType);
            layout.addView(etPoints);

            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Đánh giá Fairplay")
                    .setView(layout)
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        String input = etPoints.getText().toString();
                        int points = input.isEmpty() ? 0 : Integer.parseInt(input);
                        if (rgType.getCheckedRadioButtonId() == rbMinus.getId()) {
                            points = -Math.abs(points);
                        } else {
                            points = Math.abs(points);
                        }
                        sendDecision(id, true, points, v);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        private void sendDecision(Long id, boolean isAccepted, int points, View v) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("isAccepted", isAccepted);
            body.put("pointsApplied", points);

            ApiClient.getService(v.getContext()).resolveFairplayReview(id, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(v.getContext(), "Đã xử lý phán quyết", Toast.LENGTH_SHORT).show();
                        if (v.getContext() instanceof AdminAuditActivity) {
                            AdminAuditActivity activity = (AdminAuditActivity) v.getContext();
                            boolean currentlyPending = activity.indicatorPending.getVisibility() == View.VISIBLE;
                            activity.loadData(currentlyPending);
                        }
                    } else {
                        Toast.makeText(v.getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(v.getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() { return reports.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvTime, tvPriority, tvCategory, tvContent, tvReporter;
            ImageView ivIcon;
            MaterialCardView cvIcon;
            View vAccent;
            View btnBlock, btnIgnore;
            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvAuditTitle);
                tvTime = v.findViewById(R.id.tvAuditTime);
                tvPriority = v.findViewById(R.id.tvPriorityBadge);
                tvCategory = v.findViewById(R.id.tvCategoryBadge);
                tvContent = v.findViewById(R.id.tvAuditContent);
                tvReporter = v.findViewById(R.id.tvAuditReporter);
                ivIcon = v.findViewById(R.id.ivAuditTypeIcon);
                cvIcon = v.findViewById(R.id.cvAuditIcon);
                vAccent = v.findViewById(R.id.vAuditAccent);
                btnBlock = v.findViewById(R.id.btnBlock);
                btnIgnore = v.findViewById(R.id.btnIgnore);
            }
        }
    }
}