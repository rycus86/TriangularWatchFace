package hu.rycus.watchface.triangular.util;

public enum Constants { /* No members */ ;

    public static final long ANIMATION_DURATION = 300;
    public static final long LONG_ANIMATION_DURATION = 750;

    public enum Text { /* No members */ ;

        private static final float BASELINE_FACTOR_RECTANGULAR = 0.55f;
        private static final float BASELINE_FACTOR_ROUND = 0.435f;

        public static float getBaseline(final float height, final boolean round) {
            return height * (round ? BASELINE_FACTOR_ROUND : BASELINE_FACTOR_RECTANGULAR);
        }

    }

    public enum HandlerMessage { /* No members */;

        public static final int PER_SECOND = 0x01;

    }

}
