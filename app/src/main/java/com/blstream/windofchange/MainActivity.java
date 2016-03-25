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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IRecyclerViewPositionHelper {
    private static final String LIST_STATE_KEY = "listState";
    private static final String ADAPTER_STATE = "adapterState";
    private static final String TEMP_LIST_STATE = "tempList";
    private RecyclerView recList;
    private BroadcastReceiver mReceiver;
    private ArrayList<RSSInfo> list = new ArrayList<>();
    private ArrayList<RSSInfo> receivedList = new ArrayList<>();
    private Intent mServiceIntent;
    private RSSInfo noInternetAccess = new RSSInfo();
    private OnReceiverRefresh onReceiverRefresh;
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);

    private Menu myMenu;
    private RSSAdapter rssAdapter = new RSSAdapter(list);


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void setOnReceiverRefresh(OnReceiverRefresh onReceiverRefresh) {
        this.onReceiverRefresh = onReceiverRefresh;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recList = (RecyclerView) findViewById(R.id.recyclerViewList);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(linearLayoutManager);
        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(LIST_STATE_KEY);
            recList.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
            rssAdapter = savedInstanceState.getParcelable(ADAPTER_STATE);
            list = savedInstanceState.getParcelableArrayList(TEMP_LIST_STATE);
        }


        recList.setAdapter(rssAdapter);
        rssAdapter.setListener(MainActivity.this);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                list.clear();
                if (isNetworkAvailable(this)) {
                    // Do animation start

                    LayoutInflater inflater = (LayoutInflater) getSystemService(
                            Context.LAYOUT_INFLATER_SERVICE);
                    ImageView iv = (ImageView) inflater.inflate(R.layout.iv_refresh, null);
                    Animation rotation = AnimationUtils.loadAnimation(this, R.anim.anim);
                    rotation.setRepeatCount(Animation.INFINITE);
                    iv.startAnimation(rotation);
                    item.setActionView(iv);
                    new UpdateTask(this).execute();
                    mServiceIntent = new Intent(this, RSSPullService.class);
                    startService(mServiceIntent);
                    setOnReceiverRefresh(new OnReceiverRefresh() {
                        @Override
                        public void onRefreshListener(ArrayList<RSSInfo> list) {
                            MainActivity.this.list.addAll(list);
                            rssAdapter.notifyDataSetChanged();
                        }
                    });
                    return true;
                } else {
                    noInternetAccess.setTitle("No internet connection!");
                    list.add(noInternetAccess);
                    rssAdapter.notifyDataSetChanged();

                }

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        myMenu = menu;
        getMenuInflater().inflate(R.menu.items, menu);

        return true;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIST_STATE_KEY, recList.getLayoutManager().onSaveInstanceState());
        outState.putParcelable(ADAPTER_STATE, rssAdapter);
        outState.putParcelableArrayList(TEMP_LIST_STATE, list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receivedList = intent
                        .getParcelableArrayListExtra(Constants.EXTENDED_DATA_STATUS);
                onReceiverRefresh.onRefreshListener(receivedList);
               /* recList = (RecyclerView) findViewById(R.id.recyclerViewList);
                LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                recList.setLayoutManager(llm);
                RSSAdapter rssAdapter = new RSSAdapter(list);
                recList.setAdapter(rssAdapter);*/

            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onListen(int position) {
        if (isNetworkAvailable(this)) {
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
            ((MainActivity) mCon).resetUpdating();
        }
    }
}
