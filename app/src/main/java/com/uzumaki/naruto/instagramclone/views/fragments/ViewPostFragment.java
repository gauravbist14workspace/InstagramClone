package com.uzumaki.naruto.instagramclone.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.interfaces.OnCommentThreadSelectedListener;
import com.uzumaki.naruto.instagramclone.models.Comment;
import com.uzumaki.naruto.instagramclone.models.Heart;
import com.uzumaki.naruto.instagramclone.models.Like;
import com.uzumaki.naruto.instagramclone.models.Photo;
import com.uzumaki.naruto.instagramclone.models.UserAccount;
import com.uzumaki.naruto.instagramclone.models.UserPrivate;
import com.uzumaki.naruto.instagramclone.utils.BottomNavigationViewHelper;
import com.uzumaki.naruto.instagramclone.utils.Constants;
import com.uzumaki.naruto.instagramclone.utils.SquareImageView;
import com.uzumaki.naruto.instagramclone.utils.UniversalImageLoader;
import com.uzumaki.naruto.instagramclone.views.activities.LoginActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gaurav Bist on 24-08-2017.
 */

public class ViewPostFragment extends Fragment {
    private static final String TAG = "ViewPostFragment";
    private Context mContext;

    // widgets
    SquareImageView post_photo;
    BottomNavigationViewEx bottomNavViewBar;
    TextView username, image_caption, image_likes, image_time_stamp, backLabel, image_comments_link;
    ImageView backArrow, mEllipses, mHeartRed, mHeartWhite, mSpeechBubble;
    CircleImageView profile_photo;

    // vars
    private Heart mHeart;
    private Photo mPhoto;
    private int mActivityNumber = 0;
    private UserAccount settings;
    private UserPrivate mCurrentUser;
    private GestureDetector mGestureDetector;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUser;
    private String mLikesString = "";
    private OnCommentThreadSelectedListener listener;

    // firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);

        bindViews(view);

        setupFirebaseAuth();
        setupBottomNavigationView();

//        init();
//        testToggle();

        return view;
    }

    private void bindViews(View view) {
        backArrow = (ImageView) view.findViewById(R.id.backArrow);

        profile_photo = (CircleImageView) view.findViewById(R.id.profile_photo);
        username = (TextView) view.findViewById(R.id.username);
        mEllipses = (ImageView) view.findViewById(R.id.ellipses);

        post_photo = (SquareImageView) view.findViewById(R.id.post_photo);

        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red);
        mSpeechBubble = (ImageView) view.findViewById(R.id.speech_bubble);

        image_caption = (TextView) view.findViewById(R.id.image_caption);
        image_likes = (TextView) view.findViewById(R.id.image_likes);
        image_time_stamp = (TextView) view.findViewById(R.id.image_time_stamp);
        image_comments_link = (TextView) view.findViewById(R.id.image_comments_link);

        bottomNavViewBar = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
    }

    private void init() {
        mContext = getActivity();

        // get the activity number and the image to be displayed into square imageview
        try {
            // load the imageview statically
            UniversalImageLoader.setImage(getPhotoFromBundle().getImage_path(), post_photo, null, "");
            mActivityNumber = getActivityFromBundle();
//            mPhoto = getPhotoFromBundle();
            String photo_id = getPhotoFromBundle().getPhoto_id();
            fetchPhotoDetailsAgain(photo_id);

        } catch (NullPointerException e) {
            Log.d(TAG, "onCreateView: NullPointerException" + e.getMessage());
            mPhoto = null;
        }

//        mHeartRed.setVisibility(View.GONE);
//        mHeartWhite.setVisibility(View.VISIBLE);

        mHeart = new Heart(mHeartWhite, mHeartRed);
        mGestureDetector = new GestureDetector(new GestureListener());

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating back to Profile Activity.");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mSpeechBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating back to Profile Activity.");
                if (listener != null) {
                    listener.onCommentSelected(mPhoto);
                }
            }
        });

        if (getPhotoFromBundle().getComments().size() > 0) {
            image_comments_link.setText("View all " + getPhotoFromBundle().getComments().size() + " comments.");
        } else {
            image_comments_link.setText("");
        }

        image_comments_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCommentSelected(mPhoto);
            }
        });
    }

    // Retrieve the activity number from the intent
    @Nullable
    private int getActivityFromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getInt(getString(R.string.calling_activity));
        } else {
            return 0;
        }
    }

    // Retrieve the Photo parcelable object from the intent
    @Nullable
    private Photo getPhotoFromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.tab_photo));
        } else {
            return null;
        }
    }

    private void fetchPhotoDetailsAgain(String photo_id) {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.PHOTOS)
                .orderByChild(mContext.getString(R.string.firebaseField_photo_id))
                .equalTo(photo_id);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    // now set each value manually to the Photo object
                    Photo newPhoto = new Photo();
                    newPhoto.setCaption(objectMap.get(mContext.getString(R.string.firebaseField_caption)).toString());
                    newPhoto.setTags(objectMap.get(mContext.getString(R.string.firebaseField_tags)).toString());
                    newPhoto.setPhoto_id(objectMap.get(mContext.getString(R.string.firebaseField_photo_id)).toString());
                    newPhoto.setUser_id(objectMap.get(mContext.getString(R.string.firebaseUserId)).toString());
                    newPhoto.setDate_created(objectMap.get(mContext.getString(R.string.firebaseField_date_created)).toString());
                    newPhoto.setImage_path(objectMap.get(mContext.getString(R.string.firebaseField_image_path)).toString());

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
                    newPhoto.setComments(commentList);

                    mPhoto = newPhoto;

                    // get the details of the current user
                    getCurrentUser();

                    // retrieve the details of the photo such as postedBy, postedDate etc
                    getPhotoDetails();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query cancelled.");
            }
        });
    }

    private void getCurrentUser() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USER_PRIVATE_INFO)
                .orderByChild(getString(R.string.firebaseUserId))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mCurrentUser = singleSnapshot.getValue(UserPrivate.class);
                }

                // retrieve the number of likes and their usernames
                getLikesString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query cancelled.");
            }
        });
    }

    private void getPhotoDetails() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USER_ACCOUNT)
                .orderByChild(getString(R.string.firebaseUserId))
                .equalTo(mPhoto.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    settings = singleSnapshot.getValue(UserAccount.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query cancelled.");
            }
        });
    }

//    private void testToggle() {
//        mHeartRed.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return mGestureDetector.onTouchEvent(motionEvent);
//            }
//        });
//
//        mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return mGestureDetector.onTouchEvent(motionEvent);
//            }
//        });
//    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Query query = mFirebaseDatabase.getReference()
                    .child(Constants.USER_PHOTOS)
                    .child(mPhoto.getUser_id())
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.firebaseField_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        String keyID = singleSnapshot.getKey();

                        // Case 1: User already liked the photo
                        if (mLikedByCurrentUser &&
                                singleSnapshot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            Log.d(TAG, "onDataChange: Already liked by user.");
                            mFirebaseDatabase.getReference().child(Constants.PHOTOS)
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.firebaseField_likes))
                                    .child(keyID)
                                    .removeValue();

                            mFirebaseDatabase.getReference().child(Constants.USER_PHOTOS)
                                    .child(mPhoto.getUser_id())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.firebaseField_likes))
                                    .child(keyID)
                                    .removeValue();

                            mHeart.toggleLike();
                            getLikesString();
                        }

                        // Case 2: The user has not liked the photo
                        else if (!mLikedByCurrentUser) {
                            Log.d(TAG, "onDataChange: Not liked by user.");
                            // add new like
                            addNewLike();
                            break;
                        }
                    }

                    if (!dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: No likes till now.");
                        // add new like
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void addNewLike() {
        Log.d(TAG, "addNewLike: Adding new like.");
        String newLikeID = mFirebaseDatabase.getReference().push().getKey();

        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mFirebaseDatabase.getReference().child(Constants.PHOTOS)
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.firebaseField_likes))
                .child(newLikeID)
                .setValue(like);

        mFirebaseDatabase.getReference().child(Constants.USER_PHOTOS)
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.firebaseField_likes))
                .child(newLikeID)
                .setValue(like);

        mHeart.toggleLike();
        getLikesString();
    }

    private void getLikesString() {
        Log.d(TAG, "getLikesString: Getting the likes status.");

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USER_PHOTOS)
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.firebaseField_likes));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUser = new StringBuilder();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    // Nested query to handle the likes hashMap
                    Query query = mFirebaseDatabase.getReference()
                            .child(Constants.USER_ACCOUNT)
                            .orderByChild(getString(R.string.firebaseUserId))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mLikedByCurrentUser = false;
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                Log.d(TAG, "onDataChange: Found like: " +
                                        singleSnapshot.getValue(UserAccount.class).getUser_name());
//
//                                if(singleSnapshot.getValue(UserAccount.class).getUser_name()
//                                        .equals(mCurrentUser.getUser_name()))
//                                    mLikedByCurrentUser = true;

                                mUser.append(singleSnapshot.getValue(UserAccount.class).getUser_name());
                                mUser.append(",");
                            }

                            String[] splitUsers = mUser.toString().split(",");
                            if (mUser.toString().contains(mCurrentUser.getUser_name() + ","))
                                mLikedByCurrentUser = true;
                            else
                                mLikedByCurrentUser = false;

                            int length = splitUsers.length;
                            if (length == 1) {
                                mLikesString = "Liked by " + splitUsers[0] + ".";
                            } else if (length == 2) {
                                mLikesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1] + ".";
                            } else if (length == 3) {
                                mLikesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + " and " + splitUsers[2] + ".";
                            } else if (length > 3) {
                                mLikesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + ", " + splitUsers[2]
                                        + " and " + (splitUsers.length - 2) + " others.";
                            }

                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                if (!dataSnapshot.exists()) {
                    mLikesString = "";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupWidgets() {
        Log.d(TAG, "setupWidgets: Setting up the time stamp for post.");

        String timeStampDifference = getTimeStampDifference();
        if (!timeStampDifference.equals("0")) {
            image_time_stamp.setText(timeStampDifference + " DAYS AGO");
        } else {
            image_time_stamp.setText("TODAY");
        }

        Log.d(TAG, "setupWidgets: Setting up the profile photo and username.");
        UniversalImageLoader.setImage(settings.getProfile_photo(), profile_photo, null, "");
        username.setText(settings.getUser_name());
        image_likes.setText(mLikesString);
        image_caption.setText(mPhoto.getCaption());

        if (mLikedByCurrentUser) {
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);

            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return mGestureDetector.onTouchEvent(motionEvent);
                }
            });
        } else {
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);

            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return mGestureDetector.onTouchEvent(motionEvent);
                }
            });
        }
    }

    /**
     * Returns a string representing the number of days passed since it was posted
     *
     * @return
     */
    private String getTimeStampDifference() {
        String difference = "";

        Calendar calendar = Calendar.getInstance();
        Date photoTimeStamp, todayTimeStamp;

        try {
            String photoDate = mPhoto.getDate_created();
            photoTimeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
                    .parse(photoDate);
            todayTimeStamp = calendar.getTime();

            difference = String.valueOf(Math.round(((todayTimeStamp.getTime() - photoTimeStamp.getTime()) / 1000 / 60 / 60 / 24)));
        } catch (ParseException e) {
            Log.d(TAG, "getTimeStampDifference: ParseException" + e.getMessage());
            difference = "0";
        }
        return difference;
    }

    /**
     * BottomNavigationViewEx setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavViewBar);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), bottomNavViewBar);

        Menu menu = bottomNavViewBar.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }

    /////////////////////////////////////// FIREBASE ///////////////////////////////////////////////

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnCommentThreadSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
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
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            init();
        }
    }
}
