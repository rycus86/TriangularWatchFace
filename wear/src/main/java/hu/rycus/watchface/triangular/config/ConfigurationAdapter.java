package hu.rycus.watchface.triangular.config;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;

import hu.rycus.watchface.commons.config.ConfigurationHelper;
import hu.rycus.watchface.triangular.R;
import hu.rycus.watchface.triangular.commons.Configuration;

public class ConfigurationAdapter extends WearableListView.Adapter
        implements ConfigurationHelper.OnConfigurationDataReadCallback {

    private final Context context;
    private final LayoutInflater inflater;
    private final GoogleApiClient apiClient;

    private DataMap configuration;

    public ConfigurationAdapter(final Context context, final GoogleApiClient apiClient) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.apiClient = apiClient;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ConfigurationViewHolder(inflater.inflate(R.layout.item_config_wear, null));
    }

    @Override
    public void onBindViewHolder(final WearableListView.ViewHolder holder, final int position) {
        final ConfigurationViewHolder viewHolder = (ConfigurationViewHolder) holder;
        final Configuration configuration = Configuration.at(position);
        viewHolder.update(configuration);
        viewHolder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return Configuration.count();
    }

    @Override
    public void onConfigurationDataRead(final DataMap configuration) {
        this.configuration = configuration;
        notifyDataSetChanged();
    }

    private class ConfigurationViewHolder extends WearableListView.ViewHolder {

        private final CompoundButton button;

        public ConfigurationViewHolder(final View itemView) {
            super(itemView);
            button = (CompoundButton) itemView.findViewById(R.id.btn_config_item);
            button.setOnCheckedChangeListener(createButtonListener());
        }

        void update(final Configuration item) {
            if (Configuration.Type.Binary.equals(item.getType())) {
                button.setText(item.getString(context));
                button.setChecked(item.getBoolean(configuration));
            }
        }

        private CompoundButton.OnCheckedChangeListener createButtonListener() {
            return new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    if (apiClient.isConnected() && configuration != null) {
                        final int position = (Integer) itemView.getTag();
                        final Configuration item = Configuration.at(position);
                        configuration.putBoolean(item.getKey(), isChecked);
                        ConfigurationHelper.storeConfiguration(
                                apiClient, Configuration.PATH, configuration);
                    }
                }
            };
        }

    }

}
