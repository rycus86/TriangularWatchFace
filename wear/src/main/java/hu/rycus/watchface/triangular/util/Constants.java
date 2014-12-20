package hu.rycus.watchface.triangular.util;

public interface Constants {

    public static final long ANIMATION_DURATION = 300;

    public static class Text {

        private static final float BASELINE_FACTOR = 0.55f;

        public static float getBaseline(final float height) {
            return height * BASELINE_FACTOR;
        }

    }

}
