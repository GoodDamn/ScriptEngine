package good.damn.scriptengine.engines.script.utils;

import static good.damn.scriptengine.engines.script.utils.ScriptCommandsUtils.getSpannable;
import static good.damn.scriptengine.utils.Utilities.gn;

import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import good.damn.scriptengine.engines.script.models.ScriptImage;

public class ScriptDefinerUtils {

    private static final String TAG = "ScriptDefinerUtils";

    // 0
    public static void TextSize(byte[] chunk, int offset, int argSize, TextView target) {
        short textSize = (short) (gn(chunk[offset+1], chunk[offset+2]) / 1000);
        Log.d(TAG, "read: textSize: " + textSize);
        offset += 3;

        SpannableString spannableString = null;

        CharSequence text = target.getText();

        if (argSize == 4) { // 1 arg
            target.setTextSize(textSize);
            return;
        }

        if (argSize == 6) { // 2 args
            spannableString = getSpannable(gn(chunk[offset],chunk[offset+1]),
                    text.length(),
                    new AbsoluteSizeSpan(textSize,true),text);
        }

        if (argSize == 8) { // 3 args
            spannableString = getSpannable(gn(chunk[offset],chunk[offset+1]),
                    gn(chunk[offset+2],chunk[offset+3]),
                    new AbsoluteSizeSpan(textSize,true),text);
        }

        if (spannableString != null){
            target.setText(spannableString);
        }
    }

    // 1
    public static void Font(byte[] chunk, int currentOffset, int argSize, TextView target) {
        byte style = chunk[currentOffset+1];

        Log.d(TAG, "read: font " + style + " " + argSize);
        CharacterStyle span = null;
        currentOffset+=2;
        switch (style){
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
        }

        if (span == null)
            return;

        SpannableString spannableString = null;

        CharSequence text = target.getText();

        if (argSize == 3) { // 2 args
            spannableString = getSpannable(0,text.length(), span,text);
        }

        if (argSize == 5) { // 3 args
            spannableString = getSpannable(gn(chunk[currentOffset],chunk[currentOffset+1]),
                    text.length(),
                    span,text);
        }

        if (argSize == 7) { // 4 args
            spannableString = getSpannable(gn(chunk[currentOffset],chunk[currentOffset+1]),
                    gn(chunk[currentOffset+2],chunk[currentOffset+3]),
                    span,text);
        }

        if (spannableString != null){
            target.setText(spannableString);
        }
    }

    public static ScriptImage Image(byte[] chunk,
                                    int currentOffset) {
        currentOffset++;

        // read data of image
        Log.d(TAG, "read: offsets for imageSize: " + chunk[currentOffset] + " " + chunk[currentOffset+1]);

        int fileSize = (chunk[currentOffset] & 0xFF) * 65025 +
                (chunk[currentOffset+1] & 0xFF) * 255 + (chunk[currentOffset+2] & 0xFF);
        Log.d(TAG, "read: img " + currentOffset + " " + fileSize);
        currentOffset += 3;

        // read properties for image
        int width = gn(chunk[currentOffset], chunk[currentOffset+1]);
        Log.d(TAG, "read: width: " + chunk[currentOffset] + " " + chunk[currentOffset+1]);
        currentOffset += 2;
        int height = gn(chunk[currentOffset], chunk[currentOffset+1]);
        currentOffset += 2;
        Log.d(TAG, "read: xPos " + (gn(chunk[currentOffset], chunk[currentOffset+1]) / 1000f));
        float xPos = gn(chunk[currentOffset], chunk[currentOffset+1]) / 1000f;
        currentOffset += 2;
        float yPos = gn(chunk[currentOffset], chunk[currentOffset+1]) / 1000f;
        currentOffset += 2;

        Log.d(TAG, "read: properties " + width + " " + height + " " + xPos + " " + yPos);

        byte[] imgBytes = new byte[fileSize];
        // read file's bytes

        if (imgBytes.length > chunk.length) {
            Log.d(TAG, "read: ERROR pre-exception: IndexOutOfBounds: imgBytes.length > chunk.length" + imgBytes.length + "  " + chunk.length);
            return null;
        }

        ScriptImage scriptImage = new ScriptImage();
        scriptImage.image = imgBytes;
        scriptImage.height = height;
        scriptImage.width = width;
        scriptImage.x = xPos;
        scriptImage.y = yPos;

        System.arraycopy(chunk, currentOffset, imgBytes,0, imgBytes.length);

        return scriptImage;
    }
}
