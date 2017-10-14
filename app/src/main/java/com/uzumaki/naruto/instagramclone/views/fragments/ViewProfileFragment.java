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
import android.widget.Toast;

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

public class ViewProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private Context mContext;

    // constants
    private static final int ACTVITY_NUMBER = 4;
    private static final int NUM_GRID_COLOUMNS = 3;

    // widgets
    private TextView posts, followers, following, displayName, website, description, editProfile,
            mFollow, mUnFollow;
    private ProgressBar mProgressBar;
    private CircleImageView profileImage;
    private GridView gridView;
    private ImageView backArrow;
    private BottomNavigationViewEx bottomNavigationViewEx;

    // firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private DatabaseReference myRef;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // vars
    private int mFollowersCount = 0, mFollowingCount = 0, mPostsCount = 0;
    private UserPrivate mUser;
    private String userID;
    private ArrayList<Photo> photos;
    private OnGridImageSelectedListener mOnGridImageSelectedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);

        bindViews(view);
        init();
        setupBottomNavigationView();
        setupFirebaseAuth();

        isFollowing();
        getFollowersCount();
        getFollowingCount();
        getPostsCount();

        return view;
    }

    private void bindViews(View view) {
        backArrow = (ImageView) view.findViewById(R.id.backArrow);

        displayName = (TextView) view.findViewById(R.id.displayName);
        website = (TextView) view.findViewById(R.id.website);
        description = (TextView) view.findViewById(R.id.description);

        profileImage = (CircleImageView) view.findViewById(R.id.profileImage);
        posts = (TextView) view.findViewById(R.id.posts);
        followers = (TextView) view.findViewById(R.id.followers);
        following = (TextView) view.findViewById(R.id.following);

        editProfile = (TextView) view.findViewById(R.id.editProfile);
        mFollow = (TextView) view.findViewById(R.id.follow);
        mUnFollow = (TextView) view.findViewById(R.id.unfollow);

        mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
        gridView = (GridView) view.findViewById(R.id.gridView);

        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
    }

    private void init() {
        mContext = getActivity();

        mProgressBar.setVisibility(View.GONE);

        try {
            mUser = getDetailsFromBundle();
            getUserAccountDetails();
            getUserPhotos();
        } catch (NullPointerException e) {
            Log.d(TAG, "init: Failed to get UserPrivate object from bundle.\n" + e.getMessage());

            Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, ProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Now following: " + mUser.getUser_name());

                FirebaseDatabase.getInstance().getReference()
                        .child(Constants.FOLLOWING)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .child(getString(R.string.firebaseUserId))
                        .setValue(mUser.getUser_id());

                FirebaseDatabase.getInstance().getReference()
                        .child(Constants.FOLLOWERS)
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.firebaseUserId))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());

                setFollowing();
            }
        });

        mUnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Now unFollowing: " + mUser.getUser_name());

                FirebaseDatabase.getInstance().getReference()
                        .child(Constants.FOLLOWING)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(Constants.FOLLOWERS)
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();

                setUnFollowing();
            }
        });

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

    private void isFollowing() {
        Log.d(TAG, "isFollowing: Check if following the user.");
        setUnFollowing();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(Constants.FOLLOWING)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.firebaseUserId))
                .equalTo(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: Username found." + singleSnapshot.getValue());

                    setFollowing();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

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

    private void setFollowing() {
        Log.d(TAG, "setFollowing: Updating ui for following this user.");

        mFollow.setVisibility(View.GONE);
        mUnFollow.setVisibility(View.VISIBLE);
        editProfile.setVisibility(View.GONE);
    }

    private void setUnFollowing() {
        Log.d(TAG, "setFollowing: Updating ui for un-following this user.");

        mFollow.setVisibility(View.VISIBLE);
        mUnFollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.GONE);
    }

    private void setCurrentProfile() {
        Log.d(TAG, "setFollowing: Updating ui for current user.");

        mFollow.setVisibility(View.GONE);
        mUnFollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.VISIBLE);
    }

    private UserPrivate getDetailsFromBundle() {
        Log.d(TAG, "getDetailsFromBundle: Arguments " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.intent_user));
        } else
            return null;
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

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
            mOnGridImageSelectedListener.onGridImageSelcted(photos.get(pos), ACTVITY_NUMBER);
        }
    };

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
    }

    private void getUserAccountDetails() {
        // getting the account details of the searched user via his email
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(Constants.USER_ACCOUNT)
                .orderByChild(getString(R.string.firebaseUserId))
                .equalTo(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: Username found." + singleSnapshot.getValue(UserAccount.class).toString());

                    UserSettings settings = new UserSettings();
                    settings.setUser(mUser);
                    settings.setSettings(singleSnapshot.getValue(UserAccount.class));

                    setProfileWidgets(settings);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserPhotos() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USER_PHOTOS)
                .child(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                photos = new ArrayList<Photo>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                    photos.add(singleSnapshot.getValue(Photo.class));

                    // This tweak is used because of 'likes' hashMap inside the 'photo' node in firebase database
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    try{
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
                    }catch (NullPointerException e) {
                        Log.d(TAG, "onDataChange: NullPointerException");
                    }
                }

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
