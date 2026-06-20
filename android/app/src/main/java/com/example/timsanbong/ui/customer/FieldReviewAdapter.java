package com.example.timsanbong.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.FieldReviewResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FieldReviewAdapter extends RecyclerView.Adapter<FieldReviewAdapter.FieldReviewViewHolder> {

    private final List<FieldReviewResponse> reviews = new ArrayList<>();

    public void updateReviews(List<FieldReviewResponse> newReviews) {
        reviews.clear();
        if (newReviews != null) {
            reviews.addAll(newReviews);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FieldReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_field_review, parent, false);
        return new FieldReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FieldReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class FieldReviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvReviewerInitials, tvReviewerName, tvCreatedAt, tvComment, tvRatingValue;
        private final RatingBar rbReviewRating;

        FieldReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewerInitials = itemView.findViewById(R.id.tvReviewerInitials);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvRatingValue = itemView.findViewById(R.id.tvRatingValue);
            rbReviewRating = itemView.findViewById(R.id.rbReviewRating);
        }

        void bind(FieldReviewResponse review) {
            String name = review.getReviewerName() == null || review.getReviewerName().trim().isEmpty()
                    ? itemView.getContext().getString(R.string.review_anonymous)
                    : review.getReviewerName().trim();
            int rating = review.getRating() == null ? 0 : review.getRating();
            String comment = review.getComment() == null ? "" : review.getComment().trim();

            tvReviewerInitials.setText(getInitials(name));
            tvReviewerName.setText(name);
            tvCreatedAt.setText(formatCreatedAt(review.getCreatedAt()));
            rbReviewRating.setRating(rating);
            tvRatingValue.setText(String.format(Locale.US, "%d/5", rating));
            tvComment.setText(comment.isEmpty()
                    ? itemView.getContext().getString(R.string.review_no_comment)
                    : comment);
        }

        private String getInitials(String fullName) {
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length == 0 || parts[0].isEmpty()) {
                return "U";
            }
            if (parts.length == 1) {
                return parts[0].substring(0, 1).toUpperCase(Locale.US);
            }
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                    .toUpperCase(Locale.US);
        }

        private String formatCreatedAt(String createdAt) {
            if (createdAt == null || createdAt.length() < 10) {
                return "";
            }
            String date = createdAt.substring(0, 10);
            String[] parts = date.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
            return date;
        }
    }
}
