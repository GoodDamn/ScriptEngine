package good.damn.scriptengine.engines.script;

import android.content.Context;
import android.text.SpannableString;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import good.damn.scriptengine.engines.script.interfaces.OnCreateScriptTextViewListener;
import good.damn.scriptengine.engines.script.interfaces.OnReadCommandListener;
import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;
import good.damn.scriptengine.engines.script.models.ScriptResourceFile;
import good.damn.scriptengine.engines.script.models.ScriptTextConfig;
import good.damn.scriptengine.engines.script.utils.ScriptCommandsUtils;
import good.damn.scriptengine.engines.script.utils.ScriptDefinerUtils;
import good.damn.scriptengine.interfaces.OnFileScriptListener;
import good.damn.scriptengine.engines.script.models.ScriptBuildResult;
import good.damn.scriptengine.utils.Utilities;

public class ScriptEngine {

    private static final String TAG = "ScriptEngine";

    public static final byte READ_BACKGROUND = 2;
    public static final byte READ_IMAGE = 3;
    public static final byte READ_GIF = 4;
    public static final byte READ_SFX = 5;
    public static final byte READ_AMBIENT = 6;
    public static final byte READ_VECTOR = 7;

    private OnCreateScriptTextViewListener mOnCreateScriptTextViewListener;

    private OnFileScriptListener mOnFileScriptListener;

    private OnReadCommandListener mOnReadCommandListener;

    public void setOnCreateViewListener(OnCreateScriptTextViewListener configureViewListener) {
        mOnCreateScriptTextViewListener = configureViewListener;
    }

    public void setFileScriptListener(OnFileScriptListener onFileScriptListener) {
        mOnFileScriptListener = onFileScriptListener;
    }

    public void setReadCommandListener(OnReadCommandListener onReadCommandListener) {
        mOnReadCommandListener = onReadCommandListener;
    }

    public ScriptBuildResult compile(String line, Context context) {
        byte[] args = null;

        String[] argv = line.split("\\s+");

        ScriptBuildResult scriptBuildResult = new ScriptBuildResult();

        argv[0] = argv[0].trim();

        if (argv[0].isEmpty())
            return scriptBuildResult;

        switch (argv[0].toLowerCase()){
            case "textsize": // 0
                args = ScriptCommandsUtils.TextSize(argv,context);
                break;
            case "font": // 1
                args = ScriptCommandsUtils.Font(argv,context);
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
            case "amb": // 6
                args = ScriptCommandsUtils.Ambient(argv,scriptBuildResult);
                break;
            case "vect": // 7
                args = ScriptCommandsUtils.Vector(argv,scriptBuildResult);
                break;
            default:
                Utilities.showMessage("Invalid command: " + argv[0], context);
                break;
        }

        scriptBuildResult.setCompiledScript(args);
        return scriptBuildResult;
    }

    public void read(byte[] chunk) {

        int offset = 0;

        Log.d(TAG, "read: CHUNK: " + Arrays.toString(chunk));

        Log.d(TAG, "read: CHUNK_LENGTH: " + " CHUNK[0]:" + chunk[offset]);

        short textLength = Utilities.gn(chunk[offset],chunk[offset+1]);

        offset += 2;

        byte[] textBytes = new byte[textLength];
        System.arraycopy(chunk, offset,textBytes,0,textLength);

        String text = new String(textBytes, StandardCharsets.UTF_8).trim();
        ScriptTextConfig textConfig = new ScriptTextConfig();

        String[] advancedText = text.split("\\|");

        if (advancedText.length != 1) {
            textConfig.mAdvancedText = advancedText;
        }

        textConfig.spannableString = new SpannableString(advancedText[0]);

        Log.d(TAG, "read: TEXT_BYTES_LENGTH: " + textBytes.length + " TEXT_LENGTH: " + textLength + " TEXT:" + text);

        int i = textLength;

        if (chunk.length == i+offset) { // No script to miss this one
            mOnCreateScriptTextViewListener.onCreate(textConfig);
            return;
        }

        short scriptSize = (short) (chunk[i+offset] & 0xFF);
        i++;
        Log.d(TAG, "read: SCRIPT_SIZE: "+ scriptSize);

        for (int j = 0; j < scriptSize;) {

            ScriptResourceFile sResFile = null;

            int currentOffset = i+j+offset;
            int argSize = chunk[currentOffset] & 0xFF;
            currentOffset++;
            byte commandIndex = chunk[currentOffset];

            Log.d(TAG, "read: J: "+ j + " SCRIPT_SIZE:" +scriptSize +
                    " OFFSET:" + currentOffset + " ARG_SIZE: " + argSize +
                    " COMMAND_INDEX: " + commandIndex);
            switch (commandIndex) {
                case 0: // textSize
                    ScriptDefinerUtils.TextSize(chunk,currentOffset,argSize,textConfig);
                    break;
                case 1: // font
                    ScriptDefinerUtils.Font(chunk,currentOffset,argSize,textConfig);
                    break;
                case READ_BACKGROUND: // bg
                    int color = ScriptDefinerUtils.Background(chunk,currentOffset);
                    Log.d(TAG, "read: BACKGROUND COLOR: " + color);

                    if (mOnReadCommandListener != null)
                        mOnReadCommandListener.onBackground(color);

                    break;
                case READ_IMAGE:
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

                    if (mOnReadCommandListener != null)
                        mOnReadCommandListener.onImage(img,scriptImage);

                    break;
                case READ_GIF:
                    ScriptGraphicsFile gifScript = ScriptDefinerUtils.Gif(chunk,currentOffset);

                    byte[] gif = null;
                    if (mOnFileScriptListener != null) {
                        gif = mOnFileScriptListener.onResource(gifScript.resID);
                    }

                    if (gif == null) {
                        return;
                    }

                    if (mOnReadCommandListener != null)
                        mOnReadCommandListener.onGif(gif, gifScript);
                    break;
                case READ_SFX:
                    sResFile = ScriptDefinerUtils.SFX(chunk,currentOffset);

                    byte[] sfx = null;
                    if (mOnFileScriptListener != null) {
                        sfx = mOnFileScriptListener.onResource(sResFile.resID);
                    }

                    if (sfx == null) {
                        return;
                    }

                    if (mOnReadCommandListener != null)
                        mOnReadCommandListener.onSFX(sfx);

                    break;
                case READ_AMBIENT:
                    sResFile = ScriptDefinerUtils.Ambient(chunk,currentOffset);
                    byte[] ambientMusic = null;
                    if (mOnFileScriptListener != null) {
                        ambientMusic = mOnFileScriptListener.onResource(sResFile.resID);
                    }

                    if (ambientMusic == null) {
                        return;
                    }

                    if (mOnReadCommandListener != null)
                        mOnReadCommandListener.onAmbient(ambientMusic);

                    break;
                case READ_VECTOR:
                    sResFile = ScriptDefinerUtils.Vector(chunk,currentOffset);
                    byte[] vect = null;
                    if (mOnFileScriptListener != null) {
                        vect = mOnFileScriptListener.onResource(sResFile.resID);
                    }

                    if (vect == null) {
                        return;
                    }

                    if (mOnReadCommandListener != null) {
                        mOnReadCommandListener.onVector(vect,textConfig.mAdvancedText);
                    }
                    break;
                default:
                    mOnReadCommandListener.onError("Invalid command index: " + commandIndex);
                    break;
            }
            j += argSize;
        }
        mOnCreateScriptTextViewListener.onCreate(textConfig);
    }

    public static void releaseResources(Context context) {
        Log.d(TAG, "releaseResources: CLEARING TEMP FOLDER...");
        File file = new File(context.getCacheDir() + "/tempTopic");

        if (!file.exists()) {
            return;
        }

        File[] files = file.listFiles();
        if (files == null){
            return;
        }

        for (File temp: files) {
            if (temp.delete()) {
                Log.d(TAG, "releaseResources:" + temp.getName() + " IS CLEARED FROM TEMP FOLDER");
            }
        }
    }

    public static File createTempFile(byte[] file,String extension,Context context) throws IOException {

        File dir = new File(context.getCacheDir() + "/tempTopic");

        if (!dir.exists()) {
            if (dir.mkdir()) {
                Log.d(TAG, "createTempFile: DIRECTORY HAS BEEN CREATED");
            }
        }

        File tempAmbient = File.createTempFile(
                String.valueOf(System.currentTimeMillis()),
                extension,
                dir);

        FileOutputStream fos = new FileOutputStream(tempAmbient);
        fos.write(file);
        fos.close();

        return tempAmbient;
    }
}
