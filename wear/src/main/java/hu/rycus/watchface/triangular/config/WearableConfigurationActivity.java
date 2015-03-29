package hu.rycus.watchface.triangular.config;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import hu.rycus.watchface.commons.config.ConfigurationHelper;
import hu.rycus.watchface.triangular.R;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.commons.Palette;

public class WearableConfigurationActivity extends Activity
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            ConfigurationAdapter.OnGroupSelectedListener,
            ConfigurationAdapter.OnPaletteParentSelectedListener {

    private static final String TAG = "ConfigActivity";

    private static final int REQ_GROUP_PICKER = 0x10;
    private static final int REQ_PALETTE_PICKER = 0x11;

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

        configurationAdapter = new ConfigurationAdapter(this, this, this, apiClient);

        final WearableListView listView = (WearableListView) findViewById(R.id.list_config);
        listView.setAdapter(configurationAdapter);
        listView.setClickListener(configurationAdapter);
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

    @Override
    public void onGroupSelected(final Configuration group, final Configuration selection) {
        final int current = group.getGroupValues().indexOf(selection);

        final Intent intent = new Intent(this, GroupSelectionActivity.class);
        intent.putExtra(GroupSelectionActivity.EXTRA_GROUP, group);
        intent.putExtra(GroupSelectionActivity.EXTRA_SELECTED_INDEX, current);
        startActivityForResult(intent, REQ_GROUP_PICKER);
    }

    @Override
    public void onPaletteParentSelected(final Configuration item, final Palette selection) {
        final int current = Palette.indexOf(selection);

        final Intent intent = new Intent(this, PaletteSelectionActivity.class);
        intent.putExtra(PaletteSelectionActivity.EXTRA_ITEM, item);
        intent.putExtra(PaletteSelectionActivity.EXTRA_SELECTED_INDEX, current);
        startActivityForResult(intent, REQ_PALETTE_PICKER);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (REQ_GROUP_PICKER == requestCode && resultCode == RESULT_OK) {
            final Configuration group = (Configuration)
                    data.getSerializableExtra(GroupSelectionActivity.EXTRA_GROUP);
            final Configuration selection = (Configuration)
                    data.getSerializableExtra(GroupSelectionActivity.EXTRA_RESULT);

            if (group != null && selection != null) {
                configurationAdapter.onGroupSelectionResult(group, selection);
            }
        } else if (REQ_PALETTE_PICKER == requestCode && resultCode == RESULT_OK) {
            final Configuration item = (Configuration)
                    data.getSerializableExtra(PaletteSelectionActivity.EXTRA_ITEM);
            final Palette selection = (Palette)
                    data.getSerializableExtra(PaletteSelectionActivity.EXTRA_RESULT);
            System.out.println("Palette: " + item + " -> " + selection);

            if (item != null && selection != null) {
                configurationAdapter.onPaletteSelectionResult(item, selection);
            }
        }
    }
}
