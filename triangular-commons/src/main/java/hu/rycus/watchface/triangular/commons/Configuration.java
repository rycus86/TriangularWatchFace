package hu.rycus.watchface.triangular.commons;

import android.content.Context;

import com.google.android.gms.wearable.DataMap;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public enum Configuration {

    SHOW_24_HOURS(binary()
            .key("24hours")
            .defaultValue(true)
            .stringResource(R.string.config_24_hours)),

    SHOW_SECONDS(binary()
            .key("seconds")
            .defaultValue(true)
            .stringResource(R.string.config_seconds)),

    DIR_SECONDS(group()
            .key("dir_seconds")
            .defaultValue("dir_seconds_down")
            .stringResource(R.string.config_dir_seconds)
            .dependencies(SHOW_SECONDS)),

    DIR_SECONDS_DOWN(choice(DIR_SECONDS)
            .key("dir_seconds_down")
            .defaultValue(true)
            .stringResource(R.string.config_dir_seconds_down)),

    DIR_SECONDS_UP(choice(DIR_SECONDS)
            .key("dir_seconds_up")
            .defaultValue(false)
            .stringResource(R.string.config_dir_seconds_up)),

    DIR_SECONDS_RIGHT(choice(DIR_SECONDS)
            .key("dir_seconds_right")
            .defaultValue(false)
            .stringResource(R.string.config_dir_seconds_right)),

    DIR_SECONDS_LEFT(choice(DIR_SECONDS)
            .key("dir_seconds_left")
            .defaultValue(false)
            .stringResource(R.string.config_dir_seconds_left)),

    DIR_SECONDS_ALL(choice(DIR_SECONDS)
            .key("dir_seconds_all")
            .defaultValue(false)
            .stringResource(R.string.config_dir_seconds_all)),

    ANIMATED_BACKGROUND(binary()
            .key("animbg")
            .defaultValue(true)
            .stringResource(R.string.config_animated_background)),

    PULSE_ODD_TRIANGLE(binary()
            .key("pulse")
            .defaultValue(true)
            .stringResource(R.string.config_pulse)
            .dependencies(ANIMATED_BACKGROUND));

    public enum Type {
        Binary, Group, Choice
    }

    public static final String PATH = "/triangular";

    private static final Configuration[] SELECTABLE;
    static {
        final List<Configuration> items = new LinkedList<>();
        for (final Configuration configuration : Configuration.values()) {
            if (Arrays.asList(Type.Binary, Type.Group).contains(configuration.getType())) {
                items.add(configuration);
            }
        }
        SELECTABLE = items.toArray(new Configuration[items.size()]);
    }

    private static final int LENGTH = SELECTABLE.length;

    private final Type type;
    private final String key;
    private final Object defaultValue;
    private final int stringResource;
    private final List<Configuration> dependencies;

    Configuration(final ConfigurationBuilder<?> builder) {
        assert builder.type != null;
        assert builder.key != null;
        assert builder.defaultValue != null;

        this.type = builder.type;
        this.key = builder.key;
        this.defaultValue = builder.defaultValue;
        this.stringResource = builder.stringResource;
        this.dependencies = Arrays.asList(builder.dependencies);
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

    public Configuration getGroupSelection(final DataMap configuration) {
        final String selected = getStringValue(configuration);
        for (final Configuration child : Configuration.values()) {
            if (child.getKey().equals(selected) && child.dependencies.contains(this)) {
                return child;
            }
        }

        return null;
    }

    public List<Configuration> getGroupValues() {
        final List<Configuration> items = new LinkedList<>();
        for (final Configuration child : Configuration.values()) {
            if (child.dependencies.contains(this)) {
                items.add(child);
            }
        }
        return items;
    }

    private String getStringValue(final DataMap configuration) {
        if (configuration != null) {
            return configuration.getString(key, (String) defaultValue);
        } else {
            return (String) defaultValue;
        }
    }

    public String getString(final Context context) {
        return context.getString(stringResource);
    }

    public boolean isAvailable(final DataMap configuration) {
        for (final Configuration parent : dependencies) {
            if (!parent.getBoolean(configuration)) {
                return false;
            }
        }

        return true;
    }

    public static Configuration at(final int index) {
        if (SELECTABLE.length > index) {
            return SELECTABLE[index];
        } else {
            return null;
        }
    }

    public static int count() {
        return LENGTH;
    }

    private static ConfigurationBuilder<Boolean> binary() {
        return new ConfigurationBuilder<>(Type.Binary);
    }

    private static ConfigurationBuilder<String> group() {
        return new ConfigurationBuilder<>(Type.Group);
    }

    private static ConfigurationBuilder<Boolean> choice(final Configuration group) {
        return new ConfigurationBuilder<Boolean>(Type.Choice).dependencies(group);
    }

    private static class ConfigurationBuilder<T> {

        private final Type type;
        private String key;
        private T defaultValue;
        private int stringResource;
        private Configuration[] dependencies = new Configuration[0];

        ConfigurationBuilder(final Type type) {
            this.type = type;
        }

        ConfigurationBuilder<T> key(final String key) {
            this.key = key;
            return this;
        }

        ConfigurationBuilder<T> defaultValue(final T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        ConfigurationBuilder<T> stringResource(final int stringResource) {
            this.stringResource = stringResource;
            return this;
        }

        ConfigurationBuilder<T> dependencies(final Configuration... dependencies) {
            this.dependencies = dependencies;
            return this;
        }

    }

}
