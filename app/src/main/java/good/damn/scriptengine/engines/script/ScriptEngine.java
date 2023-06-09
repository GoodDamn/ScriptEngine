package good.damn.scriptengine.engines.script;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;
import good.damn.scriptengine.engines.script.utils.ScriptCommandsUtils;
import good.damn.scriptengine.engines.script.utils.ScriptDefinerUtils;
import good.damn.scriptengine.utils.Utilities;
import good.damn.scriptengine.views.GifView;
import good.damn.scriptengine.views.TextViewPhrase;

public class ScriptEngine {

    private static final String TAG = "ScriptEngine";

    private final EditText et_target;
    private final DisplayMetrics displayMetrics;

    private void createPhrase(TextViewPhrase target, ViewGroup root, Context context) {
        target.setGravity(Gravity.CENTER);
        target.setAlpha(0.0f);

        root.addView(target, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        target.animate().alpha(1.0f).setDuration(750)
                .withEndAction(() -> target.fadeOutTransition(new Random(), context.getResources().getDisplayMetrics().density)).start();

    }

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

        String text = new String(buffer, StandardCharsets.UTF_8).trim();
        target.setText(text);
        Log.d(TAG, "read: TEXT_BYTES_LENGTH: " + i + " TEXT:" + text);
        i+=1;

        if (chunk.length == i) { // No script to miss this one
            createPhrase(target,root,context);
            return;
        }

        short scriptSize = (short) (chunk[i] & 0xFF);
        int filesOffset = 0;
        i++;
        Log.d(TAG, "read: SCRIPT_SIZE: "+ scriptSize);
        for (int j = 0; j < scriptSize;) {
            int currentOffset = i+j+filesOffset;
            int argSize = chunk[currentOffset] & 0xFF;
            currentOffset++;
            byte commandIndex = chunk[currentOffset];
            Log.d(TAG, "read: J: "+ j + " SCRIPT_SIZE:" +scriptSize + " OFFSET:" + currentOffset + " ARG_SIZE: " + argSize + " COMMAND_INDEX: " + commandIndex);
            switch (commandIndex) {
                case 0: // textSize
                    ScriptDefinerUtils.TextSize(chunk,currentOffset,argSize,target);
                    break;
                case 1: // font
                    ScriptDefinerUtils.Font(chunk,currentOffset,argSize,target);
                    break;
                case 3: // img
                    ScriptGraphicsFile scriptImage = ScriptDefinerUtils.Image(chunk,currentOffset);
                    if (scriptImage == null) {
                        return;
                    }

                    byte[] img = scriptImage.file;
                    filesOffset += img.length - 1;

                    ImageView imageView = new ImageView(target.getContext());
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(img, 0, img.length));
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
                case 4: // Gif
                    ScriptGraphicsFile gifScript = ScriptDefinerUtils.Gif(chunk,currentOffset);

                    filesOffset += gifScript.file.length - 1;

                    GifView gifView = new GifView(target.getContext());
                    gifView.setSource(gifScript.file);

                    FrameLayout.LayoutParams par =
                            new FrameLayout.LayoutParams(gifView.width(), gifView.height());

                    par.leftMargin = (int) (gifScript.x * displayMetrics.widthPixels);
                    par.topMargin = (int) (gifScript.y * displayMetrics.heightPixels);

                    root.addView(gifView, par);
                    gifView.play();

                    gifView.animate()
                            .setStartDelay(5500)
                            .alpha(0.0f)
                            .withEndAction(()-> root.removeView(gifView)).start();

                    break;
                default:
                    Utilities.showMessage(context, "Invalid command index: " + commandIndex);
                    break;
            }
            j += argSize;
        }
        createPhrase(target,root,context);
    }

    public byte[] execute(String line) {
        byte[] args = null;

        String[] argv = line.split("\\s+");

        Context context = et_target.getContext();

        argv[0] = argv[0].trim();

        if (argv[0].isEmpty())
            return null;

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
