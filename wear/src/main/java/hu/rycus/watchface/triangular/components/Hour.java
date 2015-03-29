package hu.rycus.watchface.triangular.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.format.Time;

import com.google.android.gms.wearable.DataMap;

import hu.rycus.watchface.commons.Component;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.commons.Palette;
import hu.rycus.watchface.triangular.util.Constants;

public class Hour extends Component {

    private float textLeft;
    private float textBottom;

    private boolean display24hours = true;

    private Palette palette = Palette.getDefault();

    @Override
    protected void onCreate(final boolean visible, final boolean inAmbientMode) {
        super.onCreate(visible, inAmbientMode);
        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());
    }

    @Override
    protected void onSetupPaint(final Paint paint) {
        paint.setAntiAlias(true);
        paint.setTextSize(100);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override
    protected void onSizeSet(final int width, final int height, final boolean round) {
        super.onSizeSet(width, height, round);

        final Rect bounds = new Rect();
        paint.getTextBounds("23", 0, 2, bounds);

        textLeft = width / 2f - bounds.width();
        textBottom = Constants.Text.getBaseline(height, round);
    }

    @Override
    protected void onAmbientModeChanged(final boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);
        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());
    }

    @Override
    protected void onApplyConfiguration(final DataMap configuration) {
        super.onApplyConfiguration(configuration);

        display24hours = Configuration.SHOW_24_HOURS.getBoolean(configuration);

        palette = Configuration.COLOR_PALETTE.getPalette(configuration);
        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());
    }

    @Override
    protected void onDraw(final Canvas canvas, final Time time) {
        final String formatted;
        if (display24hours) {
            formatted = time.format("%H");
        } else {
            formatted = time.format("%I");
        }

        canvas.drawText(formatted, textLeft, textBottom, paint);
    }

}
