package hu.rycus.watchface.triangular.config;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import hu.rycus.watchface.commons.config.ConfigurationHelper;
import hu.rycus.watchface.triangular.R;
import hu.rycus.watchface.triangular.commons.Configuration;

public class WearableConfigurationActivity extends Activity
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            ConfigurationHelper.OnConfigurationDataReadCallback {

    private static final String TAG = "ConfigActivity";

    private GoogleApiClient apiClient;
    private DataMap configuration;

    private CompoundButton sw24hours;
    private CompoundButton swAnimatedBackground;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wear);

        sw24hours = (CompoundButton) findViewById(R.id.sw_config_24hours);
        swAnimatedBackground = (CompoundButton) findViewById(R.id.sw_config_anim_background);

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
                createButtonListener(Configuration.SHOW_24_HOURS.getKey()));
        swAnimatedBackground.setOnCheckedChangeListener(
                createButtonListener(Configuration.ANIMATED_BACKGROUND.getKey()));
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
        Log.d(TAG, "GoolgeApiClient connected");
        ConfigurationHelper.loadLocalConfiguration(apiClient, Configuration.PATH, this);
    }

    @Override
    public void onConnectionSuspended(final int i) {
        Log.w(TAG, "GoolgeApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {
        Log.d(TAG, "GoolgeApiClient connection failed: ");
    }

    private CompoundButton.OnCheckedChangeListener createButtonListener(final String key) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (configuration != null) {
                    configuration.putBoolean(key, isChecked);
                    ConfigurationHelper.storeConfiguration(
                            apiClient, Configuration.PATH, configuration);
                }
            }
        };
    }

}
