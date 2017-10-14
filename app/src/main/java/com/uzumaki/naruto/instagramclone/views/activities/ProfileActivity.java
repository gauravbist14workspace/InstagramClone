package com.uzumaki.naruto.instagramclone.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.interfaces.OnCommentThreadSelectedListener;
import com.uzumaki.naruto.instagramclone.interfaces.OnGridImageSelectedListener;
import com.uzumaki.naruto.instagramclone.models.Photo;
import com.uzumaki.naruto.instagramclone.models.UserPrivate;
import com.uzumaki.naruto.instagramclone.views.adapters.GridImageAdapter;
import com.uzumaki.naruto.instagramclone.views.fragments.ProfileFragment;
import com.uzumaki.naruto.instagramclone.views.fragments.ViewCommentsFragment;
import com.uzumaki.naruto.instagramclone.views.fragments.ViewPostFragment;
import com.uzumaki.naruto.instagramclone.views.fragments.ViewProfileFragment;

import java.util.ArrayList;

/**
 * Created by Gaurav Bist on 04-07-2017.
 */

public class ProfileActivity extends AppCompatActivity implements
        OnGridImageSelectedListener,
        OnCommentThreadSelectedListener {
    private static final String TAG = "ProfileActivity";
    private Context mContext;

    // constants
    private static final int NUM_GRID_COLOUMNS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();

        if (savedInstanceState == null) {

        }
    }

    private void init() {
        mContext = this;

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "init: Searching for UserPrivate object attached as an extra.");

            if (intent.hasExtra(getString(R.string.intent_user))) {
                if (intent.hasExtra(getString(R.string.intent_user))) {
                    UserPrivate user = intent.getParcelableExtra(getString(R.string.intent_user));
                    if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        Log.d(TAG, "init: Inflating different user profile.");
                        ViewProfileFragment fragment = new ViewProfileFragment();
                        Bundle args = new Bundle();
                        args.putParcelable(getString(R.string.intent_user), intent.getParcelableExtra(getString(R.string.intent_user)));
                        fragment.setArguments(args);

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, fragment)
                                .addToBackStack(getString(R.string.view_profile_fragment))
                                .commit();
                    } else{
                        Log.d(TAG, "init: Inflating user own profile.");

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, new ProfileFragment())
                                .commit();
                    }
                } else
                    Toast.makeText(mContext, "Something went wrong.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "init: Inflating user own profile.");

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, new ProfileFragment())
                    .commit();
        }
    }

    private void tempGridSetup() {
        ArrayList<String> imgURLs = new ArrayList<>();

        imgURLs.add("https://i.redd.it/9bf67ygj710z.jpg");
        imgURLs.add("https://c1.staticflickr.com/5/4276/34102458063_7be616b993_o.jpg");
        imgURLs.add("http://i.imgur.com/EwZRpvQ.jpg");
        imgURLs.add("http://i.imgur.com/JTb2pXP.jpg");
        imgURLs.add("https://i.redd.it/59kjlxxf720z.jpg");
        imgURLs.add("https://i.redd.it/pwduhknig00z.jpg");
        imgURLs.add("https://i.redd.it/clusqsm4oxzy.jpg");
        imgURLs.add("https://i.redd.it/svqvn7xs420z.jpg");
        imgURLs.add("http://i.imgur.com/j4AfH6P.jpg");
        imgURLs.add("https://i.redd.it/89cjkojkl10z.jpg");
        imgURLs.add("https://i.redd.it/aw7pv8jq4zzy.jpg");

        setImageGrid(imgURLs);
    }

    private void setImageGrid(ArrayList<String> imgURLs) {
        GridView gridView = (GridView) findViewById(R.id.gridView);

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imgWidth = gridWidth / NUM_GRID_COLOUMNS;

        gridView.setColumnWidth(imgWidth);

        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview, "", imgURLs);
        gridView.setAdapter(adapter);
    }

    @Override
    public void onGridImageSelcted(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelcted: Image selected from ProfileFragment: " + photo.toString());

        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.tab_photo), photo);
        args.putInt(getString(R.string.calling_activity), activityNumber);

        ViewPostFragment fragment = new ViewPostFragment();
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment)
                .addToBackStack(getString(R.string.view_post_fragment))
                .commit();
    }

    @Override
    public void onCommentSelected(Photo photo) {
        Log.d(TAG, "onCommentSelected: Selected a comment thread.");

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.tab_photo), photo);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment)
                .addToBackStack(getString(R.string.view_comment_fragment))
                .commit();

    }
}
