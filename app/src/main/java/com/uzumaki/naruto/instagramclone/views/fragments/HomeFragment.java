package com.uzumaki.naruto.instagramclone.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.models.Comment;
import com.uzumaki.naruto.instagramclone.models.Photo;
import com.uzumaki.naruto.instagramclone.utils.Constants;
import com.uzumaki.naruto.instagramclone.views.adapters.MainFeedAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gaurav Bist on 04-07-2017.
 */

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private Context mContext;

    // vars
    private List<Photo> mPhotos;
    private List<Photo> mPaginatedPhotos;
    private ArrayList<String> mFollowing;
    private MainFeedAdapter adapter;
    private int mResults;

    // widgets
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        bindViews(view);
        init();

        return view;
    }

    private void bindViews(View view) {
        listView = (ListView) view.findViewById(R.id.listView);
    }

    private void init() {
        mContext = getActivity();

        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();
        mPaginatedPhotos = new ArrayList<>();

        // get list of user who our user is following so he can see their post
        getFollowing();

    }

    private void getFollowing() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FOLLOWING)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: Followers found: " +
                            singleSnapshot.child(getString(R.string.firebaseUserId)).getValue());

                    mFollowing.add(singleSnapshot.child(getString(R.string.firebaseUserId)).getValue().toString());
                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                // fetch the photos of the user list
                getPhotos();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPhotos() {
        Log.d(TAG, "getPhotos: Getting photos from the server ...");

        for (int i = 0; i < mFollowing.size(); i++) {
            final int count = i;

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.USER_PHOTOS)
                    .child(mFollowing.get(count))
                    .orderByChild(getString(R.string.firebaseUserId))
                    .equalTo(mFollowing.get(count));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

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

                        // we dont need likes method because already handled in the arrayAdapter

                        mPhotos.add(photo);
                    }

                    if (count >= mFollowing.size() - 1) {
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void displayPhotos() {
        if (mPhotos != null) {
            try {
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo photo, Photo t1) {
                        return t1.getDate_created().compareTo(photo.getDate_created());
                    }
                });

                int iteration = mPhotos.size();

                if (iteration > 10) {
                    iteration = 10;
                }

                mResults = 10;
                for (int i = 0; i < iteration; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                adapter = new MainFeedAdapter(mContext, R.layout.item_layout_mainfeed_list, mPaginatedPhotos);
                listView.setAdapter(adapter);
            } catch (NullPointerException e) {
                Log.d(TAG, "displayPhotos: NullPointerException: " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage());
            }
        }
    }

    public void displayMorePhotos() {
        Log.d(TAG, "displayMorePhotos: Displaying more photos.");

        try {
            if (mPhotos.size() > mResults && mPhotos.size() > 0) {
                int iteration;
                if (mPhotos.size() > (mResults + 10)) {
                    Log.d(TAG, "displayMorePhotos: There are again more than 10 photos.");
                    iteration = 10;
                } else {
                    Log.d(TAG, "displayMorePhotos: There are less than 10 photos.");
                    iteration = mPhotos.size() - mResults;
                }

                // add the new photos to paginated results
                for (int i = mResults; i < mResults + iteration; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults = mResults + iteration;

                adapter = new MainFeedAdapter(mContext, R.layout.item_layout_mainfeed_list, mPaginatedPhotos);
                listView.setAdapter(adapter);
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "displayPhotos: NullPointerException: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage());
        }
    }
}
