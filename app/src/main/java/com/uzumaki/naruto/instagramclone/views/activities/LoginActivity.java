package com.uzumaki.naruto.instagramclone.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.interfaces.LoginSuccessListener;
import com.uzumaki.naruto.instagramclone.utils.FirebaseHelper;

/**
 * Created by Gaurav Bist on 05-07-2017.
 */

public class LoginActivity extends AppCompatActivity implements LoginSuccessListener {

    private static final String TAG = "LoginActivity";
    private String email, password;

    // firebase
    private FirebaseHelper mFirebaseHelper;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // widgets
    private Button btnLogin;
    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText inputEmail, inputPassword;
    private TextView pleaseWait, link_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindViews();
        init();
        setupFirebaseAuth();
    }

    private void bindViews() {
        btnLogin = (Button) findViewById(R.id.btnLogin);
        mProgressBar = (ProgressBar) findViewById(R.id.loginRequestLoadingProgressBar);
        pleaseWait = (TextView) findViewById(R.id.textPleaseWait);
        link_signup = (TextView) findViewById(R.id.link_signup);
        inputEmail = (EditText) findViewById(R.id.inputEmail);
        inputPassword = (EditText) findViewById(R.id.inputPassword);
    }

    private void init() {
        mContext = this;

        mProgressBar.setVisibility(View.GONE);
        pleaseWait.setVisibility(View.GONE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = inputEmail.getText().toString();
                password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
                    Toast.makeText(mContext, getString(R.string.fill_details), Toast.LENGTH_SHORT).show();
                else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    pleaseWait.setVisibility(View.VISIBLE);

                    mFirebaseHelper.performFirebaseLogin(email, password);
                }

            }
        });

        link_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating to RegisterActivity");
                startActivity(new Intent(mContext, RegisterActivity.class));
            }
        });
    }

    /**
     * ------------------------------------ FIREBASE -----------------------------------------------
     */

    private void setupFirebaseAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseHelper = new FirebaseHelper(mContext);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                mProgressBar.setVisibility(View.VISIBLE);
                pleaseWait.setVisibility(View.VISIBLE);

                if (user != null) {
                    // UserPrivate is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    mProgressBar.setVisibility(View.GONE);
                    pleaseWait.setVisibility(View.GONE);

                    Toast.makeText(mContext, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();

                    // finish the login activity and direct user to home activity
                    finish();
                    startActivity(new Intent(mContext, HomeActivity.class));
                } else {
                    // UserPrivate is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    mProgressBar.setVisibility(View.GONE);
                    pleaseWait.setVisibility(View.GONE);
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onLoginSuccessful() {
        mProgressBar.setVisibility(View.GONE);
        pleaseWait.setVisibility(View.GONE);
    }
}
