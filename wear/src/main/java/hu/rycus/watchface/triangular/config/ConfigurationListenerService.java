package hu.rycus.watchface.triangular.config;

import hu.rycus.watchface.commons.config.WearableConfigurationListener;
import hu.rycus.watchface.triangular.util.Constants;

public class ConfigurationListenerService extends WearableConfigurationListener {

    @Override
    protected String[] getConfigurationPaths() {
        return new String[] {Constants.Configuration.PATH};
    }

}
