package good.damn.scriptengine.engines.script.utils;

import static good.damn.scriptengine.engines.script.utils.ScriptCommandsUtils.makeSpannable;
import static good.damn.scriptengine.utils.Utilities.gn;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.TextView;

import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;
import good.damn.scriptengine.engines.script.models.ScriptResourceFile;
import good.damn.scriptengine.engines.script.models.ScriptTextConfig;
import good.damn.scriptengine.utils.Utilities;

public class ScriptDefinerUtils {

    private static final String TAG = "ScriptDefinerUtils";

    // 0
    public static void TextSize(byte[] chunk, int offset, int argSize, ScriptTextConfig textConfig) {
        short textSize = (short) (gn(chunk[offset+1], chunk[offset+2]) / 1000);
        Log.d(TAG, "read: textSize: " + textSize);
        offset += 3;

        SpannableString spannableString = null;

        CharSequence text = textConfig.spannableString;

        if (argSize == 4) { // 1 arg
            textConfig.textSize = textSize;
            return;
        }

        if (argSize == 6) { // 2 args
            spannableString = makeSpannable(gn(chunk[offset],chunk[offset+1]),
                    text.length(),
                    new AbsoluteSizeSpan(textSize,true),text);
        }

        if (argSize == 8) { // 3 args
            spannableString = makeSpannable(gn(chunk[offset],chunk[offset+1]),
                    gn(chunk[offset+2],chunk[offset+3]),
                    new AbsoluteSizeSpan(textSize,true),text);
        }

        if (spannableString != null){
            textConfig.spannableString = spannableString;
        }
    }

    // 1
    public static void Font(byte[] chunk, int currentOffset, int argSize, ScriptTextConfig textConfig) {
        byte style = chunk[currentOffset+1];

        boolean isColorSpan = false;
        int color = 0;

        Log.d(TAG, "read: font " + style + " " + argSize);
        CharacterStyle span = null;
        currentOffset+=2;
        switch (style) {
            case 0:
                span = new UnderlineSpan();
                break;
            case 1:
                span = new StrikethroughSpan();
                break;
            case 2:
                span = new StyleSpan(Typeface.BOLD);
                break;
            case 3:
                span = new StyleSpan(Typeface.ITALIC);
                break;
            case 4:

                int alpha = chunk[currentOffset] & 0xff;
                int red = chunk[currentOffset+1] & 0xff;
                int green = chunk[currentOffset+2] & 0xff;
                int blue = chunk[currentOffset+3] & 0xff;

                color = alpha << 24 |
                            red << 16  |
                            green << 8 |
                            blue;
                currentOffset += 4;
                argSize -= 4;

                Log.d(TAG, "Font: ARGB_GET:" + alpha + " " + red + " " + green + " " + blue);

                Log.d(TAG, "Font: COLOR SPAN: " + color);
                span = new ForegroundColorSpan(color);
                isColorSpan = true;
                break;
        }

        if (span == null)
            return;

        SpannableString spannableString = null;

        CharSequence text = textConfig.spannableString;

        if (argSize == 3) { // 2 args
            if (isColorSpan) {
                textConfig.textColor = color;
                return;
            }
            spannableString = makeSpannable(0,text.length(), span,text);
        }

        if (argSize == 5) { // 3 args
            spannableString = makeSpannable(gn(chunk[currentOffset],chunk[currentOffset+1]),
                    text.length(),
                    span,text);
        }

        if (argSize == 7) { // 4 args
            spannableString = makeSpannable(gn(chunk[currentOffset],chunk[currentOffset+1]),
                    gn(chunk[currentOffset+2],chunk[currentOffset+3]),
                    span,text);
        }

        if (spannableString != null) {
            textConfig.spannableString = spannableString;
        }

    }

    //2
    public static int Background(byte[] chunk, int currentOffset) {
        currentOffset++;
        return Utilities.gn(chunk, currentOffset);
    }

    // 3
    public static ScriptGraphicsFile Image(byte[] chunk,
                                           int currentOffset) {
        currentOffset++;

        // read data of image
        Log.d(TAG, "Image: ENCODED ARGS: " + chunk[currentOffset] + " " + chunk[currentOffset+1]);
        Log.d(TAG, "Image: CURRENT_OFFSET: " + currentOffset);

        // read properties for image
        int width = gn(chunk[currentOffset], chunk[currentOffset+1]);
        Log.d(TAG, "Image: WIDTH_ENCODED: " + chunk[currentOffset] + " " + chunk[currentOffset+1]);
        currentOffset += 2;
        int height = gn(chunk[currentOffset], chunk[currentOffset+1]);
        currentOffset += 2;

        float xPos = (float) gn(chunk[currentOffset], chunk[currentOffset+1]) / Short.MAX_VALUE;
        currentOffset += 2;
        float yPos = (float) gn(chunk[currentOffset], chunk[currentOffset+1]) / Short.MAX_VALUE;
        currentOffset += 2;

        Log.d(TAG, "read: TOTAL: WIDTH: " + width + " HEIGHT: " + height + " X_POS: " + xPos + " Y_POS: " + yPos);

        byte resID = chunk[currentOffset];

        if (resID <= -1) {
            Log.d(TAG, "read: ERROR pre-exception: InvalidResourceReference: RES_ID: " + resID);
            return null;
        }

        ScriptGraphicsFile scriptImage = new ScriptGraphicsFile();
        scriptImage.resID = resID;
        scriptImage.height = height;
        scriptImage.width = width;
        scriptImage.x = xPos;
        scriptImage.y = yPos;

        return scriptImage;
    }

    public static ScriptGraphicsFile Gif(byte[] chunk, int currentOffset) {
        currentOffset++;

        // read data of image
        float xPos = (float) gn(chunk[currentOffset], chunk[currentOffset+1]) / Short.MAX_VALUE;
        currentOffset += 2;
        float yPos = (float) gn(chunk[currentOffset], chunk[currentOffset+1]) / Short.MAX_VALUE;
        currentOffset += 2;

        byte resID = chunk[currentOffset];

        if (resID <= -1) {
            Log.d(TAG, "read: ERROR pre-exception: InvalidResourceReference: (GIF) RES_ID: " + resID);
            return null;
        }

        ScriptGraphicsFile scriptGif = new ScriptGraphicsFile();
        scriptGif.x = xPos;
        scriptGif.y = yPos;
        scriptGif.resID = resID;
        return scriptGif;
    }

    // 5
    public static ScriptResourceFile SFX(byte[] chunk, int currentOffset) {
        currentOffset++;
        Log.d(TAG, "SFX: CHUNK(current): " + chunk[currentOffset]);
        byte resID = chunk[currentOffset];
        if (resID <= -1) {
            Log.d(TAG, "read: ERROR pre-exception: InvalidResourceReference: (SFX) RES_ID: " + resID);
            return null;
        }

        ScriptResourceFile srf = new ScriptResourceFile();
        srf.resID = resID;

        return srf;
    }

    // 6
    public static ScriptResourceFile Ambient(byte[] chunk, int currentOffset) {
        currentOffset++;
        Log.d(TAG, "Ambient: CHUNK(current): " + chunk[currentOffset]);
        byte resID = chunk[currentOffset];
        if (resID <= -1) {
            Log.d(TAG, "read: ERROR pre-exception: InvalidResourceReference: (Ambient) RES_ID: " + resID);
            return null;
        }

        ScriptResourceFile srf = new ScriptResourceFile();
        srf.resID = resID;

        return srf;
    }

    // 7
    public static ScriptResourceFile Vector(byte[] chunk, int currentOffset) {
        currentOffset++;
        Log.d(TAG, "Vector: CHUNK(current): " + chunk[currentOffset]);

        byte resID = chunk[currentOffset];
        if (resID <= -1) {
            Log.d(TAG, "read: ERROR pre-exception: InvalidResourceReference: (Vector) RES_ID: " + resID);
            return null;
        }

        ScriptResourceFile srf = new ScriptResourceFile();
        srf.resID = resID;

        return srf;
    }
}
