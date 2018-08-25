package com.sandy_rock_studios.macbookair.randomdateideagenerator.activity.user;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.RequestQueue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.R;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.activity.settings.SettingsActivity;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.DateIdea;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.SavedDateIdeasAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SavedDateIdeasActivity extends AppCompatActivity {
    private static final int MENU_SETTINGS = Menu.FIRST;
    private static final int MENU_SIGN_OUT = Menu.FIRST + 1;
    private static final int MENU_DATE_SUGGESTER = Menu.FIRST + 2;

    FirebaseAuth myAuth;
    FirebaseUser myUser;
    FirebaseDatabase myDatabase;

    private static final String TAG = "SavedDateIdeasActivity";
    private RecyclerView myRecyclerView;
    private RecyclerView.Adapter myAdapter;
    private RecyclerView.LayoutManager myLayoutManager;
    private List<DateIdea> myDataset;


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, MENU_SETTINGS, Menu.NONE, "Settings");
        menu.add(0, MENU_SIGN_OUT, Menu.NONE, "Sign out");
        menu.add(0, MENU_DATE_SUGGESTER, Menu.NONE, "Date suggester");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SIGN_OUT:
                signOut();
                return true;
            case MENU_SETTINGS:
                redirectToSettingsScreen();
                return true;
            case MENU_DATE_SUGGESTER:
                redirectToDateSuggester();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void redirectToDateSuggester(){
        Intent intent = new Intent(SavedDateIdeasActivity.this, DateIdeaSuggesterActivity.class);
        startActivity(intent);
    }

    private void redirectToSettingsScreen(){
        Intent intent = new Intent(SavedDateIdeasActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void redirectToLoginScreen(){
        Intent intent = new Intent(SavedDateIdeasActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void signOut(){
        myAuth.signOut();
        redirectToLoginScreen();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myAuth = FirebaseAuth.getInstance();
        myUser = myAuth.getCurrentUser();
        myDatabase = FirebaseDatabase.getInstance();

        setContentView(R.layout.activity_saved_date_ideas);
        myDataset = new ArrayList<>();
        myRecyclerView = (RecyclerView) findViewById(R.id.saved_date_ideas_recycler_view);        //initialize Adapterclass with List
        myAdapter = new SavedDateIdeasAdapter(myDataset);
        myLayoutManager = new LinearLayoutManager(getApplicationContext());
        myRecyclerView.setLayoutManager(myLayoutManager);
        myRecyclerView.setItemAnimator(new DefaultItemAnimator());
        myRecyclerView.setAdapter(myAdapter);
        addDataToDataSet();
    }

    private void addDataToDataSet(){
        final DatabaseReference savedUserRef = myDatabase.getReference("saved").child(myUser.getUid());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dateIdeaStrings = dataSnapshot.getChildren().iterator();
                while(dateIdeaStrings.hasNext()){
                    DataSnapshot data = dateIdeaStrings.next();
                    String dateStr = data.getKey();
                    Log.w(TAG, "Date string: " + dateStr);
                    myDataset.add(new DateIdea(dateStr, getApplicationContext()));
                    myAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        };
        savedUserRef.addListenerForSingleValueEvent(valueEventListener);
    }
}
