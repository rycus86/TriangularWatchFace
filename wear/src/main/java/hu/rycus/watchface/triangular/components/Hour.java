package hu.rycus.watchface.triangular.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.format.Time;

import hu.rycus.watchface.commons.Component;
import hu.rycus.watchface.triangular.util.Constants;

public class Hour extends Component {

    private float textLeft;
    private float textBottom;

    @Override
    protected void onSetupPaint(final Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextSize(100);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override
    protected void onSizeSet(final int width, final int height) {
        super.onSizeSet(width, height);

        final Rect bounds = new Rect();
        paint.getTextBounds("23", 0, 2, bounds);

        textLeft = width / 2f - bounds.width();
        textBottom = Constants.Text.getBaseline(height);
    }

    @Override
    protected void onDraw(final Canvas canvas, final Time time) {
        canvas.drawText(time.format("%H"), textLeft, textBottom, paint);
    }

}
