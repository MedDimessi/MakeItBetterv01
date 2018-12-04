package com.next.gdg.makeitbetterv01;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends Activity implements View.OnClickListener{

    private View myProgressView;
    private View myLoginFormView;
    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignIn;
    private FirebaseAuth myFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Initializing views and buttons
        buttonRegister = findViewById(R.id.register_button);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        textViewSignIn = findViewById(R.id.signInTextView);
        myLoginFormView = findViewById(R.id.login_form);
        myProgressView = findViewById(R.id.login_progress);
        // FireBase instance
        myFirebaseAuth = FirebaseAuth.getInstance();
        // Setting Listeners on SingIn button and SignUp TextView
        buttonRegister.setOnClickListener(this);
        textViewSignIn.setOnClickListener(this);
    }




    @Override
    public void onClick(View view) {
        if(view == buttonRegister){
            registerUser();
        }
        if(view == textViewSignIn){
            startActivity(new Intent(LoginActivity.this,SignInActivity.class));
        }
    }

    private void registerUser() {
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        boolean cancel=false;
        View focusView = null;
        // Check for valid Password
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
           createUserAccount(email,password);
        }
    }
    private void createUserAccount(String email,String password){

myFirebaseAuth.createUserWithEmailAndPassword(email,password)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this,"Authentication successful.",Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(LoginActivity.this,PostActivity.class));
                }else {
                    Toast.makeText(LoginActivity.this,"Authentication Failed.",Toast.LENGTH_SHORT).show();
                }
            }


        });


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
    @Override
    public void onBackPressed(){

    }


    /* Progress Method of showing and hiding UI Components */
    private void showProgress(final boolean show) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB_MR2){
            int shortAnimeTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
             myLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
             myLoginFormView.animate().setDuration(shortAnimeTime).alpha(
                     show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                 @Override
                 public void onAnimationEnd(Animator animator) {
                    myLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            myLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length()>4;
    }

    private boolean isEmailValid(String email) {
        return(email.contains("@"));
    }
}