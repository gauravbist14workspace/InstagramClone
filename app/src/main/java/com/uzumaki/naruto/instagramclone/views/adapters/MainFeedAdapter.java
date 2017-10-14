package com.uzumaki.naruto.instagramclone.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.interfaces.OnLoadMoreItemsListener;
import com.uzumaki.naruto.instagramclone.models.Comment;
import com.uzumaki.naruto.instagramclone.models.Heart;
import com.uzumaki.naruto.instagramclone.models.Like;
import com.uzumaki.naruto.instagramclone.models.Photo;
import com.uzumaki.naruto.instagramclone.models.UserAccount;
import com.uzumaki.naruto.instagramclone.models.UserPrivate;
import com.uzumaki.naruto.instagramclone.utils.Constants;
import com.uzumaki.naruto.instagramclone.utils.SquareImageView;
import com.uzumaki.naruto.instagramclone.views.activities.HomeActivity;
import com.uzumaki.naruto.instagramclone.views.activities.ProfileActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gaurav Bist on 29-09-2017.
 */

public class MainFeedAdapter extends ArrayAdapter<Photo> {
    private static final String TAG = "MainFeedAdapter";
    private Context mContext;

    // vars
    private OnLoadMoreItemsListener listener;
    private String currentUserName;
    private LayoutInflater inflater;
    private int mLayoutResource;


    public MainFeedAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mContext = context;

        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
    }

    private class Holder {
        CircleImageView profile_photo;
        TextView username, image_likes, image_caption, image_comments_link, image_time_stamp;
        SquareImageView post_photo;
        ImageView image_heart_red, image_heart_white, speech_bubble;

        UserAccount userAccount = new UserAccount();
        UserPrivate user = new UserPrivate();
        StringBuilder users;
        String mLikesString;
        boolean likedByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
    }

    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = inflater.inflate(mLayoutResource, parent, false);

            holder.profile_photo = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.image_likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.image_caption = (TextView) convertView.findViewById(R.id.image_caption);
            holder.image_comments_link = (TextView) convertView.findViewById(R.id.image_comments_link);
            holder.image_time_stamp = (TextView) convertView.findViewById(R.id.image_time_stamp);

            holder.post_photo = (SquareImageView) convertView.findViewById(R.id.post_photo);

            holder.image_heart_red = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.image_heart_white = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.speech_bubble = (ImageView) convertView.findViewById(R.id.speech_bubble);

            holder.heart = new Heart(holder.image_heart_white, holder.image_heart_red);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));

            holder.users = new StringBuilder();

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        // getting the current username (need for fetching likes string)
        getCurrentUsername();

        // get likes string
        getLikesString(holder);

        // set the caption
        holder.image_caption.setText(getItem(position).getCaption());

        // set the comment
        List<Comment> commentList = getItem(position).getComments();
        holder.image_comments_link.setText("View all " + commentList.size() + " comments.");
        holder.image_comments_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Loading comment thread for " + getItem(position).getPhoto_id());

                ((HomeActivity) mContext).onCommentThreadSelectec(holder.photo,
                        mContext.getString(R.string.home_activity));

                //TODO got to add something here
                ((HomeActivity) mContext).hideRelLayout();
            }
        });

        // set the time it was posted
        String timeStampDifference = getTimeStampDifference(holder.photo);
        if (!timeStampDifference.equals("0")) {
            holder.image_time_stamp.setText(timeStampDifference + " DAYS AGO.");
        } else {
            holder.image_time_stamp.setText("TODAY");
        }

        // set the posted image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.post_photo);

        // get the profile of uploader
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USER_ACCOUNT)
                .orderByChild(mContext.getString(R.string.firebaseUserId))
                .equalTo(holder.photo.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: Found the user.");
//                    currentUserName = singleSnapshot.getValue(UserAccount.class).getUser_name();

                    holder.username.setText(singleSnapshot.getValue(UserAccount.class).getUser_name());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: Navigating to profile of " + holder.user.getUser_name());

                            mContext.startActivity(new Intent(mContext, ProfileActivity.class)
                                    .putExtra(mContext.getString(R.string.calling_activity),
                                            mContext.getString(R.string.home_activity))
                                    .putExtra(mContext.getString(R.string.intent_user),
                                            holder.user));
                        }
                    });

                    imageLoader.displayImage(singleSnapshot.getValue(UserAccount.class).getProfile_photo(), holder.profile_photo);
                    holder.profile_photo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: Navigating to profile of " + holder.user.getUser_name());

                            mContext.startActivity(new Intent(mContext, ProfileActivity.class)
                                    .putExtra(mContext.getString(R.string.calling_activity),
                                            mContext.getString(R.string.home_activity))
                                    .putExtra(mContext.getString(R.string.intent_user),
                                            holder.user));
                        }
                    });

                    holder.userAccount = singleSnapshot.getValue(UserAccount.class);
                    holder.speech_bubble.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: Loading[via speech bubble] comment thread for " + getItem(position).getPhoto_id());

                            ((HomeActivity) mContext).onCommentThreadSelectec(holder.photo,
                                    mContext.getString(R.string.home_activity));

                            //TODO got to add some other thing
                            ((HomeActivity) mContext).hideRelLayout();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // retrieve the user object
        query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USER_ACCOUNT)
                .orderByChild(mContext.getString(R.string.firebaseUserId))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: User found " + singleSnapshot.getValue(UserAccount.class).getUser_name());

                    holder.user = singleSnapshot.getValue(UserPrivate.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (reachedEndOfList(position)) {
            loadMoreData();
        }

        return convertView;
    }

    private boolean reachedEndOfList(int position) {
        return position == getCount() - 1;
    }

    private void loadMoreData() {
        try {
            listener = (OnLoadMoreItemsListener) getContext();
        } catch (ClassCastException e) {
            Log.d(TAG, "loadMoreData: ClassCastException: " + e.getMessage());
        }

        try {
            listener.onLoadMore();
        } catch (NullPointerException e) {
            Log.d(TAG, "loadMoreData: NullPointerException: " + e.getMessage());
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private Holder holder;

        public GestureListener(Holder holder) {
            this.holder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.USER_PHOTOS)
                    .child(holder.photo.getUser_id())
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.firebaseField_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        String keyID = singleSnapshot.getKey();

                        // Case 1: User already liked the photo
                        if (holder.likedByCurrentUser &&
                                singleSnapshot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            Log.d(TAG, "onDataChange: Already liked by user.");
                            FirebaseDatabase.getInstance().getReference()
                                    .child(Constants.PHOTOS)
                                    .child(holder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.firebaseField_likes))
                                    .child(keyID)
                                    .removeValue();

                            FirebaseDatabase.getInstance().getReference()
                                    .child(Constants.USER_PHOTOS)
                                    .child(holder.photo.getUser_id())
                                    .child(holder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.firebaseField_likes))
                                    .child(keyID)
                                    .removeValue();

                            holder.heart.toggleLike();
                            getLikesString(holder);
                        }

                        // Case 2: The user has not liked the photo
                        else if (!holder.likedByCurrentUser) {
                            Log.d(TAG, "onDataChange: Not liked by user.");
                            // add new like
                            addNewLike(holder);
                            break;
                        }
                    }

                    if (!dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: No likes till now.");
                        // add new like
                        addNewLike(holder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void addNewLike(Holder holder) {
        Log.d(TAG, "addNewLike: Adding new like.");
        String newLikeID = FirebaseDatabase.getInstance().getReference().push().getKey();

        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseDatabase.getInstance().getReference().child(Constants.PHOTOS)
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.firebaseField_likes))
                .child(newLikeID)
                .setValue(like);

        FirebaseDatabase.getInstance().getReference().child(Constants.USER_PHOTOS)
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.firebaseField_likes))
                .child(newLikeID)
                .setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void getCurrentUsername() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USER_ACCOUNT)
                .orderByChild(mContext.getString(R.string.firebaseUserId))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    currentUserName = singleSnapshot.getValue(UserAccount.class).getUser_name();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getLikesString(final Holder holder) {
        Log.d(TAG, "getLikesString: Getting the likes status.");

        try {
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.USER_PHOTOS)
                    .child(holder.photo.getUser_id())
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.firebaseField_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        // Nested query to handle the likes hashMap
                        Query query = FirebaseDatabase.getInstance().getReference()
                                .child(Constants.USER_ACCOUNT)
                                .orderByChild(mContext.getString(R.string.firebaseUserId))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                holder.likedByCurrentUser = false;
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: Found like: " +
                                            singleSnapshot.getValue(UserAccount.class).getUser_name());

                                    holder.users.append(singleSnapshot.getValue(UserAccount.class).getUser_name());
                                    holder.users.append(",");
                                }

                                String[] splitUsers = holder.users.toString().split(",");
                                if (holder.users.toString().contains(currentUserName + ","))
                                    holder.likedByCurrentUser = true;
                                else
                                    holder.likedByCurrentUser = false;

                                int length = splitUsers.length;
                                if (length == 1) {
                                    holder.mLikesString = "Liked by " + splitUsers[0] + ".";
                                } else if (length == 2) {
                                    holder.mLikesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1] + ".";
                                } else if (length == 3) {
                                    holder.mLikesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + " and " + splitUsers[2] + ".";
                                } else if (length > 3) {
                                    holder.mLikesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + ", " + splitUsers[2]
                                            + " and " + (splitUsers.length - 2) + " others.";
                                }

                                setupLikesString(holder, holder.mLikesString);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    if (!dataSnapshot.exists()) {
                        holder.mLikesString = "";
                        holder.likedByCurrentUser = false;
                        setupLikesString(holder, holder.mLikesString);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {
            Log.d(TAG, "getLikesString: NullPointerException " + e.getMessage());
            holder.mLikesString = "";
            holder.likedByCurrentUser = false;

            setupLikesString(holder, holder.mLikesString);
        }
    }

    private void setupLikesString(final Holder holder, String likesString) {
        if (holder.likedByCurrentUser) {
            holder.image_heart_white.setVisibility(View.GONE);
            holder.image_heart_red.setVisibility(View.VISIBLE);
            holder.image_heart_red.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return holder.detector.onTouchEvent(motionEvent);
                }
            });
        } else {
            holder.image_heart_white.setVisibility(View.VISIBLE);
            holder.image_heart_red.setVisibility(View.GONE);
            holder.image_heart_white.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return holder.detector.onTouchEvent(motionEvent);
                }
            });
        }

        holder.image_likes.setText(likesString);
    }

    private String getTimeStampDifference(Photo photo) {
        String difference = "";

        Calendar calendar = Calendar.getInstance();
        Date photoTimeStamp, todayTimeStamp;

        try {
            String photoDate = photo.getDate_created();
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
}
