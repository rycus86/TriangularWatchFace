package hu.rycus.watchface.triangular.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.Time;

import com.google.android.gms.wearable.DataMap;

import hu.rycus.watchface.commons.Animation;
import hu.rycus.watchface.commons.Component;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.commons.Palette;
import hu.rycus.watchface.triangular.util.Constants;

public class Minute extends Component {

    private static final int MIN_SIZE = 64;
    private static final int MAX_SIZE = 90;
    private static final int SIZE_DIFFERENCE = MAX_SIZE - MIN_SIZE;

    private float textLeft;
    private float textBottom;

    private Palette palette = Palette.getDefault();

    private boolean secondsAreShown;

    @Override
    protected void onCreate(final boolean visible, final boolean inAmbientMode) {
        super.onCreate(visible, inAmbientMode);
        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());
    }

    @Override
    protected void onSetupPaint(final Paint paint) {
        paint.setAntiAlias(true);
        paint.setTextSize(inAmbientMode ? MAX_SIZE : MIN_SIZE);
        paint.setTypeface(Typeface.DEFAULT);
    }

    @Override
    protected void onSizeSet(final int width, final int height, final boolean round) {
        super.onSizeSet(width, height, round);

        textLeft = width / 2f + 8f;
        textBottom = Constants.Text.getBaseline(height, round);
    }

    @Override
    protected void onAmbientModeChanged(final boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);

        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());

        if (inAmbientMode) {
            setAnimation(createGrowAnimation());
        } else {
            setAnimation(createShrinkAnimation());
        }
    }

    @Override
    protected void onApplyConfiguration(final DataMap configuration) {
        secondsAreShown = Configuration.SHOW_SECONDS.getBoolean(configuration);

        palette = Configuration.COLOR_PALETTE.getPalette(configuration);
        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());
    }

    @Override
    protected void onDraw(final Canvas canvas, final Time time) {
        canvas.drawText(time.format("%M"), textLeft, textBottom, paint);
    }

    private Animation createGrowAnimation() {
        final long factor = secondsAreShown ? 2 : 1;
        final long duration = factor * Constants.ANIMATION_DURATION;

        return new Animation(duration) {
            @Override
            protected void apply(final float progress) {
                if (secondsAreShown) {
                    final float delayedProgress = Math.max(0, (progress - 0.5f) * 2f);
                    paint.setTextSize(MIN_SIZE + SIZE_DIFFERENCE * delayedProgress);
                } else {
                    paint.setTextSize(MIN_SIZE + SIZE_DIFFERENCE * progress);
                }
            }
        };
    }

    private Animation createShrinkAnimation() {
        return new Animation(Constants.ANIMATION_DURATION) {
            @Override
            protected void apply(final float progress) {
                paint.setTextSize(MAX_SIZE - SIZE_DIFFERENCE * progress);
            }
        };
    }

}
