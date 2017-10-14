package com.uzumaki.naruto.instagramclone.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Gaurav Bist on 04-07-2017.
 */

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final HashMap<Fragment, Integer> mFragment = new HashMap<>();
    private final HashMap<String, Integer> mFragmentNumbers = new HashMap<>();
    private final HashMap<Integer, String> mFragmentNames = new HashMap<>();

    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public  void addFragment (Fragment fragment, String fragmentName) {
        mFragmentList.add(fragment);
        mFragment.put(fragment, mFragmentList.size() - 1);
        mFragmentNumbers.put(fragmentName, mFragmentList.size() - 1);
        mFragmentNames.put(mFragmentList.size() - 1, fragmentName);
    }

    /*
    * get fragment number from fragment name
     */
    public Integer getFragmentNumber(String fragmentName) {
        if(mFragmentNumbers.containsKey(fragmentName))
            return mFragmentNumbers.get(fragmentName);
        else
            return null;
    }

    /*
    * get fragment name from fragment number
     */
    public Integer getFragmentNumber(Fragment fragment) {
        if(mFragmentNumbers.containsKey(fragment))
            return mFragmentNumbers.get(fragment);
        else
            return null;
    }

    /*
    * get fragment name from fragment number
     */
    public String getFragmentName(Integer fragmentNumber) {
        if(mFragmentNames.containsKey(fragmentNumber))
            return mFragmentNames.get(fragmentNumber);
        else
            return null;
    }
}
