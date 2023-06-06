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
            case 4:

                int alpha = (chunk[currentOffset] & 0xFF << 24);
                int red = (chunk[currentOffset+1] & 0xFF << 16);
                int green = (chunk[currentOffset+2] & 0xFF << 8);
                int blue = (chunk[currentOffset+3] & 0xFF);

                int color = alpha |
                            red   |
                            green |
                            blue;
                currentOffset += 4;
                argSize -= 4;

                Log.d(TAG, "Font: ARGB_GET:" + alpha + " " + red + " " + green + " " + blue);

                Log.d(TAG, "Font: COLOR SPAN: " + color);
                span = new ForegroundColorSpan(color);
                break;
        }

        if (span == null)
            return;

        SpannableString spannableString = null;

        CharSequence text = target.getText();

        if (argSize == 3) { // 2 args
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

        if (spannableString != null){
            target.setText(spannableString);
        }
    }

    public static ScriptGraphicsFile Image(byte[] chunk,
                                           int currentOffset) {
        currentOffset++;

        // read data of image
        Log.d(TAG, "read: ENCODED IMAGE_SIZE: " + chunk[currentOffset] + " " + chunk[currentOffset+1]);

        int fileSize = (chunk[currentOffset] & 0xFF) * 65025 +
                (chunk[currentOffset+1] & 0xFF) * 255 + (chunk[currentOffset+2] & 0xFF);
        Log.d(TAG, "read: CURRENT_OFFSET: " + currentOffset + " FILE_SIZE:" + fileSize);
        currentOffset += 3;

        // read properties for image
        int width = gn(chunk[currentOffset], chunk[currentOffset+1]);
        Log.d(TAG, "read: WIDTH_ENCODED: " + chunk[currentOffset] + " " + chunk[currentOffset+1]);
        currentOffset += 2;
        int height = gn(chunk[currentOffset], chunk[currentOffset+1]);
        currentOffset += 2;
        Log.d(TAG, "read: xPos ENCODED: " + (gn(chunk[currentOffset], chunk[currentOffset+1]) / 1000f));
        float xPos = gn(chunk[currentOffset], chunk[currentOffset+1]) / 1000f;
        currentOffset += 2;
        float yPos = gn(chunk[currentOffset], chunk[currentOffset+1]) / 1000f;
        currentOffset += 2;

        Log.d(TAG, "read: TOTAL: WIDTH: " + width + " HEIGHT: " + height + " X_POS: " + xPos + " Y_POS: " + yPos);

        byte[] imgBytes = new byte[fileSize];
        // read file's bytes

        if (imgBytes.length > chunk.length) {
            Log.d(TAG, "read: ERROR pre-exception: IndexOutOfBounds: imgBytes.length > chunk.length" + imgBytes.length + "  " + chunk.length);
            return null;
        }

        ScriptGraphicsFile scriptImage = new ScriptGraphicsFile();
        scriptImage.file = imgBytes;
        scriptImage.height = height;
        scriptImage.width = width;
        scriptImage.x = xPos;
        scriptImage.y = yPos;

        System.arraycopy(chunk, currentOffset, imgBytes,0, imgBytes.length);

        return scriptImage;
    }

    public static ScriptGraphicsFile Gif(byte[] chunk, int currentOffset) {
        currentOffset++;

        // read data of image
        Log.d(TAG, "read: offsets for GIFSize: " + chunk[currentOffset] + " " + chunk[currentOffset+1]);

        int fileSize = (chunk[currentOffset] & 0xFF) * 65025 +
                (chunk[currentOffset+1] & 0xFF) * 255 + (chunk[currentOffset+2] & 0xFF);
        Log.d(TAG, "read: GIF " + currentOffset + " " + fileSize);
        currentOffset += 3;

        Log.d(TAG, "read: xPos ENCODED: " + (gn(chunk[currentOffset], chunk[currentOffset+1]) / 1000f));
        float xPos = gn(chunk[currentOffset], chunk[currentOffset+1]) / 1000f;
        currentOffset += 2;
        float yPos = gn(chunk[currentOffset], chunk[currentOffset+1]) / 1000f;
        currentOffset += 2;

        byte[] gif = new byte[fileSize];
        System.arraycopy(chunk,currentOffset,gif,0,gif.length);

        ScriptGraphicsFile scriptGif = new ScriptGraphicsFile();
        scriptGif.x = xPos;
        scriptGif.y = yPos;
        scriptGif.file = gif;
        return scriptGif;
    }
}
