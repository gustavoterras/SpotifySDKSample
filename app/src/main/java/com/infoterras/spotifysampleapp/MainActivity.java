/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.infoterras.spotifysampleapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.infoterras.spotifysampleapp.adapter.TrackAdapter;
import com.infoterras.spotifysampleapp.components.CircularTransition;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity implements
        Player.NotificationCallback, ConnectionStateCallback {
    //   ____                _              _
    //  / ___|___  _ __  ___| |_ __ _ _ __ | |_ ___
    // | |   / _ \| '_ \/ __| __/ _` | '_ \| __/ __|
    // | |__| (_) | | | \__ \ || (_| | | | | |_\__ \
    //  \____\___/|_| |_|___/\__\__,_|_| |_|\__|___/
    //

    @SuppressWarnings("SpellCheckingInspection")
    private static final String CLIENT_ID = "c5b87fc74c4643af9ab325b2d5ae2aa8";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String REDIRECT_URI = "http://mysite.com/callback/";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String TEST_SONG_URI = "spotify:track:6KywfgRqvgvfJc3JRwaZdZ";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String TEST_SONG_MONO_URI = "spotify:track:1FqY3uJypma5wkYw66QOUi";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String TEST_SONG_48kHz_URI = "spotify:track:3wxTNS3aqb9RbBLZgJdZgH";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String TEST_PLAYLIST_URI = "spotify:user:1186282358:playlist:7vWYYFgUsEf1Z7t7sTLOnl";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String TEST_ALBUM_URI = "spotify:album:2lYmxilk8cXJlxxXmns1IU";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String TEST_QUEUE_SONG_URI = "spotify:track:5EEOjaJyWvfMglmEwf9bG3";

    /**
     * Request code that will be passed together with authentication result to the onAuthenticationResult
     */
    private static final int REQUEST_CODE = 1337;

    public static final String TAG = "SpotifySampleApp";

    //  _____ _      _     _
    // |  ___(_) ___| | __| |___
    // | |_  | |/ _ \ |/ _` / __|
    // |  _| | |  __/ | (_| \__ \
    // |_|   |_|\___|_|\__,_|___/
    //

    /**
     * The player used by this activity. There is only ever one instance of the player,
     * which is owned by the {@link com.spotify.sdk.android.player.Spotify} class and refcounted.
     * This means that you may use the Player from as many Fragments as you want, and be
     * assured that state remains consistent between them.
     * <p/>
     * However, each fragment, activity, or helper class <b>must</b> call
     * {@link com.spotify.sdk.android.player.Spotify#destroyPlayer(Object)} when they are no longer
     * need that player. Failing to do so will result in leaked resources.
     */
    private SpotifyPlayer mPlayer;

    private PlaybackState mCurrentPlaybackState;

    /**
     * Used to get notifications from the system about the current network state in order
     * to pass them along to
     * {@link SpotifyPlayer#setConnectivityStatus(Player.OperationCallback, Connectivity)}
     * Note that this implies <pre>android.permission.ACCESS_NETWORK_STATE</pre> must be
     * declared in the manifest. Not setting the correct network state in the SDK may
     * result in strange behavior.
     */
    private BroadcastReceiver mNetworkStateReceiver;

    /**
     * Used to log messages to a {@link android.widget.TextView} in this activity.
     */

    private SpotifyApi api;

    private TrackAdapter adapter;

    private Metadata mMetadata;

    private Scene playerScene;

    private Scene searchScene;

    private Scene loginScene;

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            logStatus("OK!");
        }

        @Override
        public void onError(Error error) {
            logStatus("ERROR:" + error);
        }
    };

    //  ___       _ _   _       _ _          _   _
    // |_ _|_ __ (_) |_(_) __ _| (_)______ _| |_(_) ___  _ __
    //  | || '_ \| | __| |/ _` | | |_  / _` | __| |/ _ \| '_ \
    //  | || | | | | |_| | (_| | | |/ / (_| | |_| | (_) | | | |
    // |___|_| |_|_|\__|_|\__,_|_|_/___\__,_|\__|_|\___/|_| |_|
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ViewGroup container = findViewById(R.id.container);

        loginScene = Scene.getSceneForLayout(container, R.layout.activity_login, this);
        searchScene = Scene.getSceneForLayout(container, R.layout.activity_search, this);
        playerScene = Scene.getSceneForLayout(container, R.layout.activity_player, this);

        updateView();
        logStatus("Ready");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set up the broadcast receiver for network events. Note that we also unregister
        // this receiver again in onPause().
        mNetworkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mPlayer != null) {
                    Connectivity connectivity = getNetworkConnectivity(getBaseContext());
                    logStatus("Network state changed: " + connectivity.toString());
                    mPlayer.setConnectivityStatus(mOperationCallback, connectivity);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkStateReceiver, filter);

        if (mPlayer != null) {
            mPlayer.addNotificationCallback(MainActivity.this);
            mPlayer.addConnectionStateCallback(MainActivity.this);
        }
    }

    private TextWatcher searchMusic = new TextWatcher() {

        Timer timer;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (timer != null) {
                timer.cancel();
            }
        }

        @Override
        public void afterTextChanged(final Editable editable) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    api.getService().searchTracks(editable.toString(), new Callback<TracksPager>() {
                        @Override
                        public void success(TracksPager tracksPager, Response response) {
                            if(adapter != null)
                                adapter.setTrackList(tracksPager.tracks.items);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(MainActivity.this, "failure :/", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }, 1000); //
        }
    };

    private void changeScene(boolean loggedIn) {

        if(loggedIn && playerScene.getSceneRoot().hasFocus()) return;

        Transition transition = TransitionInflater.from(this).inflateTransition(android.R.transition.fade);
        TransitionManager.go(loggedIn ? playerScene : loginScene, transition);
    }

    /**
     * Registering for connectivity changes in Android does not actually deliver them to
     * us in the delivered intent.
     *
     * @param context Android context
     * @return Connectivity state to be passed to the SDK
     */
    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }

    //     _         _   _                _   _           _   _
    //    / \  _   _| |_| |__   ___ _ __ | |_(_) ___ __ _| |_(_) ___  _ __
    //   / _ \| | | | __| '_ \ / _ \ '_ \| __| |/ __/ _` | __| |/ _ \| '_ \
    //  / ___ \ |_| | |_| | | |  __/ | | | |_| | (_| (_| | |_| | (_) | | | |
    // /_/   \_\__,_|\__|_| |_|\___|_| |_|\__|_|\___\__,_|\__|_|\___/|_| |_|
    //

    private void openLoginWindow() {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"})
                .build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    onAuthenticationComplete(response);
                    break;

                // Auth flow returned an error
                case ERROR:
                    logStatus("Auth error: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    logStatus("Auth result: " + response.getType());
            }
        }
    }

    private void onAuthenticationComplete(AuthenticationResponse authResponse) {
        // Once we have obtained an authorization token, we can proceed with creating a Player.
        logStatus("Got authentication token");
        if (mPlayer == null) {
            Config playerConfig = new Config(getApplicationContext(), authResponse.getAccessToken(), CLIENT_ID);
            // Since the Player is a static singleton owned by the Spotify class, we pass "this" as
            // the second argument in order to refcount it properly. Note that the method
            // Spotify.destroyPlayer() also takes an Object argument, which must be the same as the
            // one passed in here. If you pass different instances to Spotify.getPlayer() and
            // Spotify.destroyPlayer(), that will definitely result in resource leaks.
            mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    logStatus("-- Player initialized --");
                    mPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(MainActivity.this));
                    mPlayer.addNotificationCallback(MainActivity.this);
                    mPlayer.addConnectionStateCallback(MainActivity.this);
                    // Trigger UI refresh
                    updateView();
                }

                @Override
                public void onError(Throwable error) {
                    logStatus("Error in initialization: " + error.getMessage());
                }
            });
        } else {
            mPlayer.login(authResponse.getAccessToken());
        }

        if(api == null)
            api = new SpotifyApi();

        api.setAccessToken(authResponse.getAccessToken());
    }

    //  _   _ ___   _____                 _
    // | | | |_ _| | ____|_   _____ _ __ | |_ ___
    // | | | || |  |  _| \ \ / / _ \ '_ \| __/ __|
    // | |_| || |  | |___ \ V /  __/ | | | |_\__ \
    //  \___/|___| |_____| \_/ \___|_| |_|\__|___/
    //

    private void updateView() {
        boolean loggedIn = isLoggedIn();

        changeScene(loggedIn);

        if (!loggedIn) {
            // Login button should be the inverse of the logged in state
            Button loginButton = findViewById(R.id.login_button);
            loginButton.setText(loggedIn ? R.string.logout_button_label : R.string.login_button_label);
        } else {
            if (mMetadata != null) {
                findViewById(R.id.skip_next_button).setEnabled(mMetadata.nextTrack != null);
                findViewById(R.id.skip_prev_button).setEnabled(mMetadata.prevTrack != null);
                findViewById(R.id.pause_button).setEnabled(mMetadata.currentTrack != null);
            }

            final ImageView coverArtView = findViewById(R.id.cover_art);
            if (mMetadata != null && mMetadata.currentTrack != null) {
                final String durationStr = String.format(Locale.getDefault(), " %d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(mMetadata.currentTrack.durationMs),
                        TimeUnit.MILLISECONDS.toSeconds(mMetadata.currentTrack.durationMs) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mMetadata.currentTrack.durationMs))
                );
                ((TextView) findViewById(R.id.metadata)).setText(mMetadata.contextName + "\n" + mMetadata.currentTrack.name + " - " + mMetadata.currentTrack.artistName + durationStr);

                Picasso.get()
                        .load(mMetadata.currentTrack.albumCoverWebUrl)
                        .transform(new Transformation() {
                            @Override
                            public Bitmap transform(Bitmap source) {
                                // really ugly darkening trick
                                final Bitmap copy = source.copy(source.getConfig(), true);
                                source.recycle();
                                final Canvas canvas = new Canvas(copy);
                                canvas.drawColor(0xbb000000);
                                return copy;
                            }

                            @Override
                            public String key() {
                                return "darken";
                            }
                        })
                        .into(coverArtView);
            } else {
                coverArtView.setBackground(null);
            }
        }

    }

    private boolean isLoggedIn() {
        return mPlayer != null && mPlayer.isLoggedIn();
    }

    public void onSearchButtonClicked(View view) {
        Transition transition = new CircularTransition();
        TransitionManager.go(searchScene, transition);

        ((EditText) findViewById(R.id.edt_search)).addTextChangedListener(searchMusic);

        RecyclerView recyclerView = findViewById(R.id.rv);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new TrackAdapter(new ArrayList<Track>());
        adapter.setOnClick(new TrackAdapter.OnClick() {
            @Override
            public void click(Track track) {
                mPlayer.playUri(mOperationCallback, track.uri, 0, 0);
                onCloseSearchButtonClicked(null);
            }
        });

        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    public void onCloseSearchButtonClicked(View view) {
        Transition transition = TransitionInflater.from(this).inflateTransition(android.R.transition.fade);
        TransitionManager.go(playerScene, transition);

        updateView();
    }

    public void onLoginButtonClicked(View view) {
        if (!isLoggedIn()) {
            logStatus("Logging in");
            openLoginWindow();
        } else {
            mPlayer.logout();
        }
    }

    public void onPlayButtonClicked(View view) {

        String uri;
        switch (view.getId()) {
            case R.id.play_track_button:
                uri = TEST_SONG_URI;
                break;
            case R.id.play_mono_track_button:
                uri = TEST_SONG_MONO_URI;
                break;
            case R.id.play_48khz_track_button:
                uri = TEST_SONG_48kHz_URI;
                break;
            case R.id.play_playlist_button:
                uri = TEST_PLAYLIST_URI;
                break;
            case R.id.play_album_button:
                uri = TEST_ALBUM_URI;
                break;
            default:
                throw new IllegalArgumentException("View ID does not have an associated URI to play");
        }

        logStatus("Starting playback for " + uri);
        mPlayer.playUri(mOperationCallback, uri, 0, 0);
    }

    public void onPauseButtonClicked(View view) {
        if (mCurrentPlaybackState != null && mCurrentPlaybackState.isPlaying) {
            mPlayer.pause(mOperationCallback);
        } else {
            mPlayer.resume(mOperationCallback);
        }
    }

    public void onSkipToPreviousButtonClicked(View view) {
        mPlayer.skipToPrevious(mOperationCallback);
    }

    public void onSkipToNextButtonClicked(View view) {
        mPlayer.skipToNext(mOperationCallback);
    }

    public void onQueueSongButtonClicked(View view) {
        mPlayer.queue(mOperationCallback, TEST_QUEUE_SONG_URI);
        Toast toast = Toast.makeText(this, R.string.song_queued_toast, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onToggleShuffleButtonClicked(View view) {
        mPlayer.setShuffle(mOperationCallback, !mCurrentPlaybackState.isShuffling);
    }

    public void onToggleRepeatButtonClicked(View view) {
        mPlayer.setRepeat(mOperationCallback, !mCurrentPlaybackState.isRepeating);
    }

//    public void onSeekButtonClicked(View view) {
//        final Integer seek = Integer.valueOf(mSeekEditText.getText().toString());
//        mPlayer.seekToPosition(mOperationCallback, seek);
//    }

    public void onLowBitrateButtonPressed(View view) {
        mPlayer.setPlaybackBitrate(mOperationCallback, PlaybackBitrate.BITRATE_LOW);
    }

    public void onNormalBitrateButtonPressed(View view) {
        mPlayer.setPlaybackBitrate(mOperationCallback, PlaybackBitrate.BITRATE_NORMAL);
    }

    public void onHighBitrateButtonPressed(View view) {
        mPlayer.setPlaybackBitrate(mOperationCallback, PlaybackBitrate.BITRATE_HIGH);
    }

    //   ____      _ _ _                _      __  __      _   _               _
    //  / ___|__ _| | | |__   __ _  ___| | __ |  \/  | ___| |_| |__   ___   __| |___
    // | |   / _` | | | '_ \ / _` |/ __| |/ / | |\/| |/ _ \ __| '_ \ / _ \ / _` / __|
    // | |__| (_| | | | |_) | (_| | (__|   <  | |  | |  __/ |_| | | | (_) | (_| \__ \
    //  \____\__,_|_|_|_.__/ \__,_|\___|_|\_\ |_|  |_|\___|\__|_| |_|\___/ \__,_|___/
    //

    @Override
    public void onLoggedIn() {
        logStatus("Login complete");
        updateView();
    }

    @Override
    public void onLoggedOut() {
        logStatus("Logout complete");
        updateView();
    }

    public void onLoginFailed(Error error) {
        logStatus("Login error " + error);
    }

    @Override
    public void onTemporaryError() {
        logStatus("Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(final String message) {
        logStatus("Incoming connection message: " + message);
    }

    //  _____                       _   _                 _ _ _
    // | ____|_ __ _ __ ___  _ __  | | | | __ _ _ __   __| | (_)_ __   __ _
    // |  _| | '__| '__/ _ \| '__| | |_| |/ _` | '_ \ / _` | | | '_ \ / _` |
    // | |___| |  | | | (_) | |    |  _  | (_| | | | | (_| | | | | | | (_| |
    // |_____|_|  |_|  \___/|_|    |_| |_|\__,_|_| |_|\__,_|_|_|_| |_|\__, |
    //                                                                 |___/

    /**
     * Print a status message from a callback (or some other place) to the TextView in this
     * activity
     *
     * @param status Status message
     */
    private void logStatus(String status) {
        Log.i(TAG, status);
    }

    //  ____            _                   _   _
    // |  _ \  ___  ___| |_ _ __ _   _  ___| |_(_) ___  _ __
    // | | | |/ _ \/ __| __| '__| | | |/ __| __| |/ _ \| '_ \
    // | |_| |  __/\__ \ |_| |  | |_| | (__| |_| | (_) | | | |
    // |____/ \___||___/\__|_|   \__,_|\___|\__|_|\___/|_| |_|
    //

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkStateReceiver);

        // Note that calling Spotify.destroyPlayer() will also remove any callbacks on whatever
        // instance was passed as the refcounted owner. So in the case of this particular example,
        // it's not strictly necessary to call these methods, however it is generally good practice
        // and also will prevent your application from doing extra work in the background when
        // paused.
        if (mPlayer != null) {
            mPlayer.removeNotificationCallback(MainActivity.this);
            mPlayer.removeConnectionStateCallback(MainActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        // *** ULTRA-IMPORTANT ***
        // ALWAYS call this in your onDestroy() method, otherwise you will leak native resources!
        // This is an unfortunate necessity due to the different memory management models of
        // Java's garbage collector and C++ RAII.
        // For more information, see the documentation on Spotify.destroyPlayer().
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent event) {
        // Remember kids, always use the English locale when changing case for non-UI strings!
        // Otherwise you'll end up with mysterious errors when running in the Turkish locale.
        // See: http://java.sys-con.com/node/46241
        logStatus("Event: " + event);
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        mMetadata = mPlayer.getMetadata();
        Log.i(TAG, "Player state: " + mCurrentPlaybackState);
        Log.i(TAG, "Metadata: " + mMetadata);
        updateView();
    }

    @Override
    public void onPlaybackError(Error error) {
        logStatus("Err: " + error);
    }
}