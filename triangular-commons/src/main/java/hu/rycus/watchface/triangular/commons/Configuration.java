package hu.rycus.watchface.triangular.commons;

import android.content.Context;

import com.google.android.gms.wearable.DataMap;

public enum Configuration {

    SHOW_24_HOURS(Type.Binary, "24hours", true, R.string.config_24_hours),
    ANIMATED_BACKGROUND(Type.Binary, "animbg", true, R.string.config_animated_background);

    public enum Type {
        Binary
    }

    public static final String PATH = "/triangular";

    private static final int LENGTH = Configuration.values().length;

    private final Type type;
    private final String key;
    private final Object defaultValue;
    private final int stringResource;

    Configuration(final Type type, final String key, final Object defaultValue,
                  final int stringResource) {
        this.type = type;
        this.key = key;
        this.defaultValue = defaultValue;
        this.stringResource = stringResource;
    }

    public Type getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Object getValue(final DataMap configuration) {
        if (type.equals(Type.Binary)) {
            return getBoolean(configuration);
        } else {
            return defaultValue;
        }
    }

    public boolean getBoolean(final DataMap configuration) {
        if (configuration != null) {
            return configuration.getBoolean(key, (Boolean) defaultValue);
        } else {
            return (Boolean) defaultValue;
        }
    }

    public String getString(final Context context) {
        return context.getString(stringResource);
    }

    public static Configuration at(final int index) {
        final Configuration[] values = Configuration.values();
        if (values.length > index) {
            return values[index];
        } else {
            return null;
        }
    }

    public static int count() {
        return LENGTH;
    }

}
