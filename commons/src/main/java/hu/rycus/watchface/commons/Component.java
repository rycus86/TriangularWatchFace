package hu.rycus.watchface.commons;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Component {

    protected final Paint paint = new Paint();

    protected int canvasWidth;
    protected int canvasHeight;
    protected boolean visible;
    protected boolean isRound;
    protected boolean inAmbientMode;
    protected boolean burnInProtection;
    protected boolean lowBitAmbient;

    private final AtomicReference<Animation> animation = new AtomicReference<>();
    private Thread animationThread;

    protected void onCreate(final boolean visible, final boolean inAmbientMode) {
        this.visible = visible;
        this.inAmbientMode = inAmbientMode;

        this.onSetupPaint(paint);
    }

    protected void onSetupPaint(final Paint paint) {
    }

    protected void onSizeSet(final int width, final int height, final boolean round) {
        this.canvasWidth = width;
        this.canvasHeight = height;
        this.isRound = round;
    }

    protected void onAmbientModeChanged(final boolean inAmbientMode) {
        this.inAmbientMode = inAmbientMode;
    }

    protected void onPropertiesChanged(final boolean burnInProtection, final boolean lowBitAmbient) {
        this.burnInProtection = burnInProtection;
        this.lowBitAmbient = lowBitAmbient;
    }

    protected void onVisibilityChanged(final boolean visible) {
        this.visible = visible;
    }

    protected void onDraw(final Canvas canvas, final Time time) { }

    protected void onAnimationTick(final Animation animation) {
        if (animation != null) {
            animation.apply(animation.getProgress());
            if (animation.isFinished()) {
                animation.onFinished();
                unsetAnimation(animation);
            }
        }
    }

    private void unsetAnimation(final Animation animation) {
        this.animation.compareAndSet(animation, null);
    }

    private void replaceAnimation(final Animation animation) {
        final Animation oldAnimation = this.animation.getAndSet(animation);
        if (animation != null && oldAnimation != null) {
            animation.onReplacing(oldAnimation);
        }
    }

    protected void setAnimation(final Animation animation) {
        stopAnimationThread();
        replaceAnimation(animation);
        startAnimationThread(animation);
    }

    private void stopAnimationThread() {
        if (animationThread != null) {
            animationThread.interrupt();
            animationThread = null;
        }
    }

    private void startAnimationThread(final Animation newAnimation) {
        if (newAnimation != null) {
            onAnimationTick(newAnimation);

            animationThread = new Thread() {
                final int TARGET_LOOP_TIME = 1000 / 60;

                @Override
                public void run() {
                    long last = SystemClock.elapsedRealtime();

                    try {
                        Animation animation;
                        do {
                            animation = getAnimation();
                            onAnimationTick(animation);

                            if (animation != null) {
                                final long current = SystemClock.elapsedRealtime();
                                final long elapsed = current - last;
                                last = current;

                                delayAnimation(TARGET_LOOP_TIME - elapsed);
                            }
                        } while (animation != null);
                    } finally {
                        animationThread = null;
                    }
                }
            };
            animationThread.start();
        }
    }

    private Animation getAnimation() {
        return animation.get();
    }

    private void delayAnimation(final long millis) {
        if (millis > 0L) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {
                Log.d("Animation", "Sleep interrupted for " + getClass(), ex);
            }
        }
    }

    protected boolean shouldInvalidate() {
        return animation.get() != null;
    }

}
