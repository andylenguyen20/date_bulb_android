package com.sandy_rock_studios.macbookair.randomdateideagenerator.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.interfaces.JSONResponseHandler;

import org.json.JSONObject;

import java.util.List;

public class RequestHandler {
    public static final String TAG = "RequestHandler";
    private static final String API_KEY = "AIzaSyCesDdvSQdcOpFHvXtr1EXKW85KNMbbjXs";
    private static final double METERS_PER_MILE = 1609.3;

    private JSONResponseHandler myHandler;
    private FirebaseDatabase myDatabase;
    private FirebaseUser myUser;
    private PlaceDetectionClient myPlaceDetectionClient;

    public RequestHandler(Context context, JSONResponseHandler handler){
        myHandler = handler;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        myUser = auth.getCurrentUser();
        myDatabase = FirebaseDatabase.getInstance();
        myPlaceDetectionClient = Places.getPlaceDetectionClient(context);
    }

    private String getNearbyPlacesSearchUrl(double lat, double lon, int radius, String placeType){
        StringBuilder url = new StringBuilder();
        url.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        url.append("location=" + lat + "," + lon);
        url.append("&radius=" + radius * METERS_PER_MILE);
        url.append("&type=" + placeType);
        url.append("&key=" + API_KEY);
        return url.toString();
    }

    public void requestNearbyPlacesInformationCurrentLocation(final RequestQueue queue, final String tag, final int radius, final String type) throws SecurityException{
        //this throws a SecurityException if user did not grant permission to get current location
        Task<PlaceLikelihoodBufferResponse> placeResult = myPlaceDetectionClient.getCurrentPlace(null);
        placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>(){
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                if(task.isSuccessful()){
                    PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                    LatLng latLng = likelyPlaces.get(0).getPlace().getLatLng();
                    double latitude = latLng.latitude;
                    double longitude = latLng.longitude;
                    sendRequest(queue, tag, getNearbyPlacesSearchUrl(latitude, longitude, radius, type));
                    likelyPlaces.release();
                }
            }
        });
    }

    public void requestNearbyPlacesInformationOtherLocation(final RequestQueue queue, final String tag, final int radius, final String type){
        final DatabaseReference userRef = myDatabase.getReference("users").child(myUser.getUid());

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double latitude = dataSnapshot.child("last_selected_location").child("lat").getValue(Double.class);
                double longitude = dataSnapshot.child("last_selected_location").child("lng").getValue(Double.class);
                Log.w(TAG, "lat: " + latitude + "long: " + longitude);
                sendRequest(queue, tag, getNearbyPlacesSearchUrl(latitude, longitude, radius, type));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        };
        userRef.addListenerForSingleValueEvent(valueEventListener);
    }

    private void sendRequest(RequestQueue queue, String tag, String requestUrl){

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, requestUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        myHandler.handleJSONResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w(TAG, error.getMessage());
                    }
                });

        jsonObjectRequest.setTag(tag);
        queue.add(jsonObjectRequest);

    }
}
