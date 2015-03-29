package hu.rycus.watchface.commons;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.format.Time;

public class BlackAmbientBackground extends Component {

    @Override
    protected void onSetupPaint(final Paint paint) {
        paint.setColor(Color.BLACK);
    }

    @Override
    protected final void onDraw(final Canvas canvas, final Time time) {
        if (inAmbientMode) {
            onDrawBackground(canvas, time);
        }
    }

    protected void onDrawBackground(final Canvas canvas, final Time time) {
        canvas.drawPaint(paint);
    }

}
