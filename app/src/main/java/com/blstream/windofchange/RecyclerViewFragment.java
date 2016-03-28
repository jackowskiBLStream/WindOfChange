package com.blstream.windofchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.ArrayList;

public class RecyclerViewFragment extends Fragment implements IRecyclerViewPositionHelper {
    private static final String LIST_STATE_KEY = "listState";
    private static final String ADAPTER_STATE = "adapterState";
    private static final String TEMP_LIST_STATE = "tempList";
    private static final String TITLE = "title";
    private static final String PUB_DATE = "pubDate";

    private String titleReceived = "";
    private String pubDateReceived = "";
    private RecyclerView recList;
    private BroadcastReceiver mReceiver;
    private ArrayList<RSSInfo> list = new ArrayList<>();
    private ArrayList<RSSInfo> receivedList = new ArrayList<>();
    private RSSInfo noInternetAccess = new RSSInfo();
    private OnReceiverRefresh onReceiverRefresh;
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
    private Menu myMenu;
    private RSSAdapter rssAdapter = new RSSAdapter(list);
    private IManageFragments changeFragmentListener;


    public void setChangeFragmentListener(IManageFragments changeFragmentListener) {
        this.changeFragmentListener = changeFragmentListener;
    }

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
        // titleInsideActionBar = (TextView)view.findViewById(R.id.titleInsideActionBar);
        //  pubDateInsideActionBar = (TextView) view.findViewById(R.id.pubDateInsideActionBar);
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

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIST_STATE_KEY, recList.getLayoutManager().onSaveInstanceState());
        outState.putParcelable(ADAPTER_STATE, rssAdapter);
        outState.putParcelableArrayList(TEMP_LIST_STATE, list);
        outState.putString(PUB_DATE, pubDateReceived);
        outState.putString(TITLE, titleReceived);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                list.clear();
                if (isNetworkAvailable(getActivity())) {
                    changeFragmentListener.onChangeFragmentListener(isNetworkAvailable(getActivity()));
                    // Do animation start
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                            Context.LAYOUT_INFLATER_SERVICE);
                    ImageView iv = (ImageView) inflater.inflate(R.layout.iv_refresh, null);
                    Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim);
                    rotation.setRepeatCount(Animation.INFINITE);
                    iv.startAnimation(rotation);
                    item.setActionView(iv);
                    new UpdateTask(getActivity()).execute();
                    Intent mServiceIntent = new Intent(getActivity(), RSSPullService.class);
                    getActivity().startService(mServiceIntent);
                    setOnReceiverRefresh(new OnReceiverRefresh() {
                        @Override
                        public void onRefreshListener(ArrayList<RSSInfo> list) {
                            RecyclerViewFragment.this.list.addAll(list);
                            rssAdapter.notifyDataSetChanged();
                        }
                    });
                    return true;
                } else {
                    changeFragmentListener.onChangeFragmentListener(isNetworkAvailable(getActivity()));
                    list.add(noInternetAccess);
                    rssAdapter.notifyDataSetChanged();
                    pubDateReceived = "";
                    MainActivity.pubDateInsideActionBar.setText(pubDateReceived);
                    titleReceived = getString(R.string.app_name);
                    MainActivity.titleInsideActionBar.setText(titleReceived);
                }
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        myMenu = menu;
        inflater.inflate(R.menu.items, menu);
        super.onCreateOptionsMenu(menu, inflater);
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

    public void resetUpdating() {
        // Get our refresh item from the menu
        MenuItem m = myMenu.findItem(R.id.action_refresh);
        if (m.getActionView() != null) {
            // Remove the animation.
            m.getActionView().clearAnimation();
            m.setActionView(null);
        }
    }

    public class UpdateTask extends AsyncTask<Void, Void, Void> {

        private Context mCon;

        public UpdateTask(Context con) {
            mCon = con;
        }

        @Override
        protected Void doInBackground(Void... nope) {
            try {
                // Set a time to simulate a long update process.
                Thread.sleep(1000);
                return null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void nope) {
            // Change the menu back
            resetUpdating();
        }
    }
}
