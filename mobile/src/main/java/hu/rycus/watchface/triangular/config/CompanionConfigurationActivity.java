package hu.rycus.watchface.triangular.config;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import hu.rycus.watchface.commons.config.ConfigurationHelper;
import hu.rycus.watchface.triangular.R;
import hu.rycus.watchface.triangular.commons.Configuration;

public class CompanionConfigurationActivity extends Activity
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Companion";

    private GoogleApiClient apiClient;
    private String peerId;
    private ConfigurationAdapter configurationAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_companion);

        peerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);

        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        configurationAdapter = new ConfigurationAdapter(this, apiClient, peerId);

        final ListView listView = (ListView) findViewById(R.id.config_list);
        listView.setAdapter(configurationAdapter);
        listView.setOnItemClickListener(configurationAdapter);
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
            ConfigurationHelper.loadConfiguration(
                    apiClient, Configuration.PATH, peerId, configurationAdapter);
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

}
