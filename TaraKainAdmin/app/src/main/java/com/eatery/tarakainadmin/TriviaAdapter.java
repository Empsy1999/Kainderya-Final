package com.eatery.tarakainadmin;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TriviaAdapter extends RecyclerView.Adapter<TriviaAdapter.ViewHolder> {
    Context context;
    ArrayList<Trivia> triviaArrayList;
    TriviaInterface triviaInterface;

    public TriviaAdapter(Context context, ArrayList<Trivia> triviaArrayList, TriviaInterface triviaInterface) {
        this.context = context;
        this.triviaArrayList = triviaArrayList;
        this.triviaInterface = triviaInterface;
    }

    @NonNull
    @Override
    public TriviaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_trivia_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TriviaAdapter.ViewHolder holder, int position) {
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
        holder.editTrivia.setOnClickListener(view -> {
            triviaInterface.onSetValues(trivia);
        });

        holder.deleteTrivia.setOnClickListener(view -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Trivia");
            reference.child(trivia.getId()).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Toast.makeText(context, "Trivia Deleted", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context, "Unable to delete. Try again later...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Error: " +  e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return triviaArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView triviaDate, triviaTitle, triviaDescription;
        LinearLayout editTrivia, deleteTrivia;
        ImageView triviaImg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            triviaDate = itemView.findViewById(R.id.trivia_date);
            triviaTitle = itemView.findViewById(R.id.trivia_title);
            triviaDescription = itemView.findViewById(R.id.trivia_description);
            editTrivia = itemView.findViewById(R.id.edit_trivia);
            deleteTrivia = itemView.findViewById(R.id.delete_trivia);
            triviaImg = itemView.findViewById(R.id.trivia_image);
        }
    }

    public interface TriviaInterface {
        void onSetValues(Trivia t);
    }

}
