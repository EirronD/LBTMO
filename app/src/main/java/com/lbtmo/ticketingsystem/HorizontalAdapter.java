package com.lbtmo.ticketingsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import java.util.List;

public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.ViewHolder> {

    private final List<ItemHorizontalRecyclerViewModel> itemList;
    private final Context context;

    public HorizontalAdapter(Context context, List<ItemHorizontalRecyclerViewModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemHorizontalRecyclerViewModel item = itemList.get(position);
        holder.itemTitle.setText(item.getTitle());

        // Use Glide to load the image with a circular transformation
        Glide.with(context)
                .load(item.getImageResId())
                .transform(new CircleCrop())
                .into(holder.itemImage);

        // Set OnClickListener to show dialog on item click
        holder.itemView.setOnClickListener(v -> showDialog(item));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // Method to show dialog with item details
    private void showDialog(ItemHorizontalRecyclerViewModel item) {
        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_home_user_recycler, null);

        // Set up the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        // Find views in the dialog layout
        ImageView dialogImage = dialogView.findViewById(R.id.dialogImage);
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogDescription = dialogView.findViewById(R.id.dialogDescription);
        Button okButton = dialogView.findViewById(R.id.okButton);

        // Set the item data in the dialog
        Glide.with(context).load(item.getImageResId()).into(dialogImage);
        dialogTitle.setText(item.getTitle());
        dialogDescription.setText(item.getDescription()); // Ensure this method is available in your model

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set the OK button to dismiss the dialog
        okButton.setOnClickListener(v -> dialog.dismiss());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemTitle = itemView.findViewById(R.id.item_title);
        }
    }
}
