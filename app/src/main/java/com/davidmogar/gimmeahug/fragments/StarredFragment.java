package com.davidmogar.gimmeahug.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davidmogar.gimmeahug.R;
import com.davidmogar.gimmeahug.activities.MainActivity;
import com.davidmogar.gimmeahug.adapters.UsersAdapter;
import com.davidmogar.gimmeahug.helpers.DividerItemDecoration;
import com.davidmogar.gimmeahug.models.User;

import java.util.ArrayList;
import java.util.List;


public class StarredFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private OnProfileRequestListener callback;

    public static StarredFragment newInstance() {
        StarredFragment fragment = new StarredFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new UsersAdapter(new ArrayList<User>());
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL_LIST));

        callback.onRequestUsers();
    }

    public void updateUsersList(List<User> users) {
        adapter = new UsersAdapter(users);
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = adapter.getUsers().get(recyclerView.getChildAdapterPosition(view));
                callback.onProfileRequest(user);
            }
        });

        if (recyclerView != null) {
            recyclerView.swapAdapter(adapter, false);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            callback = (OnProfileRequestListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    public interface OnProfileRequestListener {
        public void onProfileRequest(User user);
        public void onRequestUsers();
    }

}
