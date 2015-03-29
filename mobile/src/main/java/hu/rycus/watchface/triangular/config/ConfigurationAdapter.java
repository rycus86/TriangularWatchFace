package hu.rycus.watchface.triangular.config;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;

import java.util.List;

import hu.rycus.watchface.commons.config.ConfigurationHelper;
import hu.rycus.watchface.triangular.R;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.commons.Palette;
import hu.rycus.watchface.triangular.commons.PaletteView;

public class ConfigurationAdapter extends BaseAdapter
        implements
            AdapterView.OnItemClickListener,
            ConfigurationHelper.OnConfigurationDataReadCallback {

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

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
                            final int position, final long id) {
        final Configuration item = Configuration.at(position);
        if (!item.isAvailable(configuration)) {
            return;
        }

        if (item.getType().equals(Configuration.Type.Group)) {
            final List<Configuration> values = item.getGroupValues();

            final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    final Configuration selection = values.get(which);
                    if (peerId != null && configuration != null) {
                        configuration.putString(item.getKey(), selection.getKey());
                        ConfigurationHelper.sendConfiguration(
                                apiClient, Configuration.PATH, peerId, configuration);
                        notifyDataSetChanged();
                    }
                }
            };

            final CharSequence[] items = new CharSequence[values.size()];
            for (int index = 0; index < items.length; index++) {
                items[index] = values.get(index).getString(context);
            }

            new AlertDialog.Builder(context)
                    .setItems(items, clickListener)
                    .setTitle(item.getString(context))
                    .show();
        } else if (item.getType().equals(Configuration.Type.Palette)) {
            final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    final Palette selection = Palette.at(which);
                    if (peerId != null && configuration != null) {
                        configuration.putString(item.getKey(), selection.name());
                        ConfigurationHelper.sendConfiguration(
                                apiClient, Configuration.PATH, peerId, configuration);
                        notifyDataSetChanged();
                    }
                }
            };

            new AlertDialog.Builder(context)
                    .setAdapter(Palette.getAdapter(), clickListener)
                    .setTitle(item.getString(context))
                    .show();
        }
    }

    private class ViewHolder {

        private final CompoundButton btnBinary;
        private final TextView txtTitle;
        private final TextView txtDescription;
        private final PaletteView vPalette;

        private int position;
        private boolean listenForEvents = true;

        private ViewHolder(final View itemView) {
            this.btnBinary = find(itemView, R.id.btn_config_binary);
            this.btnBinary.setOnCheckedChangeListener(createButtonListener());
            this.txtTitle = find(itemView, R.id.txt_config_title);
            this.txtDescription = find(itemView, R.id.txt_config_description);
            this.vPalette = find(itemView, R.id.v_config_palette);
        }

        @SuppressWarnings("unchecked")
        private <T extends View> T find(final View itemView, final int id) {
            return (T) itemView.findViewById(id);
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
                btnBinary.setVisibility(View.VISIBLE);
                btnBinary.setText(item.getString(context));
                btnBinary.setChecked(item.getBoolean(configuration));
                btnBinary.setEnabled(item.isAvailable(configuration));
                txtTitle.setVisibility(View.GONE);
                txtDescription.setVisibility(View.GONE);
                vPalette.setVisibility(View.GONE);
            } else if (item.getType().equals(Configuration.Type.Group)) {
                final Configuration selected = item.getGroupSelection(configuration);
                btnBinary.setVisibility(View.GONE);
                txtTitle.setVisibility(View.VISIBLE);
                txtTitle.setText(item.getString(context));
                txtTitle.setEnabled(item.isAvailable(configuration));
                txtDescription.setVisibility(View.VISIBLE);
                txtDescription.setText(selected.getString(context));
                txtDescription.setEnabled(item.isAvailable(configuration));
                vPalette.setVisibility(View.GONE);
            } else if (item.getType().equals(Configuration.Type.Palette)) {
                final Palette selected = item.getPalette(configuration);
                btnBinary.setVisibility(View.GONE);
                txtTitle.setVisibility(View.VISIBLE);
                txtTitle.setText(item.getString(context));
                txtTitle.setEnabled(item.isAvailable(configuration));
                txtDescription.setVisibility(View.GONE);
                vPalette.setVisibility(View.VISIBLE);
                vPalette.setEnabled(item.isAvailable(configuration));
                vPalette.setPalette(selected);
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
