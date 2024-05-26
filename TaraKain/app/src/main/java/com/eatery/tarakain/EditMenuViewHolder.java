package com.eatery.tarakain;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EditMenuViewHolder extends RecyclerView.ViewHolder {
    public TextView title, description, price, category;
    public ImageView menuImg, editImg, deleteImg;
    public LinearLayout buttonLayout, menuInfoLayout;

    public EditMenuViewHolder(@NonNull View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.update_title);
        description = itemView.findViewById(R.id.update_desc);
        price = itemView.findViewById(R.id.update_price);
        category = itemView.findViewById(R.id.update_category);
        menuImg = itemView.findViewById(R.id.update_image);
        editImg = itemView.findViewById(R.id.edit_menu_item);
        deleteImg = itemView.findViewById(R.id.delete_menu_item);
        buttonLayout = itemView.findViewById(R.id.button_layout);
        menuInfoLayout = itemView.findViewById(R.id.menu_info_layout);
    }
}
