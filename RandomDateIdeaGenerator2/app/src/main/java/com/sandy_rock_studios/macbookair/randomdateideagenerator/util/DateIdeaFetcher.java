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
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.interfaces.IdeaListHandler;

import java.util.ArrayList;
import java.util.List;

public class DateIdeaFetcher {
    private static final String TAG = "DateIdeaFetcher";
    private FirebaseAuth mAuth;
    private FirebaseUser myUser;
    private FirebaseDatabase myDatabase;

    public DateIdeaFetcher(){
        mAuth = FirebaseAuth.getInstance();
        myUser = mAuth.getCurrentUser();
        myDatabase = FirebaseDatabase.getInstance();
    }

    public void getIdeasNotYetEncountered(final List<String> ideas, final IdeaListHandler handler){
        DatabaseReference userRef = myDatabase.getReference("users").child(myUser.getUid());
        DatabaseReference encounteredRef = userRef.child("encountered");
        if(encounteredRef == null) {
            handler.handle(ideas);
        }
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot encounteredIdea: dataSnapshot.getChildren()) {
                    String encounteredIdeaStr = encounteredIdea.getKey();
                    if(ideas.contains(encounteredIdeaStr)){
                        ideas.remove(encounteredIdeaStr);
                    }
                }
                handler.handle(ideas);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        };
        encounteredRef.addListenerForSingleValueEvent(valueEventListener);
    }
}
