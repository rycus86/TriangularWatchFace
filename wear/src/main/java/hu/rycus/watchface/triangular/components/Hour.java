package hu.rycus.watchface.triangular.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.format.Time;

import com.google.android.gms.wearable.DataMap;

import hu.rycus.watchface.commons.Component;
import hu.rycus.watchface.commons.DateTimeUI;
import hu.rycus.watchface.commons.TimeField;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.commons.Palette;
import hu.rycus.watchface.triangular.util.Constants;

public class Hour extends Component {

    private final DateTimeUI ui = new DateTimeUI.Builder()
            .field(TimeField.HOUR)
            .format("%H")
            .build();

    private float textLeft;
    private float textBottom;

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

        final boolean display24hours = Configuration.SHOW_24_HOURS.getBoolean(configuration);
        ui.changeFormat(display24hours ? "%H" : "%I");

        palette = Configuration.COLOR_PALETTE.getPalette(configuration);
        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());
    }

    @Override
    protected void onDraw(final Canvas canvas, final Time time) {
        ui.update(time, paint);
        canvas.drawText(ui.text(), textLeft, textBottom, paint);
    }

}
