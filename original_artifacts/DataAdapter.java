/**
 * Christopher Carnell
 * CS-360
 *
 * This adapter class is used to bind weight entry data to the RecyclerView in DataDisplayActivity.
 * It manages the display of weight entries in a list format.
 * It handles user interactions such as deleting entries from the list and the database.
 */


package com.cs360.weightwatcher;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {

    private ArrayList<WeightEntry> weightEntries;
    private DatabaseManager dbManager;

    public DataAdapter(ArrayList<WeightEntry> weightEntries, DatabaseManager dbManager) {
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

        //delete button action
        holder.buttonDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                //get the WeightEntry to delete
                WeightEntry entryToDelete = weightEntries.get(adapterPosition);

                //delete from database
                int result = dbManager.deleteWeightEntry(entryToDelete.getId());
                if (result > 0) {
                    //remove from list and notify adapter
                    weightEntries.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                    notifyItemRangeChanged(adapterPosition, weightEntries.size());
                    Toast.makeText(v.getContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(), "Failed to delete entry", Toast.LENGTH_SHORT).show();
                }
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
