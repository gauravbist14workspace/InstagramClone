package com.uzumaki.naruto.instagramclone.views.activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.utils.BottomNavigationViewHelper;
import com.uzumaki.naruto.instagramclone.utils.Permissions;
import com.uzumaki.naruto.instagramclone.views.adapters.SectionPagerAdapter;
import com.uzumaki.naruto.instagramclone.views.fragments.GalleryFragment;
import com.uzumaki.naruto.instagramclone.views.fragments.PhotoFragment;

/**
 * Created by Gaurav Bist on 04-07-2017.
 */

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";
    private Context mContext;

    // widgets
    ViewPager container;
    TabLayout tabs_bottom;

    // constants
    private static final int ACTVITY_NUMBER = 2;
    private static final int VERIFY_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        bindViews();
        init();
        setupViewPager();
//        setupBottomNavigationView();
    }

    private void bindViews() {
        container = (ViewPager) findViewById(R.id.viewpager_container);
        tabs_bottom = (TabLayout) findViewById(R.id.tabs_bottom);
    }

    private void init() {
        mContext = this;

        if (checkPermissionsArray(Permissions.PERMISSIONS)) {

        } else {
            verifyPermissions(Permissions.PERMISSIONS);
        }
    }

    private void setupViewPager() {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        container.setAdapter(adapter);

        tabs_bottom.setupWithViewPager(container);

        tabs_bottom.getTabAt(0).setText(getString(R.string.tab_gallery));
        tabs_bottom.getTabAt(1).setText(getString(R.string.tab_photo));
    }

    /**
     * This method will return 0, if no flags were set while calling intent for ShareActivity.
     * This method will return '268435456' when calling intent has flag set as FLAG_ACTIVITY_NEW_TASK
     *
     * @return
     */
    public int getTask() {
        Log.d(TAG, "getTask: TASK: " + getIntent().getFlags());

        return getIntent().getFlags();          // 0 when no added flags |
    }

    /**
     * To retrieve the current fragment/tab number
     *
     * @return 0: GalleryFragment
     * 1: PhotoFragment
     */
    public int getCurrentTabNumber() {
        return container.getCurrentItem();
    }

    /**
     * Checking an array of permissions
     *
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions) {
        Log.d(TAG, "checkPermissionsArray: Checking permissions array.");

        for (int i = 0; i < permissions.length; i++) {
            String check = permissions[i];
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission if it has been granted
     *
     * @param check
     * @return
     */
    public boolean checkPermissions(String check) {
        Log.d(TAG, "checkPermissions: Checking for permission " + check);
        int permissionRequest = ActivityCompat.checkSelfPermission(mContext, check);
        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: \nPermission was not granted for: " + check);
            return false;
        } else {
            Log.d(TAG, "checkPermissions: \nPermission granted for: " + check);
            return true;
        }
    }

    /**
     * If permissions are not already granted then ask for them
     *
     * @param permissions
     */
    private void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: Verifying permissions.");
        ActivityCompat.requestPermissions((Activity) mContext, permissions, VERIFY_PERMISSIONS);
    }

    /* BottomNavigationView Setup */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTVITY_NUMBER);
        menuItem.setChecked(true);
    }
}
