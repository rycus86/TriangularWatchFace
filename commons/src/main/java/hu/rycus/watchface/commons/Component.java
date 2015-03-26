package hu.rycus.watchface.commons;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.format.Time;

import com.google.android.gms.wearable.DataMap;

public abstract class Component {

    protected final Paint paint = new Paint();

    protected int canvasWidth;
    protected int canvasHeight;
    protected boolean active;
    protected boolean visible;
    protected boolean isRound;
    protected boolean inAmbientMode;
    protected boolean burnInProtection;
    protected boolean lowBitAmbient;

    private BaseCanvasWatchFaceService.BaseEngine engine;
    private Animation animation;

    void onSetEngine(final BaseCanvasWatchFaceService.BaseEngine engine) {
        this.engine = engine;
    }

    protected void onCreate(final boolean visible, final boolean inAmbientMode) {
        this.visible = visible;
        this.inAmbientMode = inAmbientMode;
        this.active = isActiveByDefault();

        this.onSetupPaint(paint);
    }

    protected void onSetupPaint(final Paint paint) {
    }

    protected void onSizeSet(final int width, final int height, final boolean round) {
        this.canvasWidth = width;
        this.canvasHeight = height;
        this.isRound = round;
    }

    protected void onApplyConfiguration(final DataMap configuration) {
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

    protected void onAnimationTick() {
        if (animation != null) {
            animation.apply(animation.getProgress());
            if (animation.isFinished()) {
                // copy reference in case we'll replace it in onFinished
                final Animation animation = this.animation;
                this.animation = null;

                animation.onFinished();
            }
        }
    }

    protected void setAnimation(final Animation animation) {
        if (animation != null && this.animation != null) {
            animation.onReplacing(this.animation);
        }

        this.animation = animation;
    }

    protected boolean hasAnimation() {
        return animation != null;
    }

    protected boolean shouldInvalidate() {
        return hasAnimation();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    protected boolean isActiveByDefault() {
        return true;
    }

    protected boolean needsHandler() {
        return false;
    }

    protected void schedule(final int what, final long interval) {
        engine.startHandlerSchedule(what, interval);
    }

    protected void onHandleMessage(final int what) { }

}
