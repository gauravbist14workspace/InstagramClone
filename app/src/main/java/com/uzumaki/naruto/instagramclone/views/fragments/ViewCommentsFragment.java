package com.uzumaki.naruto.instagramclone.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.models.Comment;
import com.uzumaki.naruto.instagramclone.models.Photo;
import com.uzumaki.naruto.instagramclone.utils.Constants;
import com.uzumaki.naruto.instagramclone.utils.FirebaseHelper;
import com.uzumaki.naruto.instagramclone.views.activities.HomeActivity;
import com.uzumaki.naruto.instagramclone.views.activities.LoginActivity;
import com.uzumaki.naruto.instagramclone.views.adapters.CommentListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gaurav Bist on 24-08-2017.
 */

public class ViewCommentsFragment extends Fragment {
    private static final String TAG = "ViewPostFragment";
    private Context mContext;

    // firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // vars
    private CommentListAdapter adapter;
    private FirebaseHelper mFirebaseHelper;
    private Photo mPhoto;
    private ArrayList<Comment> mComments;
    private ListView mListView;

    // widgets
    ImageView mBackArrow, mCheckMark;
    EditText mComment;

    public ViewCommentsFragment() {
        super();
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);

        bindViews(view);
        init();
        setupFirebaseAuth();

        return view;
    }

    private void bindViews(View view) {
        mBackArrow = (ImageView) view.findViewById(R.id.iv_back_arrow);

        mListView = (ListView) view.findViewById(R.id.listView);

        mComment = (EditText) view.findViewById(R.id.edit_comment);
        mCheckMark = (ImageView) view.findViewById(R.id.iv_post_comment);
    }

    private void init() {
        mContext = getActivity();
        mFirebaseHelper = new FirebaseHelper(mContext);

        mComments = new ArrayList<>();
        try {
            mPhoto = getPhotoFromBundle();
        } catch (NullPointerException e) {
            Log.d(TAG, "init: NullPointerException: " + e.getMessage());
        }

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating back.");
                if (callingActivityFromBundle().equals(getString(R.string.home_activity))) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    ((HomeActivity) getActivity()).showRelLayout();
                }
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mComment.getText().toString().equals(" ")) {
                    Log.d(TAG, "onClick: Submitting new comment.");

                    addNewComment(mComment.getText().toString());

                    mComment.setText("");
                    closeKeyboard();
                } else
                    Toast.makeText(mContext, "Comment must not be blank!", Toast.LENGTH_SHORT).show();
            }
        });
        adapter = new CommentListAdapter(mContext, R.layout.item_layout_comments, mComments);
        mListView.setAdapter(adapter);
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment) {
        Log.d(TAG, "addNewComment: Addingn new comment" + newComment);

        String commentId = mFirebaseDatabase.getReference().push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(mFirebaseHelper.getTimestamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mFirebaseDatabase.getReference().child(Constants.PHOTOS)
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.firebaseField_comments))
                .child(commentId)
                .setValue(comment);

        mFirebaseDatabase.getReference().child(Constants.USER_PHOTOS)
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.firebaseField_comments))
                .child(commentId)
                .setValue(comment);
    }

    @Nullable
    private String callingActivityFromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getString(mContext.getString(R.string.home_activity));
        } else {
            return null;
        }
    }

    // Retrieve the Photo parcelable object from the intent
    @Nullable
    private Photo getPhotoFromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(mContext.getString(R.string.tab_photo));
        } else {
            return null;
        }
    }

    /////////////////////////////////////// FIREBASE ///////////////////////////////////////////////

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
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

        if (getPhotoFromBundle().getComments().size() == 0) {
            grabTheFirstComment();
            adapter.notifyDataSetChanged();
        }

        myRef.child(Constants.PHOTOS)
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.firebaseField_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        getChanges();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getChanges() {

        Query query = myRef.child(Constants.PHOTOS)
                .orderByChild(mContext.getString(R.string.firebaseField_photo_id))
                .equalTo(mPhoto.getPhoto_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                    photos.add(singleSnapshot.getValue(Photo.class));

                    // This tweak is used because of likes hashMap inside the photo hashMap in firebase database
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    // now set each value manually to the Photo object
                    Photo photo = new Photo();
                    photo.setCaption(objectMap.get(mContext.getString(R.string.firebaseField_caption)).toString());
                    photo.setTags(objectMap.get(mContext.getString(R.string.firebaseField_tags)).toString());
                    photo.setPhoto_id(objectMap.get(mContext.getString(R.string.firebaseField_photo_id)).toString());
                    photo.setUser_id(objectMap.get(mContext.getString(R.string.firebaseUserId)).toString());
                    photo.setDate_created(objectMap.get(mContext.getString(R.string.firebaseField_date_created)).toString());
                    photo.setImage_path(objectMap.get(mContext.getString(R.string.firebaseField_image_path)).toString());

                    // Now to get the likes hashMap from firebase database
//                    List<Like> likesList = new ArrayList<Like>();
//                    for (DataSnapshot dSnapshot : singleSnapshot
//                            .child(mContext.getString(R.string.firebaseField_likes)).getChildren()) {
//                        Like like = new Like();
//                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//
//                        likesList.add(like);
//                    }
//
                    grabTheFirstComment();

                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child(mContext.getString(R.string.firebaseField_comments)).getChildren()) {
                        Comment comment = new Comment();
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());

                        mComments.add(comment);
                    }

                    photo.setComments(mComments);
                    mPhoto = photo;

                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query cancelled.");
            }
        });
    }

    private void grabTheFirstComment() {
        mComments.clear();

        Comment firstComment = new Comment();
        firstComment.setComment(mPhoto.getCaption());
        firstComment.setUser_id(mPhoto.getUser_id());
        firstComment.setDate_created(mPhoto.getDate_created());

        mComments.add(firstComment);
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
