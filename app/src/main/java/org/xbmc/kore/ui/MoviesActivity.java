/*
 * Copyright 2015 Synced Synapse. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xbmc.kore.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import org.xbmc.kore.R;
import org.xbmc.kore.utils.LogUtils;
import org.xbmc.kore.utils.Utils;

/**
 * Controls the presentation of Movies information (list, details)
 * All the information is presented by specific fragments
 */
public class MoviesActivity extends BaseActivity
        implements MovieListFragment.OnMovieSelectedListener {
    private static final String TAG = LogUtils.makeLogTag(MoviesActivity.class);

    public static final String MOVIEID = "movie_id";
    public static final String MOVIETITLE = "movie_title";

    private int selectedMovieId = -1;
    private String selectedMovieTitle;

    private NavigationDrawerFragment navigationDrawerFragment;

    @TargetApi(21)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Request transitions on lollipop
        if (Utils.isLollipopOrLater()) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_media);

        // Set up the drawer.
        navigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        if (savedInstanceState == null) {
            MovieListFragment movieListFragment = new MovieListFragment();

            // Setup animations
            if (Utils.isLollipopOrLater()) {
                //Fade added to prevent shared element from disappearing very shortly at the start of the transition.
                Transition fade = TransitionInflater
                        .from(this)
                        .inflateTransition(android.R.transition.fade);
                movieListFragment.setExitTransition(fade);
                movieListFragment.setReenterTransition(fade);
                movieListFragment.setSharedElementReturnTransition(TransitionInflater.from(
                        this).inflateTransition(R.transition.change_image));
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, movieListFragment)
                    .commit();
        } else {
            selectedMovieId = savedInstanceState.getInt(MOVIEID, -1);
            selectedMovieTitle = savedInstanceState.getString(MOVIETITLE, null);
        }

        setupActionBar(selectedMovieTitle);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MOVIEID, selectedMovieId);
        outState.putString(MOVIETITLE, selectedMovieTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (!navigationDrawerFragment.isDrawerOpen()) {
//            getMenuInflater().inflate(R.menu.media_info, menu);
//        }
        getMenuInflater().inflate(R.menu.media_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_remote:
                // Starts remote
                Intent launchIntent = new Intent(this, RemoteActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(launchIntent);
                return true;
            case android.R.id.home:
                // Only respond to this if we are showing the movie details in portrait mode,
                // which can be checked by checking if selected movie != -1, in which case we
                // should go back to the previous fragment, which is the list.
                if (selectedMovieId != -1) {
                    selectedMovieId = -1;
                    selectedMovieTitle = null;
                    setupActionBar(null);
                    getSupportFragmentManager().popBackStack();
                    return true;
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If we are showing movie details in portrait, clear selected and show action bar
        if (selectedMovieId != -1) {
            selectedMovieId = -1;
            selectedMovieTitle = null;
            setupActionBar(null);
        }
        super.onBackPressed();
    }

    private void setupActionBar(String movieTitle) {
        Toolbar toolbar = (Toolbar)findViewById(R.id.default_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (movieTitle != null) {
            navigationDrawerFragment.setDrawerIndicatorEnabled(false);
            actionBar.setTitle(movieTitle);
        } else {
            navigationDrawerFragment.setDrawerIndicatorEnabled(true);
            actionBar.setTitle(R.string.movies);
        }
    }

    /**
     * Callback from movielist fragment when a movie is selected.
     * Switch fragment in portrait
     * @param vh ViewHolder holding movie info of item clicked
     */
    @TargetApi(21)
    public void onMovieSelected(MovieListFragment.ViewHolder vh) {
        selectedMovieTitle = vh.movieTitle;
        selectedMovieId = vh.movieId;

        // Set up transitions
        if (Utils.isLollipopOrLater()) {
            MovieDetailsFragment movieDetailsFragment = MovieDetailsFragment.newInstance(vh);
            FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();

            movieDetailsFragment.setEnterTransition(TransitionInflater
                    .from(this)
                    .inflateTransition(R.transition.media_details));
            movieDetailsFragment.setReturnTransition(null);

            Transition changeImageTransition = TransitionInflater.from(
                    this).inflateTransition(R.transition.change_image);
            movieDetailsFragment.setSharedElementReturnTransition(changeImageTransition);
            movieDetailsFragment.setSharedElementEnterTransition(changeImageTransition);

            fragTrans.replace(R.id.fragment_container, movieDetailsFragment)
                    .addToBackStack(null)
                    .addSharedElement(vh.artView, vh.artView.getTransitionName())
                    .commit();
        } else {
            MovieDetailsFragment movieDetailsFragment = MovieDetailsFragment.newInstance(vh);
            FragmentTransaction fragTrans = getSupportFragmentManager().beginTransaction();

            fragTrans.setCustomAnimations(R.anim.fragment_details_enter, 0,
                    R.anim.fragment_list_popenter, 0);
            fragTrans.replace(R.id.fragment_container, movieDetailsFragment)
                    .addToBackStack(null)
                    .commit();
        }

        setupActionBar(selectedMovieTitle);
    }
}
