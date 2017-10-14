package com.uzumaki.naruto.instagramclone.views.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.models.Comment;
import com.uzumaki.naruto.instagramclone.models.UserAccount;
import com.uzumaki.naruto.instagramclone.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gaurav Bist on 14-09-2017.
 */

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private static final String TAG = "CommentListAdapter";
    private Context mContext;

    private LayoutInflater mLayoutInflater;
    private int layoutResource;

    public CommentListAdapter(Context context, int resource, List<Comment> objects) {
        super(context, resource, objects);

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }

    private class ViewHolder {
        TextView comment_username, comment, comment_time, comment_reply, comment_likes;
        CircleImageView comments_profile_image;
        ImageView like;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            convertView = mLayoutInflater.inflate(layoutResource, parent, false);

            viewHolder.comment_username = (TextView) convertView.findViewById(R.id.comment_username);
            viewHolder.comment = (TextView) convertView.findViewById(R.id.comment);
            viewHolder.comment_time = (TextView) convertView.findViewById(R.id.comment_time);
            viewHolder.comment_likes = (TextView) convertView.findViewById(R.id.comment_likes);
            viewHolder.comment_reply = (TextView) convertView.findViewById(R.id.comment_reply);

            viewHolder.comments_profile_image = (CircleImageView) convertView.findViewById(R.id.comments_profile_image);
            viewHolder.like = (ImageView) convertView.findViewById(R.id.comment_like);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // set the comments
        viewHolder.comment.setText(getItem(position).getComment());

        // set the timestamp difference
        String timeStampDifference = getTimeStampDifference(getItem(position));
        if (!timeStampDifference.equals("0")) {
            viewHolder.comment_time.setText(timeStampDifference + "d");
        } else {
            viewHolder.comment_time.setText("today");
        }

        // set the username and profile image
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USER_ACCOUNT)
                .orderByChild(mContext.getString(R.string.firebaseUserId))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    viewHolder.comment_username.setText(
                            singleSnapshot.getValue(UserAccount.class).getUser_name());
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(
                            singleSnapshot.getValue(UserAccount.class).getProfile_photo(),
                            viewHolder.comments_profile_image);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query cancelled.");
            }
        });

        // handle the first comment which is the Image Caption
        if (position == 0) {
            viewHolder.like.setVisibility(View.GONE);
            viewHolder.comment_likes.setVisibility(View.GONE);
            viewHolder.comment_reply.setVisibility(View.GONE);
        }

        return convertView;
    }

    /**
     * Returns a string representing the number of days passed sinced it was posted
     *
     * @return
     */
    private String getTimeStampDifference(Comment comment) {
        String difference = "";

        Calendar calendar = Calendar.getInstance();
        Date photoTimeStamp, todayTimeStamp;

        try {
            String photoDate = comment.getDate_created();
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
