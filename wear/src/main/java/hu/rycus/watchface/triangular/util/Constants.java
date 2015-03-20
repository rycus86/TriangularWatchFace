package hu.rycus.watchface.triangular.util;

public interface Constants {

    public static final long ANIMATION_DURATION = 300;
    public static final long LONG_ANIMATION_DURATION = 750;

    public static class Text {

        private static final float BASELINE_FACTOR_RECTANGULAR = 0.55f;
        private static final float BASELINE_FACTOR_ROUND = 0.435f;

        public static float getBaseline(final float height, final boolean round) {
            return height * (round ? BASELINE_FACTOR_ROUND : BASELINE_FACTOR_RECTANGULAR);
        }

    }

}
