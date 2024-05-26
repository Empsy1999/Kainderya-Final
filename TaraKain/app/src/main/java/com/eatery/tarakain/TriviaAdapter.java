package com.eatery.tarakain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eatery.tarakain.model.Trivia;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TriviaAdapter extends RecyclerView.Adapter<TriviaAdapter.ViewHolder> {
    Context context;
    ArrayList<Trivia> triviaArrayList;

    public TriviaAdapter(Context context, ArrayList<Trivia> triviaArrayList) {
        this.context = context;
        this.triviaArrayList = triviaArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_trivia_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trivia trivia = triviaArrayList.get(position);
        holder.triviaDate.setText(trivia.getDate());
        holder.triviaTitle.setText(trivia.getTitle());
        holder.triviaDescription.setText(trivia.getDescription());
        if (trivia.getImage() != null) {
            Glide.with(context).load(trivia.getImage()).into(holder.triviaImg);
            holder.triviaImg.setVisibility(View.VISIBLE);
        }else {
            holder.triviaImg.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return triviaArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView triviaDate, triviaTitle, triviaDescription;
        ImageView triviaImg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            triviaDate = itemView.findViewById(R.id.trivia_date);
            triviaTitle = itemView.findViewById(R.id.trivia_title);
            triviaDescription = itemView.findViewById(R.id.trivia_description);
            triviaImg = itemView.findViewById(R.id.trivia_image);
        }
    }

}
