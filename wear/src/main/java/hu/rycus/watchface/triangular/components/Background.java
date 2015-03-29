package hu.rycus.watchface.triangular.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.format.Time;

import com.google.android.gms.wearable.DataMap;

import hu.rycus.watchface.commons.NonAmbientBackground;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.commons.Palette;

public class Background extends NonAmbientBackground {

    private static final int N_HORIZONTAL = 6;
    private static final int N_VERTICAL = 6;

    private Palette palette = Palette.getDefault();
    private Bitmap bitmap;

    @Override
    protected boolean isActiveByDefault() {
        return !Configuration.ANIMATED_BACKGROUND.getBoolean(null);
    }

    @Override
    protected void onSizeSet(final int width, final int height, final boolean round) {
        super.onSizeSet(width, height, round);
        prepareBackgroundBitmap();
    }

    @Override
    protected void onApplyConfiguration(final DataMap configuration) {
        setActive(!Configuration.ANIMATED_BACKGROUND.getBoolean(configuration));

        palette = Configuration.COLOR_PALETTE.getPalette(configuration);
        prepareBackgroundBitmap();
    }

    @Override
    protected void onDrawBackground(final Canvas canvas, final Time time) {
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }

    private void prepareBackgroundBitmap() {
        final int width = canvasWidth;
        final int height = canvasHeight;

        paint.setColor(palette.background());
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bitmap);
        canvas.drawPaint(paint);

        final float w = (float) width / (float) N_HORIZONTAL;
        final float h = (float) height / (float) N_VERTICAL;

        final Path path = new Path();
        path.rLineTo(w, 0);
        path.rLineTo(-w / 2f, h);
        path.close();

        int colorIndex = 0;

        for (int yi = 0; yi < N_VERTICAL; yi++) {
            int n = N_HORIZONTAL;
            if (yi % 2 == 0) {
                n = N_HORIZONTAL + 1;
                colorIndex = 0;
                path.offset(-w / 2f, 0); // shift back
            } else {
                colorIndex++;
            }

            for (int xi = 0; xi < n; xi++) {
                final int color = palette.triangle(colorIndex++);

                if (xi == 5 && yi == 1) { // add an odd color
                    paint.setColor(palette.odd());
                } else {
                    paint.setColor(color);
                }

                canvas.drawPath(path, paint);
                path.offset(w, 0); // shift
            }

            path.offset(-width, h); // new line

            if (yi % 2 == 0) {
                path.offset(-w / 2f, 0); // shift back
            }
        }

        this.bitmap = bitmap;
    }

}
