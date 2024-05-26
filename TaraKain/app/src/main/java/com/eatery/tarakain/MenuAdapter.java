package com.eatery.tarakain;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eatery.tarakain.model.Menus;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    Context context;
    ArrayList<Menus> menusArrayList;

    public MenuAdapter(Context context, ArrayList<Menus> menusArrayList) {
        this.context = context;
        this.menusArrayList = menusArrayList;
    }

    @NonNull
    @Override
    public MenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_menu_items, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MenuAdapter.ViewHolder holder, int position) {
        Menus model = menusArrayList.get(position);
        Glide.with(context)
                .load(model.getIdPicUrl())
                .into(holder.menuImgTop);
        Glide.with(context)
                .load(model.getIdPicUrl())
                .into(holder.menuImgBottom);
        holder.menuPrice.setText(context.getString(R.string.price_php) + model.getPrice());
        holder.menuDesc.setText(model.getDescription());
        holder.menuNoTxt.setText(context.getString(R.string.menu) + " " + (position + 1));
        if (position <= 2 && position % 2 == 1) {
            holder.menuImgTop.setVisibility(View.GONE);
            holder.menuImgBottom.setVisibility(View.VISIBLE);
        }

        YoYo.with(Techniques.Pulse)
                .repeat(-1)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(8000)
                .playOn(holder.menuImgTop);

        YoYo.with(Techniques.Pulse)
                .repeat(-1)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(8000)
                .playOn(holder.menuImgBottom);

        if (position > 2) {
            holder.menuImgTop.setVisibility(View.VISIBLE);
            holder.menuImgBottom.setVisibility(View.GONE);
            if (position % 2 == 0) {
                holder.menuImgTop.setVisibility(View.GONE);
                holder.menuImgBottom.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return menusArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView menuImgTop, menuImgBottom;
        public TextView menuNoTxt, menuDesc, menuPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            menuImgTop = itemView.findViewById(R.id.menu_img_top);
            menuImgBottom = itemView.findViewById(R.id.menu_img_bottom);
            menuNoTxt = itemView.findViewById(R.id.menu_no_txt);
            menuDesc = itemView.findViewById(R.id.menu_desc);
            menuPrice = itemView.findViewById(R.id.menu_price);
        }
    }
}
