package hu.rycus.watchface.commons;

import android.graphics.Canvas;
import android.text.format.Time;

public abstract class BatteryListenerComponent extends Component {

    protected int batteryLevel = -1;

    protected void onBatteryLevelChanged(final int level) {
        this.batteryLevel = level;
    }

    @Override
    protected final void onDraw(final Canvas canvas, final Time time) {
        if (batteryLevel > -1) {
            onDrawBattery(canvas, time);
        }
    }

    protected abstract void onDrawBattery(final Canvas canvas, final Time time);

}
