package hu.rycus.watchface.triangular.config;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;

import hu.rycus.watchface.commons.config.ConfigurationHelper;
import hu.rycus.watchface.triangular.R;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.commons.Palette;
import hu.rycus.watchface.triangular.commons.PaletteView;

public class ConfigurationAdapter extends WearableListView.Adapter
        implements
            WearableListView.ClickListener,
            ConfigurationHelper.OnConfigurationDataReadCallback {

    private final Context context;
    private final OnGroupSelectedListener onGroupSelectedListener;
    private final OnPaletteParentSelectedListener onPaletteParentSelectedListener;
    private final LayoutInflater inflater;
    private final GoogleApiClient apiClient;

    private DataMap configuration;
    private DataMap pendingConfiguration;

    public ConfigurationAdapter(final Context context,
                                final OnGroupSelectedListener onGroupSelectedListener,
                                final OnPaletteParentSelectedListener onPaletteParentSelectedListener,
                                final GoogleApiClient apiClient) {
        this.context = context;
        this.onGroupSelectedListener = onGroupSelectedListener;
        this.onPaletteParentSelectedListener = onPaletteParentSelectedListener;
        this.inflater = LayoutInflater.from(context);
        this.apiClient = apiClient;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View itemView = inflater.inflate(R.layout.item_config_wear, null);
        return new ConfigurationViewHolder(itemView);
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
        if (pendingConfiguration != null) {
            this.configuration.putAll(pendingConfiguration);
            ConfigurationHelper.storeConfiguration(
                    apiClient, Configuration.PATH, this.configuration);
        }
        notifyDataSetChanged();
    }

    public void onGroupSelectionResult(final Configuration group, final Configuration selection) {
        if (apiClient.isConnected() && configuration != null) {
            configuration.putString(group.getKey(), selection.getKey());
            ConfigurationHelper.storeConfiguration(
                    apiClient, Configuration.PATH, configuration);
            notifyDataSetChanged();
        } else {
            final DataMap pending = new DataMap();
            pending.putString(group.getKey(), selection.getKey());
            pendingConfiguration = pending;
        }
    }

    public void onPaletteSelectionResult(final Configuration item, final Palette selection) {
        if (apiClient.isConnected() && configuration != null) {
            configuration.putString(item.getKey(), selection.name());
            ConfigurationHelper.storeConfiguration(
                    apiClient, Configuration.PATH, configuration);
            notifyDataSetChanged();
        } else {
            final DataMap pending = new DataMap();
            pending.putString(item.getKey(), selection.name());
            pendingConfiguration = pending;
        }
    }

    @Override
    public void onClick(final WearableListView.ViewHolder viewHolder) {
        final int position = (Integer) viewHolder.itemView.getTag();
        final Configuration item = Configuration.at(position);
        if (!item.isAvailable(configuration)) {
            return;
        }

        if (item.getType().equals(Configuration.Type.Group)) {
            final Configuration current = item.getGroupSelection(configuration);
            onGroupSelectedListener.onGroupSelected(item, current);
        } else if (item.getType().equals(Configuration.Type.Palette)) {
            final Palette current = item.getPalette(configuration);
            onPaletteParentSelectedListener.onPaletteParentSelected(item, current);
        }
    }

    @Override
    public void onTopEmptyRegionClick() { }

    private class ConfigurationViewHolder extends WearableListView.ViewHolder {

        private final CompoundButton btnBinary;
        private final TextView txtTitle;
        private final TextView txtDescription;
        private final TextView txtSingleTitle;
        private final PaletteView vPalette;

        private boolean listenForEvents = true;

        public ConfigurationViewHolder(final View itemView) {
            super(itemView);
            btnBinary = find(itemView, R.id.btn_config_binary);
            btnBinary.setOnCheckedChangeListener(createButtonListener());
            txtTitle = find(itemView, R.id.txt_config_title);
            txtDescription = find(itemView, R.id.txt_config_description);
            txtSingleTitle = find(itemView, R.id.txt_config_single_title);
            vPalette = find(itemView, R.id.v_config_palette);
        }

        @SuppressWarnings("unchecked")
        private <T extends View> T find(final View itemView, final int id) {
            return (T) itemView.findViewById(id);
        }

        void update(final Configuration item) {
            listenForEvents = false;
            try {
                onUpdate(item);
            } finally {
                listenForEvents = true;
            }
        }

        void onUpdate(final Configuration item) {
            if (Configuration.Type.Binary.equals(item.getType())) {
                btnBinary.setVisibility(View.VISIBLE);
                btnBinary.setText(item.getString(context));
                btnBinary.setChecked(item.getBoolean(configuration));
                btnBinary.setEnabled(item.isAvailable(configuration));
                txtTitle.setVisibility(View.GONE);
                txtDescription.setVisibility(View.GONE);
                txtSingleTitle.setVisibility(View.GONE);
                vPalette.setVisibility(View.GONE);
            } else if (Configuration.Type.Group.equals(item.getType())) {
                final boolean enabled = item.isAvailable(configuration);
                final Configuration selected = item.getGroupSelection(configuration);
                btnBinary.setVisibility(View.GONE);
                txtTitle.setVisibility(View.VISIBLE);
                txtTitle.setText(item.getString(context));
                txtTitle.setEnabled(enabled);
                txtDescription.setVisibility(View.VISIBLE);
                txtDescription.setText(selected.getString(context));
                txtDescription.setEnabled(enabled);
                txtSingleTitle.setVisibility(View.GONE);
                vPalette.setVisibility(View.GONE);
            } else if (Configuration.Type.Palette.equals(item.getType())) {
                final Palette selected = item.getPalette(configuration);
                btnBinary.setVisibility(View.GONE);
                txtTitle.setVisibility(View.GONE);
                txtDescription.setVisibility(View.GONE);
                txtSingleTitle.setVisibility(View.VISIBLE);
                txtSingleTitle.setText(item.getString(context));
                txtSingleTitle.setEnabled(item.isAvailable(configuration));
                vPalette.setVisibility(View.VISIBLE);
                vPalette.setPalette(selected);
            }
        }

        private CompoundButton.OnCheckedChangeListener createButtonListener() {
            return new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    if (listenForEvents && apiClient.isConnected() && configuration != null) {
                        final int position = (Integer) itemView.getTag();
                        final Configuration item = Configuration.at(position);
                        configuration.putBoolean(item.getKey(), isChecked);
                        ConfigurationHelper.storeConfiguration(
                                apiClient, Configuration.PATH, configuration);
                        notifyDataSetChanged();
                    }
                }
            };
        }

    }

    public interface OnGroupSelectedListener {

        public void onGroupSelected(Configuration group, Configuration current);

    }

    public interface OnPaletteParentSelectedListener {

        public void onPaletteParentSelected(Configuration item, Palette current);

    }

}
