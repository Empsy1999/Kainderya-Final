package com.eatery.tarakainadmin.notification;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.eatery.tarakainadmin.R;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {
    static RequestQueue requestQueue;
    public static void newVolleyRequestQueue(Activity activity){
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(activity);
        }
    }

    public static void sendPushNotification(Context context, String userId,
                                            String senderId, String receiverId, String title, String body,
                                            String type) {
        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(receiverId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Token token = dataSnapshot.getValue(Token.class);
                        Data data = new Data(senderId, body, title, receiverId, type, R.drawable.logo);
                        assert token != null;
                        Sender sender = new Sender(data, token.getToken());
                        Log.d("==not", receiverId + ":" + token.getToken());
                        try {
                            JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                            //noinspection deprecation
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send",
                                    jsonObject,
                                    response -> {
                                        Log.d("==response", response.toString());
                                        saveNotificationToDatabase(userId, senderId, receiverId, title, body, "unread");
                                    },
                                    error -> Log.d("onResponse%s", error.toString())) {
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("Content-Type", "application/json");
                                    headers.put("Authorization", "key=" + context.getString(R.string.server_key));
                                    return headers;
                                }
                            };
                            requestQueue.add(jsonObjectRequest);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void saveNotificationToDatabase(String userId,
                                                  String senderId, String receiverId,
                                                  String title, String body, String status) {
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        String id = notificationRef.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", id);
        hashMap.put("userId", userId);
        hashMap.put("senderId", senderId);
        hashMap.put("receiverId", receiverId);
        hashMap.put("title", title);
        hashMap.put("body", body);
        hashMap.put("status", status);
        hashMap.put("date", getCurrentDateTime());
        assert id != null;
        notificationRef.child(id).updateChildren(hashMap);
    }

    public static String getCurrentDateTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyddMMHHmmss");
        return dateFormat.format(c.getTime());
    }

}
