package com.sandy_rock_studios.macbookair.randomdateideagenerator.util;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseWriter {
    private FirebaseUser myUser;
    private FirebaseDatabase myDatabase;

    public FirebaseWriter(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        myUser = auth.getCurrentUser();
        myDatabase = FirebaseDatabase.getInstance();
    }

    public void writeChosenPlace(final double lat, final double lng){
        final FirebaseReader firebaseReader = new FirebaseReader();
        firebaseReader.checkUserExists(new FirebaseReader.FirebaseStatus() {
            @Override
            public void handle(boolean status) {
                if(!status){ // this is first time user has even used this app
                    DatabaseReference userRef = myDatabase.getReference("users").child(myUser.getUid());
                    Map<String, Map<String, Double>> valMap = new HashMap<>();
                    Map<String, Double> lastSelectedLocationMap = new HashMap<>();
                    lastSelectedLocationMap.put("lat", lat);
                    lastSelectedLocationMap.put("lng", lng);
                    valMap.put("last_selected_location", lastSelectedLocationMap);
                    userRef.setValue(valMap);
                }else{
                    firebaseReader.checkLastSelectedLocationExists(new FirebaseReader.FirebaseStatus() {
                        @Override
                        public void handle(boolean status) {
                            if(!status){ // user has not yet selected a location
                                DatabaseReference locationRef = myDatabase.getReference("users").child(myUser.getUid()).child("last_selected_location");
                                Map<String, Double> valMap = new HashMap<>();
                                valMap.put("lat", lat);
                                valMap.put("lng", lng);
                                locationRef.setValue(valMap);
                                return;
                            }else{ // normal use case
                                DatabaseReference userRef = myDatabase.getReference("users").child(myUser.getUid());
                                DatabaseReference locationRef = userRef.child("last_selected_location");
                                locationRef.child("lat").setValue(lat);
                                locationRef.child("lng").setValue(lng);
                            }
                        }
                    });
                }
            }
        });
    }

    public void writeSavedIdea(final String dateIdea){
        logEncountered(dateIdea);
        FirebaseReader firebaseReader = new FirebaseReader();
        firebaseReader.checkSavedExists(new FirebaseReader.FirebaseStatus() {
            @Override
            public void handle(boolean status) {
                if(!status){ // first time user saves idea
                    DatabaseReference savedUserRef = myDatabase.getReference("saved").child(myUser.getUid());
                    Map<String, Boolean> valMap = new HashMap<>();
                    valMap.put(dateIdea, true);
                    savedUserRef.setValue(valMap);
                }else{ // normal use case
                    DatabaseReference savedRef = myDatabase.getReference("saved");
                    savedRef.child(myUser.getUid()).child(dateIdea).setValue(true);
                }
            }
        });
    }

    public void writeDiscardedIdea(final String dateIdea){
        logEncountered(dateIdea);
        FirebaseReader firebaseReader = new FirebaseReader();
        firebaseReader.checkDiscardedExists(new FirebaseReader.FirebaseStatus() {
            @Override
            public void handle(boolean status) {
                if(!status){ // first time user has discarded an idea
                    DatabaseReference discardedUserRef = myDatabase.getReference("discarded").child(myUser.getUid());
                    Map<String, Boolean> valMap = new HashMap<>();
                    valMap.put(dateIdea, true);
                    discardedUserRef.setValue(valMap);
                }else{ // normal use case
                    DatabaseReference discardedUserRef = myDatabase.getReference("discarded").child(myUser.getUid());
                    discardedUserRef.child(dateIdea).setValue(true);
                }
            }
        });
        removeFromSaved(dateIdea);
        unFavoriteIdea(dateIdea);
    }

    public void writeFavoritedIdea(final String dateIdea){
        FirebaseReader firebaseReader = new FirebaseReader();
        firebaseReader.checkFavoriteExists(new FirebaseReader.FirebaseStatus() {
            @Override
            public void handle(boolean status) {
                if(!status){ // first time user has favorited an idea
                    DatabaseReference favoritedUserRef = myDatabase.getReference("favorited").child(myUser.getUid());
                    Map<String, Boolean> valMap = new HashMap<>();
                    valMap.put(dateIdea, true);
                    favoritedUserRef.setValue(valMap);
                }else{ // normal use case
                    DatabaseReference favoritedUserRef = myDatabase.getReference("saved");
                    favoritedUserRef.child(myUser.getUid()).child(dateIdea).setValue(true);
                }
            }
        });
    }

    public void unFavoriteIdea(String dateIdea){
        // don't care about first-time cases like with the writes because removing an unexisting node should never happen
        DatabaseReference favoritedRef = myDatabase.getReference("favorited");
        favoritedRef.child(myUser.getUid()).child(dateIdea).removeValue();
    }

    private void removeFromSaved(String dateIdea){
        // don't care about first-time cases like with the writes because removing an unexisting node should never happen
        DatabaseReference savedRef = myDatabase.getReference("saved");
        savedRef.child(myUser.getUid()).child(dateIdea).removeValue();
    }

    private void logEncountered(final String dateIdea){
        final FirebaseReader firebaseReader = new FirebaseReader();
        firebaseReader.checkUserExists(new FirebaseReader.FirebaseStatus() {
            @Override
            public void handle(boolean status) {
                if(!status){  // this is first time user has even used this app
                    DatabaseReference userRef = myDatabase.getReference("users").child(myUser.getUid());
                    Map<String, Map<String, Boolean>> userMap = new HashMap<>();
                    Map<String, Boolean> encounteredMap = new HashMap<>();
                    encounteredMap.put(dateIdea, true);
                    userMap.put("encountered", encounteredMap);
                    userRef.setValue(encounteredMap);
                }else{
                    firebaseReader.checkEncounteredExists(new FirebaseReader.FirebaseStatus() {
                        @Override
                        public void handle(boolean status) {
                            if(!status){ // this is first time user has written to encountered
                                DatabaseReference encounteredRef = myDatabase.getReference("users").child(myUser.getUid()).child("encountered");
                                Map<String, Boolean> valMap = new HashMap<>();
                                valMap.put(dateIdea, true);
                                encounteredRef.setValue(valMap);
                            }else{ // normal use case
                                DatabaseReference encounteredRef = myDatabase.getReference("users").child(myUser.getUid()).child("encountered");
                                encounteredRef.child(dateIdea).setValue(true);
                            }
                        }
                    });
                }
            }
        });
    }
}
