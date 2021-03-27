package com.example.sunrise.BeachListActivity;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.sunrise.R;


public class BeachPageAdapter extends FragmentStatePagerAdapter {

    public static final String[] queryZones = new String[]{"south", "center", "north"};
    private String[] zones;

    public BeachPageAdapter(Context context, FragmentManager fragmentManager){
        super(fragmentManager);
        zones = context.getResources().getStringArray(R.array.geoZones);
    }

    @Override
    public Fragment getItem(int i) {
        return new ZoneFragment(queryZones[i]);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return zones[position];
    }

}
