package good.damn.scriptengine.engines.script;

import static good.damn.scriptengine.engines.script.utils.ScriptCommandsUtils.getSpannable;
import static good.damn.scriptengine.utils.Utilities.gn;

import android.content.Context;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import good.damn.scriptengine.engines.script.models.ScriptImage;
import good.damn.scriptengine.engines.script.utils.ScriptCommandsUtils;
import good.damn.scriptengine.engines.script.utils.ScriptDefinerUtils;
import good.damn.scriptengine.utils.Utilities;
import good.damn.scriptengine.views.TextViewPhrase;

public class ScriptEngine {

    private static final String TAG = "ScriptEngine";

    private final EditText et_target;
    private final DisplayMetrics displayMetrics;


    public ScriptEngine(EditText target){
        et_target = target;
        displayMetrics = et_target.getContext().getResources().getDisplayMetrics();
    }

    public void read(byte[] chunk, TextViewPhrase target, ViewGroup root) {

        Context context = target.getContext();

        byte[] buffer = new byte[512];
        byte current = chunk[0];
        short i = 0;
        for (; current != 0; i++){
            buffer[i] = current;
            current = chunk[i+1];
        }

        Log.d(TAG, "read: index textBytes " + i);
        String text = new String(buffer, StandardCharsets.UTF_8).trim();
        target.setText(text);
        Log.d(TAG, "read: index textBytes " + i + " " + text);
        i+=1;
        short scriptSize = (short) (chunk[i] & 0xFF);
        int filesOffset = 0;
        i++;
        Log.d(TAG, "read: "+ scriptSize);
        for (int j = 0; j < scriptSize;) {
            int currentOffset = i+j+filesOffset;
            int argSize = chunk[currentOffset];
            currentOffset++;
            byte commandIndex = chunk[currentOffset];
            Log.d(TAG, "read: "+ j + " " +scriptSize + " " + currentOffset + " " + argSize + " " + commandIndex);
            switch (commandIndex){
                case 0: // textSize
                    ScriptDefinerUtils.TextSize(chunk,currentOffset,argSize,target);
                    break;
                case 1: // font
                    ScriptDefinerUtils.Font(chunk,currentOffset,argSize,target);
                    break;
                case 3: // img
                    ScriptImage scriptImage = ScriptDefinerUtils.Image(chunk,currentOffset);

                    if (scriptImage == null) {
                        return;
                    }

                    byte[] img = scriptImage.image;
                    filesOffset += img.length - 1;

                    ImageView imageView = new ImageView(target.getContext());
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(scriptImage.image, 0, scriptImage.image.length));
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.width = (int) (displayMetrics.density * scriptImage.width);
                    params.height = (int) (displayMetrics.density * scriptImage.height);
                    params.gravity = Gravity.START | Gravity.TOP;
                    imageView.setScaleX(.0f);
                    imageView.setScaleY(.0f);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    root.addView(imageView,params);

                    imageView.setX(scriptImage.x * displayMetrics.widthPixels);
                    imageView.setY(scriptImage.y * displayMetrics.heightPixels);

                    imageView.animate().scaleY(1.0f).scaleX(1.0f).withEndAction(() ->
                                    imageView.animate().scaleX(.0f).scaleY(.0f).setStartDelay(1250).withEndAction(() ->
                                            root.removeView(imageView)).start())
                            .start();
                    break;
                case 4:

                    break;
            }
            j += argSize;
        }

        target.setGravity(Gravity.CENTER);
        target.setAlpha(0.0f);

        target.setTextColor(0);
        root.addView(target, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        target.animate().alpha(1.0f).setDuration(750)
                .withEndAction(() -> target.fadeOutTransition(new Random(), context.getResources().getDisplayMetrics().density)).start();
    }

    public byte[] execute(String line) {
        byte[] args = null;

        String[] argv = line.split("\\s+");

        Context context = et_target.getContext();

        switch (argv[0].toLowerCase()){
            case "textsize": // 0
                args = ScriptCommandsUtils.TextSize(argv,et_target,context);
                break;
            case "font": // 1
                args = ScriptCommandsUtils.Font(argv,et_target,context);
                break;
            case "action": // 2
                switch (argv[1].toLowerCase()) {
                    case "lp": // long press
                        break;
                    case "ck": // click
                        break;
                    case "sp": // swipe
                        break;
                }
                break;
            case "img": // put image on the screen 3
                args = ScriptCommandsUtils.Image(argv,context);
                break;
            case "vect": // set of vectors 4
                break;
            // global commands
            case "bg": // background 5
                break;
            case "gif":
                args = ScriptCommandsUtils.Gif(argv,context);
                break;
            default:
                Utilities.showMessage(context, "Invalid command: " + argv[0]);
                break;
        }

        return args;
    }
}
