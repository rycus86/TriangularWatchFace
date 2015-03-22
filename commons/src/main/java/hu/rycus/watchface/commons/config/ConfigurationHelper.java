package hu.rycus.watchface.commons.config;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public final class ConfigurationHelper {

    private static final String TAG = "Config";

    private ConfigurationHelper() { }

    public static void loadLocalConfiguration(final GoogleApiClient apiClient, final String path,
                                              final OnConfigurationDataReadCallback callback) {
        Wearable.NodeApi.getLocalNode(apiClient).setResultCallback(
                new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(final NodeApi.GetLocalNodeResult getLocalNodeResult) {
                final String localNode = getLocalNodeResult.getNode().getId();
                loadConfiguration(apiClient, path, localNode, callback);
            }
        });
    }

    public static void loadConfiguration(final GoogleApiClient apiClient,
                                         final String path, final String nodeId,
                                         final OnConfigurationDataReadCallback callback) {
        Wearable.DataApi.getDataItem(apiClient, createUri(path, nodeId))
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(final DataApi.DataItemResult dataItemResult) {
                        if (dataItemResult.getStatus().isSuccess()) {
                            final DataItem dataItem = dataItemResult.getDataItem();
                            if (dataItem != null) {
                                final DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                                final DataMap config = dataMapItem.getDataMap();
                                callback.onConfigurationDataRead(config);
                                return;
                            }
                        }

                        // if not found or unsuccessful
                        callback.onConfigurationDataRead(new DataMap());
                    }
                });
    }

    public static void storeConfiguration(final GoogleApiClient apiClient, final String path,
                                          final DataMap configuration) {
        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        final DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(configuration);

        Wearable.DataApi.putDataItem(apiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(final DataApi.DataItemResult dataItemResult) {
                        Log.d(TAG, "Configuration store result: " + dataItemResult.getStatus());
                    }
                });
    }

    public static void sendConfiguration(final GoogleApiClient apiClient,
                                         final String path, final String nodeId,
                                         final DataMap configuration) {
        if (nodeId != null) {
            Wearable.MessageApi.sendMessage(apiClient, nodeId, path, configuration.toByteArray())
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(final MessageApi.SendMessageResult sendMessageResult) {
                            Log.d(TAG, "Send configuration result: " + sendMessageResult.getStatus());
                        }
                    });
        }
    }

    private static Uri createUri(final String path, final String nodeId) {
        return new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(path)
                .authority(nodeId)
                .build();
    }

    public interface OnConfigurationDataReadCallback {

        void onConfigurationDataRead(DataMap configurationMap);

    }

}
