package com.blstream.windofchange;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements IManageFragments {
    private static final String MAIN_FRAGMENT = "recycler fragment";
    static TextView titleInsideActionBar;
    static TextView pubDateInsideActionBar;
    private RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
    private NoWebAccessFragment noWebAccessFragment = new NoWebAccessFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar);
        }
        setContentView(R.layout.activity_main_layout);

        titleInsideActionBar = (TextView) findViewById(R.id.titleInsideActionBar);
        pubDateInsideActionBar = (TextView) findViewById(R.id.pubDateInsideActionBar);
        recyclerViewFragment.setChangeFragmentListener(this);
       /* if (savedInstanceState != null) {
            return;
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(recyclerViewFragment, MAIN_FRAGMENT)
                    .commit();
        }
       */
        if (savedInstanceState != null) {

        } else {
           /* getSupportFragmentManager().beginTransaction()
                    .add(recyclerViewFragment, MAIN_FRAGMENT)
                    .commit();*/
            /*getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, recyclerViewFragment)
                    .commit();*/
        }

        /*getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, recyclerViewFragment)
                .commit();*/
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, recyclerViewFragment)
                .commit();
    }

    @Override
    public void onChangeFragmentListener(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
           /* recyclerViewFragment = (RecyclerViewFragment) getSupportFragmentManager()
                    .findFragmentByTag(MAIN_FRAGMENT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, recyclerViewFragment)
                    .commit();*/
            Log.d("onChangmentListener", "blasbla");
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, noWebAccessFragment)
                    .commit();
        }
    }
}
