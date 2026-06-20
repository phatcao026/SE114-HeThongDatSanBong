package com.example.timsanbong.ui.customer;

import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.model.Booking;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public final class FieldReviewDialog {

    public interface OnSubmitListener {
        void onSubmit(Booking booking, int rating, String comment);
    }

    private FieldReviewDialog() {}

    public static void show(Context context, Booking booking, OnSubmitListener listener) {
        int padding = context.getResources().getDimensionPixelSize(R.dimen.spacing_4);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding / 2, padding, 0);

        TextView fieldName = new TextView(context);
        fieldName.setText(booking.getFieldName());
        fieldName.setTextColor(context.getColor(R.color.text_primary));
        fieldName.setTextSize(16);
        fieldName.setTypeface(null, android.graphics.Typeface.BOLD);
        container.addView(fieldName);

        RatingBar ratingBar = new RatingBar(context);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1f);
        ratingBar.setRating(5f);
        LinearLayout.LayoutParams ratingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        ratingParams.topMargin = padding / 2;
        container.addView(ratingBar, ratingParams);

        TextInputLayout commentLayout = new TextInputLayout(context);
        commentLayout.setHint(context.getString(R.string.field_review_comment_hint));
        LinearLayout.LayoutParams commentLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        commentLayoutParams.topMargin = padding;

        TextInputEditText commentInput = new TextInputEditText(context);
        commentInput.setMinLines(3);
        commentInput.setMaxLines(5);
        commentInput.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        commentInput.setSingleLine(false);
        commentLayout.addView(commentInput);
        container.addView(commentLayout, commentLayoutParams);

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.field_review_dialog_title)
                .setView(container)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.action_submit_review, (dialog, which) -> {
                    int rating = Math.max(1, Math.round(ratingBar.getRating()));
                    String comment = commentInput.getText() == null
                            ? ""
                            : commentInput.getText().toString().trim();
                    listener.onSubmit(booking, rating, comment);
                })
                .show();
    }
}
