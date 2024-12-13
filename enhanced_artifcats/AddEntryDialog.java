/**
 * Christopher Carnell
 *
 * This dialog fragment allows users to add a new weight entry.
 * It collects the user's current weight and records the date automatically.
 * The entered weight is passed back to the DataDisplayActivity to be saved in the database.
 */

package com.cs360.weightwatcher;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;



public class AddEntryDialog extends DialogFragment {

    private OnEntryAddedListener listener;
    private long userId;

    public interface OnEntryAddedListener {
        void onEntryAdded(WeightEntry weightEntry);
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setOnEntryAddedListener(OnEntryAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_entry, null);

        EditText editTextWeight = view.findViewById(R.id.editTextWeight);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view)
                .setTitle("Add Weight Entry")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String weightText = editTextWeight.getText().toString();

                if (TextUtils.isEmpty(weightText)) {
                    Toast.makeText(getActivity(), "Please enter your weight", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        double weight = Double.parseDouble(weightText);
                        if (weight <= 0) {
                            Toast.makeText(getActivity(), "Please enter a positive weight", Toast.LENGTH_SHORT).show();
                        } else {
                            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                            WeightEntry weightEntry = new WeightEntry(userId, currentDate, weight);

                            if (listener != null) {
                                listener.onEntryAdded(weightEntry);
                            }
                            dialog.dismiss();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getActivity(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
        });

        return dialog;
    }
}
