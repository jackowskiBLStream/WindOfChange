package com.blstream.windofchange;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String MAIN_FRAGMENT = "recycler fragment";
    static TextView titleInsideActionBar;
    static TextView pubDateInsideActionBar;
    private RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
    private NoWebAccessFragment noWebAccessFragment = new NoWebAccessFragment();
    private Menu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar);
        }

        titleInsideActionBar = (TextView) findViewById(R.id.titleInsideActionBar);
        pubDateInsideActionBar = (TextView) findViewById(R.id.pubDateInsideActionBar);
        recyclerViewFragment = new RecyclerViewFragment();
        noWebAccessFragment = new NoWebAccessFragment();
        if (savedInstanceState != null) {
            recyclerViewFragment = getSupportFragmentManager()
                    .getFragment(savedInstanceState, MAIN_FRAGMENT) == null ?
                    new RecyclerViewFragment() : (RecyclerViewFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, MAIN_FRAGMENT);
            noWebAccessFragment = getSupportFragmentManager()
                    .getFragment(savedInstanceState, "noweb") == null ?
                    new NoWebAccessFragment() : (NoWebAccessFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, "noweb");
        }
        setContentView(R.layout.activity_main_layout);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        if (recyclerViewFragment != null && recyclerViewFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, MAIN_FRAGMENT, recyclerViewFragment);
        if (noWebAccessFragment != null && noWebAccessFragment.isAdded())
            getSupportFragmentManager().putFragment(outState, "noweb", noWebAccessFragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //  changeFragments();
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (RecyclerViewFragment.isNetworkAvailable(this)) {
                    RecyclerViewFragment.list.clear();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, recyclerViewFragment)
                            .commit();
                    // Do animation start
                    LayoutInflater inflater = (LayoutInflater) getSystemService(
                            Context.LAYOUT_INFLATER_SERVICE);
                    ImageView iv = (ImageView) inflater.inflate(R.layout.iv_refresh, null);
                    Animation rotation = AnimationUtils.loadAnimation(this, R.anim.anim);
                    rotation.setRepeatCount(Animation.INFINITE);
                    iv.startAnimation(rotation);
                    item.setActionView(iv);
                    new UpdateTask().execute();
                    Intent mServiceIntent = new Intent(this, RSSPullService.class);
                    startService(mServiceIntent);
                    return true;
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, noWebAccessFragment)
                            .commit();
                    RecyclerViewFragment.pubDateReceived = "";
                    MainActivity.pubDateInsideActionBar.setText(RecyclerViewFragment.pubDateReceived);
                    RecyclerViewFragment.titleReceived = getString(R.string.app_name);
                    MainActivity.titleInsideActionBar.setText(RecyclerViewFragment.titleReceived);
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


    public class UpdateTask extends AsyncTask<Void, Void, Void> {


        public UpdateTask() {
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

        public void resetUpdating() {
            // Get our refresh item from the menu
            MenuItem m = myMenu.findItem(R.id.action_refresh);
            if (m.getActionView() != null) {
                // Remove the animation.
                m.getActionView().clearAnimation();
                m.setActionView(null);
            }
        }

        @Override
        protected void onPostExecute(Void nope) {
            // Change the menu back
            resetUpdating();
        }
    }
}
