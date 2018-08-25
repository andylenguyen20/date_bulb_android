package com.sandy_rock_studios.macbookair.randomdateideagenerator.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DateIdea {
    public interface CompletionHandler{
        void handleCompletion(Object created);
    }
    private static final String TAG = "DateIdea";
    // instantiated in constructor
    private GeoDataClient myGeoDataClient;
    private String myDateIdeaStr;
    private String myPlaceID;
    private String myActivity;
    // instantiated later through method calls
    private Place myPlace;
    private PlacePhoto[] myPhotos;
    private int myPhotoIndex;
    private boolean myFavorited;
    private List<PlacePhotoMetadata> myPhotoMetadataList;

    public DateIdea(String dateStr, Context context){
        myGeoDataClient = Places.getGeoDataClient(context);
        myDateIdeaStr = dateStr;
        myPlaceID = dateStr.split(DataParser.DEFAULT_DELIMITER)[0];
        myActivity = dateStr.split(DataParser.DEFAULT_DELIMITER)[1];
    }

    public void getPlace(final CompletionHandler handler){
        if(myPlace != null && myPlace.isDataValid()){
            handler.handleCompletion(myPlace);
        }else{
            getPlaceFromID(myPlaceID, new CompletionHandler() {
                @Override
                public void handleCompletion(Object created) {
                    myPlace = (Place) created;
                    if (myPlace.isDataValid()){
                        handler.handleCompletion(myPlace);
                    }
                }
            });
        }
    }

    public void favorited(final CompletionHandler handler){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        final DatabaseReference favoritedUserRef = database.getReference("favorited").child(user.getUid());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myFavorited = dataSnapshot.child(myDateIdeaStr).exists();
                handler.handleCompletion(myFavorited);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        };
        favoritedUserRef.addListenerForSingleValueEvent(valueEventListener);
    }

    public String getActivity(){
        return myActivity;
    }

    public void getPhotoFromOffset(int offset, final CompletionHandler handler){
        if(myPhotos == null){
            getPhotosFromID(myPlaceID, new CompletionHandler(){
                @Override
                public void handleCompletion(Object created) {
                    myPhotoMetadataList = (List<PlacePhotoMetadata>) created;  // initialize myPhotoMetadataList for first time here
                    if(myPhotoMetadataList.size() == 0){
                        return;
                    }
                    myPhotos = new PlacePhoto[myPhotoMetadataList.size()];     // initialize myPhotos for first time here
                    myPhotoIndex = 0;                                          // grab first photo always
                    getPhotoFromIndex(myPhotoIndex, handler);
                }
            });
        }else{
            int futureIndex = myPhotoIndex + offset;
            if(myPhotos.length == 0 || futureIndex < 0 || futureIndex >= myPhotos.length){
                return;
            }
            myPhotoIndex = futureIndex;
            getPhotoFromIndex(myPhotoIndex, handler);
        }
    }

    public void save(){
        new FirebaseWriter().writeSavedIdea(myDateIdeaStr);
    }

    public void delete(){
        new FirebaseWriter().writeDiscardedIdea(myDateIdeaStr);
    }

    public void toggleFavorite(){
        if(myFavorited){
            myFavorited = false;
            new FirebaseWriter().unFavoriteIdea(myDateIdeaStr);
        }else{
            myFavorited = true;
            new FirebaseWriter().writeFavoritedIdea(myDateIdeaStr);
        }
    }

    private void getBitmapFromMetaData(PlacePhotoMetadata photoMetadata, final CompletionHandler handler){
        Task<PlacePhotoResponse> photoResponse = myGeoDataClient.getPhoto(photoMetadata);
        photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                PlacePhotoResponse photo = task.getResult();
                Bitmap bitmap = photo.getBitmap();
                handler.handleCompletion(bitmap);
            }
        });
    }

    private void getPhotoFromIndex(final int index, final CompletionHandler handler){
        if(myPhotos[index] != null){
            handler.handleCompletion(myPhotos[index]);
        }else{
            final PlacePhotoMetadata metadata = myPhotoMetadataList.get(index);
            getBitmapFromMetaData(metadata, new CompletionHandler() {
                @Override
                public void handleCompletion(Object created) {
                    // this may give photos in different order than expected because async
                    // but we don't really care about order. Also more time efficient
                    CharSequence attributions = metadata.getAttributions();
                    Bitmap bitmap = (Bitmap) created;
                    myPhotos[index] = new PlacePhoto(bitmap, attributions);
                    handler.handleCompletion(myPhotos[index]);
                }
            });
        }
    }

    private void getPlaceFromID(String placeID, final CompletionHandler handler){
        myGeoDataClient.getPlaceById(placeID).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place place = places.get(0);
                    handler.handleCompletion(place);
                    places.release();
                }
            }
        });
    }

    private void getPhotosFromID(String placeID, final CompletionHandler handler){
        myGeoDataClient.getPlacePhotos(placeID).addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                PlacePhotoMetadataResponse photos = task.getResult();
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                List<PlacePhotoMetadata> photoMetadataList = new ArrayList<>();
                for(int i = 0; i < photoMetadataBuffer.getCount(); i++){
                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(i);
                    photoMetadataList.add(photoMetadata);
                }
                handler.handleCompletion(photoMetadataList);
            }
        });
    }
}
