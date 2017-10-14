package com.uzumaki.naruto.instagramclone.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.interfaces.EditProfileSuccessListener;
import com.uzumaki.naruto.instagramclone.interfaces.LoginSuccessListener;
import com.uzumaki.naruto.instagramclone.interfaces.PhotoUploadSuccessListener;
import com.uzumaki.naruto.instagramclone.interfaces.RegisterListener;
import com.uzumaki.naruto.instagramclone.models.Photo;
import com.uzumaki.naruto.instagramclone.models.UserAccount;
import com.uzumaki.naruto.instagramclone.models.UserPrivate;
import com.uzumaki.naruto.instagramclone.views.activities.AccountSettingActivity;
import com.uzumaki.naruto.instagramclone.views.activities.HomeActivity;
import com.uzumaki.naruto.instagramclone.views.activities.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by Gaurav Bist on 11-08-2017.
 */

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private Context mContext;

    // variables
    private String userEmail, userId, alreadyExistingName;
    private double mPhotoUploadProgress = 0;

    // firebase
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    // interfaces
    private EditProfileSuccessListener editProfileSuccessListener;
    private LoginSuccessListener loginSuccessListener;
    private RegisterListener registerListener;
    private PhotoUploadSuccessListener photoUploadSuccessListener;

    public FirebaseHelper(Context mContext) {
        this.mContext = mContext;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        if (mFirebaseAuth.getCurrentUser() != null) {
            userId = mFirebaseAuth.getCurrentUser().getUid();
        }

        //////////////////// LOGIN /////////////////////////////////////
        try {
            loginSuccessListener = (LoginSuccessListener) mContext;
        } catch (ClassCastException e) {
            e.getMessage();
        }

        //////////////////// REGISTER //////////////////////////////////
        try {
            registerListener = (RegisterListener) mContext;
        } catch (ClassCastException e) {
            e.getMessage();
        }

        //////////////////// UPDATE PROFILE ///////////////////////////

        if (mContext instanceof AccountSettingActivity) {
            Fragment fragment = ((AccountSettingActivity) mContext).getCurrentFragment();
            try {

                editProfileSuccessListener = (EditProfileSuccessListener) fragment;
            } catch (ClassCastException e) {
                e.getMessage();
            }
        }

        ///////////////////// UPLOAD PHOTO ////////////////////////////
        try {
            photoUploadSuccessListener = (PhotoUploadSuccessListener) mContext;
        } catch (ClassCastException e) {
            e.getMessage();
        }
    }

    //////////////////////////////////// LOGIN /////////////////////////////////////////////////////
    public void performFirebaseLogin(String email, String password) {

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());

                            Toast.makeText(mContext, mContext.getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            try {
                                if (user.isEmailVerified()) {
                                    Log.w(TAG, "signInWithEmail:Success. Email is verified.");

                                    Toast.makeText(mContext, mContext.getString(R.string.login_successful), Toast.LENGTH_SHORT).show();

                                    mContext.startActivity(new Intent(mContext, HomeActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                } else {
                                    Toast.makeText(mContext, mContext.getString(R.string.email_not_verified), Toast.LENGTH_SHORT).show();
                                    mFirebaseAuth.signOut();
                                }
                            } catch (NullPointerException e) {
                                Log.d(TAG, "onComplete: NullPointerException " + e.getMessage());
                            }
                        }
                        if (loginSuccessListener != null)
                            loginSuccessListener.onLoginSuccessful();
                    }
                });
    }


    /////////////////////////////////// REGISTRATION ///////////////////////////////////////////////
    public void performFirebaseRegister(String email, String password) {

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Failed to register user data.\n" + task.getException().getMessage());

                            if (task.getException().getMessage().contains("The email address is already in use"))
                                Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            // if fails to register, stop progressbar
                            if (registerListener != null)
                                registerListener.onRegisterCompleteListener();

                        } else {
                            userId = mFirebaseAuth.getCurrentUser().getUid();
                            userEmail = mFirebaseAuth.getCurrentUser().getEmail();

                            sendVerificationEmail();
                        }
                    }
                });
    }

    // This will send a verification mail to there gmail account
    public void sendVerificationEmail() {

        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            mFirebaseUser.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Verification email sent.");
                                Toast.makeText(mContext, mContext.getString(R.string.sending_verification), Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "onFailed: Could not send verification mail " + task.getException().getMessage());
                                Toast.makeText(mContext, mContext.getString(R.string.verification_mail_not_sent), Toast.LENGTH_SHORT).show();
                            }
                            if (registerListener != null)
                                registerListener.onRegisterCompleteListener();
                        }
                    });
        } else {
            if (registerListener != null)
                registerListener.onRegisterCompleteListener();

            Toast.makeText(mContext, "FirebaseUser cannot be initialised." +
                    mContext.getString(R.string.verification_mail_not_sent), Toast.LENGTH_SHORT).show();
        }
    }

    // to check if the username is not already taken
    public void checkIfUsernameExists(final String userName) {
        Log.d(TAG, "checkIfUserNameExist: Checking if username '" + userName + "' already exists.");

        Query query = mDatabaseReference.child(Constants.USER_PRIVATE_INFO)
                .orderByChild(mContext.getString(R.string.firebaseUsername))
                .equalTo(userName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                    // if a single record is found, therefore entry with same username already exists.
                    if (singleSnap.exists()) {
                        alreadyExistingName = userName;
                        Log.d(TAG, "checkIfUserNameExist: FOUND A MATCH " + singleSnap.getValue(UserPrivate.class).getUser_name());
                        Toast.makeText(mContext, mContext.getString(R.string.username_already_exist), Toast.LENGTH_SHORT).show();

                        String username = StringManipulation.condenseUsername(userName);
                        username += new Random().nextInt(10) + 1;

                        Toast.makeText(mContext, mContext.getString(R.string.trying_username) + username,
                                Toast.LENGTH_SHORT).show();

                        // though things below it would be sent onto stack so repeating of things may occur
                        checkIfUsernameExists(username);
                    }
                }

                if (!userName.equals(alreadyExistingName)) {
                    // add user to database now....
                    addNewUser(userEmail, userName, "", "", "");
                    Toast.makeText(mContext, mContext.getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                    mFirebaseAuth.signOut();
                } else {
                    // do nothing when we come here as stack and username was same
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Add the user record into the firebase database in "user_account" and "user_private" node
    private void addNewUser(String email, String username, String description, String website, String profile_photo) {
        Log.d(TAG, "addNewUser: Logging in new user record with Uid : " + userId);

        // adding values to the user_private node....
        UserPrivate user = new UserPrivate(
                email,
                1,
                userId,
                StringManipulation.condenseUsername(username));

        mDatabaseReference.child(Constants.USER_PRIVATE_INFO)
                .child(userId)
                .setValue(user);

        // adding values to user_account node....
        UserAccount settings = new UserAccount(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                username,
                userId,
                website
        );

        mDatabaseReference.child(Constants.USER_ACCOUNT)
                .child(userId)
                .setValue(settings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {

                        if (task.isSuccessful()) {
                            mContext.startActivity(new Intent(mContext, LoginActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        } else {
                            Log.d(TAG, "onComplete: Adding to node 'user_accout' failed. Removing user record.");
                            mFirebaseAuth.getCurrentUser().delete();
                        }
                    }
                });
    }


    ///////////////////////////////// PHOTO UPLOADING //////////////////////////////////////////////

    /**
     * This method will insert either of the profile photo or new photo into the database
     *
     * @param image_type
     * @param caption
     * @param count
     * @param imgUrl
     */
    public void uploadPhoto(String image_type, final String caption, int count, final String imgUrl, Bitmap bitmap) {
        Log.d(TAG, "uploadPhoto: Attempting to upload: " + image_type);

        UploadTask uploadTask = null;

        if (image_type.equals(mContext.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadPhoto: Uploading new photo.");

            // get bitmap from the image url
            if (bitmap == null)
                bitmap = ImageLoader.getBitmap(imgUrl);

            // get bytes from bitmap, but half the quality
            byte[] bytes = ImageLoader.getBytesFromBitmap(bitmap, 50);

            uploadTask = mStorageReference.child(Constants.PHOTOS)
                    .child(mFirebaseAuth.getCurrentUser().getUid())
                    .child("photo" + (count + 1))
                    .putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    // adding url to "photo_node" and "user_photo" node
                    addPhotoToDatabase(caption, downloadUrl.toString());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failure: "  + e.getMessage());
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "Photo upload progress " + String.format("%.0f", progress) + "%"
                                , Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: uploading photo progress " + progress + "% done.");
                }
            });
        } else if (image_type.equals(mContext.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadPhoto: Uploading the Profile Picture.");
            // navigate to profileActivity so the user can see their photo
            ((AccountSettingActivity) mContext).setViewPager(
                    ((AccountSettingActivity) mContext).adapter.getFragmentNumber(
                            mContext.getString(R.string.edit_profile_fragment)));

            // get bitmap from the image url
            if (bitmap == null)
                bitmap = ImageLoader.getBitmap(imgUrl);

            // get bytes from bitmap, but half the quality
            byte[] bytes = ImageLoader.getBytesFromBitmap(bitmap, 50);

            uploadTask = mStorageReference.child(Constants.PHOTOS)
                    .child(mFirebaseAuth.getCurrentUser().getUid())
                    .child("profile_photo")
                    .putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onSuccess: Photo upload succsesfull.");
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    // adding image download_path to "user_account" node
                    addProfilePhoto(downloadUrl.toString());

                    ((AccountSettingActivity) mContext).setViewPager(
                            ((AccountSettingActivity) mContext).adapter.getFragmentNumber(
                                    mContext.getString(R.string.edit_profile_fragment)));

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failure.");

                    if (photoUploadSuccessListener != null)
                        photoUploadSuccessListener.onUploadFailed();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "Photo upload progress " + String.format("%.0f", progress) + "%"
                                , Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: uploading photo progress " + progress + "% done.");
                }
            });
        }
    }

    // Add the download path of the image uploaded into the database
    private void addPhotoToDatabase(String caption, String downloadUrl) {
        Log.d(TAG, "addPhotoToDatabase: Adding photo to database.");

        String newPhotoKey = mDatabaseReference.child(Constants.PHOTOS).push().getKey();

        Photo photo = new Photo(caption,
                // TODO if prob occurs
//                new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(new Date()),
                getTimestamp(),
                downloadUrl,
                newPhotoKey,
                mFirebaseAuth.getCurrentUser().getUid(),
                StringManipulation.getTagsFromCaption(caption),
                null,
                null);

        mDatabaseReference.child(Constants.PHOTOS)
                .child(newPhotoKey)
                .setValue(photo);

        mDatabaseReference.child(Constants.USER_PHOTOS)
                .child(userId)
                .child(newPhotoKey)
                .setValue(photo);

        if (photoUploadSuccessListener != null)
            photoUploadSuccessListener.onUploadSuccessful();
    }

    public String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(new Date());
    }

    // Add the download path of profile picture into the database
    private void addProfilePhoto(String downloadUrl) {
        Log.d(TAG, "addProfilePhoto: Setting the profile photo.");

        mDatabaseReference.child(Constants.USER_ACCOUNT)
                .child(userId)
                .child(mContext.getString(R.string.firebaseProfilePhoto))
                .setValue(downloadUrl);

        if (photoUploadSuccessListener != null)
            photoUploadSuccessListener.onUploadSuccessful();
    }


    ///////////////////////////////// UPDATING PROFILE /////////////////////////////////////////////

    // Update username into firebase database
    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: updating username to " + username);

        mDatabaseReference.child(Constants.USER_PRIVATE_INFO)
                .child(userId)
                .child(mContext.getString(R.string.firebaseUsername))
                .setValue(username);

        mDatabaseReference.child(Constants.USER_ACCOUNT)
                .child(userId)
                .child(mContext.getString(R.string.firebaseUsername))
                .setValue(username);

        if (editProfileSuccessListener != null)
            editProfileSuccessListener.onProfileModified(mContext.getString(R.string.username_updated));
    }

    // Update user details into firebase database
    public void updateUserAccountSettings(String displayName, String website, String description, long phone) {
        String item = "";

        if (displayName != null) {
            mDatabaseReference.child(Constants.USER_ACCOUNT)
                    .child(userId)
                    .child(mContext.getString(R.string.firebaseDisplayName))
                    .setValue(displayName);
            item = "Display Name,";
        }

        if (website != null) {
            mDatabaseReference.child(Constants.USER_ACCOUNT)
                    .child(userId)
                    .child(mContext.getString(R.string.firebaseWebsite))
                    .setValue(website);
            item = "Website";
        }

        if (description != null) {
            mDatabaseReference.child(Constants.USER_ACCOUNT)
                    .child(userId)
                    .child(mContext.getString(R.string.firebaseDescription))
                    .setValue(description);
            item = "Description";
        }

        if (phone != 0) {
            mDatabaseReference.child(Constants.USER_PRIVATE_INFO)
                    .child(userId)
                    .child(mContext.getString(R.string.firebasePhoneNumber))
                    .setValue(phone);
            item = "Phone Number";
        }

        if (editProfileSuccessListener != null)
            editProfileSuccessListener.onProfileModified(item + " has been modified.");
    }

    // Update the user email address both normally and in firebase database
    public void reAuthenticateUser(final String newEmail, String password) {
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider.getCredential(
                mFirebaseAuth.getCurrentUser().getEmail(), password);

        //////////////////// Prompt the user to re-provide their sign-in credentials
        mFirebaseAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "reAuthenticateUser_User: re-authenticated.");

                            //////////////////// Check to see if email is not already present in the database
                            mFirebaseAuth.fetchProvidersForEmail(newEmail)
                                    .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                            try {
                                                if (task.isSuccessful()) {
                                                    if (task.getResult().getProviders().size() == 1) {
                                                        Log.d(TAG, "reAuthenticateUser_fetchProvidersForEmail_onComplete: That email is already taken.");

                                                        if (editProfileSuccessListener != null)
                                                            editProfileSuccessListener.onProfileModified(mContext.getString(R.string.email_already_taken));
                                                    } else if (task.getResult().getProviders().size() == 0) {
                                                        Log.d(TAG, "reAuthenticateUser_fetchProvidersForEmail_onComplete: That email is available.");

                                                        // The email is available so update it
                                                        updateEmail(newEmail);
                                                    }
                                                }
                                            } catch (NullPointerException e) {
                                                Log.d(TAG, "reAuthenticateUser_fetchProvidersForEmail_onComplete: NullPointerException " + e.getMessage());

                                                if (editProfileSuccessListener != null)
                                                    editProfileSuccessListener.onProfileModified(e.getMessage());
                                            }
                                        }
                                    });
                        } else {
                            Log.d(TAG, "reAuthenticateUser_User: re-authentication failed.");

                            if (editProfileSuccessListener != null)
                                editProfileSuccessListener.onProfileModified(mContext.getString(R.string.password_not_match));
                        }

                    }
                });
    }

    public void updateEmail(final String email) {
        mFirebaseAuth.getCurrentUser().updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "reAuthenticateUser_User_UpdateEmail: User email address updated.");

                            // Save the changes into firebase database
                            mDatabaseReference.child(Constants.USER_PRIVATE_INFO)
                                    .child(userId)
                                    .child(mContext.getString(R.string.firebaseEmailId))
                                    .setValue(email);

                            if (editProfileSuccessListener != null)
                                editProfileSuccessListener.onProfileModified(mContext.getString(R.string.email_updated));
                        } else {
                            if (editProfileSuccessListener != null)
                                editProfileSuccessListener.onProfileModified(task.getException().getMessage());
                        }
                    }
                });
    }
}
