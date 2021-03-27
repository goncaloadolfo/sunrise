package com.example.sunrise.BeachListActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sunrise.R;
import com.google.android.material.tabs.TabLayout;

public class BeachListFragment extends Fragment {

    private BeachPageAdapter beachPage;
    private ViewPager viewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // avoid recreation when orientation changes
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.beach_list_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        beachPage = new BeachPageAdapter(view.getContext(), getChildFragmentManager());
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(beachPage);

        // set tab layout to viewpage
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

}
