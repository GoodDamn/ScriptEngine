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

import good.damn.scriptengine.engines.script.interfaces.OnConfigureViewListener;
import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;
import good.damn.scriptengine.engines.script.utils.ScriptCommandsUtils;
import good.damn.scriptengine.engines.script.utils.ScriptDefinerUtils;
import good.damn.scriptengine.utils.Utilities;
import good.damn.scriptengine.views.GifView;
import good.damn.scriptengine.views.TextViewPhrase;

public class ScriptEngine {

    private static final String TAG = "ScriptEngine";

    private final Context mContext;
    private final DisplayMetrics metrics;

    private ViewGroup mRoot;

    private EditText et_target;

    private OnConfigureViewListener mOnConfigureViewListener;

    private void createPhrase(TextViewPhrase target) {
        target.setGravity(Gravity.CENTER);
        target.setAlpha(0.0f);

        mRoot.addView(target, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (mOnConfigureViewListener != null) {
            mOnConfigureViewListener.onConfigured(target);
        }
    }

    public ScriptEngine(Context context){
        mContext = context;
        metrics = context.getResources().getDisplayMetrics();
    }

    public void setSourceEditText(EditText et_target) {
        this.et_target = et_target;
    }

    public void setOnConfigureView(OnConfigureViewListener configureViewListener) {
        mOnConfigureViewListener = configureViewListener;
    }

    public void setRootViewGroup(ViewGroup root) {
        mRoot = root;
    }

    public Context getContext() {
        return mContext;
    }

    public byte[] compile(String line) {
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
                Utilities.showMessage("Invalid command: " + argv[0], context);
                break;
        }

        return args;
    }

    public void read(byte[] chunk, TextViewPhrase target) {
        read(chunk, target, 0);
    }

    public int read(byte[] chunk, TextViewPhrase target, int offsetChunk) {

        Context context = mRoot.getContext();

        int chunkLength = Utilities.gn(chunk,offsetChunk) + 4;

        Log.d(TAG, "read: CHUNK_LENGTH: " + chunkLength + " CHUNK[offsetChunk]:" + chunk[offsetChunk] + " OFFSET_CHUNK: " + offsetChunk);

        int offset = offsetChunk + 4;

        byte[] buffer = new byte[512];
        byte current = chunk[offset];
        short i = 0;
        for (; current != 0; i++){
            buffer[i] = current;
            current = chunk[i+1+offset];
        }

        String text = new String(buffer, StandardCharsets.UTF_8).trim();
        target.setText(text);
        Log.d(TAG, "read: TEXT_BYTES_LENGTH: " + i + " TEXT:" + text);
        i+=1;

        if (chunk.length == i+offset) { // No script to miss this one
            createPhrase(target);
            return chunkLength;
        }

        short scriptSize = (short) (chunk[i+offset] & 0xFF);
        int filesOffset = 0;
        i++;
        Log.d(TAG, "read: SCRIPT_SIZE: "+ scriptSize);
        for (int j = 0; j < scriptSize;) {
            int currentOffset = i+j+filesOffset+offset;
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
                        return chunkLength;
                    }

                    byte[] img = scriptImage.file;
                    filesOffset += img.length - 1;

                    ImageView imageView = new ImageView(context);
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(img, 0, img.length));
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.width = (int) (metrics.density * scriptImage.width);
                    params.height = (int) (metrics.density * scriptImage.height);
                    params.gravity = Gravity.START | Gravity.TOP;
                    imageView.setScaleX(.0f);
                    imageView.setScaleY(.0f);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    mRoot.addView(imageView,params);

                    imageView.setX(scriptImage.x * metrics.widthPixels);
                    imageView.setY(scriptImage.y * metrics.heightPixels);

                    imageView.animate().scaleY(1.0f).scaleX(1.0f).withEndAction(() ->
                                    imageView.animate().scaleX(.0f).scaleY(.0f).setStartDelay(1250).withEndAction(() ->
                                            mRoot.removeView(imageView)).start())
                            .start();
                    break;
                case 4: // Gif
                    ScriptGraphicsFile gifScript = ScriptDefinerUtils.Gif(chunk,currentOffset);

                    filesOffset += gifScript.file.length - 1;

                    GifView gifView = new GifView(context);
                    gifView.setSource(gifScript.file);

                    FrameLayout.LayoutParams par =
                            new FrameLayout.LayoutParams(gifView.width(), gifView.height());

                    par.leftMargin = (int) (gifScript.x * metrics.widthPixels);
                    par.topMargin = (int) (gifScript.y * metrics.heightPixels);

                    mRoot.addView(gifView, par);
                    gifView.play();

                    gifView.animate()
                            .setStartDelay(5500)
                            .alpha(0.0f)
                            .withEndAction(()-> mRoot.removeView(gifView)).start();

                    break;
                default:
                    Utilities.showMessage("Invalid command index: " + commandIndex, mContext);
                    break;
            }
            j += argSize;
        }
        createPhrase(target);
        return chunkLength;
    }
}
