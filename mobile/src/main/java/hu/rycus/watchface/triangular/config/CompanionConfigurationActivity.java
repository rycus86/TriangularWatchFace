package hu.rycus.watchface.triangular.config;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import hu.rycus.watchface.commons.config.ConfigurationHelper;
import hu.rycus.watchface.triangular.R;

public class CompanionConfigurationActivity extends Activity
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            ConfigurationHelper.OnConfigurationDataReadCallback {

    private static final String TAG = "Companion";

    private GoogleApiClient apiClient;
    private String peerId;
    private DataMap configuration;

    private Switch sw24hours;
    private Switch swAnimatedBackground;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_companion);

        peerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);

        sw24hours = (Switch) findViewById(R.id.sw_config_24hours);
        swAnimatedBackground = (Switch) findViewById(R.id.sw_config_anim_background);

        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onConfigurationDataRead(final DataMap configuration) {
        this.configuration = configuration;

        Log.d(TAG, "Configuration loaded: " + configuration);
        sw24hours.setChecked(
                Configuration.SHOW_24_HOURS.getBoolean(configuration));
        swAnimatedBackground.setChecked(
                Configuration.ANIMATED_BACKGROUND.getBoolean(configuration));

        sw24hours.setOnCheckedChangeListener(
                createSwitchListener(Configuration.SHOW_24_HOURS.getKey()));
        swAnimatedBackground.setOnCheckedChangeListener(
                createSwitchListener(Configuration.ANIMATED_BACKGROUND.getKey()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    @Override
    protected void onStop() {
        if (apiClient != null && apiClient.isConnected()) {
            apiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(final Bundle bundle) {
        Log.d(TAG, "GoogleApiClient connected");
        if (peerId != null) {
            ConfigurationHelper.loadConfiguration(apiClient, Configuration.PATH, peerId, this);
        }
    }

    @Override
    public void onConnectionSuspended(final int i) {
        Log.w(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {
        Log.e(TAG, "GoogleApiClient connection failed: " + connectionResult);
    }

    private CompoundButton.OnCheckedChangeListener createSwitchListener(final String key) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (peerId != null && configuration != null) {
                    configuration.putBoolean(key, isChecked);
                    ConfigurationHelper.sendConfiguration(
                            apiClient, Configuration.PATH, peerId, configuration);
                }
            }
        };
    }

}
