package com.uzumaki.naruto.instagramclone.views.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.utils.FilePath;
import com.uzumaki.naruto.instagramclone.utils.FileSearch;
import com.uzumaki.naruto.instagramclone.views.activities.AccountSettingActivity;
import com.uzumaki.naruto.instagramclone.views.activities.NextActivity;
import com.uzumaki.naruto.instagramclone.views.activities.ShareActivity;
import com.uzumaki.naruto.instagramclone.views.adapters.GridImageAdapter;

import java.util.ArrayList;

/**
 * Created by Gaurav Bist on 28-07-2017.
 */

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private Context mContext;

    // constants
    private static final int NUM_GRID_COLOUMNS = 3;
    public static final String mAppend = "file:/";

    // widgets
    private GridView gridView;
    private ImageView gallery_image_view, iv_close_share;
    private Spinner spinner_directory;
    private ProgressBar progress;
    private TextView tv_next;

    // variables
    private ArrayList<String> directories;
    private String mSelectedImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        bindViews(view);
        init();

        return view;
    }

    private void bindViews(View view) {
        iv_close_share = (ImageView) view.findViewById(R.id.iv_close_share);
        spinner_directory = (Spinner) view.findViewById(R.id.spinner_directory);
        tv_next = (TextView) view.findViewById(R.id.tv_next);

        gallery_image_view = (ImageView) view.findViewById(R.id.gallery_image_view);
        gridView = (GridView) view.findViewById(R.id.gridView);

        progress = (ProgressBar) view.findViewById(R.id.progress);
    }

    private void init() {
        mContext = getActivity();

        directories = new ArrayList<>();
        FilePath filePaths = new FilePath();

        ////////////////// Checking for folders inside "/storage/emulated/0/pictures"
        if (FileSearch.getDirectoryPaths(filePaths.PICTURES) != null) {
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);

        }
        ArrayList<String> directoryNames = new ArrayList<>();
        for (String name : directories) {
            int index = name.lastIndexOf("/") + 1;
            directoryNames.add(name.substring(index));
        }

        directories.add(filePaths.CAMERA);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_dropdown_item, directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_directory.setAdapter(adapter);

        spinner_directory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

                ///////// setup the gridview with images from selected path
                setupGridView(directories.get(pos));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        iv_close_share.setOnClickListener(onClickListener);
        tv_next.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.iv_close_share) {
                Log.d(TAG, "onClick: Closing the gallery fragment.");
                getActivity().finish();
            } else if (id == R.id.tv_next) {
                Log.d(TAG, "onClick: Navigating to final share screen.");

                if (isRootTask()) {
                    startActivity(new Intent(mContext, NextActivity.class)
                            .putExtra(getString(R.string.selected_img), mSelectedImage));
                } else {
                    startActivity(new Intent(getActivity(), AccountSettingActivity.class)
                            .putExtra(getString(R.string.selected_img), mSelectedImage)
                            .putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment)));

                    ((Activity) mContext).finish();
                }
            }
        }
    };

    /**
     * This method will tell whether the calling intent is root: Directly shareActivity
     * or whether it is called from the AccountSettingActivity
     * @return
     */
    private boolean isRootTask() {
        if (((ShareActivity) getActivity()).getTask() == 0) {
            return true;
        } else
            return false;
    }

    private void setupGridView(String selectedDirectory) {
        final ArrayList<String> imgUrls = FileSearch.getFilePaths(selectedDirectory);

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLOUMNS;
        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview, mAppend, imgUrls);
        gridView.setAdapter(adapter);

        // This will initialise the first image of the first directory choosen
        try {
            setImage(imgUrls.get(0), gallery_image_view, mAppend);
            mSelectedImage = imgUrls.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            progress.setVisibility(View.GONE);
        } catch (IndexOutOfBoundsException e){
            progress.setVisibility(View.GONE);
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                setImage(imgUrls.get(pos), gallery_image_view, mAppend);
                mSelectedImage = imgUrls.get(pos);
            }
        });
    }

    private void setImage(String imgUrl, ImageView imageView, String append) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgUrl, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progress.setVisibility(View.GONE);
            }
        });
    }
}
