package hu.rycus.watchface.triangular.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.text.format.Time;

import com.google.android.gms.wearable.DataMap;

import hu.rycus.watchface.commons.Animation;
import hu.rycus.watchface.commons.Component;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.util.Constants;

public class Second extends Component {

    private static final int TEXT_HEIGHT = 32;

    private final Time ownTime = new Time();

    private float textLeft;
    private float textBottom;
    private float textWidth;

    private String previous;
    private String current;

    private final PointF previousOffset = new PointF(0, 0);
    private final PointF currentOffset = new PointF(0, 0);
    private int previousAlpha;
    private int currentAlpha;

    private Configuration directionConfiguration = Configuration.DIR_SECONDS.getGroupSelection(null);

    private enum Direction { UP, RIGHT, DOWN, LEFT }

    @Override
    protected boolean isActiveByDefault() {
        return Configuration.SHOW_SECONDS.getBoolean(null);
    }

    @Override
    protected boolean needsHandler() {
        return true;
    }

    @Override
    protected void onCreate(final boolean visible, final boolean inAmbientMode) {
        super.onCreate(visible, inAmbientMode);

        current = "00";
        previous = "00";
        previousAlpha = 0;
        currentAlpha = 0;

        updateTime();
        setAnimation(createFadeInAnimation());
    }

    @Override
    protected void onSetupPaint(final Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextSize(TEXT_HEIGHT);
        paint.setTypeface(Typeface.DEFAULT);

        textWidth = paint.measureText("59");
    }

    @Override
    protected void onSizeSet(final int width, final int height, final boolean round) {
        super.onSizeSet(width, height, round);

        textLeft = width * 3f / 4f + (round ? 8f : 12f);
        textBottom = Constants.Text.getBaseline(height, round);
    }

    @Override
    protected void onAmbientModeChanged(final boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);
        updateTime();

        if (!inAmbientMode) {
            setAnimation(createFadeInAnimation());
            schedule(Constants.HandlerMessage.PER_SECOND, 1000L);
        } else if (!hasAnimation()) {
            setAnimation(createFadeOutAnimation());
        }
    }

    @Override
    protected void onVisibilityChanged(final boolean visible) {
        super.onVisibilityChanged(visible);
        updateTime();
    }

    @Override
    protected void onApplyConfiguration(final DataMap configuration) {
        super.onApplyConfiguration(configuration);
        setActive(Configuration.SHOW_SECONDS.getBoolean(configuration));

        directionConfiguration = Configuration.DIR_SECONDS.getGroupSelection(configuration);

        if (isActive()) {
            updateTime();
            schedule(Constants.HandlerMessage.PER_SECOND, 1000L);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas, final Time time) {
        if (previousAlpha > 0) {
            final float x = textLeft + previousOffset.x;
            final float y = textBottom + previousOffset.y;

            paint.setAlpha(previousAlpha);
            canvas.drawText(previous, x, y, paint);
        }

        final float x = textLeft + currentOffset.x;
        final float y = textBottom + currentOffset.y;

        paint.setAlpha(currentAlpha);
        canvas.drawText(current, x, y, paint);
    }

    @Override
    protected void onHandleMessage(final int what) {
        if (Constants.HandlerMessage.PER_SECOND == what) {
            updateTime();

            if (!hasAnimation()) {
                setAnimation(createChangeAnimation());
            }
        }
    }

    private void updateTime() {
        ownTime.setToNow();

        final String last = current;
        current = ownTime.format("%S");

        if (!current.equals(last)) {
            previous = last;
        }
    }

    private Animation createChangeAnimation() {
        final Direction direction;
        switch (directionConfiguration) {
            case DIR_SECONDS_DOWN:
                direction = Direction.DOWN;
                break;
            case DIR_SECONDS_UP:
                direction = Direction.UP;
                break;
            case DIR_SECONDS_LEFT:
                direction = Direction.LEFT;
                break;
            case DIR_SECONDS_RIGHT:
                direction = Direction.RIGHT;
                break;
            case DIR_SECONDS_ALL:
                final long seconds = System.currentTimeMillis() / 1000L;
                final int index = (int) ((seconds / 2) % 4);
                direction = Direction.values()[index];
                break;
            default:
                direction = Direction.DOWN;
                break;
        }

        return new Animation(Constants.ANIMATION_DURATION) {
            @Override
            protected void apply(final float progress) {
                previousAlpha = (int) (0xFF - 0xFF * progress * progress);
                currentAlpha = (int) (0xFF * progress * progress);

                switch (direction) {
                    case UP:
                        previousOffset.set(0, -TEXT_HEIGHT * progress);
                        currentOffset.set(0, TEXT_HEIGHT - TEXT_HEIGHT * progress);
                        break;
                    case DOWN:
                        previousOffset.set(0, TEXT_HEIGHT * progress);
                        currentOffset.set(0, -TEXT_HEIGHT + TEXT_HEIGHT * progress);
                        break;
                    case LEFT:
                        previousOffset.set(-12f * progress, 0);
                        currentOffset.set(textWidth - textWidth * progress, 0);
                        break;
                    case RIGHT:
                        previousOffset.set(textWidth * progress, 0);
                        currentOffset.set(-12f * (1f - progress), 0);
                        break;
                }
            }

            @Override
            protected void onFinished() {
                if (inAmbientMode) {
                    setAnimation(createFadeOutAnimation());
                }
            }
        };
    }

    private Animation createFadeInAnimation() {
        return new Animation(2 * Constants.ANIMATION_DURATION) {
            @Override
            protected void apply(final float progress) {
                final float delayedProgress = Math.max(0, (progress - 0.5f) * 2f);
                currentAlpha = (int) (0xFF * delayedProgress);
            }
        };
    }

    private Animation createFadeOutAnimation() {
        return new Animation(Constants.ANIMATION_DURATION) {
            @Override
            protected void apply(final float progress) {
                currentAlpha = (int) (0xFF * (1f - progress));
            }
        };
    }

}
