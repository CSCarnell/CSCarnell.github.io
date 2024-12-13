/**
 * Christopher Carnell
 *
 * This adapter class is used to bind weight entry data to the RecyclerView in DataDisplayActivity.
 * It manages the display of weight entries in a list format.
 * It handles user interactions such as deleting entries from the list and the database.
 */

package com.cs360.weightwatcher;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {

    private final List<WeightEntry> weightEntries;
    private final DatabaseManager dbManager;

    public DataAdapter(List<WeightEntry> weightEntries, DatabaseManager dbManager) {
        this.weightEntries = weightEntries;
        this.dbManager = dbManager;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data_entry, parent, false);
        return new DataViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        WeightEntry entry = weightEntries.get(position);
        holder.textViewDate.setText(entry.getDate());
        holder.textViewWeight.setText(String.valueOf(entry.getWeight()));

        // Delete button action with confirmation dialog and exception handling
        holder.buttonDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                // Show confirmation dialog before deletion
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            try {
                                // Get the WeightEntry to delete
                                WeightEntry entryToDelete = weightEntries.get(adapterPosition);

                                // Delete from database
                                int result = dbManager.deleteWeightEntry(entryToDelete.getId());
                                if (result > 0) {
                                    // Remove from list and notify adapter
                                    weightEntries.remove(adapterPosition);
                                    notifyItemRemoved(adapterPosition);
                                    Toast.makeText(v.getContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(v.getContext(), "Failed to delete entry", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(v.getContext(), "An error occurred while deleting the entry", Toast.LENGTH_SHORT).show();
                                Log.e("DataAdapter", "Error in onBindViewHolder ", e);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return weightEntries.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewDate;
        public TextView textViewWeight;
        public Button buttonDelete;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewWeight = itemView.findViewById(R.id.textViewWeight);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
