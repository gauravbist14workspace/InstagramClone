package com.uzumaki.naruto.instagramclone.views.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.utils.Permissions;
import com.uzumaki.naruto.instagramclone.views.activities.AccountSettingActivity;
import com.uzumaki.naruto.instagramclone.views.activities.NextActivity;
import com.uzumaki.naruto.instagramclone.views.activities.ShareActivity;

/**
 * Created by Gaurav Bist on 28-07-2017.
 */

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";
    private Context mContext;

    private static final int PHOTO_FRAGMENT_NUMBER = 1;
    private static final int GALLERY_FRAGMENT_NUMBER = 2;
    private static final int CAMERA_REQUEST_CODE = 100;

    Button btn_launch_camera;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        bindViews(view);
        init();

        return view;
    }

    private void bindViews(View view) {
        btn_launch_camera = (Button) view.findViewById(R.id.btn_launch_camera);
    }

    private void init() {
        mContext = getActivity();

        btn_launch_camera.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();

            if (id == R.id.btn_launch_camera) {
                if (((ShareActivity) mContext).getCurrentTabNumber() == PHOTO_FRAGMENT_NUMBER) {
                    if (((ShareActivity) mContext).checkPermissions(Permissions.CAMERA_PERMISSIONS[0])) {
                        Log.d(TAG, "onClick: starting camera intent");

                        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                , CAMERA_REQUEST_CODE);
                    } else {
                        Log.d(TAG, "onClick: Camera permissons were not granted");

                        startActivity(new Intent(mContext, ShareActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                }
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: Done taking photo via camera");
            Log.d(TAG, "onActivityResult: Starting to navigate to final share screen");

            Uri imageUri = data.getData();
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (isRootTask()) {
                try {
                    Log.d(TAG, "onActivityResult: Recieved new bitmap from camera." + bitmap);
                    startActivity(new Intent(getActivity(), NextActivity.class)
                            .putExtra(getString(R.string.selected_bitmap), bitmap));
                } catch (NullPointerException e) {
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                }
            } else {
                try {
                    Log.d(TAG, "onActivityResult: Recieved new bitmap from camera." + bitmap);
                    startActivity(new Intent(getActivity(), AccountSettingActivity.class)
                            .putExtra(getString(R.string.selected_bitmap), bitmap)
                            .putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment)));
                    ((Activity)mContext).finish();
                } catch (NullPointerException e) {
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                }
            }
        }
    }

    /**
     * This method will tell whether the calling intent is root: Directly shareActivity
     * or whether it is called from the AccountSettingActivity
     *
     * @return
     */
    private boolean isRootTask() {
        if (((ShareActivity) getActivity()).getTask() == 0) {
            return true;
        } else
            return false;
    }
}
