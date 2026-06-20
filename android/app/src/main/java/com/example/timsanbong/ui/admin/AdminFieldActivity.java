package com.example.timsanbong.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.Field;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFieldActivity extends AppCompatActivity {

    private AdminFieldAdapter adapter;
    private List<Field> allFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_field);

        RecyclerView rv = findViewById(R.id.rvAdminFields);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        allFields = new ArrayList<>();
        adapter = new AdminFieldAdapter(allFields);
        rv.setAdapter(adapter);

        fetchFields();

        findViewById(R.id.cvAdminAvatar).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });

        AdminNavBarManager navBarManager = new AdminNavBarManager(this, AdminNavBarManager.ITEM_AUDIT);
        navBarManager.setup();
    }

    private void fetchFields() {
        ApiClient.getService(this).getAdminFields().enqueue(new Callback<List<Field>>() {
            @Override
            public void onResponse(Call<List<Field>> call, Response<List<Field>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allFields.clear();
                    allFields.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    ((TextView) findViewById(R.id.tvFieldCount)).setText(allFields.size() + " sân bóng");
                } else {
                    Toast.makeText(AdminFieldActivity.this, "Không thể tải danh sách sân bóng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Field>> call, Throwable t) {
                Toast.makeText(AdminFieldActivity.this, "Lỗi kết nối - Sử dụng dữ liệu demo", Toast.LENGTH_SHORT).show();
                setupDemoFields();
            }
        });
    }

    private void setupDemoFields() {
        allFields.clear();
        allFields.add(new Field(1, "Sân Trần Bình", "Quận 1", 250000, "", "Sân cỏ nhân tạo", "Sân 7", true));
        allFields.add(new Field(2, "Sân Phú Mỹ Hưng", "Quận 7", 400000, "", "Sân chuẩn quốc tế", "Sân 7", true));
        adapter.notifyDataSetChanged();
        ((TextView) findViewById(R.id.tvFieldCount)).setText(allFields.size() + " sân bóng");
    }

    static class AdminFieldAdapter extends RecyclerView.Adapter<AdminFieldAdapter.ViewHolder> {
        private final List<Field> fields;
        AdminFieldAdapter(List<Field> fields) { this.fields = fields; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_field, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Field f = fields.get(position);
            holder.tvFieldName.setText(f.getName());
            holder.tvFieldAddress.setText(f.getAddress());
            holder.tvFieldInfo.setText(String.format(java.util.Locale.getDefault(), "%s · %,.0fđ", f.getTypeLabel(), f.getPricePerHour()));
            holder.tvFieldStatus.setText(f.isAvailable() ? "Hoạt động" : "Ngừng hoạt động");
            
            if (!f.isAvailable()) {
                holder.tvFieldStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEF2F2")));
                holder.tvFieldStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));
            }
        }

        @Override
        public int getItemCount() { return fields.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvFieldName, tvFieldAddress, tvFieldInfo, tvFieldStatus;
            ViewHolder(View v) {
                super(v);
                tvFieldName = v.findViewById(R.id.tvFieldName);
                tvFieldAddress = v.findViewById(R.id.tvFieldAddress);
                tvFieldInfo = v.findViewById(R.id.tvFieldInfo);
                tvFieldStatus = v.findViewById(R.id.tvFieldStatus);
            }
        }
    }
}
