package good.damn.scriptengine.engines.script;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import good.damn.scriptengine.engines.script.interfaces.OnCreateScriptTextViewListener;
import good.damn.scriptengine.engines.script.interfaces.OnReadCommandListener;
import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;
import good.damn.scriptengine.engines.script.models.ScriptResourceFile;
import good.damn.scriptengine.engines.script.models.ScriptTextConfig;
import good.damn.scriptengine.engines.script.utils.ScriptCommandsUtils;
import good.damn.scriptengine.engines.script.utils.ScriptDefinerUtils;
import good.damn.scriptengine.engines.script.models.ScriptBuildResult;
import good.damn.scriptengine.interfaces.OnFileResourceListener;
import good.damn.scriptengine.utils.Utilities;
import good.damn.traceview.models.FileSVC;
import good.damn.traceview.utils.ByteUtils;
import good.damn.traceview.utils.FileUtils;

public class ScriptEngine {

    private static final String TAG = "ScriptEngine";

    public static final byte READ_BACKGROUND = 2;
    public static final byte READ_IMAGE = 3;
    public static final byte READ_GIF = 4;
    public static final byte READ_SFX = 5;
    public static final byte READ_AMBIENT = 6;
    public static final byte READ_VECTOR = 7;

    private final HashMap<String, ReadCommand> mReadCommands = new HashMap<>();

    private final SoundPool mSFXPool;

    private Object[] mResources;

    private OnCreateScriptTextViewListener mOnCreateScriptTextViewListener;

    private OnFileResourceListener mOnFileResourceListener;

    private OnReadCommandListener mOnReadCommandListener;

    public ScriptEngine() {
        initCommands();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();

            mSFXPool = new SoundPool.Builder()
                    .setMaxStreams(3)
                    .setAudioAttributes(audioAttributes)
                    .build();
            return;
        }

        mSFXPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
    }

    public void setOnCreateViewListener(OnCreateScriptTextViewListener configureViewListener) {
        mOnCreateScriptTextViewListener = configureViewListener;
    }

    public void setFileResourceListener(OnFileResourceListener listener) {
        mOnFileResourceListener = listener;
    }

    public void setReadCommandListener(OnReadCommandListener onReadCommandListener) {
        mOnReadCommandListener = onReadCommandListener;
    }

    public ScriptBuildResult compile(String line, Context context) {
        String[] argv = line.split("\\s+");

        ScriptBuildResult scriptBuildResult = new ScriptBuildResult();

        argv[0] = argv[0].trim();

        if (argv[0].isEmpty())
            return scriptBuildResult;

        ReadCommand command = mReadCommands.get(argv[0].toLowerCase());

        if (command == null) {
            Utilities.showMessage("Invalid command: " + argv[0], context);
        } else {
            scriptBuildResult.setCompiledScript(
                    command.read(argv,context,scriptBuildResult)
            );
        }

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
                    if (mOnReadCommandListener != null)
                        mOnReadCommandListener.onImage((Bitmap) mResources[scriptImage.resID],scriptImage);
                    break;
                case READ_GIF:
                    ScriptGraphicsFile gifScript = ScriptDefinerUtils.Gif(chunk,currentOffset);
                    if (mOnReadCommandListener != null)
                        mOnReadCommandListener.onGif((Movie) mResources[gifScript.resID], gifScript);
                    break;
                case READ_SFX:
                    sResFile = ScriptDefinerUtils.SFX(chunk,currentOffset);
                    if (mOnReadCommandListener != null) {
                        Log.d(TAG, "read: SFX: " + mResources[sResFile.resID] + " " + sResFile.resID);
                        mOnReadCommandListener.onSFX((Byte) mResources[sResFile.resID],mSFXPool);
                    }
                    break;
                case READ_AMBIENT:
                    sResFile = ScriptDefinerUtils.Ambient(chunk,currentOffset);
                    if (mOnReadCommandListener != null)
                        mOnReadCommandListener.onAmbient((MediaPlayer) mResources[sResFile.resID]);
                    break;
                case READ_VECTOR:
                    sResFile = ScriptDefinerUtils.Vector(chunk,currentOffset);
                    if (mOnReadCommandListener != null) {
                        mOnReadCommandListener.onVector((FileSVC) mResources[sResFile.resID],textConfig.mAdvancedText);
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

    public void loadResources(File skcFile, Context context) {

        try {
            FileInputStream fis = new FileInputStream(skcFile);
            byte[] bufInt = new byte[4];

            fis.read(bufInt);

            int resLength = ByteUtils.integer(bufInt);

            int resPosition = (int) (skcFile.length()-resLength-4);

            fis.skip(resPosition);

            byte resCount = (byte) fis.read();

            mResources = new Object[resCount];

            int prevPos = 0;
            int currentPos;
            int fileLength;

            int fileSectionPos = resCount * 4;

            byte sfxID = 1;
            byte[] file;

            String extension = "";

            for (byte i = 0; i < resCount; i++) {
                fis.read(bufInt); // end file position
                currentPos = ByteUtils.integer(bufInt);
                fileLength = currentPos - prevPos;

                Log.d(TAG, "loadResources: FILE_LENGTH: " + fileLength + " BUF_INT: " + Arrays.toString(bufInt));

                file = new byte[fileLength];

                int ret = fileSectionPos-(i+1)*4+prevPos;

                fis.skip(ret);
                // Read file content
                // Read header file
                byte h = (byte) fis.read();
                Log.d(TAG, "loadResources: HEADER: " +(h&0xff));
                fis.skip(-1);
                fis.read(file);

                if (h == 71) { // GIF
                    mResources[i] = Movie.decodeByteArray(file,0,file.length);
                    extension = "gif";
                } else if ((h & 0xff) == 137) { // PNG
                    mResources[i] = BitmapFactory.decodeByteArray(file,0,file.length);
                    extension = "png";
                } else if (h == 73) { // MP3
                    File tempFile = ScriptEngine.createTempFile(file, ".mp3",context);
                    extension = "mp3";
                    if (fileLength <= 1048576) { // 1 MB
                        mResources[i] = sfxID;
                        Log.d(TAG, "loadResources: SFX: " + i + " " + sfxID);
                        mSFXPool.load(tempFile.getAbsolutePath(),1);
                        sfxID++;
                    } else {
                        MediaPlayer player = MediaPlayer.create(context, Uri.fromFile(tempFile));
                        player.setLooping(true);
                        mResources[i] = player;
                    }
                } else { // vector content file
                    extension = "svc";
                    mResources[i] = FileUtils.retrieveSVCFile(file,context.getResources().getDisplayMetrics().density);
                }

                if (mOnFileResourceListener != null) {
                    mOnFileResourceListener.onFileResource(file,i,extension);
                }

                prevPos = currentPos;
                fis.skip(-fileLength-ret);
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void releaseResources(Context context) {

        mSFXPool.autoPause();
        mSFXPool.release();

        Log.d(TAG, "releaseResources: CLEARING TEMP FOLDER...");
        File file = new File(context.getCacheDir() + "/tempTopic");

        if (!file.exists()) {
            return;
        }

        File[] files = file.listFiles();
        if (files == null) {
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

        File temp = File.createTempFile(
                String.valueOf(System.currentTimeMillis()),
                extension,
                dir);

        FileOutputStream fos = new FileOutputStream(temp);
        fos.write(file);
        fos.close();

        return temp;
    }

    private void initCommands() {
        mReadCommands.put("textSize", (argv, context, scriptBuildResult) ->
                ScriptCommandsUtils.TextSize(argv,context));

        mReadCommands.put("font", (argv, context, scriptBuildResult) ->
                ScriptCommandsUtils.Font(argv,context));

        mReadCommands.put("bg", (argv, context, scriptBuildResult) ->
                ScriptCommandsUtils.Background(argv,context));

        mReadCommands.put("img", ScriptCommandsUtils::Image);
        mReadCommands.put("gif", ScriptCommandsUtils::Gif);

        mReadCommands.put("sfx", (argv, context, scriptBuildResult) ->
                ScriptCommandsUtils.SFX(argv,scriptBuildResult));

        mReadCommands.put("amb",
                (argv, context, scriptBuildResult) ->
                        ScriptCommandsUtils.Ambient(argv,scriptBuildResult));

        mReadCommands.put("vect", (argv, context, scriptBuildResult) ->
                ScriptCommandsUtils.Vector(argv,scriptBuildResult));
    }

    private interface ExecuteCommand {
        ScriptResourceFile execute();
    }

    private interface ReadCommand {
        byte[] read(String[] argv, Context context, ScriptBuildResult scriptBuildResult);
    }
}
