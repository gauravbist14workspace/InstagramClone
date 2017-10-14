package com.uzumaki.naruto.instagramclone.views.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.utils.FirebaseHelper;
import com.uzumaki.naruto.instagramclone.views.adapters.SectionsStatePagerAdapter;
import com.uzumaki.naruto.instagramclone.views.fragments.EditProfileFragment;
import com.uzumaki.naruto.instagramclone.views.fragments.SignOutFragment;

import java.util.ArrayList;

/**
 * Created by Gaurav Bist on 04-07-2017.
 */

public class AccountSettingActivity extends AppCompatActivity {

    private static final String TAG = "AccountSettingActivity";
    private Context mContext;

    // constants
    private static final int ACTVITY_NUMBER = 4;

    // widgets
    private ImageView backArrow;
    public SectionsStatePagerAdapter adapter;
    private ViewPager mViewPager;
    private RelativeLayout relativerLayout;

    // firebase
    private FirebaseHelper mFirebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        Log.d(TAG, "onCreate: AccountSettingActivity");

        bindViews();
        init();
        getIncomingIntent();
    }

    private void bindViews() {
        relativerLayout = (RelativeLayout) findViewById(R.id.relLayout1);   // this occupies the whole screen behind viewpager layout
        mViewPager = (ViewPager) findViewById(R.id.container);              // this occupies the whole screen above relative layout

        backArrow = (ImageView) findViewById(R.id.backArrow);
    }

    private void init() {
        mContext = this;

        setupFragments();
        setupSettingsList();

        // setup the back arrow to go back to the profile activity
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, ProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }

    private void getIncomingIntent() {
        Intent intent = getIntent();
        mFirebaseHelper = new FirebaseHelper(mContext);

        // if there is some imageuri attached as extra, then it was chosen from gallery/photo fragment
        Log.d(TAG, "getIncomingIntent: New incoming imageUrl");
        if (intent.hasExtra(getString(R.string.selected_img))
                || intent.hasExtra(getString(R.string.selected_bitmap))) {

            if (intent.getStringExtra(getString(R.string.return_to_fragment))
                    .equals(getString(R.string.edit_profile_fragment))) {
                if (intent.hasExtra(getString(R.string.selected_img))) {
                    // set the new profile picture
                    mFirebaseHelper.uploadPhoto(getString(R.string.profile_photo),
                            null,
                            0,
                            intent.getStringExtra(getString(R.string.selected_img)),
                            null);
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    // set the new profile picture
                    mFirebaseHelper.uploadPhoto(getString(R.string.profile_photo),
                            null,
                            0,
                            null,
                            (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap)));
                }
            }
        }

        if (intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "getIncomingIntent: Received incoming intent from " + "Profile Activity");
            setViewPager(adapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }
    }

    /*
    Adding the fragments to the adapter
     */
    private void setupFragments() {
        adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile_fragment));
        adapter.addFragment(new SignOutFragment(), getString(R.string.sign_out_fragment));
    }

    private void setupSettingsList() {
        ListView listView = (ListView) findViewById(R.id.lvAccountSettings);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile_fragment));
        options.add(getString(R.string.sign_out_fragment));

        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.d(TAG, "onItemClick: navigating to fragment#: " + position);
                setViewPager(position);
            }
        });
    }

    /*
    Setting up the viewpager with the adapter and displaying it when option is selected
     */
    public void setViewPager(int fragmentNumber) {
        relativerLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: navigating to fragment #: " + fragmentNumber);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(fragmentNumber);              // This line will now set the fragment
    }

    public Fragment getCurrentFragment() {
        return adapter.getItem(mViewPager.getCurrentItem());
    }
}
