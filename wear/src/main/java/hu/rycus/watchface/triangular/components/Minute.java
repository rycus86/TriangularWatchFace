package hu.rycus.watchface.triangular.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.Time;

import hu.rycus.watchface.commons.Animation;
import hu.rycus.watchface.commons.Component;
import hu.rycus.watchface.triangular.util.Constants;

public class Minute extends Component {

    private static final int MIN_SIZE = 64;
    private static final int MAX_SIZE = 90;
    private static final int SIZE_DIFFERENCE = MAX_SIZE - MIN_SIZE;

    private float textLeft;
    private float textBottom;

    @Override
    protected void onSetupPaint(final Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextSize(inAmbientMode ? MAX_SIZE : MIN_SIZE);
        paint.setTypeface(Typeface.DEFAULT);
    }

    @Override
    protected void onSizeSet(final int width, final int height) {
        super.onSizeSet(width, height);

        textLeft = width / 2f + 8f;
        textBottom = Constants.Text.getBaseline(height);
    }

    @Override
    protected void onAmbientModeChanged(final boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);

        if (inAmbientMode) {
            setAnimation(createGrowAnimation());
        } else {
            setAnimation(createShrinkAnimation());
        }
    }

    @Override
    protected void onDraw(final Canvas canvas, final Time time) {
        canvas.drawText(time.format("%M"), textLeft, textBottom, paint);
    }

    private Animation createGrowAnimation() {
        return new Animation(Constants.ANIMATION_DURATION) {
            @Override
            protected void apply(final float progress) {
                paint.setTextSize(MIN_SIZE + SIZE_DIFFERENCE * progress);
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
