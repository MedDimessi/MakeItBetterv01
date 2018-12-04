package com.next.gdg.makeitbetterv01;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener{


     private Button signInButton;
     private EditText editTextEmail;
     private EditText editTextPassword;
     private View myProgressView;
     private View mySignInForm;
     private TextView textViewSignUp;
     private FirebaseAuth myFireBaseAuth;
     private FirebaseAuth.AuthStateListener myAuthListener;
     private SignInButton googleSignInButton;
     private static final int RC_SIGN_IN = 1;
     private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.activity_sign_in);
        super.onCreate(savedInstanceState);
        signInButton = findViewById(R.id.signIn_Button);
        Typeface testFont=Typeface.createFromAsset(getAssets(),"fonts/PTS76F.ttf");
        signInButton.setTypeface(testFont);
        editTextEmail = findViewById(R.id.email);
        textViewSignUp = findViewById(R.id.signUpTextView);
        editTextPassword = findViewById(R.id.password);
        myProgressView = findViewById(R.id.login_progress);
        mySignInForm = findViewById(R.id.signin_form);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        myFireBaseAuth = FirebaseAuth.getInstance();
        myAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() !=null){
                    startActivity(new Intent(SignInActivity.this,MapsActivity.class));
                }
            }
        };
           signInButton.setOnClickListener(this);
           textViewSignUp.setOnClickListener(this);


           // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mgoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API,gso)
        .build();
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                signIn();
            }
        });

    }




    @Override
    protected void onStart() {
        super.onStart();
        myFireBaseAuth.addAuthStateListener(myAuthListener);
        if(myFireBaseAuth.getCurrentUser() != null){

            startActivity(new Intent(this,MapsActivity.class));
        }

    }

    @Override
    public void onClick(View view) {
         if(view == signInButton){
             signInUser();
         }
         if(view == textViewSignUp){
             startActivity(new Intent(this,LoginActivity.class));
         }
    }

    private void signInUser() {
        editTextEmail.setError(null);
        editTextPassword.setError(null);

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        boolean cancel = false;
        View focusView = null;

  if(TextUtils.isEmpty(password) && !isPasswordValid(password)){
      editTextPassword.setError(getString(R.string.error_invalid_password));
      focusView = editTextPassword;
      cancel = true;
  }
  if(TextUtils.isEmpty(email)){
      editTextEmail.setError(getString(R.string.error_field_required));
      focusView = editTextEmail;
      cancel = true;
  }else if(!isEmailValid(email)){
      editTextEmail.setError(getString(R.string.error_invalid_email));
      focusView = editTextEmail;
      cancel = true;
  }
        if(cancel){
            focusView.requestFocus();
        }else{
            showProgress(true);
            signInUserAccount(email,password);
        }

  }

    @Override
    public void onBackPressed() {


    }

    private void signInUserAccount(String email, String password){
        myFireBaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            startActivity(new Intent(SignInActivity.this,MapsActivity.class));


                        }else{
                            Toast.makeText(SignInActivity.this,"SignIn Failed!, Try again.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
  }

    /* Progress Method of showing and hiding UI Components */
    private void showProgress(final boolean show) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB_MR2){
            int shortAnimeTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mySignInForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mySignInForm.animate().setDuration(shortAnimeTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    mySignInForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }

            });
            myProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            myProgressView.animate().setDuration(shortAnimeTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter(){

                @Override
                public void onAnimationEnd(Animator animator) {
                    myProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }

            });
        }else {
            myProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mySignInForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length()>4;
    }

    private boolean isEmailValid(String email) {
        return(email.contains("@"));
      }


    // Code for google sign in

      private void signIn (){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
      }
      @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
          super.onActivityResult(requestCode, resultCode, data);

          if(requestCode == RC_SIGN_IN){
              GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
              if(result.isSuccess()){
                  GoogleSignInAccount account = result.getSignInAccount();
                  firebaseAuthWithGoogle(account);
              }else {
                  // Google SIgn In is failed
              }
          }

      }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }

    }

      private void firebaseAuthWithGoogle(GoogleSignInAccount account){
          AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
             myFireBaseAuth.signInWithCredential(credential)
                     .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                         @Override
                         public void onComplete(@NonNull Task<AuthResult> task) {

                             if(!task.isSuccessful()){
                                 Toast.makeText(SignInActivity.this,"Authentication failed!",Toast.LENGTH_LONG).show();
                             }
                         }
                     });
      }


}