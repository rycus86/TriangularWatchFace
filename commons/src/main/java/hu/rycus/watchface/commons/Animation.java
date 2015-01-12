package hu.rycus.watchface.commons;

public abstract class Animation {

    private long start;
    private long duration;

    public Animation(final long duration) {
        this(System.nanoTime(), duration);
    }

    protected Animation(final long start, final long duration) {
        this.start = start;
        this.duration = duration;
    }

    protected abstract void apply(final float progress);

    protected void onFinished() {
        apply(1f);
    }

    protected void onReplacing(final Animation previous) {
        duration = (long) (duration * previous.getProgress());
    }

    protected float getProgress() {
        final long now = System.nanoTime();
        if (isFinished(now)) {
            return 1f;
        } else {
            final long elapsed = (now - start) / 1000000;
            return (float) elapsed / (float) duration;
        }
    }

    protected boolean isFinished() {
        return isFinished(System.nanoTime());
    }

    private boolean isFinished(final long now) {
        return (now - start) / 1000000 >= duration;
    }

}
