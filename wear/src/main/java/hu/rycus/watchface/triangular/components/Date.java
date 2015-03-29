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

public class Date extends Component {

    private static final int TEXT_HEIGHT = 20;

    private final Rect dowBounds = new Rect();

    private Palette palette = Palette.getDefault();

    private float textLeft;
    private float dateBottom;
    private float monthBottom;

    @Override
    protected void onCreate(final boolean visible, final boolean inAmbientMode) {
        super.onCreate(visible, inAmbientMode);
        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());
    }

    @Override
    protected void onSetupPaint(final Paint paint) {
        paint.setAntiAlias(true);
        paint.setTextSize(TEXT_HEIGHT);
    }

    @Override
    protected void onSizeSet(final int width, final int height, final boolean round) {
        super.onSizeSet(width, height, round);

        textLeft = width / 2f + 8f;
        dateBottom = Constants.Text.getBaseline(height, round) + TEXT_HEIGHT + 2f;
        monthBottom = dateBottom + TEXT_HEIGHT + 2f;
    }

    @Override
    protected void onAmbientModeChanged(final boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);
        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());
    }

    @Override
    protected void onApplyConfiguration(final DataMap configuration) {
        super.onApplyConfiguration(configuration);

        palette = Configuration.COLOR_PALETTE.getPalette(configuration);
        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());
    }

    @Override
    protected void onDraw(final Canvas canvas, final Time time) {
        final String dowText = time.format("%a");
        paint.getTextBounds(dowText, 0, dowText.length(), dowBounds);

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText(time.format("%d"), textLeft + dowBounds.width() + 4f, dateBottom, paint);

        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText(dowText, textLeft, dateBottom, paint);
        canvas.drawText(time.format("%B"), textLeft, monthBottom, paint);
    }

}
