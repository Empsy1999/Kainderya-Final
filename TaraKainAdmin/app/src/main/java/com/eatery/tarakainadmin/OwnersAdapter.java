package com.eatery.tarakainadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OwnersAdapter extends RecyclerView.Adapter<OwnersAdapter.ViewHolder> {
    Context context;
    ArrayList<Owners> ownersArrayList;
    DataTransferInterface dataTransferInterface;

    public OwnersAdapter(Context context, ArrayList<Owners> ownersArrayList, DataTransferInterface dataTransferInterface) {
        this.context = context;
        this.ownersArrayList = ownersArrayList;
        this.dataTransferInterface = dataTransferInterface;
    }

    @NonNull
    @Override
    public OwnersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.user_name_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OwnersAdapter.ViewHolder holder, int position) {
        if (position <= ownersArrayList.size() - 1) {
            holder.ownerName.setText(ownersArrayList.get(position).getOwnerName());
            holder.ownerName.setOnClickListener(view -> {
                dataTransferInterface.onSetValues(ownersArrayList.get(position));
            });
        }
    }

    @Override
    public int getItemCount() {
        return ownersArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView ownerName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ownerName = itemView.findViewById(R.id.owner_name);
        }
    }

    public interface DataTransferInterface {
        void onSetValues(Owners owners);
        void onSetValues(String id);
    }

}
