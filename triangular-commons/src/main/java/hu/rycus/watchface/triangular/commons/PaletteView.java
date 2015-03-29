package hu.rycus.watchface.triangular.commons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class PaletteView extends View {

    private static final String TEXT = "12";
    private static final float PADDING = 3f;

    private final Paint paint = new Paint();
    private final Path path = new Path();
    private final Rect textBounds = new Rect();

    private Palette palette = Palette.getDefault();

    public PaletteView(final Context context) {
        super(context);
    }

    public PaletteView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public Palette getPalette() {
        return palette;
    }

    public void setPalette(final Palette palette) {
        this.palette = palette;
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();

        paint.setAntiAlias(true);

        paint.setColor(palette.background());
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, paint);

        paint.setColor(0xFF212121);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, width, height, paint);

        paint.setStyle(Paint.Style.FILL);

        final int numShapes = palette.numberOfTriangles();
        final float tWidth = (float) width / (1.5f + numShapes / 2f);

        for (int index = 0; index < numShapes; index++) {
            paint.setColor(palette.triangle(index));
            paintTriangle(canvas, index, tWidth, height);
        }

        paint.setColor(palette.odd());
        paintTriangle(canvas, numShapes, tWidth, height);

        paint.setColor(palette.text());
        paint.setTextSize(height - (2f * PADDING));

        paint.getTextBounds(TEXT, 0, TEXT.length(), textBounds);

        final float textLeft = width - textBounds.width() - PADDING * 4f;
        final float textTop = height - (2f * PADDING);
        canvas.drawText(TEXT, textLeft, textTop, paint);
    }

    private void paintTriangle(final Canvas canvas, final int index,
                               final float width, final float height) {
        path.reset();

        if (index % 2 == 0) {
            path.moveTo(width * (index / 2f) + PADDING, PADDING);
            path.rLineTo(width - (2f * PADDING), 0f);
            path.rLineTo(-width / 2f + PADDING, height - (2f * PADDING));
        } else {
            path.moveTo(width * (index / 2f) + PADDING, height - PADDING);
            path.rLineTo(width - (2f * PADDING), 0f);
            path.rLineTo(-width / 2f + PADDING, -height + (2f * PADDING));
        }

        path.close();

        canvas.drawPath(path, paint);
    }

}
