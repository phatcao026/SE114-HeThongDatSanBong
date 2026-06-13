package com.example.timsanbong.ui.owner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Field;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OwnerFieldAdapter extends RecyclerView.Adapter<OwnerFieldAdapter.OwnerFieldViewHolder> {
    public interface Listener {
        void onEditField(Field field);
        void onDeleteField(Field field);
        void onManageTimeSlots(Field field);
    }

    private final List<Field> fields = new ArrayList<>();
    private final Listener listener;

    public OwnerFieldAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Field> newFields) {
        fields.clear();
        if (newFields != null) {
            fields.addAll(newFields);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OwnerFieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_owner_field, parent, false);
        return new OwnerFieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OwnerFieldViewHolder holder, int position) {
        holder.bind(fields.get(position));
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    class OwnerFieldViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFieldName;
        private final TextView tvFieldAddress;
        private final TextView tvFieldType;
        private final TextView tvFieldStatus;
        private final MaterialButton btnEditField;
        private final MaterialButton btnDeleteField;
        private final MaterialButton btnManageSlots;

        OwnerFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFieldName = itemView.findViewById(R.id.tvOwnerFieldName);
            tvFieldAddress = itemView.findViewById(R.id.tvOwnerFieldAddress);
            tvFieldType = itemView.findViewById(R.id.tvOwnerFieldType);
            tvFieldStatus = itemView.findViewById(R.id.tvOwnerFieldStatus);
            btnEditField = itemView.findViewById(R.id.btnEditOwnerField);
            btnDeleteField = itemView.findViewById(R.id.btnDeleteOwnerField);
            btnManageSlots = itemView.findViewById(R.id.btnManageOwnerSlots);
        }

        void bind(Field field) {
            tvFieldName.setText(nonEmpty(field.getName(), "Chua co ten san"));
            tvFieldAddress.setText(nonEmpty(field.getAddress(), "Chua co dia chi"));
            tvFieldType.setText(nonEmpty(field.getFieldType(), "Chua co loai san"));
            tvFieldStatus.setText(field.isAvailable() ? "Dang mo" : "Tam dung");

            btnEditField.setOnClickListener(v -> listener.onEditField(field));
            btnDeleteField.setOnClickListener(v -> listener.onDeleteField(field));
            btnManageSlots.setOnClickListener(v -> listener.onManageTimeSlots(field));
        }

        private String nonEmpty(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
