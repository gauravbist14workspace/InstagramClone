package com.uzumaki.naruto.instagramclone.views.activities;

import android.content.Context;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.interfaces.RegisterListener;
import com.uzumaki.naruto.instagramclone.utils.FirebaseHelper;

/**
 * Created by Gaurav Bist on 05-07-2017.
 */

public class RegisterActivity extends AppCompatActivity implements RegisterListener {

    private static final String TAG = "RegisterActivity";
    private Context mContext;

    // widgets
    private ProgressBar mProgressBar;
    private EditText inputEmail, inputPassword, inputUserName;
    private Button btnRegister;
    private TextView pleaseWait;

    // firebase
    private FirebaseHelper mFirebaseHelper;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;

    String email, password, userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();
        init();
        setupFirebaseAuth();
    }

    private void bindViews() {
        inputEmail = (EditText) findViewById(R.id.inputEmail);
        inputUserName = (EditText) findViewById(R.id.inputUserName);
        inputPassword = (EditText) findViewById(R.id.inputPassword);

        btnRegister = (Button) findViewById(R.id.btnRegister);

        mProgressBar = (ProgressBar) findViewById(R.id.registerRequestLoadingProgressBar);
        pleaseWait = (TextView) findViewById(R.id.textPleaseWait);
    }

    private void init() {
        mContext = this;
        mFirebaseHelper = new FirebaseHelper(mContext);

        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);
        pleaseWait.setVisibility(View.GONE);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                pleaseWait.setVisibility(View.VISIBLE);

                email = inputEmail.getText().toString();
                password = inputPassword.getText().toString();
                userName = inputUserName.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(email) || TextUtils.isEmpty(email)) {
                    Toast.makeText(mContext, getString(R.string.fill_details), Toast.LENGTH_SHORT).show();
                } else {
                    mFirebaseHelper.performFirebaseRegister(email, password);
                }
            }
        });
    }

    /**
     * ---------------FIREBASE SETUP----------------------------------------------------------------
     */
    private void setupFirebaseAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // UserPrivate is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    mFirebaseHelper.checkIfUsernameExists(userName);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
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
    public void onRegisterCompleteListener() {
        mProgressBar.setVisibility(View.VISIBLE);
        pleaseWait.setVisibility(View.VISIBLE);
    }
}
