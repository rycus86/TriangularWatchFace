package hu.rycus.watchface.commons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public abstract class BaseCanvasWatchFaceService extends CanvasWatchFaceService {

    @Override
    public abstract BaseEngine onCreateEngine();

    public abstract class BaseEngine extends Engine {

        private final List<Component> components = new LinkedList<>();
        private final Time currentTime = new Time();
        private final DeviceShape shape = new DeviceShape();

        private BroadcastReceiver timezoneReceiver;
        private BroadcastReceiver batteryReceiver;

        private boolean timezoneReceiverRegistered;
        private boolean batteryReceiverRegistered;

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

            for (final Component component : components) {
                component.onCreate(visible, inAmbientMode);
            }
        }

        protected abstract WatchFaceStyle buildStyle(final WatchFaceStyle.Builder builder);

        @Override
        public void onApplyWindowInsets(final WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            shape.setShape(insets.isRound());
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
                registerBatteryReceiver();
                registerTimeZoneChangeReceiver();

                // update current time in case it changed when the receiver was unregistered
                updateCurrentTimeWithTimeZone(TimeZone.getDefault().getID());
            } else {
                unregisterBatteryReceiver();
                unregisterTimeZoneChangeReceiver();
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
                component.onAnimationTick();
                component.onDraw(canvas, currentTime);
                shouldInvalidate |= component.shouldInvalidate();
            }

            if (shouldInvalidate) {
                invalidate();
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
            super.onDestroy();
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
