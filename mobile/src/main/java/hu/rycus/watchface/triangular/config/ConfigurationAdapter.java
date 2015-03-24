package hu.rycus.watchface.triangular.config;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;

import hu.rycus.watchface.commons.config.ConfigurationHelper;
import hu.rycus.watchface.triangular.R;
import hu.rycus.watchface.triangular.commons.Configuration;

public class ConfigurationAdapter extends BaseAdapter
        implements ConfigurationHelper.OnConfigurationDataReadCallback {

    private final Context context;
    private final GoogleApiClient apiClient;
    private final String peerId;

    private DataMap configuration;

    public ConfigurationAdapter(final Context context, final GoogleApiClient apiClient,
                                final String peerId) {
        this.context = context;
        this.apiClient = apiClient;
        this.peerId = peerId;
    }

    @Override
    public int getCount() {
        return Configuration.count();
    }

    @Override
    public Object getItem(final int position) {
        return Configuration.at(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view;
        final ViewHolder viewHolder;
        if (convertView != null) {
            view = convertView;
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            view = View.inflate(context, R.layout.item_config_companion, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }

        viewHolder.update(position);

        return view;
    }

    @Override
    public void onConfigurationDataRead(final DataMap configuration) {
        this.configuration = configuration;
        notifyDataSetChanged();
    }

    private class ViewHolder {

        private final CompoundButton button;
        private int position;
        private boolean listenForEvents = true;

        private ViewHolder(final View itemView) {
            this.button = (CompoundButton) itemView.findViewById(R.id.sw_config_item);
            this.button.setOnCheckedChangeListener(createButtonListener());
        }

        private void update(final int position) {
            this.position = position;
            this.listenForEvents = false;
            try {
                onUpdate();
            } finally {
                this.listenForEvents = true;
            }
        }

        private void onUpdate() {
            final Configuration item = Configuration.at(position);
            if (item.getType().equals(Configuration.Type.Binary)) {
                button.setText(item.getString(context));
                button.setChecked(item.getBoolean(configuration));
                button.setEnabled(item.isAvailable(configuration));
            }
        }

        private CompoundButton.OnCheckedChangeListener createButtonListener() {
            return new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    if (listenForEvents && peerId != null && configuration != null) {
                        final Configuration item = Configuration.at(position);
                        configuration.putBoolean(item.getKey(), isChecked);
                        ConfigurationHelper.sendConfiguration(
                                apiClient, Configuration.PATH, peerId, configuration);
                        notifyDataSetChanged();
                    }
                }
            };
        }

    }

}
