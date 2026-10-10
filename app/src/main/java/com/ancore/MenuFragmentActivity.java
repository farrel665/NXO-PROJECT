package com.ancore;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class MenuFragmentActivity extends Fragment {

    private ChipGroup container_chip;
    private Chip chip1, chip2, chip3;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_fragment, container, false);
        initialize(view);
        initializeLogic();
        return view;
    }

    private void initialize(View view) {
        container_chip = view.findViewById(R.id.container_chip);
        chip1 = view.findViewById(R.id.chip1);
        chip2 = view.findViewById(R.id.chip2);
        chip3 = view.findViewById(R.id.chip3);
    }

    private void initializeLogic() {
        Typeface semibold = Typeface.createFromAsset(requireContext().getAssets(), "fonts/gfsemibold.ttf");
        Typeface regular = Typeface.createFromAsset(requireContext().getAssets(), "fonts/gfregular.ttf");

        // Font awal — chip1 default terpilih
        chip1.setTypeface(semibold);
        chip2.setTypeface(regular);
        chip3.setTypeface(regular);

        // Update font setiap kali pilihan berubah
        container_chip.setOnCheckedStateChangeListener((group, checkedIds) -> {
            chip1.setTypeface(chip1.isChecked() ? semibold : regular);
            chip2.setTypeface(chip2.isChecked() ? semibold : regular);
            chip3.setTypeface(chip3.isChecked() ? semibold : regular);
        });
    }
}