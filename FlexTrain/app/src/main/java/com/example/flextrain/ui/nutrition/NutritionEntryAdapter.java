package com.example.flextrain.ui.nutrition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.flextrain.R;

import java.util.List;

public class NutritionEntryAdapter extends ArrayAdapter<NutritionEntry> {

    private LayoutInflater inflater;
    private ModifyDeleteClickListener modifyDeleteClickListener;

    public NutritionEntryAdapter(Context context, List<NutritionEntry> entries) {
        super(context, 0, entries);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        ViewHolder holder;

        if (itemView == null) {
            itemView = inflater.inflate(R.layout.item_food, parent, false);
            holder = new ViewHolder();
            holder.textViewEntry = itemView.findViewById(R.id.textViewEntry);
            holder.modifyButton = itemView.findViewById(R.id.modifyButtonFood);
            holder.deleteButton = itemView.findViewById(R.id.deleteButtonFood);
            itemView.setTag(holder);
        } else {
            holder = (ViewHolder) itemView.getTag();
        }

        // Get the current entry
        NutritionEntry entry = getItem(position);

        // Display entry details
        if (entry != null) {
            holder.textViewEntry.setText(entry.toString());
        }

        //  "Modify" button click
        holder.modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modifyDeleteClickListener != null) {
                    modifyDeleteClickListener.onModifyClicked(entry);
                }
            }
        });

        //  "Delete" button click
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modifyDeleteClickListener != null) {
                    modifyDeleteClickListener.onDeleteClicked(entry);
                }
            }
        });

        return itemView;
    }

    static class ViewHolder {
        TextView textViewEntry;
        Button modifyButton;
        Button deleteButton;
    }

    public interface ModifyDeleteClickListener {
        void onModifyClicked(NutritionEntry entry);

        void onDeleteClicked(NutritionEntry entry);
    }

    public void setModifyDeleteClickListener(ModifyDeleteClickListener listener) {
        this.modifyDeleteClickListener = listener;
    }
}
