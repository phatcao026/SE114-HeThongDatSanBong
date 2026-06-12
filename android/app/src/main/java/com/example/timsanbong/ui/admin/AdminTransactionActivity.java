package com.example.timsanbong.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.PaymentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminTransactionActivity extends AppCompatActivity {

    private AdminTransactionAdapter adapter;
    private List<PaymentResponse> allTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_transaction);

        setupHeaderCards();
        initRecyclerView();
        fetchTransactions();
        setupFilters();

        findViewById(R.id.cvAdminAvatar).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });

        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_TRANSACTIONS);
        navBarManager.setup();
    }

    private void fetchTransactions() {
        ApiClient.getService(this).getAdminPayments().enqueue(new Callback<List<PaymentResponse>>() {
            @Override
            public void onResponse(Call<List<PaymentResponse>> call, Response<List<PaymentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allTransactions = response.body();
                    adapter.updateList(allTransactions);
                } else {
                    Toast.makeText(AdminTransactionActivity.this, "Không thể tải danh sách giao dịch", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PaymentResponse>> call, Throwable t) {
                Toast.makeText(AdminTransactionActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupHeaderCards() {
        View cardGmv = findViewById(R.id.cardGmv);
        ((TextView) cardGmv.findViewById(R.id.tvStatLabel)).setText("Giao dịch gần đây");
        ((TextView) cardGmv.findViewById(R.id.tvStatValue)).setText("Dữ liệu thực");
        ((TextView) cardGmv.findViewById(R.id.tvTrendValue)).setText("Theo thời gian thực");
        ((TextView) cardGmv.findViewById(R.id.tvTrendValue)).setTextColor(Color.parseColor("#10B981"));
        cardGmv.findViewById(R.id.ivStatIcon).setVisibility(View.GONE);

        View cardFee = findViewById(R.id.cardFee);
        ((TextView) cardFee.findViewById(R.id.tvStatLabel)).setText("Cổng thanh toán");
        ((TextView) cardFee.findViewById(R.id.tvStatValue)).setText("Stripe / Tiền mặt");
        ((TextView) cardFee.findViewById(R.id.tvTrendValue)).setText("Phí dịch vụ 5%");
        ((TextView) cardFee.findViewById(R.id.tvTrendValue)).setTextColor(Color.parseColor("#64748B"));
        cardFee.findViewById(R.id.ivStatIcon).setVisibility(View.GONE);
        cardFee.findViewById(R.id.ivTrendIcon).setVisibility(View.GONE);
    }

    private void initRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvAdminTransactions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setNestedScrollingEnabled(false);

        allTransactions = new ArrayList<>();
        adapter = new AdminTransactionAdapter(new ArrayList<>());
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
            filter("CASH");
        });
        btnStripe.setOnClickListener(v -> {
            updateFilterButtons(btnStripe, btnAll, btnMoMo, btnFailed, btnRefund);
            filter("STRIPE");
        });
        btnFailed.setOnClickListener(v -> {
            updateFilterButtons(btnFailed, btnAll, btnMoMo, btnStripe, btnRefund);
            filter("FAILED");
        });
        btnRefund.setOnClickListener(v -> {
            updateFilterButtons(btnRefund, btnAll, btnMoMo, btnStripe, btnFailed);
            filter("REFUNDED");
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
        List<PaymentResponse> filtered = new ArrayList<>();
        if (criteria.equals("Tất cả")) {
            filtered.addAll(allTransactions);
        } else {
            for (PaymentResponse t : allTransactions) {
                if (criteria.equalsIgnoreCase(t.getPaymentMethod()) || criteria.equalsIgnoreCase(t.getStatus())) {
                    filtered.add(t);
                }
            }
        }
        adapter.updateList(filtered);
    }

    static class AdminTransactionAdapter extends RecyclerView.Adapter<AdminTransactionAdapter.ViewHolder> {
        private final List<PaymentResponse> list;
        AdminTransactionAdapter(List<PaymentResponse> list) { this.list = list; }

        public void updateList(List<PaymentResponse> newList) {
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
            PaymentResponse t = list.get(position);
            holder.tvName.setText("User ID: " + t.getUserId());
            holder.tvIdTime.setText("TXN-" + t.getId() + " · " + t.getCreatedAt());
            
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            holder.tvAmount.setText(currencyFormat.format(t.getAmount()));
            
            String status = t.getStatus();
            holder.tvStatus.setText(status);
            
            String method = t.getPaymentMethod();
            holder.tvIcon.setText(method != null && method.length() >= 2 ? method.substring(0, 2) : "TX");
            
            if ("STRIPE".equalsIgnoreCase(method)) {
                holder.cvIcon.setCardBackgroundColor(Color.parseColor("#6366F1"));
            } else {
                holder.cvIcon.setCardBackgroundColor(Color.parseColor("#A855F7"));
            }

            // Amount color
            if ("FAILED".equalsIgnoreCase(status)) {
                holder.tvAmount.setTextColor(Color.parseColor("#EF4444"));
            } else {
                holder.tvAmount.setTextColor(Color.parseColor("#166534"));
            }

            // Status Badge
            View badge = (View) holder.tvStatus.getParent();
            View dot = holder.itemView.findViewById(R.id.vStatusDot);
            if ("SUCCESS".equalsIgnoreCase(status)) {
                badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F0FDF4")));
                holder.tvStatus.setTextColor(Color.parseColor("#166534"));
                dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));
            } else if ("FAILED".equalsIgnoreCase(status)) {
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
