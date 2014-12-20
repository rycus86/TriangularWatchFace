package hu.rycus.watchface.triangular.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.format.Time;

import hu.rycus.watchface.commons.Animation;
import hu.rycus.watchface.commons.BatteryListenerComponent;
import hu.rycus.watchface.triangular.util.Constants;

public class Battery extends BatteryListenerComponent {

    private static final int TEXT_GAP = 44;

    private final Rect textBounds = new Rect();
    private final RectF iconBounds = new RectF();
    private final RectF iconTopBounds = new RectF();
    private final RectF iconBorderBounds = new RectF();
    private final RectF iconLevelBounds = new RectF();
    private final RectF iconBodyBounds = new RectF();

    private float textBottom;
    private float textRight;
    private float textLeft;

    private int alpha = 0xFF;

    private String text;

    @Override
    protected void onSetupPaint(final Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextSize(18f);
    }

    @Override
    protected void onSizeSet(final int width, final int height) {
        super.onSizeSet(width, height);

        textBottom = Constants.Text.getBaseline(height) + TEXT_GAP;
        textRight = width / 2f - 4f;

        iconBounds.bottom = textBottom;
        iconBounds.top = textBottom - paint.getTextSize() * 0.9f;
        iconBounds.right = textRight - 4f;
        iconBounds.left = iconBounds.right - iconBounds.height() * 0.65f;
    }

    @Override
    protected void onAmbientModeChanged(final boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);

        if (inAmbientMode) {
            setAnimation(createHideAnimation());
        } else {
            setAnimation(createShowAnimation());
        }
    }

    @Override
    protected void onBatteryLevelChanged(final int level) {
        super.onBatteryLevelChanged(level);

        text = String.format("%d%%", level);
        paint.getTextBounds(text, 0, text.length(), textBounds);

        textLeft = textRight - textBounds.width();
        iconBounds.offsetTo(textLeft - iconBounds.width() - 4f, iconBounds.top);

        iconBorderBounds.set(
                iconBounds.left, iconBounds.top + 2f,
                iconBounds.right, iconBounds.bottom);
        iconTopBounds.set(
                iconBounds.left + iconBounds.width() / 4f,
                iconBounds.top,
                iconBounds.left + iconBounds.width() * 3f / 4f,
                iconBounds.top + 2f);
        iconLevelBounds.set(
                iconBounds.left,
                iconBounds.top + 2f + (iconBounds.height() - 2f) * ((100f - level) / 100f),
                iconBounds.right, iconBounds.bottom);
        iconBodyBounds.set(
                iconBounds.left, iconBounds.top + 2f,
                iconBounds.right, iconBounds.bottom);
    }

    @Override
    protected void onDrawBattery(final Canvas canvas, final Time time) {
        if (alpha <= 0) {
            return;
        }

        paint.setStyle(Paint.Style.FILL);

        canvas.drawText(text, textLeft, textBottom, paint);

        if (!(inAmbientMode && lowBitAmbient)) {
            paint.setARGB(alpha, 0x21, 0x21, 0x21);
            canvas.drawRect(iconBodyBounds, paint);

            paint.setARGB(alpha, 0xFF, 0xFF, 0xFF);
        }

        canvas.drawRect(iconTopBounds, paint);
        canvas.drawRect(iconLevelBounds, paint);

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(iconBorderBounds, paint);
    }

    private Animation createHideAnimation() {
        return new Animation(Constants.ANIMATION_DURATION) {
            @Override
            protected void apply(final float progress) {
                alpha = (int) (0xFF * (1f - progress));
            }
        };
    }

    private Animation createShowAnimation() {
        return new Animation(Constants.ANIMATION_DURATION) {
            @Override
            protected void apply(final float progress) {
                alpha = (int) (0xFF * progress);
            }
        };
    }

}
