package com.android2.taxidrivershelpeachother.controller;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.android2.taxidrivershelpeachother.view.AvailableShuttleFragment;
import com.android2.taxidrivershelpeachother.view.IRefreshableFragment;
import com.android2.taxidrivershelpeachother.view.NewShuttleFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPager2Adapter extends FragmentStateAdapter {
    private List<IRefreshableFragment> fragments = new ArrayList<>();

    public ViewPager2Adapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        init();
    }

    public ViewPager2Adapter(@NonNull Fragment fragment) {
        super(fragment);
        init();
    }

    public ViewPager2Adapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        init();
    }

    private void init() {
        fragments.add(new AvailableShuttleFragment("availableShuttles"));
        fragments.add(new AvailableShuttleFragment("commitmentShuttles"));
        fragments.add(new AvailableShuttleFragment("leadManagement"));
        fragments.add(new NewShuttleFragment());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        return (Fragment) fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    public IRefreshableFragment getFragmentInPosition(int position){
        return fragments.get(position);
    }
}
