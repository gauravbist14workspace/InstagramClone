package com.uzumaki.naruto.instagramclone.views.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.interfaces.PhotoUploadSuccessListener;
import com.uzumaki.naruto.instagramclone.utils.Constants;
import com.uzumaki.naruto.instagramclone.utils.FirebaseHelper;
import com.uzumaki.naruto.instagramclone.utils.UniversalImageLoader;

/**
 * Created by Gaurav Bist on 01-08-2017.
 */

public class NextActivity extends AppCompatActivity implements PhotoUploadSuccessListener {
    private static final String TAG = "NextActivity";
    private Context mContext;

    // widgets
    ImageView img_share, iv_close_share;
    TextView tv_share;
    EditText image_caption;
    ProgressBar progress;

    // variables
    private String mSelectedImage;
    private Bitmap bitmap;

    // constants
    public static final String mAppend = "file:/";
    private int imageCount = 0;

    Intent intent;

    // firebase
    private FirebaseHelper mFirebaseHelper;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        bindViews();
        init();
        setupFirebaseAuth();
    }

    private void bindViews() {
        iv_close_share = (ImageView) findViewById(R.id.iv_close_share);
        tv_share = (TextView) findViewById(R.id.tv_share);

        img_share = (ImageView) findViewById(R.id.img_share);
        image_caption = (EditText) findViewById(R.id.image_caption);
        progress = (ProgressBar) findViewById(R.id.progress);
    }

    private void init() {
        mContext = this;
        mFirebaseHelper = new FirebaseHelper(mContext);

        progress.setIndeterminate(true);

        iv_close_share.setOnClickListener(onClickListener);
        tv_share.setOnClickListener(onClickListener);

        getIntentValues();
    }

    private void getIntentValues() {
        intent = getIntent();
        if (intent.hasExtra(getString(R.string.selected_img))) {
            mSelectedImage = getIntent().getStringExtra(getString(R.string.selected_img));
            UniversalImageLoader.setImage(mSelectedImage, img_share, progress, mAppend);
        } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
            img_share.setImageBitmap(bitmap);

            progress.setVisibility(View.GONE);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.iv_close_share) {
                Log.d(TAG, "onClick: Closing the NextActivity.");
                finish();
            } else if (id == R.id.tv_share) {
                Log.d(TAG, "onClick: Sharing the image.");

                String caption = image_caption.getText().toString();

                if (intent.hasExtra(getString(R.string.selected_img))) {
                    mFirebaseHelper.uploadPhoto(getString(R.string.new_photo), caption, imageCount, mSelectedImage, null);
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    mFirebaseHelper.uploadPhoto(getString(R.string.new_photo), caption, imageCount, null, bitmap);
                }
            }
        }
    };

    /**
     * ---------------FIREBASE SETUP----------------------------------------------------------------
     */
    private void setupFirebaseAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // UserPrivate is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imageCount = getImageCount(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // get the number of imags already posted by the user
    private int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;
        for (DataSnapshot ds : dataSnapshot.child(Constants.USER_PHOTOS)
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .getChildren()) {
            count++;
        }
        return count;
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
    public void onUploadSuccessful() {
        progress.setVisibility(View.GONE);
        Toast.makeText(mContext, "Photo upload success.", Toast.LENGTH_SHORT).show();

        finish();
        startActivity(new Intent(mContext, ProfileActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    public void onUploadFailed() {
        progress.setVisibility(View.GONE);
        Toast.makeText(mContext, "Photo upload failed!", Toast.LENGTH_SHORT).show();
    }
}
