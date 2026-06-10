package com.example.timsanbong.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AdminTransactionActivity extends AppCompatActivity {

    private AdminTransactionAdapter adapter;
    private List<Transaction> allTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_transaction);

        setupHeaderCards();
        initRecyclerView();
        setupFilters();

        findViewById(R.id.cvAdminAvatar).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });

        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_TRANSACTIONS);
        navBarManager.setup();
    }

    private void setupHeaderCards() {
        View cardGmv = findViewById(R.id.cardGmv);
        ((TextView) cardGmv.findViewById(R.id.tvStatLabel)).setText("Tổng giao dịch hôm nay");
        ((TextView) cardGmv.findViewById(R.id.tvStatValue)).setText("1,2tr đ");
        ((TextView) cardGmv.findViewById(R.id.tvTrendValue)).setText("+18.6% so với ngày qua");
        ((TextView) cardGmv.findViewById(R.id.tvTrendValue)).setTextColor(Color.parseColor("#10B981"));
        cardGmv.findViewById(R.id.ivStatIcon).setVisibility(View.GONE);

        View cardFee = findViewById(R.id.cardFee);
        ((TextView) cardFee.findViewById(R.id.tvStatLabel)).setText("Phí dịch vụ thu");
        ((TextView) cardFee.findViewById(R.id.tvStatValue)).setText("60k đ");
        ((TextView) cardFee.findViewById(R.id.tvTrendValue)).setText("5% mỗi cọc");
        ((TextView) cardFee.findViewById(R.id.tvTrendValue)).setTextColor(Color.parseColor("#64748B"));
        cardFee.findViewById(R.id.ivStatIcon).setVisibility(View.GONE);
        cardFee.findViewById(R.id.ivTrendIcon).setVisibility(View.GONE);
    }

    private void initRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvAdminTransactions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setNestedScrollingEnabled(false);

        allTransactions = new ArrayList<>();
        allTransactions.add(new Transaction("Trần Đăng Khoa", "TSB-7C4F-2308 · 14:32", "150.000đ", "Thành công", "MoMo", "Mo", "#A855F7"));
        allTransactions.add(new Transaction("Phạm Quốc Khánh", "TSB-A12B-2308 · 13:18", "100.000đ", "Thành công", "Stripe", "Vi", "#6366F1"));
        allTransactions.add(new Transaction("Trần Đăng Khoa", "TSB-D99F-2308 · 12:44", "150.000đ", "Thành công", "MoMo", "Mo", "#A855F7"));
        allTransactions.add(new Transaction("FC Hậu Vệ", "TSB-3F11-2308 · 12:21", "240.000đ", "Lỗi", "Stripe", "Vi", "#6366F1"));
        allTransactions.add(new Transaction("Thủ Đức A-S", "TSB-5A22-2308 · 11:55", "150.000đ", "Thành công", "MoMo", "Mo", "#A855F7"));
        allTransactions.add(new Transaction("Đội Bão Đông", "TSB-92AA-2208 · Hôm qua", "-300.000đ", "Hoàn", "MoMo", "Mo", "#A855F7"));

        adapter = new AdminTransactionAdapter(new ArrayList<>(allTransactions));
        rv.setAdapter(adapter);
    }

    private void setupFilters() {
        MaterialButton btnAll = findViewById(R.id.btnFilterAll);
        MaterialButton btnMoMo = findViewById(R.id.btnFilterMoMo);
        MaterialButton btnStripe = findViewById(R.id.btnFilterStripe);
        MaterialButton btnFailed = findViewById(R.id.btnFilterFailed);
        MaterialButton btnRefund = findViewById(R.id.btnFilterRefund);

        btnAll.setOnClickListener(v -> {
            updateFilterButtons(btnAll, btnMoMo, btnStripe, btnFailed, btnRefund);
            filter("Tất cả");
        });
        btnMoMo.setOnClickListener(v -> {
            updateFilterButtons(btnMoMo, btnAll, btnStripe, btnFailed, btnRefund);
            filter("MoMo");
        });
        btnStripe.setOnClickListener(v -> {
            updateFilterButtons(btnStripe, btnAll, btnMoMo, btnFailed, btnRefund);
            filter("Stripe");
        });
        btnFailed.setOnClickListener(v -> {
            updateFilterButtons(btnFailed, btnAll, btnMoMo, btnStripe, btnRefund);
            filter("Lỗi");
        });
        btnRefund.setOnClickListener(v -> {
            updateFilterButtons(btnRefund, btnAll, btnMoMo, btnStripe, btnFailed);
            filter("Hoàn");
        });
    }

    private void updateFilterButtons(MaterialButton selected, MaterialButton... others) {
        selected.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_light)));
        selected.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        selected.setStrokeWidth(0);

        String text = selected.getText().toString();
        if (!text.contains("✓")) {
            selected.setText("✓ " + text);
        }

        for (MaterialButton btn : others) {
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.admin_background)));
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_heading));
            btn.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
            btn.setStrokeColor(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.border_gray)));

            String otherText = btn.getText().toString();
            if (otherText.contains("✓")) {
                btn.setText(otherText.replace("✓ ", "").replace("✓", "").trim());
            }
        }
    }

    private void filter(String criteria) {
        List<Transaction> filtered = new ArrayList<>();
        if (criteria.equals("Tất cả")) {
            filtered.addAll(allTransactions);
        } else {
            for (Transaction t : allTransactions) {
                if (t.method.equals(criteria) || t.status.equals(criteria)) {
                    filtered.add(t);
                }
            }
        }
        adapter.updateList(filtered);
    }

    static class Transaction {
        String name, idTime, amount, status, method, iconText, iconColor;
        Transaction(String n, String it, String a, String s, String m, String txt, String col) {
            name = n; idTime = it; amount = a; status = s; method = m; iconText = txt; iconColor = col;
        }
    }

    static class AdminTransactionAdapter extends RecyclerView.Adapter<AdminTransactionAdapter.ViewHolder> {
        private final List<Transaction> list;
        AdminTransactionAdapter(List<Transaction> list) { this.list = list; }

        public void updateList(List<Transaction> newList) {
            list.clear();
            list.addAll(newList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_transaction, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Transaction t = list.get(position);
            holder.tvName.setText(t.name);
            holder.tvIdTime.setText(t.idTime);
            holder.tvAmount.setText(t.amount);
            holder.tvStatus.setText(t.status);
            holder.tvIcon.setText(t.iconText);
            holder.cvIcon.setCardBackgroundColor(Color.parseColor(t.iconColor));

            // Amount color
            if (t.amount.startsWith("-")) {
                holder.tvAmount.setTextColor(Color.parseColor("#64748B"));
            } else if (t.status.equals("Lỗi")) {
                holder.tvAmount.setTextColor(Color.parseColor("#EF4444"));
            } else {
                holder.tvAmount.setTextColor(Color.parseColor("#166534"));
            }

            // Status Badge
            View badge = (View) holder.tvStatus.getParent();
            View dot = holder.itemView.findViewById(R.id.vStatusDot);
            if (t.status.equals("Thành công")) {
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F0FDF4")));
                holder.tvStatus.setTextColor(Color.parseColor("#166534"));
                dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));
            } else if (t.status.equals("Lỗi")) {
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FEF2F2")));
                holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
                dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444")));
            } else {
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F3F4F6")));
                holder.tvStatus.setTextColor(Color.parseColor("#64748B"));
                dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#64748B")));
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvIdTime, tvAmount, tvStatus, tvIcon;
            MaterialCardView cvIcon;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvTransactionName);
                tvIdTime = v.findViewById(R.id.tvTransactionIdTime);
                tvAmount = v.findViewById(R.id.tvTransactionAmount);
                tvStatus = v.findViewById(R.id.tvTransactionStatus);
                tvIcon = v.findViewById(R.id.tvTransactionIconText);
                cvIcon = v.findViewById(R.id.cvTransactionIcon);
            }
        }
    }
}