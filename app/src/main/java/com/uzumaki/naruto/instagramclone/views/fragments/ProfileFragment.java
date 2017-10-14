package com.uzumaki.naruto.instagramclone.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.interfaces.OnGridImageSelectedListener;
import com.uzumaki.naruto.instagramclone.models.Comment;
import com.uzumaki.naruto.instagramclone.models.Like;
import com.uzumaki.naruto.instagramclone.models.Photo;
import com.uzumaki.naruto.instagramclone.models.UserAccount;
import com.uzumaki.naruto.instagramclone.models.UserPrivate;
import com.uzumaki.naruto.instagramclone.models.UserSettings;
import com.uzumaki.naruto.instagramclone.utils.BottomNavigationViewHelper;
import com.uzumaki.naruto.instagramclone.utils.Constants;
import com.uzumaki.naruto.instagramclone.utils.UniversalImageLoader;
import com.uzumaki.naruto.instagramclone.views.activities.AccountSettingActivity;
import com.uzumaki.naruto.instagramclone.views.activities.LoginActivity;
import com.uzumaki.naruto.instagramclone.views.activities.ProfileActivity;
import com.uzumaki.naruto.instagramclone.views.adapters.GridImageAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gaurav Bist on 06-07-2017.
 */

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private Context mContext;

    // constants
    private static final int ACTVITY_NUMBER = 4;
    private static final int NUM_GRID_COLOUMNS = 3;

    // widgets
    private TextView posts, followers, following, displayName, website, description, editProfile;
    private ProgressBar mProgressBar;
    private CircleImageView profileImage;
    private GridView gridView;
    private Toolbar profileToolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationViewEx;

    // firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private DatabaseReference myRef;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // variabes
    private int mFollowersCount = 0, mFollowingCount = 0, mPostsCount = 0;
    private String userID;
    private ArrayList<Photo> photos;
    private OnGridImageSelectedListener mOnGridImageSelectedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        bindViews(view);
        init();
        setupBottomNavigationView();
        setupFirebaseAuth();
        setupGridView();

        getFollowersCount();
        getFollowingCount();
        getPostsCount();

        return view;
    }

    private void bindViews(View view) {
        profileToolbar = (Toolbar) view.findViewById(R.id.profileToolbar);
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);

        displayName = (TextView) view.findViewById(R.id.displayName);
        website = (TextView) view.findViewById(R.id.website);
        description = (TextView) view.findViewById(R.id.description);

        profileImage = (CircleImageView) view.findViewById(R.id.profileImage);
        posts = (TextView) view.findViewById(R.id.posts);
        followers = (TextView) view.findViewById(R.id.followers);
        following = (TextView) view.findViewById(R.id.following);
        editProfile = (TextView) view.findViewById(R.id.editProfile);

        mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
        gridView = (GridView) view.findViewById(R.id.gridView);

        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
    }

    private void init() {
        mContext = getActivity();

        setupToolbar();

        mProgressBar.setVisibility(View.GONE);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to Profile.");

                startActivity(new Intent(mContext, AccountSettingActivity.class)
                        .putExtra(mContext.getString(R.string.calling_activity), getString(R.string.profile_activity)));
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    private void setupToolbar() {
        ((ProfileActivity) mContext).setSupportActionBar(profileToolbar);

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating to AccountSettings");
                startActivity(new Intent(mContext, AccountSettingActivity.class));
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    /**
     * A method to statically set an image from given url to the imageView
     */
//    private void setProfileImage() {
//        String imageUrl = "crackberry.com/sites/crackberry.com/files/styles/large/public/topic_images/2013/ANDROID.png?itok=xhm7jaxS";
//        UniversalImageLoader.setImage(imageUrl, profileImage, mProgressBar, "https://");
//    }

    /**
     * BottomNavigationViewEx setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTVITY_NUMBER);
        menuItem.setChecked(true);
    }

    private void setupGridView() {
        Log.d(TAG, "setupGridView: Setting up the image grid.");
        if (mFirebaseStorage == null)
            mFirebaseStorage = FirebaseStorage.getInstance();

        photos = new ArrayList<>();

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USER_PHOTOS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                    photos.add(singleSnapshot.getValue(Photo.class));

                    // This tweak is used because of likes hashMap inside the photo hashMap in firebase database
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    try {
                        // now set each value manually to the Photo object
                        Photo photo = new Photo();
                        photo.setCaption(objectMap.get(getString(R.string.firebaseField_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.firebaseField_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.firebaseField_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.firebaseUserId)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.firebaseField_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.firebaseField_image_path)).toString());

                        // Now to get the comments hashMap from the firebase database
                        List<Comment> commentList = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(mContext.getString(R.string.firebaseField_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());

                            commentList.add(comment);
                        }

                        photo.setComments(commentList);

                        // Now to get the likes hashMap from firebase database
                        List<Like> likesList = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.firebaseField_likes)).getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());

                            likesList.add(like);
                        }
                        photo.setLikes(likesList);

                        photos.add(photo);
                    } catch (NullPointerException e) {
                        Log.d(TAG, "onDataChange: NullPointerException: " + e.getMessage());
                    }
                }

                // setup the image grid
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / NUM_GRID_COLOUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<String>();
                for (int i = 0; i < photos.size(); i++) {
                    imgUrls.add(photos.get(i).getImage_path());
                }
                GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview,
                        "", imgUrls);
                gridView.setAdapter(adapter);
                gridView.setOnItemClickListener(onItemClickListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query cancelled.");
            }
        });
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
            mOnGridImageSelectedListener.onGridImageSelcted(photos.get(pos), ACTVITY_NUMBER);
        }
    };

    private void getFollowersCount() {
        mFollowersCount = 0;

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FOLLOWERS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: Followers found." + singleSnapshot.getValue());

                    mFollowersCount++;
                }

                followers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount() {
        mFollowingCount = 0;

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FOLLOWING)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: Following number of users found." + singleSnapshot.getValue());

                    mFollowingCount++;
                }

                following.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPostsCount() {
        mPostsCount = 0;

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USER_PHOTOS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: Following number of users found." + singleSnapshot.getValue());

                    mPostsCount++;
                }

                posts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*
   ------------------------------------ Firebase ---------------------------------------------
    */
    private void setupFirebaseAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out\nReturning to LoginActivity");
                    startActivity(new Intent(mContext, LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                } else {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //retrieve user information from the database as soon as there is some change in firebase user records....
                setProfileWidgets(getUserSettings(dataSnapshot));

                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase.");

        userID = mFirebaseAuth.getCurrentUser().getUid();

        UserAccount settings = new UserAccount();
        UserPrivate user = new UserPrivate();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            // user_account node
            if (ds.getKey().equals(Constants.USER_ACCOUNT)) {
                Log.d(TAG, "getUserAccountSettings: user account settings node datasnapshot: " + ds);

                try {
                    settings.setDisplay_name(ds.child(userID)
                            .getValue(UserAccount.class)
                            .getDisplay_name()
                    );
                    settings.setUser_name(ds.child(userID)
                            .getValue(UserAccount.class)
                            .getUser_name()
                    );
                    settings.setWebsite(ds.child(userID)
                            .getValue(UserAccount.class)
                            .getWebsite()
                    );
                    settings.setDescription(ds.child(userID)
                            .getValue(UserAccount.class)
                            .getDescription()
                    );
                    settings.setProfile_photo(ds.child(userID)
                            .getValue(UserAccount.class)
                            .getProfile_photo()
                    );
                    settings.setPosts(ds.child(userID)
                            .getValue(UserAccount.class)
                            .getPosts()
                    );
                    settings.setFollowing(ds.child(userID)
                            .getValue(UserAccount.class)
                            .getFollowing()
                    );
                    settings.setFollowers(ds.child(userID)
                            .getValue(UserAccount.class)
                            .getFollowers()
                    );

                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + settings.toString());
                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + e.getMessage());
                }
            }


            // users node
            Log.d(TAG, "getUserSettings: snapshot key: " + ds.getKey());
            if (ds.getKey().equals(Constants.USER_PRIVATE_INFO)) {
                Log.d(TAG, "getUserAccountSettings: users node datasnapshot: " + ds);

                user.setUser_name(ds.child(userID)
                        .getValue(UserPrivate.class)
                        .getUser_name());

                user.setEmailId(ds.child(userID)
                        .getValue(UserPrivate.class)
                        .getEmailId()
                );
                user.setPhone_number(ds.child(userID)
                        .getValue(UserPrivate.class)
                        .getPhone_number()
                );
                user.setUser_id(ds.child(userID)
                        .getValue(UserPrivate.class)
                        .getUser_id()
                );

                Log.d(TAG, "getUserAccountSettings: retrieved users information: " + user.toString());
            }
        }
        return new UserSettings(user, settings);
    }

    private void setProfileWidgets(UserSettings userSettings) {
        UserAccount settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), profileImage, null, "");

        //displayName.setText(settings.getDisplay_name());
        displayName.setText(settings.getDisplay_name());
        website.setText(settings.getWebsite());
        description.setText(settings.getDescription());
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(Context context) {
        try {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
        super.onAttach(context);
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
}
