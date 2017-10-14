package com.uzumaki.naruto.instagramclone.utils;

import android.os.Environment;

/**
 * Created by Gaurav Bist on 28-07-2017.
 */

public class FilePath {

    // this will get us path "/storage/emulated/0"
    public final String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public final String CAMERA = ROOT_DIR + "/DCIM/camera";
    public final String PICTURES = ROOT_DIR + "/Pictures";
}
