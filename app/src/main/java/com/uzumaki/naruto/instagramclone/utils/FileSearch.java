package com.uzumaki.naruto.instagramclone.utils;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Gaurav Bist on 28-07-2017.
 */

public class FileSearch {
    private static final String TAG = "FileSearch";

    /**
     * Search a directory and return a list of **directories** contained inside it.
     * @param directory "/storage/emulated/0/Pictures"
     * @return pathArray "An arraylist of strings containing list of all the image directories inside
     * the "/storage/emulated/0/Pictures" path
     */
    public static ArrayList<String> getDirectoryPaths(String directory) {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for(int i = 0; i < listFiles.length; i ++) {
            if(listFiles[i].isDirectory()) {
                pathArray.add(listFiles[i].getAbsolutePath());
            }
        }

        return pathArray;
    }

    /**
     * Search a directory and return a list of **image file paths** contained inside it.
     * @param directory "/storage/emulated/0/Pictures/FolderName"
     * @return {"/storage/emulated/0/Pictures/FolderName/image.jpg"}
     */
    public static ArrayList<String> getFilePaths(String directory) {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for(int i = 0; i < listFiles.length; i ++) {
            if(listFiles[i].isFile()) {
                pathArray.add(listFiles[i].getAbsolutePath());
            }
        }

        return pathArray;
    }
}
