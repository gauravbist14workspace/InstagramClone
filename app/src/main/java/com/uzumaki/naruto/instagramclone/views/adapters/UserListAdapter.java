package com.uzumaki.naruto.instagramclone.views.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.models.UserAccount;
import com.uzumaki.naruto.instagramclone.models.UserPrivate;
import com.uzumaki.naruto.instagramclone.utils.Constants;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gaurav Bist on 22-09-2017.
 */

public class UserListAdapter extends ArrayAdapter<UserPrivate> {
    private static final String TAG = "UserListAdapter";
    private Context mContext;

    // vars
    private LayoutInflater inflater;
    private int layoutResource;
    private List<UserPrivate> mUserList;

    public UserListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<UserPrivate> objects) {
        super(context, resource, objects);
        mContext = context;

        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mUserList = objects;
    }

    private class Holder {
        TextView username, email;
        CircleImageView profileImage;
    }

    @Nullable
    @Override
    public UserPrivate getItem(int position) {
        return mUserList.get(position);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Holder holder;

        if (convertView == null) {
            holder = new Holder();

            convertView = inflater.inflate(layoutResource, parent, false);

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.email = (TextView) convertView.findViewById(R.id.email);

            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);

            convertView.setTag(holder);
        } else
            holder = (Holder) convertView.getTag();

        holder.username.setText(getItem(position).getUser_name());
        holder.email.setText(getItem(position).getEmailId());

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        Query query = myRef.child(Constants.USER_ACCOUNT)
                .orderByChild(mContext.getString(R.string.firebaseUserId))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: Found user" + singleSnapshot.getValue(UserAccount.class).toString());

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccount.class).getProfile_photo(),
                            holder.profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return convertView;
    }
}
