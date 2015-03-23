package hu.rycus.watchface.triangular.config;

import hu.rycus.watchface.commons.config.WearableConfigurationListener;
import hu.rycus.watchface.triangular.commons.Configuration;

public class ConfigurationListenerService extends WearableConfigurationListener {

    @Override
    protected String[] getConfigurationPaths() {
        return new String[] {Configuration.PATH};
    }

}
