package hu.rycus.watchface.triangular.commons;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public enum Palette {

    ORIGINAL(0xFF121212, list(0xFF388E3C, 0xFFC2185B, 0xFF757575), 0xFFFF5722, Color.WHITE),
    SILVER_RED(0xFF121212, list(0xFF757575, 0xFF616161, 0xFF424242), 0xFFD50000, Color.WHITE),
    SILVER_YELLOW(0xFF121212, list(0xFF757575, 0xFF616161, 0xFF424242), 0xFFFFFF00, Color.WHITE),
    RED_YELLOW(Color.BLACK, list(0xFFEF9A9A, 0xFFEF5350, 0xFFD50000), 0xFFFFFF00, Color.WHITE),
    WARM_COLORS(0xFF3E2723, list(0xFFF9A825, 0xFF607D8B, 0xFF7CB342), 0xFFC2185B, Color.WHITE),
    NATURE_COLORS(0xFF212121, list(0xFF2196F3, 0xFF388E3C, 0xFFAFB42B), 0xFFC62828, Color.WHITE),
    STRONG_COLORS(0xFF212121, list(0xFFC62828, 0xFF00695C, 0xFF558B2F), 0xFFFFEA00, 0xFFCFD8DC);

    public static final int LENGTH = Palette.values().length;

    private final int background;
    private final int[] triangles;
    private final int odd;
    private final int text;

    Palette(final int background, final int[] triangles, final int odd, final int text) {
        this.background = background;
        this.triangles = triangles;
        this.odd = odd;
        this.text = text;
    }

    private static int[] list(final int... colors) {
        return colors;
    }

    public int background() {
        return background;
    }

    public int triangle(final int index) {
        return triangles[index % triangles.length];
    }

    int numberOfTriangles() {
        return triangles.length;
    }

    public int odd() {
        return odd;
    }

    public int text() {
        return text;
    }

    public static Palette at(final int index) {
        return Palette.values()[index];
    }

    public static int indexOf(final Palette palette) {
        return palette.ordinal();
    }

    public static Palette getDefault() {
        return Configuration.COLOR_PALETTE.getPalette(null);
    }

    public static ListAdapter getAdapter() {
        return new PaletteAdapter();
    }

    private static class PaletteAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return LENGTH;
        }

        @Override
        public Object getItem(final int position) {
            return Palette.at(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view;
            final PaletteHolder holder;
            if (convertView != null) {
                view = convertView;
                holder = (PaletteHolder) convertView.getTag();
            } else {
                view = View.inflate(parent.getContext(), R.layout.item_config_palette, null);
                holder = new PaletteHolder(view);
                view.setTag(holder);
            }

            holder.update(position);

            return view;
        }

        private static class PaletteHolder {

            private final PaletteView vPalette;

            PaletteHolder(final View itemView) {
                this.vPalette = (PaletteView) itemView.findViewById(R.id.v_config_palette);
            }

            void update(final int position) {
                final Palette palette = Palette.at(position);
                vPalette.setPalette(palette);
            }

        }
    }

}
