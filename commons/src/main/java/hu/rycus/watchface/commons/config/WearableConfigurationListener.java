package hu.rycus.watchface.commons.config;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public abstract class WearableConfigurationListener extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "ConfigListener";

    private Collection<String> configurationPaths = Collections.emptyList();
    private GoogleApiClient apiClient;

    protected abstract String[] getConfigurationPaths();

    @Override
    public void onCreate() {
        super.onCreate();

        final String[] paths = getConfigurationPaths();
        if (paths != null) {
            this.configurationPaths = Arrays.asList(paths);
        }
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        final String path = messageEvent.getPath();
        Log.d(TAG, "Processing message for path: " + path);

        if (!configurationPaths.contains(path)) {
            return;
        }

        if (prepareApiClient()) {
            final DataMap configuration = DataMap.fromByteArray(messageEvent.getData());
            ConfigurationHelper.storeConfiguration(apiClient, path, configuration);
        }
    }

    private boolean prepareApiClient() {
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API)
                    .build();
        }

        if (!apiClient.isConnected()) {
            final ConnectionResult connectionResult =
                    apiClient.blockingConnect(30, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Failed to connect to GoogleApiClient.");
            }
        }

        return apiClient.isConnected();
    }

    @Override
    public void onConnected(final Bundle bundle) {
        Log.d(TAG, "GoogleApiClient connected");
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
