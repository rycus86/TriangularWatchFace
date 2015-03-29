package hu.rycus.watchface.triangular.config;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.rycus.watchface.triangular.R;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.commons.Palette;
import hu.rycus.watchface.triangular.commons.PaletteView;

public class PaletteSelectionActivity extends Activity {

    public static final String EXTRA_ITEM = "sel$item";
    public static final String EXTRA_SELECTED_INDEX = "sel$idx";
    public static final String EXTRA_RESULT = "sel$result";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wear);

        final Configuration item = (Configuration) getIntent().getSerializableExtra(EXTRA_ITEM);
        final int selectedIndex = getIntent().getIntExtra(EXTRA_SELECTED_INDEX, 0);

        final WearableListView listView = (WearableListView) findViewById(R.id.list_config);
        listView.setAdapter(new PaletteAdapter());
        listView.scrollToPosition(selectedIndex);
        listView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(final WearableListView.ViewHolder viewHolder) {
                final Palette selection = (Palette) viewHolder.itemView.getTag();
                final Intent data = new Intent();
                data.putExtra(EXTRA_ITEM, item);
                data.putExtra(EXTRA_RESULT, selection);
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void onTopEmptyRegionClick() { }
        });
    }

    private class PaletteAdapter extends WearableListView.Adapter {

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(PaletteSelectionActivity.this);
            final View itemView = inflater.inflate(R.layout.item_config_palette_wear, null);
            return new ItemHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final WearableListView.ViewHolder holder, final int position) {
            final ItemHolder itemHolder = (ItemHolder) holder;
            final Palette palette = Palette.at(position);
            itemHolder.update(palette);
        }

        @Override
        public int getItemCount() {
            return Palette.LENGTH;
        }

    }

    private class ItemHolder extends WearableListView.ViewHolder {

        private final PaletteView vPalette;

        public ItemHolder(final View itemView) {
            super(itemView);
            vPalette = (PaletteView) itemView.findViewById(R.id.v_config_palette);
        }

        public void update(final Palette palette) {
            vPalette.setPalette(palette);
            itemView.setTag(palette);
        }

    }

}
