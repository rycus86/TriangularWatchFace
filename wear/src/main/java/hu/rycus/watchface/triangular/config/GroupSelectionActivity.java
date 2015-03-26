package hu.rycus.watchface.triangular.config;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import hu.rycus.watchface.triangular.R;
import hu.rycus.watchface.triangular.commons.Configuration;

public class GroupSelectionActivity extends Activity {

    public static final String EXTRA_GROUP = "sel$group";
    public static final String EXTRA_SELECTED_INDEX = "sel$idx";
    public static final String EXTRA_RESULT = "sel$result";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wear);

        final Configuration group = (Configuration) getIntent().getSerializableExtra(EXTRA_GROUP);
        final int selectedIndex = getIntent().getIntExtra(EXTRA_SELECTED_INDEX, 0);

        final WearableListView listView = (WearableListView) findViewById(R.id.list_config);
        listView.setAdapter(new SelectionAdapter(group.getGroupValues()));
        listView.scrollToPosition(selectedIndex);
        listView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(final WearableListView.ViewHolder viewHolder) {
                final Configuration selection = (Configuration) viewHolder.itemView.getTag();
                final Intent data = new Intent();
                data.putExtra(EXTRA_GROUP, group);
                data.putExtra(EXTRA_RESULT, selection);
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void onTopEmptyRegionClick() { }
        });
    }

    private class SelectionAdapter extends WearableListView.Adapter {

        private final List<Configuration> items;

        private SelectionAdapter(final List<Configuration> items) {
            this.items = items;
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(GroupSelectionActivity.this);
            final View itemView = inflater.inflate(R.layout.item_config_selection, null);
            return new ItemHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final WearableListView.ViewHolder holder, final int position) {
            final ItemHolder itemHolder = (ItemHolder) holder;
            final Configuration configuration = items.get(position);
            itemHolder.update(configuration);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

    }

    private class ItemHolder extends WearableListView.ViewHolder {

        public ItemHolder(final View itemView) {
            super(itemView);
        }

        public void update(final Configuration configuration) {
            final TextView view = (TextView) itemView;
            view.setText(configuration.getString(GroupSelectionActivity.this));
            itemView.setTag(configuration);
        }

    }

}
