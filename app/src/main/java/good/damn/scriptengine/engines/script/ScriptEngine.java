package good.damn.scriptengine.engines.script;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import good.damn.scriptengine.engines.script.interfaces.OnConfigureViewListener;
import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;
import good.damn.scriptengine.engines.script.models.ScriptResourceFile;
import good.damn.scriptengine.engines.script.utils.ScriptCommandsUtils;
import good.damn.scriptengine.engines.script.utils.ScriptDefinerUtils;
import good.damn.scriptengine.interfaces.OnFileScriptListener;
import good.damn.scriptengine.engines.script.models.ScriptBuildResult;
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

    private OnFileScriptListener mOnFileScriptListener;

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

    public void setFileScriptListener(OnFileScriptListener mOnFileScriptListener) {
        this.mOnFileScriptListener = mOnFileScriptListener;
    }

    public void setRootViewGroup(ViewGroup root) {
        mRoot = root;
    }

    public Context getContext() {
        return mContext;
    }

    public ScriptBuildResult compile(String line) {
        byte[] args = null;

        String[] argv = line.split("\\s+");

        ScriptBuildResult scriptBuildResult = new ScriptBuildResult();

        Context context = et_target.getContext();

        argv[0] = argv[0].trim();

        if (argv[0].isEmpty())
            return scriptBuildResult;

        switch (argv[0].toLowerCase()){
            case "textsize": // 0
                args = ScriptCommandsUtils.TextSize(argv,et_target,context);
                break;
            case "font": // 1
                args = ScriptCommandsUtils.Font(argv,et_target,context);
                break;
            case "bg": // 2 background
                args = ScriptCommandsUtils.Background(argv,context);
                break;
            case "img": // put image on the screen 3
                args = ScriptCommandsUtils.Image(argv,context,scriptBuildResult);
                break;
            // global commands
            case "gif": // 4
                args = ScriptCommandsUtils.Gif(argv,context,scriptBuildResult);
                break;
            case "sfx": // 5
                args = ScriptCommandsUtils.SFX(argv,scriptBuildResult);
                break;
            case "amb":
                args = ScriptCommandsUtils.Ambient(argv,scriptBuildResult);
                break;
            default:
                Utilities.showMessage("Invalid command: " + argv[0], context);
                break;
        }

        scriptBuildResult.setCompiledScript(args);
        return scriptBuildResult;
    }

    public void read(byte[] chunk, TextViewPhrase target) {

        Context context = mRoot.getContext();

        int offset = 0;

        Log.d(TAG, "read: CHUNK_LENGTH: " + " CHUNK[0]:" + chunk[offset]);

        short textLength = Utilities.gn(chunk[offset],chunk[offset+1]);

        offset += 2;

        byte[] textBytes = new byte[textLength];
        System.arraycopy(chunk, offset,textBytes,0,textLength);

        String text = new String(textBytes, StandardCharsets.UTF_8).trim();
        target.setText(text);
        Log.d(TAG, "read: TEXT_BYTES_LENGTH: " + textBytes.length + " TEXT_LENGTH: " + textLength + " TEXT:" + text);

        int i = textLength;

        if (chunk.length == i+offset) { // No script to miss this one
            createPhrase(target);
            return;
        }

        short scriptSize = (short) (chunk[i+offset] & 0xFF);
        i++;
        Log.d(TAG, "read: SCRIPT_SIZE: "+ scriptSize);
        for (int j = 0; j < scriptSize;) {
            int currentOffset = i+j+offset;
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
                case 2: // bg
                    int color = ScriptDefinerUtils.Background(chunk,currentOffset);
                    Log.d(TAG, "read: BACKGROUND COLOR: " + color);
                    mRoot.setBackgroundColor(color);
                    break;
                case 3: // img
                    ScriptGraphicsFile scriptImage = ScriptDefinerUtils.Image(chunk,currentOffset);
                    if (scriptImage == null) {
                        return;
                    }

                    byte[] img = null;
                    if (mOnFileScriptListener != null) {
                        img = mOnFileScriptListener.onResource(scriptImage.resID);
                    }

                    if (img == null) {
                        return;
                    }


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
                case 4: // gif
                    ScriptGraphicsFile gifScript = ScriptDefinerUtils.Gif(chunk,currentOffset);

                    byte[] gif = null;
                    if (mOnFileScriptListener != null) {
                        gif = mOnFileScriptListener.onResource(gifScript.resID);
                    }

                    if (gif == null) {
                        return;
                    }

                    GifView gifView = new GifView(context);
                    gifView.setSource(gif);

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
                case 5: // SFX
                    ScriptResourceFile srf = ScriptDefinerUtils.SFX(chunk,currentOffset);

                    byte[] sfx = null;
                    if (mOnFileScriptListener != null) {
                        sfx = mOnFileScriptListener.onResource(srf.resID);
                    }

                    if (sfx == null) {
                        return;
                    }

                    try {
                        File tempSFX = File.createTempFile(String.valueOf(System.currentTimeMillis()),".mp3",context.getCacheDir());

                        FileOutputStream fos = new FileOutputStream(tempSFX);
                        fos.write(sfx);
                        fos.close();

                        MediaPlayer mediaPlayer = MediaPlayer.create(context,Uri.fromFile(tempSFX));

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                Log.d(TAG, "onCompletion: MEDIA_PLAYER_SFX: " + tempSFX.getName());
                                mediaPlayer.stop();
                                mediaPlayer.release();
                                if (tempSFX.delete()) {
                                    Log.d(TAG, "onCompletion: FILE HAS BEEN DELETED!");
                                }
                            }
                        });

                        mediaPlayer.start();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 6: // Ambient
                    ScriptResourceFile sResFile = ScriptDefinerUtils.Ambient(chunk,currentOffset);
                    byte[] ambientMusic = null;
                    if (mOnFileScriptListener != null) {
                        ambientMusic = mOnFileScriptListener.onResource(sResFile.resID);
                    }

                    if (ambientMusic == null) {
                        return;
                    }
                    break;
                default:
                    Utilities.showMessage("Invalid command index: " + commandIndex, mContext);
                    break;
            }
            j += argSize;
        }
        createPhrase(target);
    }
}
