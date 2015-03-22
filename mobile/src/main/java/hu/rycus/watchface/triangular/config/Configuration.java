package hu.rycus.watchface.triangular.config;

import com.google.android.gms.wearable.DataMap;

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
