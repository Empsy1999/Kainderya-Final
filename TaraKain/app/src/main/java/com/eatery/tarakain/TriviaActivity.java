package com.eatery.tarakain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.eatery.tarakain.model.Trivia;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class TriviaActivity extends AppCompatActivity {
    private ImageView backImg;
    private RecyclerView recyclerView;
    private TriviaAdapter adapter;
    private ArrayList<Trivia> triviaArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);

        backImg = findViewById(R.id.go_back);
        recyclerView = findViewById(R.id.trivia_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false
        ));

        getTriviaPost();

        backImg.setOnClickListener(view -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

    }

    private void getTriviaPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Trivia");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                triviaArrayList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Trivia trivia = dataSnapshot.getValue(Trivia.class);
                        assert trivia != null;
                        triviaArrayList.add(trivia);
                        Collections.sort(triviaArrayList, (t1, t2) -> {
                            try {
                                Date start = new SimpleDateFormat("yyyyddMMHHmmss")
                                        .parse(t1.getDateTime());
                                Date end = new SimpleDateFormat("yyyyddMMHHmmss")
                                        .parse(t2.getDateTime());
                                Log.d("== sts", start + " " + end);
                                assert end != null;
                                return end.compareTo(start);
                            } catch (ParseException e) {
                                return t2.getDateTime().compareToIgnoreCase(t1.getDateTime());
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                }else {
                    Toast.makeText(TriviaActivity.this, "No trivia.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TriviaActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        adapter = new TriviaAdapter(this, triviaArrayList);
        recyclerView.setAdapter(adapter);
    }


}