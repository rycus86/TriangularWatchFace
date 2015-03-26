package hu.rycus.watchface.triangular.components;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.format.Time;

import com.google.android.gms.wearable.DataMap;

import java.util.Arrays;
import java.util.Random;

import hu.rycus.watchface.commons.Animation;
import hu.rycus.watchface.commons.NonAmbientBackground;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.util.Constants;

public class AnimatedBackground extends NonAmbientBackground {

    private static final int N_HORIZONTAL = 6;
    private static final int N_VERTICAL = 6;

    private final int[] colors = { 0xFF388E3C, 0xFFC2185B, 0xFF757575 };
    private final int[] opacity = new int[N_HORIZONTAL * N_VERTICAL + N_HORIZONTAL / 2];
    private final Random random = new Random();
    private final Path path = new Path();
    private final Path oddPath = new Path();
    private final Path oddPathTransformed = new Path();
    private final Matrix oddMatrix = new Matrix();
    private float cxOdd;
    private float cyOdd;
    private int oddPosition;
    private int oddAlpha;
    private boolean pulse = Configuration.PULSE_ODD_TRIANGLE.getBoolean(null);

    private float w;
    private float h;

    public AnimatedBackground() {
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    protected boolean isActiveByDefault() {
        return Configuration.ANIMATED_BACKGROUND.getBoolean(null);
    }

    @Override
    protected boolean needsHandler() {
        return true;
    }

    @Override
    protected void onSizeSet(final int width, final int height, final boolean round) {
        super.onSizeSet(width, height, round);

        w = (float) width / (float) N_HORIZONTAL;
        h = (float) height / (float) N_VERTICAL;

        path.moveTo(0, 0);
        path.rLineTo(w, 0);
        path.rLineTo(-w / 2f, h);
        path.close();

        oddPosition = round ? 4 : 5;
        oddPath.set(path);
        oddPath.offset(oddPosition * w, h);

        cxOdd = w / 2f + (oddPosition * w);
        cyOdd = h / 2f + h;

        resetState();
    }

    @Override
    protected void onApplyConfiguration(final DataMap configuration) {
        setActive(Configuration.ANIMATED_BACKGROUND.getBoolean(configuration));

        pulse = Configuration.PULSE_ODD_TRIANGLE.getBoolean(configuration);
        schedulePulseIfNeeded();
    }

    @Override
    protected void onHandleMessage(final int what) {
        if (Constants.HandlerMessage.PER_SECOND == what) {
            if (active && visible && !inAmbientMode && pulse && !hasAnimation()) {
                setAnimation(createPulseAnimation());
            }
        }
    }

    @Override
    protected void onDrawBackground(final Canvas canvas, final Time time) {
        paint.setColor(0xFF121212);

        canvas.drawRect(0, 0, canvasWidth, canvasHeight, paint);

        path.reset();
        path.rLineTo(w, 0);
        path.rLineTo(-w / 2f, h);
        path.close();

        int colorIndex = 0;

        for (int yi = 0; yi < N_VERTICAL; yi++) {
            int n = N_HORIZONTAL;
            if (yi % 2 == 0) {
                n = N_HORIZONTAL + 1;
                colorIndex = 0;
                path.offset(-w / 2f, 0); // shift back
            } else {
                colorIndex++;
            }

            for (int xi = 0; xi < n; xi++) {
                if (xi == oddPosition && yi == 1) {
                    // will add an odd color later
                    colorIndex++;
                } else {
                    final int color = colors[colorIndex++ % 3];

                    paint.setColor(color);
                    paint.setAlpha(opacity[xi + yi * N_HORIZONTAL]);

                    canvas.drawPath(path, paint);
                }

                path.offset(w, 0); // shift
            }

            path.offset(-canvasWidth, h); // new line

            if (yi % 2 == 0) {
                path.offset(-w / 2f, 0); // shift back
            }
        }

        // add an odd color
        paint.setColor(0xFFFF5722);
        paint.setAlpha(oddAlpha);
        canvas.drawPath(oddPathTransformed, paint);
    }

    @Override
    protected void onAmbientModeChanged(final boolean inAmbientMode) {
        super.onAmbientModeChanged(inAmbientMode);

        if (!active) {
            return;
        }

        if (inAmbientMode) {
            setAnimation(null);
            resetState();
        } else {
            final int count = N_HORIZONTAL * N_VERTICAL + N_VERTICAL / 2;
            final float[] startDelays = new float[count];

            for (int idx = 0; idx < count; idx++) {
                startDelays[idx] = random.nextFloat() * 0.5f;
            }

            setAnimation(createFadeInAnimation(startDelays));
            schedulePulseIfNeeded();
        }
    }

    private Animation createFadeInAnimation(final float[] startDelays) {
        final int count = startDelays.length;
        return new Animation(Constants.LONG_ANIMATION_DURATION) {
            @Override
            protected void apply(final float progress) {
                updateOddTriangleScaleForAnimationProgress(progress);
                oddAlpha = (int) (0xFF * progress * progress);

                for (int idx = 0; idx < count; idx++) {
                    float alpha = Math.min(Math.max(progress - startDelays[idx], 0f), 0.5f) * 2f;
                    opacity[idx] = (int) (0xFF * alpha);
                }
            }
            @Override
            protected void onFinished() {
                resetState();
            }
        };
    }

    private Animation createPulseAnimation() {
        return new Animation(Constants.ANIMATION_DURATION) {
            @Override
            protected void apply(final float progress) {
                if (progress < 0.5f) {
                    updateOddTriangleScaleForAnimationProgress(1f - (progress * 2f));
                } else {
                    updateOddTriangleScaleForAnimationProgress((progress - 0.5f) * 2f);
                }
            }
        };
    }

    private void updateOddTriangleScaleForAnimationProgress(final float progress) {
        final float scale = 1.25f - (progress * progress * 0.25f);
        oddMatrix.setScale(scale, scale, cxOdd, cyOdd);
        oddPath.transform(oddMatrix, oddPathTransformed);
    }

    private void resetState() {
        oddPathTransformed.set(oddPath);
        oddAlpha = 0xFF;
        Arrays.fill(opacity, 0xFF);
    }

    private void schedulePulseIfNeeded() {
        if (pulse) {
            schedule(Constants.HandlerMessage.PER_SECOND, 1000L);
        }
    }

}
