package com.android2.taxidrivershelpeachother.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.controller.MenuLogic;
import com.android2.taxidrivershelpeachother.controller.ShuttleItemAdapter;
import com.android2.taxidrivershelpeachother.model.FireBaseHandler;
import com.android2.taxidrivershelpeachother.model.Passenger;
import com.android2.taxidrivershelpeachother.model.ShuttleItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.android2.taxidrivershelpeachother.view.MenuFragment.timeBetweenClicksIsOk;

public class AvailableShuttleFragment extends Fragment {
    private FragmentManager fragmentManager;
    private RecyclerView shuttlesRecyclerView;
    private ShuttleItemAdapter shuttleItemAdapter;
    private List<ShuttleItem> shuttleItems = new ArrayList<>();
    private String fragmentName;

    public AvailableShuttleFragment(String fragmentName){
        this.fragmentName = fragmentName;
    }

    public String getFragmentName() {
        return fragmentName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getActivity().getSupportFragmentManager();

        FireBaseHandler.getInstance().getAvailableShuttlesFromDB(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.available_shuttles, container, false);

        shuttleItemAdapter = new ShuttleItemAdapter(shuttleItems, fragmentName);
        shuttlesRecyclerView = view.findViewById(R.id.shuttles_recycler);
        shuttlesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        MenuFragment.resetLastClickTime();
        adapterInit();

        return view;
    }

    private void adapterInit() {
        shuttleItemAdapter.setListener(new ShuttleItemAdapter.MyItemListener() {
            @Override
            public void onItemClicked(int position, View view) {
                if (timeBetweenClicksIsOk()) {
                    // TODO take the shuttle
//                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(shuttleItems.get(position).getLink()));
//                    startActivity(browserIntent);
                }
            }
        });

        shuttlesRecyclerView.setAdapter(shuttleItemAdapter);
    }

    public List<ShuttleItem> getShuttleItems() {
        return shuttleItems;
    }

    public void setShuttleItems(List<ShuttleItem> shuttleItems) {
        this.shuttleItems = shuttleItems;
        shuttleItemAdapter = new ShuttleItemAdapter(shuttleItems, fragmentName);
        shuttleItemAdapter.setFragment(this);
        adapterInit();
    }

    public void refresh(){
        adapterInit();
    }
}