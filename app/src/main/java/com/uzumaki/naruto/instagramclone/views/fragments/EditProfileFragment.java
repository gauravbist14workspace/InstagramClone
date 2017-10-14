package com.uzumaki.naruto.instagramclone.views.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.dialogs.ConfirmPasswordDialog;
import com.uzumaki.naruto.instagramclone.interfaces.EditProfileSuccessListener;
import com.uzumaki.naruto.instagramclone.interfaces.OnConfirmPasswordListener;
import com.uzumaki.naruto.instagramclone.models.UserAccount;
import com.uzumaki.naruto.instagramclone.models.UserPrivate;
import com.uzumaki.naruto.instagramclone.models.UserSettings;
import com.uzumaki.naruto.instagramclone.utils.Constants;
import com.uzumaki.naruto.instagramclone.utils.FirebaseHelper;
import com.uzumaki.naruto.instagramclone.utils.UniversalImageLoader;
import com.uzumaki.naruto.instagramclone.views.activities.ShareActivity;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gaurav Bist on 04-07-2017.
 */

public class EditProfileFragment extends Fragment implements OnConfirmPasswordListener, EditProfileSuccessListener {

    private static final String TAG = "EditProfileFragment";
    private Context mContext;

    // widgets
    public EditText displayName, phone, email, userName, website, description;
    private TextView changeProfilePhoto;
    private ProgressBar mProgressBar;
    private CircleImageView profilePhoto;
    private ImageView backArrow, saveChanges;

    // firebase
    private FirebaseHelper mFirebaseHelper;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // variables
    private UserSettings mUserSettings;

    private String DisplayName, UserName, Website, Description, Email;
    private Long Phone;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        bindViews(view);
        init();
        setupFirebaseAuth();

        return view;
    }

    private void bindViews(View view) {
        backArrow = (ImageView) view.findViewById(R.id.backArrow);
        saveChanges = (ImageView) view.findViewById(R.id.saveChanges);

        profilePhoto = (CircleImageView) view.findViewById(R.id.profilePhoto);
        changeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);

        displayName = (EditText) view.findViewById(R.id.displayName);
        userName = (EditText) view.findViewById(R.id.userName);
        website = (EditText) view.findViewById(R.id.webiste);
        description = (EditText) view.findViewById(R.id.description);

        email = (EditText) view.findViewById(R.id.email);
        phone = (EditText) view.findViewById(R.id.phone);
    }

    private void init() {
        mContext = getActivity();
        mFirebaseHelper = new FirebaseHelper(mContext);

        mProgressBar.setVisibility(View.GONE);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating back to ProfileActivity");
                ((Activity) mContext).finish();
            }
        });

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Saving the changes into firebase database");

                mProgressBar.setVisibility(View.VISIBLE);
                saveProfileSettings();
            }
        });

        changeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, ShareActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));               // adding 268435456

                ((Activity) mContext).finish();
            }
        });
    }

    /**
     * Retrieves the data contained in the widgets and submits it to the database
     * Before doing so it checks to make sure the username chosen is unique
     */
    private void saveProfileSettings() {
        DisplayName = displayName.getText().toString();
        UserName = userName.getText().toString();
        Website = website.getText().toString();
        Description = description.getText().toString();
        Email = email.getText().toString();
        Phone = Long.parseLong(phone.getText().toString());

        // case1: User changed his username
        if (!mUserSettings.getUser().getUser_name().equals(UserName)) {
            checkIfUserNameExist(UserName);
        }

        // case2: User changed his email address
        if (!mUserSettings.getUser().getEmailId().equals(Email)) {
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this, 1);
        }

        ///////////////////////// Changing rest of the settings
        // case3: DisplayName
        if (!mUserSettings.getSettings().getDisplay_name().equals(DisplayName)) {
            mFirebaseHelper.updateUserAccountSettings(DisplayName, null, null, 0);
        }

        // case4: Website Content
        if (!mUserSettings.getSettings().getWebsite().equals(Website)) {
            mFirebaseHelper.updateUserAccountSettings(null, Website, null, 0);
        }

        // case5: Description
        if (!mUserSettings.getSettings().getDescription().equals(Description)) {
            mFirebaseHelper.updateUserAccountSettings(null, null, Description, 0);
        }

        // case6: Phone Number
        if (mUserSettings.getUser().getPhone_number() != Phone) {
            mFirebaseHelper.updateUserAccountSettings(null, null, null, Phone);
        }
    }

    /*
    The only drawback of this query is I cannot return boolean coz of default override methods-_-
     */
    private void checkIfUserNameExist(final String username) {
        Log.d(TAG, "checkIfUserNameExist: Checking if username " + username + " already exists.");

        Query query = mFirebaseDatabase.getReference().child(Constants.USER_PRIVATE_INFO)
                .orderByChild(getString(R.string.firebaseUsername))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    mFirebaseHelper.updateUsername(username);
                    Toast.makeText(getActivity(), "Username saved !!", Toast.LENGTH_SHORT).show();
                } else {
                    for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                        if (singleSnap.exists()) {
                            Log.d(TAG, "checkIfUserNameExist: FOUND A MATCH " + singleSnap.getValue(UserPrivate.class).getUser_name());
                            Toast.makeText(getActivity(), "Username already exists !\nTry adding some suffix !", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.toString());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.getUser().getEmailId());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.getUser().getPhone_number());

        mUserSettings = userSettings;

        UserAccount settings = userSettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(), profilePhoto, null, "");

        displayName.setText(settings.getDisplay_name());
        userName.setText(settings.getUser_name());
        website.setText(settings.getWebsite());
        description.setText(settings.getDescription());
        email.setText(userSettings.getUser().getEmailId());
        phone.setText(String.valueOf(userSettings.getUser().getPhone_number()));
    }

    private void setProfileImage() {
        Log.d(TAG, "setProfileImage:  setting profile image");
        String imageUrl = "https://crackberry.com/sites/crackberry.com/files/styles/large/public/topic_images/2013/ANDROID.png?itok=xhm7jaxS";
        UniversalImageLoader.setImage(imageUrl, profilePhoto, null, "");
    }

    /*
    *------------------------------------ Firebase ---------------------------------------------
    */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mFirebaseAuth.getCurrentUser().getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //retrieve user information from the database
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
                        .getUser_name()
                );
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

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onConfirm(String password) {
        Log.d(TAG, "onConfirm: Got the password from dialog: " + password);
        mFirebaseHelper.reAuthenticateUser(email.getText().toString(), password);
    }

    @Override
    public void onProfileModified(String message) {
        mProgressBar.setVisibility(View.GONE);

        if (!message.equals(""))
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}