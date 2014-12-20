package hu.rycus.watchface.commons;

import android.graphics.Canvas;
import android.text.format.Time;

public abstract class NonAmbientBackground extends Component {

    @Override
    protected final void onDraw(final Canvas canvas, final Time time) {
        if (!inAmbientMode) {
            onDrawBackground(canvas, time);
        }
    }

    protected abstract void onDrawBackground(final Canvas canvas, final Time time);

}
