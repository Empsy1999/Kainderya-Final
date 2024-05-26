package com.eatery.tarakain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eatery.tarakain.model.Owners;
import com.eatery.tarakain.model.Reviews;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private String BASEURL = "http://maps.google.com/maps?q=16.414646,120.594642";
    private ProgressDialog progressDialog;
    private String budget, category;
    private GoogleMap mMap;
    private final static int LOCATION_REQUEST_CODE = 20;
    boolean locationPermission = false;
    private ArrayList<Owners> ownersArrayList = new ArrayList<>();
    private ReviewsAdapter adapter;
    private String eateryAddress;
    private String fromLatitude, fromLongitude, toLatitude, toLongitude, fromLocName, toLocName;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        budget = getIntent().getStringExtra("budget");
        category = getIntent().getStringExtra("category");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        requestPermission();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getEateryAddress();

    }

    private void getEateryAddress() {
        DatabaseReference eaterDetailRef = FirebaseDatabase.getInstance().getReference().child("Eatery Info");
        eaterDetailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    eateryAddress = snapshot.child("eateryAddress").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        YoYo.with(Techniques.BounceIn)
                .duration(1300)
                .repeatMode(ValueAnimator.REVERSE)
                .playOn(findViewById(R.id.map_img2));

        YoYo.with(Techniques.BounceIn)
                .duration(1500)
                .repeatMode(ValueAnimator.REVERSE)
                .playOn(findViewById(R.id.map_img1));

        YoYo.with(Techniques.BounceIn)
                .duration(1700)
                .repeatMode(ValueAnimator.REVERSE)
                .playOn(findViewById(R.id.map_img4));

        YoYo.with(Techniques.BounceIn)
                .duration(1900)
                .repeatMode(ValueAnimator.REVERSE)
                .playOn(findViewById(R.id.map_img3));

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        getMyLocation();
        progressDialog.dismiss();
    }

    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        addCustomMarkers();

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
                fromLatitude = String.valueOf(location.getLatitude());
                fromLongitude = String.valueOf(location.getLongitude());
//                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
//                        ltlng, 10f);
//                mMap.animateCamera(cameraUpdate);
            }
        });

        LatLng ltlng = new LatLng(16.413599, 120.591616);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                ltlng, 10f);
        mMap.animateCamera(cameraUpdate);

    }

    private void requestPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            locationPermission = true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //if permission granted.
                locationPermission = true;
                getMyLocation();

            } else {

            }
            return;
        }
    }


    private void addCustomMarkers() {
        DatabaseReference ownersRef = FirebaseDatabase.getInstance().getReference().child("Owners");
        ownersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Owners owners = dataSnapshot.getValue(Owners.class);
                        assert owners != null;
                        if (owners.getLatitude() != null && owners.getLongitude() != null){
                            if (!owners.getLongitude().isEmpty() && !owners.getLatitude().isEmpty()){
                                ownersArrayList.add(owners);
                                addEateryLocations(owners);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShowMapActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                for (int i = 0; i < ownersArrayList.size(); i++){
                    Owners owners = ownersArrayList.get(i);
                    if (owners.getLatitude() != null && owners.getLongitude() != null) {
                        LatLng latLng = new LatLng(Double.parseDouble(owners.getLatitude()), Double.parseDouble(owners.getLongitude()));
                        Log.d("==latlang", marker.getPosition() +" : " + latLng);
                        if (latLng.equals(marker.getPosition())){
                            toLatitude = owners.getLatitude();
                            toLongitude = owners.getLongitude();
                            fromLocName = "My Location";
                            toLocName = owners.getEateryName();
                            showStoreMapOptionDialog(owners);
                            showDirectionOnMaps();
                            Log.d("==owne Id", owners.getId() + " " + owners.getEmail());
                            return true;
                        }
                    }
                }
                return false;
            }
        });

    }

    private void addEateryLocations(Owners owners) {
        double latitude = Double.parseDouble(owners.getLatitude());
        double longitude = Double.parseDouble(owners.getLongitude());
        URL url = null;
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.logo);
        try {
            if (owners.getEateryImage() != null && !owners.getEateryImage().isEmpty()) {
                url = new URL(owners.getEateryImage());
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Bitmap bitmap = getResizedBitmap(bmp,150, 150);
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng)
                .title(owners.getEateryName())
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
    }

    private void showStoreMapOptionDialog(Owners owners) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_store_map_option);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        CircleImageView eateryImg = dialog.findViewById(R.id.eatery_img);
        TextView eateryName = dialog.findViewById(R.id.eatery_name);
        TextView ownerEmail = dialog.findViewById(R.id.owner_email);
        AppCompatButton showMenuBtn = dialog.findViewById(R.id.show_menu_btn);
        AppCompatButton showReviewBtn = dialog.findViewById(R.id.show_reviews_btn);
        ImageView closeImg = dialog.findViewById(R.id.close_img);

        closeImg.setOnClickListener(view -> {
            dialog.dismiss();
        });

        Glide.with(this).load(owners.getEateryImage())
                        .placeholder(R.drawable.logo)
                                .into(eateryImg);
        eateryName.setText(owners.getEateryName());
        ownerEmail.setText(eateryAddress);

        showMenuBtn.setOnClickListener(view -> {
            dialog.dismiss();
            startActivity(new Intent(ShowMapActivity.this, ShowMenuActivity.class)
                    .putExtra("ownerId", owners.getId())
                    .putExtra("budget", budget)
                    .putExtra("category", category));
        });

        showReviewBtn.setOnClickListener(view -> {
            dialog.dismiss();
            showOwnerReviewsDialog(owners);
        });

        dialog.show();

    }

    private void showOwnerReviewsDialog(Owners owners) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_show_reviews_list);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView recyclerView = dialog.findViewById(R.id.reviews_list);
        TextView noData = dialog.findViewById(R.id.no_review_txt);
        ImageView closeImg = dialog.findViewById(R.id.close_img);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);


        closeImg.setOnClickListener(view -> {
            dialog.dismiss();
        });

        noData.setText(getString(R.string.loading));
        ArrayList<Reviews> reviewsArrayList = new ArrayList<>();
        getOwnerReviews(owners, recyclerView, reviewsArrayList, layoutManager, noData);

        dialog.show();
    }

    private void getOwnerReviews(Owners owners, RecyclerView recyclerView, ArrayList<Reviews> reviewsArrayList, LinearLayoutManager layoutManager, TextView noData) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Reviews");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewsArrayList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot :  snapshot.getChildren()) {
                        Reviews reviews = dataSnapshot.getValue(Reviews.class);
                        assert reviews != null;
                        if (reviews.getOwnerId() != null) {
                            if (reviews.getOwnerId().equals(owners.getId())) {
                                noData.setVisibility(View.GONE);
                                reviewsArrayList.add(reviews);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
                if (reviewsArrayList.isEmpty()){
                    noData.setText(getString(R.string.no_reviews));
                    noData.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShowMapActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        adapter = new ReviewsAdapter(this, reviewsArrayList, 2);
        recyclerView.setAdapter(adapter);
        LinearSnapHelper linearSnapHelper = new LinearSnapHelper();
        linearSnapHelper.attachToRecyclerView(recyclerView);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (layoutManager.findLastCompletelyVisibleItemPosition() <
                        (adapter.getItemCount() - 1)){
                    layoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(),
                            layoutManager.findLastCompletelyVisibleItemPosition() + 1);
                } else if (layoutManager.findLastCompletelyVisibleItemPosition() ==
                        (adapter.getItemCount() - 1)){
                    layoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(),
                            0);
                }
            }
        },0 ,3000);
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return GetBitmapClippedCircle(resizedBitmap);
    }

    public static Bitmap GetBitmapClippedCircle(Bitmap bitmap) {

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle(
                (float)(width / 2)
                , (float)(height / 2)
                , (float) Math.min(width, (height / 2))
                , Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }

    private void showDirectionOnMaps() {
        if (fromLatitude == null || fromLatitude.isEmpty() ||
                toLatitude == null || toLatitude.isEmpty() ||
                fromLongitude == null || fromLongitude.isEmpty() ||
                toLongitude == null || toLongitude.isEmpty()) {
            Toast.makeText(this, "Please select starting and destination to show directions", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        mMap.clear();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("destination", toLatitude + ", " + toLongitude)
                .appendQueryParameter("origin", fromLatitude + ", " + fromLongitude)
                .appendQueryParameter("mode", "driving")
                .appendQueryParameter("key", getString(R.string.maps_api_key))
                .toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("==dire " + " : " + response);
                try {
                    String status = response.getString("status");
                    if (status.equals("OK")) {
                        JSONArray routes = response.getJSONArray("routes");

                        ArrayList<LatLng> points;
                        PolylineOptions polylineOptions = null;

                        for (int i = 0; i < routes.length(); i++) {
                            points = new ArrayList<>();
                            polylineOptions = new PolylineOptions();
                            JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");

                            String polyline = routes.getJSONObject(i).getJSONObject("overview_polyline").getString("points");
                            List<LatLng> list = decode(polyline);

                            for (int l = 0; l < list.size(); l++) {
                                LatLng position = new LatLng((list.get(l)).latitude, (list.get(l)).longitude);
                                points.add(position);
                            }

//                            for (int j=0;j<legs.length();j++){
//                                JSONArray steps = legs.getJSONObject(j).getJSONArray("steps");
//
//                                for (int k=0;k<steps.length();k++){
//                                    String polyline = routes.getJSONObject(i).getJSONObject("overview_polyline").getString("points");
//                                    List<LatLng> list = decodePoly(polyline);
//
//                                    for (int l=0;l<list.size();l++){
//                                        LatLng position = new LatLng((list.get(l)).latitude, (list.get(l)).longitude);
//                                        points.add(position);
//                                    }
//                                }
//                            }
                            polylineOptions.addAll(points);
                            polylineOptions.width(10);
                            polylineOptions.color(ContextCompat.getColor(ShowMapActivity.this, R.color.menu_item_clr));
                            polylineOptions.geodesic(true);
                        }
                        mMap.addPolyline(polylineOptions);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(fromLatitude), Double.parseDouble(fromLongitude))).title(fromLocName));
                        mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(toLatitude), Double.parseDouble(toLongitude))).title(toLocName));

                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(new LatLng(Double.parseDouble(fromLatitude), Double.parseDouble(fromLongitude)))
                                .include(new LatLng(Double.parseDouble(toLatitude), Double.parseDouble(toLongitude))).build();
                        Point point = new Point();
                        getWindowManager().getDefaultDisplay().getSize(point);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, point.x, 150, 30));
                    }else {
                        Toast.makeText(ShowMapActivity.this, "No Route found between these places. Please select other nearest place.", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.d("==dire", "Error: " + error.getMessage());
            }
        });
        for (int i = 0; i< ownersArrayList.size(); i++){
            Owners owners = ownersArrayList.get(i);
            addEateryLocations(owners);
        }
        RetryPolicy retryPolicy = new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }

    private List<LatLng> decode(final String encodedPath) {
        int len = encodedPath.length();

        // For speed we preallocate to an upper bound on the final length, then
        // truncate the array before returning.
        final List<LatLng> path = new ArrayList<LatLng>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            path.add(new LatLng(lat * 1e-5, lng * 1e-5));
        }

        return path;
    }


}