package com.example.timsanbong.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Field;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class FieldAdapter extends RecyclerView.Adapter<FieldAdapter.FieldViewHolder> {

    public interface OnFieldClickListener {
        void onBook(Field field);
    }

    private List<Field> fields;
    private final OnFieldClickListener listener;

    public FieldAdapter(List<Field> fields, OnFieldClickListener listener) {
        this.fields = fields;
        this.listener = listener;
    }

    public void updateFields(List<Field> newFields) {
        this.fields = newFields;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_field, parent, false);
        return new FieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FieldViewHolder holder, int position) {
        holder.bind(fields.get(position));
    }

    @Override
    public int getItemCount() {
        return fields == null ? 0 : fields.size();
    }

    class FieldViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivFieldImage;
        private final TextView tvFieldName, tvFieldAddress, tvFieldType, tvPrice, tvPriceHint, tvRating;
        private final MaterialButton btnBook;

        FieldViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFieldImage = itemView.findViewById(R.id.ivFieldImage);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            tvFieldAddress = itemView.findViewById(R.id.tvFieldAddress);
            tvFieldType = itemView.findViewById(R.id.tvFieldType);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPriceHint = itemView.findViewById(R.id.tvPriceHint);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnBook = itemView.findViewById(R.id.btnBook);
        }

        void bind(Field field) {
            tvFieldName.setText(field.getName() == null ? "" : field.getName());
            tvFieldAddress.setText(field.getAddress() == null ? "" : field.getAddress());
            tvFieldType.setText(field.getTypeLabel() + " - "
                    + itemView.getContext().getString(R.string.field_surface_artificial));

            double rating = field.getAverageRating() != null ? field.getAverageRating() : 0;
            tvRating.setText(String.format(Locale.US, "%.1f", rating));

            double price = field.getPricePerHour();
            if (price > 0) {
                tvPrice.setText(itemView.getContext().getString(
                        R.string.price_from_format,
                        String.format(Locale.US, "%,.0f", price),
                        itemView.getContext().getString(R.string.currency_vnd)));
                tvPriceHint.setVisibility(View.VISIBLE);
            } else {
                tvPrice.setText(R.string.price_by_slot);
                tvPriceHint.setVisibility(View.GONE);
            }

            Glide.with(itemView.getContext())
                    .load(field.getImageUrl())
                    .placeholder(R.drawable.bg_pitch_cover)
                    .error(R.drawable.bg_pitch_cover)
                    .centerCrop()
                    .into(ivFieldImage);

            boolean available = field.isAvailable();
            itemView.setAlpha(available ? 1f : 0.55f);
            btnBook.setEnabled(available);
            btnBook.setText(available ? R.string.action_book : R.string.booked);
            btnBook.setOnClickListener(v -> {
                if (available) listener.onBook(field);
            });
            itemView.setOnClickListener(v -> {
                if (available) listener.onBook(field);
            });
        }
    }
}
