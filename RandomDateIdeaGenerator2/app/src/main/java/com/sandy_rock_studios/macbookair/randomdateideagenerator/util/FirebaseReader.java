package com.sandy_rock_studios.macbookair.randomdateideagenerator.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseReader {
    public interface FirebaseStatus{
        void handle(boolean status);
    }
    private static final String TAG = "FirebaseReader";
    private FirebaseDatabase myDatabase;
    private FirebaseAuth myAuth;
    private FirebaseUser myUser;
    public FirebaseReader(){
        myDatabase = FirebaseDatabase.getInstance();
        myAuth = FirebaseAuth.getInstance();
        myUser = myAuth.getCurrentUser();
    }

    public void checkFavoriteExists(FirebaseStatus status){
        checkExistsNonNested("favorited", status);
    }

    public void checkSavedExists(FirebaseStatus status){
        checkExistsNonNested("saved", status);
    }

    public void checkDiscardedExists(FirebaseStatus status){
        checkExistsNonNested("discarded", status);
    }

    public void checkEncounteredExists(FirebaseStatus status){
        checkExistsNested("encountered", status);
    }

    public void checkLastSelectedLocationExists(FirebaseStatus status){
        checkExistsNested("last_selected_location", status);
    }

    public void checkUserExists(final FirebaseStatus status){
        final DatabaseReference ref = myDatabase.getReference("users");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.child(myUser.getUid()).exists();
                status.handle(exists);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        };
        ref.addListenerForSingleValueEvent(valueEventListener);
    }

    private void checkExistsNonNested(String nodeName, final FirebaseStatus status){
        final DatabaseReference ref = myDatabase.getReference(nodeName);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.child(myUser.getUid()).exists();
                status.handle(exists);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        };
        ref.addListenerForSingleValueEvent(valueEventListener);
    }

    private void checkExistsNested(final String nodeName, final FirebaseStatus status){
        final DatabaseReference ref = myDatabase.getReference("users").child(myUser.getUid());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.child(nodeName).exists();
                status.handle(exists);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        };
        ref.addListenerForSingleValueEvent(valueEventListener);
    }
}
