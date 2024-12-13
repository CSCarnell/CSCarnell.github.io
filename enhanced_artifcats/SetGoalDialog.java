/**
 * Christopher Carnell
 * CS-360
 *
 * This dialog fragment allows users to set or update their goal weight.
 * It collects the desired goal weight from the user.
 * The goal weight is then saved in the database and displayed in the DataDisplayActivity.
 */



package com.cs360.weightwatcher;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SetGoalDialog extends DialogFragment {

    private OnGoalSetListener listener;

    public interface OnGoalSetListener {
        void onGoalSet(float goalWeight);
    }

    public void setOnGoalSetListener(OnGoalSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_set_goal, null);

        EditText editTextGoalWeight = view.findViewById(R.id.editTextGoalWeight);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view)
                .setTitle("Set Goal Weight")
                .setPositiveButton("Save", (dialog, id) -> {
                    String goalWeightText = editTextGoalWeight.getText().toString();

                    if (TextUtils.isEmpty(goalWeightText)) {
                        Toast.makeText(getActivity(), "Please enter your goal weight", Toast.LENGTH_SHORT).show();
                    } else {
                        float goalWeight = Float.parseFloat(goalWeightText);

                        if (listener != null) {
                            listener.onGoalSet(goalWeight);
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.dismiss();
                });

        return builder.create();
    }
}