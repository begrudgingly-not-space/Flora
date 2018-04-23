package com.asg.florafauna;

import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

/**
 * Created by kkey on 2/16/2018.
 */

class FloraFaunaActionBar {
    static void createActionBar(ActionBar actionBar, @LayoutRes int resource) {
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(resource);

        // Make action bar extend across entire width of screen
        Toolbar toolbar = (Toolbar) actionBar.getCustomView().getParent();
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setPadding(0, 0, 0, 0);
    }
}
