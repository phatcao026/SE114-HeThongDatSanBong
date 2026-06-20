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
                        tvProcessedBadge.setText("0");
                    } else {
                        Toast.makeText(AdminAuditActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<OpponentReviewResponse>> call, @NonNull Throwable t) {
                    Toast.makeText(AdminAuditActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            currentReports.clear();
            adapter.notifyDataSetChanged();
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
            holder.tvTitle.setText(report.getRatingType());
            holder.tvTime.setText(report.getCreatedAt());
            holder.tvPriority.setText(report.getStatus());
            holder.tvCategory.setText("Fairplay");
            holder.tvContent.setText(report.getComment());
            holder.tvReporter.setText(String.format("Từ: %s -> %s", report.getReviewerName(), report.getRevieweeName()));
            holder.ivIcon.setImageResource(R.drawable.ic_admin_shield);

            int colorInt = Color.parseColor("#E53E3E");
            int bgColorInt = Color.parseColor("#FEF2F2");

            holder.ivIcon.setImageTintList(ColorStateList.valueOf(colorInt));
            holder.cvIcon.setCardBackgroundColor(bgColorInt);
            holder.tvPriority.setTextColor(colorInt);
            holder.tvPriority.setBackgroundTintList(ColorStateList.valueOf(bgColorInt));
            
            holder.vAccent.setBackgroundColor(colorInt);

            holder.btnBlock.setOnClickListener(v -> resolveReport(report.getId(), "APPROVED", v));
            holder.btnIgnore.setOnClickListener(v -> resolveReport(report.getId(), "DISMISSED", v));
            holder.btnHide.setOnClickListener(v -> resolveReport(report.getId(), "DISMISSED", v));

            holder.itemView.setOnClickListener(v -> {
                // Show detail if needed
            });
        }

        private void resolveReport(Long id, String action, View v) {
            if (action.equals("APPROVED")) {
                showDecisionDialog(id, v);
            } else {
                sendDecision(id, false, 0, v);
            }
        }

        private void showDecisionDialog(Long id, View v) {
            android.widget.EditText etPoints = new android.widget.EditText(v.getContext());
            etPoints.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
            etPoints.setHint("Ví dụ: -10 hoặc 5");

            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Phán quyết Fairplay")
                    .setMessage("Nhập số điểm uy tín thay đổi cho người bị tố cáo:")
                    .setView(etPoints)
                    .setPositiveButton("Xác nhận vi phạm", (dialog, which) -> {
                        String input = etPoints.getText().toString();
                        int points = input.isEmpty() ? 0 : Integer.parseInt(input);
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
                            ((AdminAuditActivity) v.getContext()).loadData(true);
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
            View btnBlock, btnHide, btnIgnore;
            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvAuditTitle);
                tvTime = v.findViewById(R.id.tvAuditTime);
                tvPriority = v.findViewById(R.id.tvPriorityBadge);
                tvCategory = v.findViewById(R.id.tvCategoryBadge);
                tvContent = v.findViewById(R.id.tvAuditContent);
                tvReporter = v.findViewById(R.id.tvAuditReporter);
                ivIcon = v.findViewById(R.id.ivAuditTypeIcon);
                cvIcon = (MaterialCardView) v.findViewById(R.id.cvAuditIcon);
                vAccent = v.findViewById(R.id.vAuditAccent);
                btnBlock = v.findViewById(R.id.btnBlock);
                btnHide = v.findViewById(R.id.btnHide);
                btnIgnore = v.findViewById(R.id.btnIgnore);
            }
        }
    }
}