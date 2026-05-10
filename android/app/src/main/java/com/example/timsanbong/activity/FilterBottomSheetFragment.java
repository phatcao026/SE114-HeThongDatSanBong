package com.example.timsanbong.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.timsanbong.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    public interface OnFilterApplyListener {
        void onFilterApply(MainActivity.FilterState state);
    }

    private static final String ARG_FIELD_TYPE  = "arg_field_type";
    private static final String ARG_PRICE_RANGE = "arg_price_range";
    private static final String ARG_AVAILABLE   = "arg_available";

    private ChipGroup chipGroupType, chipGroupPrice;
    private Chip chipAvailableOnly;
    private OnFilterApplyListener listener;

    static FilterBottomSheetFragment newInstance(MainActivity.FilterState state) {
        FilterBottomSheetFragment f = new FilterBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FIELD_TYPE, state.fieldType);
        args.putInt(ARG_PRICE_RANGE, state.priceRange);
        args.putBoolean(ARG_AVAILABLE, state.availableOnly);
        f.setArguments(args);
        return f;
    }

    void setOnFilterApplyListener(OnFilterApplyListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheet_Timsanbong;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipGroupType     = view.findViewById(R.id.chipGroupType);
        chipGroupPrice    = view.findViewById(R.id.chipGroupPrice);
        chipAvailableOnly = view.findViewById(R.id.chipAvailableOnly);

        Bundle args = getArguments();
        if (args != null) {
            restoreTypeSelection(args.getString(ARG_FIELD_TYPE));
            restorePriceSelection(args.getInt(ARG_PRICE_RANGE));
            chipAvailableOnly.setChecked(args.getBoolean(ARG_AVAILABLE));
        } else {
            chipGroupType.check(R.id.chipTypeAll);
            chipGroupPrice.check(R.id.chipPriceAll);
        }

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.btnReset).setOnClickListener(v -> {
            if (listener != null) listener.onFilterApply(new MainActivity.FilterState());
            dismiss();
        });

        view.findViewById(R.id.btnApply).setOnClickListener(v -> {
            if (listener != null) listener.onFilterApply(buildFilterState());
            dismiss();
        });
    }

    private void restoreTypeSelection(String fieldType) {
        if ("5 người".equals(fieldType))       chipGroupType.check(R.id.chipType5);
        else if ("7 người".equals(fieldType))  chipGroupType.check(R.id.chipType7);
        else if ("11 người".equals(fieldType)) chipGroupType.check(R.id.chipType11);
        else                                   chipGroupType.check(R.id.chipTypeAll);
    }

    private void restorePriceSelection(int priceRange) {
        if (priceRange == 1)      chipGroupPrice.check(R.id.chipPriceUnder200);
        else if (priceRange == 2) chipGroupPrice.check(R.id.chipPrice200to300);
        else if (priceRange == 3) chipGroupPrice.check(R.id.chipPriceOver300);
        else                      chipGroupPrice.check(R.id.chipPriceAll);
    }

    private MainActivity.FilterState buildFilterState() {
        MainActivity.FilterState state = new MainActivity.FilterState();

        int typeId = chipGroupType.getCheckedChipId();
        if (typeId == R.id.chipType5)       state.fieldType = "5 người";
        else if (typeId == R.id.chipType7)  state.fieldType = "7 người";
        else if (typeId == R.id.chipType11) state.fieldType = "11 người";

        int priceId = chipGroupPrice.getCheckedChipId();
        if (priceId == R.id.chipPriceUnder200)    state.priceRange = 1;
        else if (priceId == R.id.chipPrice200to300) state.priceRange = 2;
        else if (priceId == R.id.chipPriceOver300)  state.priceRange = 3;

        state.availableOnly = chipAvailableOnly.isChecked();

        return state;
    }
}
