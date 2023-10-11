package good.damn.scriptengine.engines.script.models;

import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

public class ScriptGraphicsFile extends ScriptResourceFile {
    public float x;
    public float y;
    public int width;
    public int height;

    public String xyString(DisplayMetrics metrics) {
        return " " + x * metrics.widthPixels + " " + y * metrics.heightPixels;
    }

    public String sizeString(DisplayMetrics metrics) {
        return " "+width * metrics.density + " " + height * metrics.density;
    }
}
