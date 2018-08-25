package com.sandy_rock_studios.macbookair.randomdateideagenerator.activity.user;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.sandy_rock_studios.macbookair.randomdateideagenerator.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 0;
    private static final String CLIENT_ID = "664924230590-glb324k2h3fa9fcci1ujmstuu5bqihoh.apps.googleusercontent.com";
    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initGoogleSignInClient();
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            default:
                Log.w(TAG, "Didn't click on Sign in Button!");
        }
    }

    /**
     * Purpose: Handles the sign in result of the intent created from the GoogleSignInClient
     * @param requestCode Request code returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
     * @param resultCode Result code returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
     * @param data Data returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * Purpose: Get the intent created from the sign in client and then start the sign in process
     */
    private void signIn() {
        Log.w(TAG, "Signing in now!!!");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Purpose: Try getting the account from the completed task. If this failed for some reason, log error.
     * @param completedTask
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.w(TAG, "Login Successful!!!");
            // Signed in successfully, show authenticated UI.
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    /**
     * Purpose: Redirect to the DateIdeaSuggesterActivity given the account login status. If not logged in yet, do nothing
     * @param user: The firebase user. If not null, the user has already signed in. Else, the user
     *               needs to sign in still
     */
    private void updateUI(FirebaseUser user){
        if(user != null){
            makeShortToast("Welcome " + user.getDisplayName() + "!");
            redirectToDateIdeaSuggester();
        }else{
            makeShortToast("Not logged in");
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            makeShortToast("Authentication failed");
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void makeShortToast(String displayName){
        Context context = getApplicationContext();
        CharSequence text = displayName;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void redirectToDateIdeaSuggester(){
        Intent intent = new Intent(LoginActivity.this, DateIdeaSuggesterActivity.class);
        startActivity(intent);
    }

    /**
     * Purpose: Initialize the GoogleSignInClient that can authenticate our user
     * Code taken from: https://firebase.google.com/docs/auth/android/google-signin
     */
    private void initGoogleSignInClient(){
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(CLIENT_ID)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
}
