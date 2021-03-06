package hu.rycus.watchface.triangular.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.format.Time;

import com.google.android.gms.wearable.DataMap;

import hu.rycus.watchface.commons.Animation;
import hu.rycus.watchface.commons.BatteryListenerComponent;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.commons.Palette;
import hu.rycus.watchface.triangular.util.Constants;

public class Battery extends BatteryListenerComponent {

    private static final int TEXT_GAP = 44;
    private static final float ANIMATION_TRACK_LENGTH = 24f;

    private final Rect textBounds = new Rect();
    private final RectF iconBounds = new RectF();
    private final RectF iconTopBounds = new RectF();
    private final RectF iconBorderBounds = new RectF();
    private final RectF iconLevelBounds = new RectF();
    private final RectF iconBodyBounds = new RectF();

    private float textBottom;
    private float textRight;
    private float textLeft;

    private Palette palette = Palette.getDefault();

    private float animationOffset;

    private int alpha = 0xFF;

    private String text;

    @Override
    protected void onCreate(final boolean visible, final boolean inAmbientMode) {
        super.onCreate(visible, inAmbientMode);
        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());
    }

    @Override
    protected void onSetupPaint(final Paint paint) {
        paint.setAntiAlias(true);
        paint.setTextSize(18f);
    }

    @Override
    protected void onSizeSet(final int width, final int height, final boolean round) {
        super.onSizeSet(width, height, round);

        textBottom = Constants.Text.getBaseline(height, round) + TEXT_GAP;
        textRight = width / 2f - 4f;

        iconBounds.bottom = textBottom;
        iconBounds.top = textBottom - paint.getTextSize() * 0.9f;
        iconBounds.right = textRight - 4f;
        iconBounds.left = iconBounds.right - iconBounds.height() * 0.65f;
    }

    @Override
    protected void onAmbientModeChanged(final boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);

        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());

        if (inAmbientMode) {
            setAnimation(createHideAnimation());
        } else {
            setAnimation(createShowAnimation());
        }
    }

    @Override
    protected void onApplyConfiguration(final DataMap configuration) {
        super.onApplyConfiguration(configuration);

        palette = Configuration.COLOR_PALETTE.getPalette(configuration);
        paint.setColor(inAmbientMode ? Color.WHITE : palette.text());
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

        if (animationOffset > 0) {
            canvas.translate(-animationOffset, 0);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(palette.text());
        paint.setAlpha(alpha);

        canvas.drawText(text, textLeft, textBottom, paint);

        if (!(inAmbientMode && lowBitAmbient)) {
            paint.setColor(palette.background());
            paint.setAlpha(alpha);

            canvas.drawRect(iconBodyBounds, paint);

            paint.setColor(palette.text());
            paint.setAlpha(alpha);
        }

        canvas.drawRect(iconTopBounds, paint);
        canvas.drawRect(iconLevelBounds, paint);

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(iconBorderBounds, paint);

        if (animationOffset > 0) {
            canvas.translate(animationOffset, 0);
        }
    }

    private Animation createHideAnimation() {
        return new Animation(Constants.ANIMATION_DURATION) {
            @Override
            protected void apply(final float progress) {
                alpha = (int) (0xFF * (1f - progress));
                animationOffset = ANIMATION_TRACK_LENGTH * progress;
            }
        };
    }

    private Animation createShowAnimation() {
        return new Animation(Constants.ANIMATION_DURATION) {
            @Override
            protected void apply(final float progress) {
                alpha = (int) (0xFF * progress);
                animationOffset = ANIMATION_TRACK_LENGTH * (1f - progress);
            }
        };
    }

}
