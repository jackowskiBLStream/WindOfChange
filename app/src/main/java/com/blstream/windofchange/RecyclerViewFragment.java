package com.blstream.windofchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class RecyclerViewFragment extends Fragment implements IRecyclerViewPositionHelper {
    private static final String LIST_STATE_KEY = "listState";
    private static final String ADAPTER_STATE = "adapterState";
    private static final String TEMP_LIST_STATE = "tempList";
    private static final String TITLE = "title";
    private static final String PUB_DATE = "pubDate";

    static String titleReceived = "";
    static String pubDateReceived = "";
    static ArrayList<RSSInfo> list = new ArrayList<>();
    static RSSAdapter rssAdapter = new RSSAdapter(list);
    private RecyclerView recList;
    private BroadcastReceiver mReceiver;
    private ArrayList<RSSInfo> receivedList = new ArrayList<>();
    private OnReceiverRefresh onReceiverRefresh;
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void setOnReceiverRefresh(OnReceiverRefresh onReceiverRefresh) {
        this.onReceiverRefresh = onReceiverRefresh;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycler_view_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recList = (RecyclerView) view.findViewById(R.id.recyclerViewList);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(linearLayoutManager);
        setHasOptionsMenu(true);
        titleReceived = getString(R.string.app_name);
        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(LIST_STATE_KEY);
            recList.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
            rssAdapter = savedInstanceState.getParcelable(ADAPTER_STATE);
            list = savedInstanceState.getParcelableArrayList(TEMP_LIST_STATE);
            pubDateReceived = savedInstanceState.getString(PUB_DATE);
            titleReceived = savedInstanceState.getString(TITLE);
            MainActivity.pubDateInsideActionBar.setText(pubDateReceived);
        }
        MainActivity.titleInsideActionBar.setText(titleReceived);
        recList.setAdapter(rssAdapter);
        rssAdapter.setListener(RecyclerViewFragment.this);
        setOnReceiverRefresh(new OnReceiverRefresh() {
            @Override
            public void onRefreshListener(ArrayList<RSSInfo> list) {
                RecyclerViewFragment.list.addAll(list);
                rssAdapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIST_STATE_KEY, recList.getLayoutManager()
                .onSaveInstanceState());
        outState.putParcelable(ADAPTER_STATE, rssAdapter);
        outState.putParcelableArrayList(TEMP_LIST_STATE, list);
        outState.putString(PUB_DATE, pubDateReceived);
        outState.putString(TITLE, titleReceived);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receivedList = intent
                        .getParcelableArrayListExtra(Constants.EXTENDED_DATA_STATUS);
                onReceiverRefresh.onRefreshListener(receivedList);
                titleReceived = intent.getStringExtra(Constants.CHANNEL_TITLE);
                pubDateReceived = intent.getStringExtra(Constants.CHANNEL_PUB_DATE);
                MainActivity.titleInsideActionBar.setText(titleReceived);
                MainActivity.pubDateInsideActionBar.setText(pubDateReceived);
            }
        };
        //registering our receiver
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onListPositionListener(int position) {
        if (isNetworkAvailable(getActivity())) {
            String url = list.get(position).getLink();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }
}
