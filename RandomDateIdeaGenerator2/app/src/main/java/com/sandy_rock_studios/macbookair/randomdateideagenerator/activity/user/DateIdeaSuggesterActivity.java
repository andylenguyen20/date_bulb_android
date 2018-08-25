package com.sandy_rock_studios.macbookair.randomdateideagenerator.activity.user;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.fragment.OutOfIdeasFragment;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.R;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.activity.settings.SettingsActivity;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.fragment.DateIdeaFragment;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.DateSuggester;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.util.interfaces.IdeaListHandler;

import java.util.List;

public class DateIdeaSuggesterActivity extends AppCompatActivity implements DateIdeaFragment.OnFragmentInteractionListener, OutOfIdeasFragment.OnFragmentInteractionListener
{
    private static final int MENU_SETTINGS = Menu.FIRST;
    private static final int MENU_SIGN_OUT = Menu.FIRST + 1;
    private static final int MENU_SAVED_IDEAS = Menu.FIRST + 2;

    private static final String TAG = "DateIdeaSuggester";
    private FirebaseAuth mAuth;
    private RequestQueue queue;
    private Thread myRequestBackgroundTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_date_idea_suggester);
        mAuth = FirebaseAuth.getInstance();
        queue = Volley.newRequestQueue(this);
        closeFragment();
        requestPlacesInformation();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, MENU_SETTINGS, Menu.NONE, "Settings");
        menu.add(0, MENU_SIGN_OUT, Menu.NONE, "Sign out");
        menu.add(0, MENU_SAVED_IDEAS, Menu.NONE, "Saved Ideas");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeFragment();
        requestPlacesInformation();
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
            case MENU_SAVED_IDEAS:
                redirectToSavedIdeas();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void redirectToSavedIdeas(){
        Intent intent = new Intent(DateIdeaSuggesterActivity.this, SavedDateIdeasActivity.class);
        startActivity(intent);
    }

    private void redirectToSettingsScreen(){
        Intent intent = new Intent(DateIdeaSuggesterActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void redirectToLoginScreen(){
        Intent intent = new Intent(DateIdeaSuggesterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void requestPlacesInformation(){
        final Activity activity = this;
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        myRequestBackgroundTask = new Thread(new Runnable() {
            public void run() {
                new DateSuggester(activity, queue, TAG).suggest(new IdeaListHandler() {
                    @Override
                    public void handle(List<String> ideas) {
                        if(ideas.size() == 0) {
                            closeFragment();
                            displayOutOfIdeasFragment();
                        }else{
                            String randomDateIdea = ideas.get((int)(Math.random() * ideas.size()));
                            closeFragment();
                            displayDateIdeaFragment(randomDateIdea);
                        }
                    }
                });
            }
        });
        myRequestBackgroundTask.start();
    }

    public void displayOutOfIdeasFragment(){
        findViewById(R.id.progress_bar).setVisibility(View.GONE);
        OutOfIdeasFragment outOfIdeasFragment = OutOfIdeasFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.date_idea_fragment_container, outOfIdeasFragment).addToBackStack(null).commit();
    }

    public void displayDateIdeaFragment(String dateIdea) {
        DateIdeaFragment dateIdeaFragment = DateIdeaFragment.newInstance(dateIdea);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.date_idea_fragment_container, dateIdeaFragment).addToBackStack(null).commit();
    }

    public void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.date_idea_fragment_container);
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment).commit();
        }
    }

    private void signOut(){
        mAuth.signOut();
        redirectToLoginScreen();
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
        if(myRequestBackgroundTask != null){
            myRequestBackgroundTask.interrupt();
        }
    }

    @Override
    public void onIdeaInteraction() {
        closeFragment();
        requestPlacesInformation();
    }

    @Override
    public void onOutOfIdeasInteraction() {
        closeFragment();
        requestPlacesInformation();
    }
}
