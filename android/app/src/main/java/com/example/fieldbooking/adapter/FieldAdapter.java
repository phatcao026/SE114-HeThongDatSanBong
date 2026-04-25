package com.example.fieldbooking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fieldbooking.R;
import com.example.fieldbooking.model.Field;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class FieldAdapter extends RecyclerView.Adapter<FieldAdapter.FieldViewHolder> {

    public interface OnFieldClickListener {
        void onBook(Field field);
    }

    private final List<Field> fields;
    private final OnFieldClickListener listener;

    public FieldAdapter(List<Field> fields, OnFieldClickListener listener) {
        this.fields = fields;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_field, parent, false);
        return new FieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FieldViewHolder holder, int position) {
        holder.bind(fields.get(position));
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    class FieldViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivFieldImage;
        private final TextView tvFieldName, tvFieldAddress, tvPrice;
        private final MaterialButton btnBook;

        FieldViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFieldImage = itemView.findViewById(R.id.ivFieldImage);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            tvFieldAddress = itemView.findViewById(R.id.tvFieldAddress);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnBook = itemView.findViewById(R.id.btnBook);
        }

        void bind(Field field) {
            tvFieldName.setText(field.getName());
            tvFieldAddress.setText(field.getAddress());
            tvPrice.setText(String.format("%,.0f đ/giờ", field.getPricePerHour()));

            Glide.with(itemView.getContext())
                    .load(field.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(ivFieldImage);

            btnBook.setOnClickListener(v -> listener.onBook(field));
            itemView.setOnClickListener(v -> listener.onBook(field));
        }
    }
}
