package hu.rycus.watchface.triangular.config;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import hu.rycus.watchface.commons.config.ConfigurationHelper;
import hu.rycus.watchface.triangular.R;
import hu.rycus.watchface.triangular.commons.Configuration;

public class WearableConfigurationActivity extends Activity
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "ConfigActivity";

    private GoogleApiClient apiClient;

    private ConfigurationAdapter configurationAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wear);

        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        configurationAdapter = new ConfigurationAdapter(this, apiClient);

        final WearableListView listView = (WearableListView) findViewById(R.id.list_config);
        listView.setAdapter(configurationAdapter);
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
        ConfigurationHelper.loadLocalConfiguration(
                apiClient, Configuration.PATH, configurationAdapter);
    }

    @Override
    public void onConnectionSuspended(final int i) {
        Log.w(TAG, "GoolgeApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {
        Log.d(TAG, "GoolgeApiClient connection failed: ");
    }

}
