package hu.rycus.watchface.commons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import hu.rycus.watchface.commons.config.ConfigurationHelper;

public abstract class BaseCanvasWatchFaceService extends CanvasWatchFaceService {

    @Override
    public abstract BaseEngine onCreateEngine();

    public abstract class BaseEngine extends Engine
            implements
                DataApi.DataListener,
                GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener,
                ConfigurationHelper.OnConfigurationDataReadCallback {

        private final List<Component> components = new LinkedList<>();
        private final Time currentTime = new Time();
        private final DeviceShape shape = new DeviceShape();

        private BroadcastReceiver timezoneReceiver;
        private BroadcastReceiver batteryReceiver;

        private boolean timezoneReceiverRegistered;
        private boolean batteryReceiverRegistered;

        private final Map<Integer, Long> handlerSchedules = new HashMap<>();
        private Handler handler;

        private final GoogleApiClient apiClient =
                new GoogleApiClient.Builder(BaseCanvasWatchFaceService.this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Wearable.API)
                        .build();

        protected String getLogTag() {
            return "Engine";
        }

        protected abstract String[] getConfigurationPaths();

        protected abstract void createComponents(final Collection<Component> components);

        @Override
        public void onCreate(final SurfaceHolder holder) {
            super.onCreate(holder);

            createComponents(components);
            initializeBatteryReceiverIfNeeded();
            initializeTimeZoneChangeReceiver();

            setWatchFaceStyle(buildStyle(
                    new WatchFaceStyle.Builder(BaseCanvasWatchFaceService.this)));

            final boolean visible = isVisible();
            final boolean inAmbientMode = isInAmbientMode();

            boolean needsHandler = false;

            for (final Component component : components) {
                component.onSetEngine(this);
                component.onCreate(visible, inAmbientMode);
                needsHandler |= component.needsHandler();
            }

            if (needsHandler) {
                createHandler();
            }
        }

        protected abstract WatchFaceStyle buildStyle(final WatchFaceStyle.Builder builder);

        @Override
        public void onApplyWindowInsets(final WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                shape.setShape(insets.isRound());
            }
            notifyComponentsIfShapeIsReady();
        }

        @Override
        public void onSurfaceChanged(final SurfaceHolder holder, final int format,
                                     final int width, final int height) {
            super.onSurfaceChanged(holder, format, width, height);

            shape.setSize(width, height);
            notifyComponentsIfShapeIsReady();
        }

        private void notifyComponentsIfShapeIsReady() {
            if (shape.ready()) {
                for (final Component component : components) {
                    component.onSizeSet(shape.width, shape.height, shape.round);
                }
            }
        }

        @Override
        public void onAmbientModeChanged(final boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            for (final Component component : components) {
                component.onAmbientModeChanged(inAmbientMode);
            }

            invalidate();
        }

        @Override
        public void onPropertiesChanged(final Bundle properties) {
            super.onPropertiesChanged(properties);

            final boolean burnInProtection =
                    properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, true);
            final boolean lowBitAmbient =
                    properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, true);

            for (final Component component : components) {
                component.onPropertiesChanged(burnInProtection, lowBitAmbient);
            }
        }

        @Override
        public void onVisibilityChanged(final boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                apiClient.connect();

                registerBatteryReceiver();
                registerTimeZoneChangeReceiver();
                rescheduleAllHandlers();

                // update current time in case it changed when the receiver was unregistered
                updateCurrentTimeWithTimeZone(TimeZone.getDefault().getID());
            } else {
                unregisterBatteryReceiver();
                unregisterTimeZoneChangeReceiver();
                clearAllHandlers();

                if (apiClient != null && apiClient.isConnected()) {
                    Wearable.DataApi.removeListener(apiClient, this);
                    apiClient.disconnect();
                }
            }

            for (final Component component : components) {
                component.onVisibilityChanged(visible);
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onDraw(final Canvas canvas, final Rect bounds) {
            currentTime.setToNow();

            boolean shouldInvalidate = false;

            for (final Component component : components) {
                if (!component.isActive()) {
                    continue;
                }

                component.onAnimationTick();
                component.onDraw(canvas, currentTime);
                shouldInvalidate |= component.shouldInvalidate();
            }

            if (shouldInvalidate) {
                invalidate();
            }
        }

        private void createHandler() {
            this.handler = new Handler() {
                @Override
                public void handleMessage(final Message msg) {
                    if (!isHandlerAllowedToRun()) {
                        return;
                    }

                    final int what = msg.what;

                    boolean hasActiveReceiver = false;
                    boolean shouldInvalidate = false;

                    try {
                        for (final Component component : components) {
                            if (component.isActive() && component.needsHandler()) {
                                component.onHandleMessage(what);

                                hasActiveReceiver = true;
                                shouldInvalidate |= component.shouldInvalidate();
                            }
                        }
                    } catch (Exception ex) {
                        Log.e("BaseEngine", String.format("Failed to handle message %x", what), ex);
                    }

                    if (hasActiveReceiver) {
                        rescheduleHandler(what);

                        /*
                         * TODO sometimes multiple registration happens
                         * move Handler management to Scheduler helper class
                         */
                    }

                    if (shouldInvalidate) {
                        invalidate();
                    }
                }
            };
        }

        void startHandlerSchedule(final int what, final long interval) {
            handlerSchedules.put(what, interval);
            rescheduleHandler(what);
        }

        void clearHandlerSchedule(final int what) {
            handlerSchedules.remove(what);
            if (handler != null) {
                handler.removeMessages(what);
            }
        }

        private boolean isHandlerAllowedToRun() {
            return handler != null && isVisible() && !isInAmbientMode();
        }

        private void rescheduleAllHandlers() {
            if (isHandlerAllowedToRun()) {
                for (final int what : handlerSchedules.keySet()) {
                    rescheduleHandler(what);
                }
            }
        }

        private void rescheduleHandler(final int what) {
            if (isHandlerAllowedToRun()) {
                final long interval = handlerSchedules.get(what);
                final long delay = interval - (System.currentTimeMillis() % interval);
                handler.sendEmptyMessageDelayed(what, delay);
            }
        }

        private void clearAllHandlers() {
            final Set<Integer> keys = new HashSet<>(handlerSchedules.keySet());
            for (final int what : keys) {
                clearHandlerSchedule(what);
            }
        }

        private void initializeBatteryReceiverIfNeeded() {
            boolean listenToBatteryEvents = false;
            for (final Component component : components) {
                if (component instanceof BatteryListenerComponent) {
                    listenToBatteryEvents = true;
                    break;
                }
            }

            if (listenToBatteryEvents) {
                batteryReceiver = createBatteryEventReceiver();
            }
        }

        private void registerBatteryReceiver() {
            if (!batteryReceiverRegistered && batteryReceiver != null) {
                batteryReceiverRegistered = true;

                final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                final Intent intent = registerReceiver(batteryReceiver, filter);
                readBatteryStats(intent);
            }
        }

        private void unregisterBatteryReceiver() {
            if (batteryReceiverRegistered && batteryReceiver != null) {
                batteryReceiverRegistered = false;

                unregisterReceiver(batteryReceiver);
            }
        }

        private void readBatteryStats(final Intent intent) {
            final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            final int max = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);

            if (level > -1) {
                final int percentage = level * 100 / max;
                for (final Component component : components) {
                    if (component instanceof BatteryListenerComponent) {
                        ((BatteryListenerComponent) component).onBatteryLevelChanged(percentage);
                    }
                }
            }
        }

        @Override
        public void onDestroy() {
            unregisterBatteryReceiver();
            unregisterTimeZoneChangeReceiver();
            clearAllHandlers();
            super.onDestroy();
        }

        @Override
        public void onConnected(final Bundle bundle) {
            Log.d(getLogTag(), "GoogleApiClient connected");
            Wearable.DataApi.addListener(apiClient, this);

            for (final String path : getConfigurationPaths()) {
                ConfigurationHelper.loadLocalConfiguration(apiClient, path, this);
            }
        }

        @Override
        public void onConnectionSuspended(final int i) {
            Log.w(getLogTag(), "GoogleApiClient connection suspended");
        }

        @Override
        public void onConnectionFailed(final ConnectionResult connectionResult) {
            Log.e(getLogTag(), "GoogleApiClient connection failed: " + connectionResult);
        }

        @Override
        public void onDataChanged(final DataEventBuffer dataEvents) {
            try {
                for (final DataEvent dataEvent : dataEvents) {
                    if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                        continue;
                    }

                    final DataItem dataItem = dataEvent.getDataItem();
                    if (!Arrays.asList(getConfigurationPaths())
                            .contains(dataItem.getUri().getPath())) {
                        continue;
                    }

                    final DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                    final DataMap config = dataMapItem.getDataMap();
                    onConfigurationReceived(config);
                }
            } finally {
                dataEvents.close();
            }
        }

        @Override
        public void onConfigurationDataRead(final DataMap configurationMap) {
            onConfigurationReceived(configurationMap);
        }

        private void onConfigurationReceived(final DataMap configuration) {
            Log.d(getLogTag(), "Configuration received: " + configuration);
            for (final Component component : components) {
                component.onApplyConfiguration(configuration);
            }

            invalidate();
        }

        private BroadcastReceiver createBatteryEventReceiver() {
            return new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    readBatteryStats(intent);
                    invalidate();
                }
            };
        }

        private void initializeTimeZoneChangeReceiver() {
            timezoneReceiver = createTimeZoneChangeReceiver();
        }

        private BroadcastReceiver createTimeZoneChangeReceiver() {
            return new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    updateCurrentTimeWithTimeZone(intent.getStringExtra("time-zone"));
                    invalidate();
                }
            };
        }

        private void updateCurrentTimeWithTimeZone(final String timezoneId) {
            currentTime.clear(timezoneId);
            currentTime.setToNow();
        }

        private void registerTimeZoneChangeReceiver() {
            if (!timezoneReceiverRegistered) {
                timezoneReceiverRegistered = true;

                final IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                registerReceiver(timezoneReceiver, filter);
            }
        }

        private void unregisterTimeZoneChangeReceiver() {
            if (timezoneReceiverRegistered) {
                timezoneReceiverRegistered = false;

                unregisterReceiver(timezoneReceiver);
            }
        }

    }

    private class DeviceShape {

        int width;
        int height;
        boolean round;

        boolean sizeSet = false;
        boolean shapeSet = false;

        void setSize(final int width, final int height) {
            this.width = width;
            this.height = height;
            this.sizeSet = true;
        }

        void setShape(final boolean round) {
            this.round = round;
            this.shapeSet = true;
        }

        boolean ready() {
            return sizeSet && shapeSet;
        }

    }

}
