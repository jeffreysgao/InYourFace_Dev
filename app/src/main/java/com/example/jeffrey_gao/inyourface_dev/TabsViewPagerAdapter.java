package com.example.jeffrey_gao.inyourface_dev;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by jinnan on 2/25/17.
 */


public class TabsViewPagerAdapter extends FragmentPagerAdapter
{
    private ArrayList<Fragment> fragmentsArray;

    public static final int SETTING_INDEX = 0;
    public static final int AUTHEN_INDEX = 1;
    public static final int EMOTIONS_INDEX = 2;
    public static final String SETTING_TITLE = "SETTINGS";
    public static final String AUTHEN_TITLE = "[PLACEHOLDER]";
    public static final String EMOTIONS_TITLE = "[PLACEHOLDER]";

    public TabsViewPagerAdapter(FragmentManager fragManager, ArrayList<Fragment> fragments)
    {
        super(fragManager);
        this.fragmentsArray = fragments;
    }

    public Fragment getItem(int pos){
        return fragmentsArray.get(pos);
    }

    public int getCount(){
        return fragmentsArray.size();
    }

    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case SETTING_INDEX:
                return SETTING_TITLE;
            case AUTHEN_INDEX:
                return AUTHEN_TITLE;
            case EMOTIONS_INDEX:
                return EMOTIONS_TITLE;
            default:
                break;
        }
        return null;
    }
}
