package com.bignerdranch.RunTracker;

import android.support.v4.app.Fragment;

/**
 * Created by badgateway on 12.05.14.
 */
public class RunListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new RunListFragment();
    }
}
