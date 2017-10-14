package com.uzumaki.naruto.instagramclone.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.interfaces.OnLoadMoreItemsListener;
import com.uzumaki.naruto.instagramclone.models.Photo;
import com.uzumaki.naruto.instagramclone.utils.BottomNavigationViewHelper;
import com.uzumaki.naruto.instagramclone.utils.UniversalImageLoader;
import com.uzumaki.naruto.instagramclone.views.adapters.SectionPagerAdapter;
import com.uzumaki.naruto.instagramclone.views.fragments.CameraFragment;
import com.uzumaki.naruto.instagramclone.views.fragments.HomeFragment;
import com.uzumaki.naruto.instagramclone.views.fragments.MessagesFragment;
import com.uzumaki.naruto.instagramclone.views.fragments.ViewCommentsFragment;

/**
 * Created by Gaurav Bist on 04-07-2017.
 */

public class HomeActivity extends AppCompatActivity implements OnLoadMoreItemsListener {
    private static final String TAG = "HomeActivity";
    private Context mContext;

    // constants
    private static final int ACTVITY_NUMBER = 0;

    // widgets
    TabLayout tabLayout;
    ViewPager mViewPager;
    FrameLayout mFrameLayout;
    RelativeLayout mRelativeLayout;

    // firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mContext = this;

        bindViews();
        setupViewPager();
        setupBottomNavigationView();
        initImageLoader();

        setupFirebaseAuth();
    }

    private void bindViews() {
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        mFrameLayout = (FrameLayout) findViewById(R.id.container);

        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayoutParent);
        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
    }

    /**
     * To add the fragments to adapter : Camera, home, messages
     */
    private void setupViewPager() {
        SectionPagerAdapter sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        sectionPagerAdapter.addFragment(new CameraFragment());
        sectionPagerAdapter.addFragment(new HomeFragment());
        sectionPagerAdapter.addFragment(new MessagesFragment());

        mViewPager.setAdapter(sectionPagerAdapter);

        // setting up the top tab bar
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.instagram_logo);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_arrow);

        // Default fragment is the feed fragment
        mViewPager.setCurrentItem(1);
    }

    public void hideRelLayout() {
        Log.d(TAG, "hideRelLayout: Making the comments layout visible.");
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }

    public void showRelLayout() {
        Log.d(TAG, "hideRelLayout: Making the comments layout invisible.");
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mFrameLayout.getVisibility() == View.VISIBLE) {
            showRelLayout();
        }
    }

    // TODO setting up the image Loader
    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    public void onCommentThreadSelectec(Photo photo, String callingActivity) {
        Log.d(TAG, "onCommentThreadSelectec: Selected a comment thread.");

        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.tab_photo), photo);
        args.putString(getString(R.string.home_activity), callingActivity);

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(getString(R.string.view_comment_fragment))
                .commit();
    }

    /**
     * BottomNavigationView Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);

        // removes all types of animations in bottomnNvigationView....
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);

        // it starts the activity chosen using switch statements....
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTVITY_NUMBER);
        menuItem.setChecked(true);
    }

    private void setupFirebaseAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                checkCurrentUser(user);
            }
        };
    }

    private void checkCurrentUser(FirebaseUser mFirebaseUser) {
        if (mFirebaseUser == null) {
            // UserPrivate is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
            startActivity(new Intent(mContext, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } else {
            // UserPrivate is signed in
            Log.d(TAG, "onAuthStateChanged:signed_in:" + mFirebaseUser.getUid());
        }
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
    public void onLoadMore() {
        Log.d(TAG, "onLoadMore: Displaying more photos.");
        HomeFragment fragment = (HomeFragment) getSupportFragmentManager()
                .findFragmentByTag("android:switcher" + R.id.viewpager_container + ":" + mViewPager.getCurrentItem());

        if (fragment != null) {
            fragment.displayMorePhotos();
        }
    }
}
