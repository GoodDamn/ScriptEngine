package good.damn.scriptengine.engines.script.utils;

import static good.damn.scriptengine.utils.Utilities.gb;

import android.app.ForegroundServiceStartNotAllowedException;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import good.damn.scriptengine.utils.ArrayUtils;
import good.damn.scriptengine.utils.Utilities;
import good.damn.scriptengine.views.GifView;

public class ScriptCommandsUtils {

    private static final String TAG = "ScriptCommandsUtils";

    public static SpannableString getSpannable(int start,
                                                int end,
                                                CharacterStyle characterStyle,
                                                CharSequence text){
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(characterStyle, start,end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public static void setSpan(int start,
                                int end,
                                CharacterStyle characterStyle,
                                TextView target){
        target.setText(getSpannable(start,end,characterStyle,target.getText()));
    }

    private static CharacterStyle enumSpan(String argv, byte[] style, Context context){
        switch (argv) {
            case "ul":
                return new UnderlineSpan();
            case "st":
                style[0] = 1;
                return new StrikethroughSpan();
            case "bold":
                style[0] = 2;
                return new StyleSpan(Typeface.BOLD);
            case "italic":
                style[0] = 3;
                return new StyleSpan(Typeface.ITALIC);
        }

        if (argv.contains("#")) {
            try {
                int color = Color.parseColor(argv);
                style[0] = 4;
                return new ForegroundColorSpan(color);
            } catch (IllegalArgumentException exception) {
                Utilities.showMessage(context, "Invalid text color of hexadecimal value: " + argv);
                return null;
            }
        }

        Utilities.showMessage(context, "Invalid enum-argument for (" + argv);
        return null;
    }


    // 0
    public static byte[] TextSize(String[] argv, EditText et_target, Context context) {
        float size;
        try {
            size = Float.parseFloat(argv[1]);
        } catch (NumberFormatException exception){
            Utilities.showMessage(context, "Invalid format argument " + argv[1]);
            return null;
        }

        byte[] origin = new byte[2];
        origin[0] = 1;
        origin[1] = 0;

        byte[] args = null;

        if (argv.length == 2){
            origin[0] = 4;
            args = ArrayUtils.concatByteArrays(origin,gb((int) (size * 1000)));
            et_target.setTextSize(size);
            return args;
        }

        if (argv.length == 3) { // 2 arguments
            try {
                int startPos = Integer.parseInt(argv[2]);
                setSpan(startPos, et_target.getText().length(),new AbsoluteSizeSpan((int) size,true),et_target);
                origin[0] = 6;
                args = ArrayUtils.concatByteArrays(origin,gb((short) (size * 1000)), gb(startPos));
            } catch (NumberFormatException exception){
                Utilities.showMessage(context, "Invalid integer-argument format for ("+argv[0] + " " + argv[1] + " " + argv[2]);
            }
            return args;
        }

        if (argv.length == 4) { // 3 arguments
            try {
                int startPos = Integer.parseInt(argv[2]);
                int endPos = Integer.parseInt(argv[3]);
                setSpan(startPos, endPos,new AbsoluteSizeSpan((int) size,true),et_target);
                origin[0] = 8;
                args = ArrayUtils.concatByteArrays(origin, gb((short) (size * 1000)), gb(startPos), gb(endPos));
            } catch (NumberFormatException exception){
                Utilities.showMessage(context, "Invalid integer-argument format for (" + argv[0] + " " + argv[1] + " " + argv[2] + " " + argv[3]);
            }
            return args;
        }

        Utilities.showMessage(context, "Invalid integer-argument format for (" + argv[0]);
        return null;
    }

    // 1
    public static byte[] Font(String[] argv, EditText et_target, Context context) {

        byte[] args = null;

        byte[] a = new byte[1];

        CharacterStyle span = enumSpan(argv[1].toLowerCase(),a,context);

        if (span == null) {
            return new byte[0];
        }

        byte[] origin = new byte[2];
        origin[0] = 1; // args size in bytes
        origin[1] = 1; // command index

        byte[] style;

        if (a[0] == 4) { // span is ForegroundColorSpan
            ForegroundColorSpan colorSpan = (ForegroundColorSpan) span;
            int color = colorSpan.getForegroundColor();
            Log.d(TAG, "Font: COLOR_SPAN: BYTE COLOR: " + color);
            style = new byte[5];
            style[1] = (byte) (color >> 24); // alpha
            style[2] = (byte) ((color >> 16) & 0xFF); // red
            style[3] = (byte) ((color >> 8) & 0xFF); // green
            style[4] = (byte) (color & 0xFF); // blue

            origin[0] = 5; // args size in bytes

        } else {
            style = new byte[1];
        }

        style[0] = a[0]; // set style type

        if (argv.length == 2) {
            origin[0] += 2;
            args = ArrayUtils.concatByteArrays(origin, style);
            setSpan(0, et_target.getText().length(), span,et_target);
            return args;
        }

        if (argv.length == 3) { // 2 arguments
            try {
                origin[0] += 3;
                int startPos = Integer.parseInt(argv[2]);
                args = ArrayUtils.concatByteArrays(origin, style, gb(startPos));
                setSpan(startPos, et_target.getText().length(),span,et_target);
            } catch (NumberFormatException exception){
                Utilities.showMessage(context, "Invalid integer-argument format for (" + argv[0] + " " + argv[1] + " " + argv[2]);
            }
            return args;
        }

        if (argv.length == 4) { // 3 arguments
            try {
                origin[0] += 4;
                int startPos = Integer.parseInt(argv[2]);
                int endPos = Integer.parseInt(argv[3]);
                args = ArrayUtils.concatByteArrays(origin, style, gb(startPos), gb(endPos));
                setSpan(startPos, endPos,span,et_target);
            } catch (NumberFormatException exception){
                Utilities.showMessage(context, "Invalid integer-argument format for (" + argv[0] + " " + argv[1] + " " + argv[2] + " " + argv[3]);
            }
            return args;
        }
        Utilities.showMessage(context, "No argument for (" + argv[0]);
        return null;
    }

    // 3
    public static byte[] Image(String[] argv, Context context) {
        Log.d(TAG, "execute: IMAGE COMMAND: " + argv[1]);

        byte[] args = null;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        try {

            short xPos = Short.parseShort(argv[4]);
            short yPos = Short.parseShort(argv[5]);

            if (xPos > displayMetrics.widthPixels || xPos < 0) {
                Utilities.showMessage(context,"img command hasn't executed("+
                        xPos + " on X Axis doesn't belong to [0;"+displayMetrics.widthPixels+"]");
                return new byte[0];
            }

            if (yPos > displayMetrics.heightPixels || yPos < 0) {
                Utilities.showMessage(context,"img command hasn't executed("+
                        yPos + " on Y Axis doesn't belong to [0;"+displayMetrics.heightPixels+"]");
                return new byte[0];
            }

            byte[] img = Utilities.getBytesFromIS(argv[1]);

            byte[] origin = new byte[5];
            origin[0] = 14; // argSize (4 args * 2 bytes) + 4 next args
            origin[1] = 3; // commandIndex
            origin[2] = (byte) (img.length / 65025);
            origin[3] = (byte) (img.length / 255 % 255);
            origin[4] = (byte) (img.length % 255);

            // Arguments
            Log.d(TAG, "Image: imgLength: " + origin[2] + " " + origin[3] + " " + origin[4]);
            Log.d(TAG, "Image: imgLength: " + ((origin[2] & 0xFF) * 65025) + " " + ((origin[3] & 0xFF) * 255) + " " + (origin[4] & 0xFF) + " " + img.length);
            args = ArrayUtils.concatByteArrays(origin,
                    gb(Integer.parseInt(argv[2])), // width of image
                    gb(Integer.parseInt(argv[3])), // height of image
                    gb((int) (1000.0f * xPos / displayMetrics.widthPixels)), // normal value of X pos
                    gb((int) (1000.0f * yPos / displayMetrics.heightPixels)), // normal value of Y pos
                    img);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return args;
    }

    // 4
    public static byte[] Gif(String[] argv, Context context) {
        byte[] args = null;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        try {
            byte[] gif = Utilities.getBytesFromIS(argv[1]);
            byte[] origin = new byte[5];
            origin[0] = 10; // argSize (4 args * 2 bytes) + 4 next args
            origin[1] = 4; // commandIndex
            origin[2] = (byte) (gif.length / 65025);
            origin[3] = (byte) (gif.length / 255 % 255);
            origin[4] = (byte) (gif.length % 255);

            short xPos = Short.parseShort(argv[2]);
            short yPos = Short.parseShort(argv[3]);

            if (xPos > displayMetrics.widthPixels || xPos < 0) {
                Utilities.showMessage(context,"img command hasn't executed("+
                        xPos + " on X Axis doesn't belong to [0;"+displayMetrics.widthPixels+"]");
                return new byte[0];
            }

            if (yPos > displayMetrics.heightPixels || yPos < 0) {
                Utilities.showMessage(context,"img command hasn't executed("+
                        yPos + " on Y Axis doesn't belong to [0;"+displayMetrics.heightPixels+"]");
                return new byte[0];
            }

            args = ArrayUtils.concatByteArrays(origin,
                    gb((int) (1000.0f * xPos / displayMetrics.widthPixels)), // normal value of X pos
                    gb((int) (1000.0f * yPos / displayMetrics.heightPixels)), // normal value of Y pos
                    gif);
        } catch (IOException exception) {
            exception.printStackTrace();
            Utilities.showMessage(context, "GIF: " + exception.getMessage());
        }
        return args;
    }
}
