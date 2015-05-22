package hu.rycus.watchface.triangular.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.Time;

import com.google.android.gms.wearable.DataMap;

import hu.rycus.watchface.commons.Component;
import hu.rycus.watchface.commons.DateTimeUI;
import hu.rycus.watchface.commons.TimeField;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.commons.Palette;
import hu.rycus.watchface.triangular.util.Constants;

public class Date extends Component {

    private static final int TEXT_HEIGHT = 20;

    private final DateTimeUI dow = new DateTimeUI.Builder()
            .field(TimeField.DATE)
            .format("%a")
            .build();

    private final DateTimeUI date = new DateTimeUI.Builder()
            .field(TimeField.DATE)
            .format("%d")
            .build();

    private final DateTimeUI month = new DateTimeUI.Builder()
            .field(TimeField.DATE)
            .format("%B")
            .build();

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
        paint.setTypeface(Typeface.DEFAULT);
        dow.update(time, paint);

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        date.update(time, paint);
        canvas.drawText(date.text(), textLeft + dow.width() + 4f, dateBottom, paint);

        paint.setTypeface(Typeface.DEFAULT);
        month.update(time, paint);
        canvas.drawText(dow.text(), textLeft, dateBottom, paint);
        canvas.drawText(month.text(), textLeft, monthBottom, paint);
    }

}
