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
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AdminAuditActivity extends AppCompatActivity {

    private RecyclerView rvAudit;
    private View tabPending, tabProcessed;
    private View indicatorPending, indicatorProcessed;
    private TextView tvPendingLabel, tvProcessedLabel;

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
        List<AuditReport> reports = new ArrayList<>();
        if (pending) {
            // High Priority - Red (#E53E3E / #FEF2F2)
            reports.add(new AuditReport("Bài đăng có ngôn từ kích động", "12 phút", "Ưu tiên cao", "Bài đăng", 
                "“Tìm đối nào dám chiến, đội nào nhát thì né ra cho rộng đường…”", "Của Đỗ Văn Sang · 4 người đã báo cáo", 
                R.drawable.ic_message, "#E53E3E", "#FEF2F2"));
            
            reports.add(new AuditReport("Sân Phú Mỹ Hưng – nghi vấn sân ảo", "1 giờ", "Ưu tiên cao", "Sân", 
                "“3 khách phản ánh đến nơi không có sân. Cọc đã thu.”", "3 người đã báo cáo", 
                R.drawable.ic_bolt, "#E53E3E", "#FEF2F2"));

            // Medium Priority - Yellow (#F59E0B / #FFFBEB)
            reports.add(new AuditReport("Hoàng Tấn Lực – bùng kèo 4 lần liên tiếp", "3 giờ", "Trung bình", "Người dùng", 
                "“Trust score tụt từ 64 → 14 trong 30 ngày. Đề xuất ban.”", "6 người đã báo cáo", 
                R.drawable.ic_person, "#F59E0B", "#FFFBEB"));
            
            // Low Priority - Blue (#3B82F6 / #EFF6FF)
            reports.add(new AuditReport("Bài đăng trùng lặp", "Hôm qua", "Thấp", "Bài đăng", 
                "“Người dùng đăng 5 kèo giống nhau trong 1 giờ.”", "Của Phạm Quốc Khánh · 2 người đã báo cáo", 
                R.drawable.ic_message, "#3B82F6", "#EFF6FF"));
        } else {
            // Dummy for processed - Green (#10B981 / #ECFDF5)
            reports.add(new AuditReport("Nội dung không phù hợp (Đã khóa)", "2 ngày trước", "Xong", "Hệ thống", 
                "Tài khoản vi phạm chính sách cộng đồng nhiều lần.", "Xử lý bởi: Admin_01", 
                R.drawable.ic_admin_shield, "#10B981", "#ECFDF5"));
        }
        rvAudit.setAdapter(new AuditAdapter(reports));
    }

    static class AuditReport {
        String title, time, priority, category, content, reporter, color, bgColor;
        int iconRes;
        AuditReport(String t, String tm, String p, String cat, String con, String rep, int ic, String col, String bg) {
            title = t; time = tm; priority = p; category = cat; content = con; reporter = rep; iconRes = ic; color = col; bgColor = bg;
        }
    }

    static class AuditAdapter extends RecyclerView.Adapter<AuditAdapter.ViewHolder> {
        private final List<AuditReport> reports;
        AuditAdapter(List<AuditReport> reports) { this.reports = reports; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_audit, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AuditReport report = reports.get(position);
            holder.tvTitle.setText(report.title);
            holder.tvTime.setText(report.time);
            holder.tvPriority.setText(report.priority);
            holder.tvCategory.setText(report.category);
            holder.tvContent.setText(report.content);
            holder.tvReporter.setText(report.reporter);
            holder.ivIcon.setImageResource(report.iconRes);

            int colorInt = Color.parseColor(report.color);
            int bgColorInt = Color.parseColor(report.bgColor);

            holder.ivIcon.setImageTintList(ColorStateList.valueOf(colorInt));
            holder.cvIcon.setCardBackgroundColor(bgColorInt);
            holder.tvPriority.setTextColor(colorInt);
            holder.tvPriority.setBackgroundTintList(ColorStateList.valueOf(bgColorInt));
            
            holder.vAccent.setBackgroundColor(colorInt);

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Xem chi tiết: " + report.title, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() { return reports.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvTime, tvPriority, tvCategory, tvContent, tvReporter;
            ImageView ivIcon;
            MaterialCardView cvIcon;
            View vAccent;
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
            }
        }
    }
}