package hu.rycus.watchface.commons;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Scheduler {

    private static final String TAG = "Scheduler";

    private final Map<Integer, List<Component>> registeredComponents = new HashMap<>();
    private final Map<Integer, Long> intervals = new HashMap<>();

    private final BaseCanvasWatchFaceService.BaseEngine engine;
    private Handler handler;

    public Scheduler(final BaseCanvasWatchFaceService.BaseEngine engine) {
        this.engine = engine;
    }

    public void initialize() {
        this.handler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                if (shouldNotRunHandlerTasks()) {
                    return;
                }

                final int what = msg.what;

                final List<Component> components = registeredComponents.get(what);
                if (components == null) {
                    return;
                }

                boolean hasActiveReceiver = false;
                boolean shouldInvalidate = false;

                for (final Component component : components) {
                    try {
                        if (component.isActive() && component.needsScheduler()) {
                            component.onHandleMessage(what);

                            hasActiveReceiver = true;
                            shouldInvalidate |= component.shouldInvalidate();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, String.format("Failed to handle message %x for %s",
                                what, component.getClass().getSimpleName()), ex);
                    }
                }

                if (hasActiveReceiver) {
                    start(what);
                }

                if (shouldInvalidate) {
                    engine.invalidate();
                }
            }
        };
    }

    private boolean shouldNotRunHandlerTasks() {
        return !engine.isVisible() || engine.isInAmbientMode();
    }

    public void register(final Component component, final int what, final long interval) {
        Log.d(TAG, String.format("Register/start messages with code %x for %s",
                what, component.getClass().getSimpleName()));

        List<Component> components = registeredComponents.get(what);
        if (components == null) {
            components = new LinkedList<>();
            registeredComponents.put(what, components);
        }

        if (!components.contains(component)) {
            components.add(component);
        }

        if (intervals.containsKey(what)) {
            final long running = intervals.get(what);
            if (running != interval) {
                final String message = String.format(
                        "An interval of %d ms is already configured for %x, " +
                        "the requested %d will not be applied for %s!",
                        running, what, interval, component.getClass());
                Log.w(TAG, message);
            }
        } else {
            intervals.put(what, interval);
        }

        start(what);
    }

    public void unregister(final Component component, final int what) {
        Log.d(TAG, String.format("Unregister %s from messages with code %x",
                component.getClass().getSimpleName(), what));

        final List<Component> components = registeredComponents.get(what);
        if (components != null) {
            if (components.remove(component)) {
                if (components.isEmpty()) {
                    registeredComponents.remove(what);
                    stop(what);
                }
            }
        }
    }

    public void enable() {
        for (final int what : registeredComponents.keySet()) {
            start(what);
        }
    }

    public void disable() {
        for (final int what : registeredComponents.keySet()) {
            stop(what);
        }
    }

    private void start(final int what) {
        if (handler != null) {
            if (handler.hasMessages(what)) {
                return;
            }

            final Long interval = intervals.get(what);
            if (interval != null) {
                final long delay = interval - (System.currentTimeMillis() % interval);
                handler.sendEmptyMessageDelayed(what, delay);
            }
        }
    }

    private void stop(final int what) {
        if (handler != null) {
            handler.removeMessages(what);
        }
    }

}
