package hu.rycus.watchface.triangular.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.format.Time;

import hu.rycus.watchface.commons.NonAmbientBackground;

public class Background extends NonAmbientBackground {

    private static final int N_HORIZONTAL = 6;
    private static final int N_VERTICAL = 6;

    private Bitmap bitmap;

    @Override
    protected void onSizeSet(final int width, final int height) {
        super.onSizeSet(width, height);

        paint.setColor(0xFF121212);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, width, height, paint);

        final float w = (float) width / (float) N_HORIZONTAL;
        final float h = (float) height / (float) N_VERTICAL;

        final Path path = new Path();
        path.rLineTo(w, 0);
        path.rLineTo(-w / 2f, h);
        path.close();

        final int[] colors = { 0xFF388E3C, 0xFFC2185B, 0xFF757575 };
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
                final int color = colors[colorIndex++ % 3];

                if (xi == 5 && yi == 1) { // add an odd color
                    paint.setColor(0xFFFF5722);
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
    }

    @Override
    protected void onDrawBackground(final Canvas canvas, final Time time) {
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }

}
