package hu.rycus.watchface.triangular;

import android.support.wearable.watchface.WatchFaceStyle;
import android.view.Gravity;

import java.util.Collection;

import hu.rycus.watchface.commons.BaseCanvasWatchFaceService;
import hu.rycus.watchface.commons.BlackAmbientBackground;
import hu.rycus.watchface.commons.Component;
import hu.rycus.watchface.triangular.commons.Configuration;
import hu.rycus.watchface.triangular.components.AnimatedBackground;
import hu.rycus.watchface.triangular.components.Background;
import hu.rycus.watchface.triangular.components.Battery;
import hu.rycus.watchface.triangular.components.Date;
import hu.rycus.watchface.triangular.components.Hour;
import hu.rycus.watchface.triangular.components.Minute;
import hu.rycus.watchface.triangular.components.Second;

public class TriangularWatchFace extends BaseCanvasWatchFaceService {

    @Override
    public BaseEngine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends BaseEngine {

        @Override
        protected String[] getConfigurationPaths() {
            return new String[] {Configuration.PATH};
        }

        @Override
        protected void createComponents(final Collection<Component> components) {
            components.add(new BlackAmbientBackground());
            components.add(new Background());
            components.add(new AnimatedBackground());
            components.add(new Hour());
            components.add(new Minute());
            components.add(new Second());
            components.add(new Date());
            components.add(new Battery());
        }

        @Override
        protected WatchFaceStyle buildStyle(final WatchFaceStyle.Builder builder) {
            return builder.setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setHotwordIndicatorGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP)
                    .setStatusBarGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP)
                    .build();
        }

    }

}
