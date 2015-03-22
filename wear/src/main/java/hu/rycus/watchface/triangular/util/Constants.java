package hu.rycus.watchface.triangular.util;

import com.google.android.gms.wearable.DataMap;

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

    public enum Configuration {

        SHOW_24_HOURS("24hours", true),
        ANIMATED_BACKGROUND("animbg", true);

        public static final String PATH = "/triangular";

        private final String key;
        private final Object defaultValue;

        Configuration(final String key, final Object defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String getKey() {
            return key;
        }

        public boolean getBoolean(final DataMap configuration) {
            return configuration.getBoolean(key, (Boolean) defaultValue);
        }

    }

}
