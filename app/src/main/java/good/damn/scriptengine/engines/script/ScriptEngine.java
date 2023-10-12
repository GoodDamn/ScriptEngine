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

import androidx.annotation.NonNull;

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

    private SoundPool mSFXPool;

    private ResourceFile[] mResources;

    private OnCreateScriptTextViewListener mOnCreateScriptTextViewListener;

    private OnFileResourceListener mOnFileResourceListener;

    private OnReadCommandListener mOnReadCommandListener;

    private final ExecuteCommand[] mExecuteCommands = new ExecuteCommand[]{
            ScriptDefinerUtils::TextSize,
            ScriptDefinerUtils::Font,
            (chunk, currentOffset, argSize, textConfig) -> {
                int color = ScriptDefinerUtils.Background(chunk, currentOffset);
                Log.d(TAG, "read: BACKGROUND COLOR: " + color);
                mOnReadCommandListener.onBackground(color);
            },
            (chunk, currentOffset, argSize, textConfig) -> {
                ScriptGraphicsFile scriptImage = ScriptDefinerUtils.Image(chunk,currentOffset);
                if (scriptImage == null) {
                    return;
                }
                ResourceFile res = mResources[scriptImage.resID];
                scriptImage.fileName = res.fileName;
                mOnReadCommandListener.onImage((Bitmap) res.resource,scriptImage);
            },
            (chunk, currentOffset, argSize, textConfig) -> {
                ScriptGraphicsFile gifScript = ScriptDefinerUtils.Gif(chunk,currentOffset);
                if (gifScript == null) {
                    return;
                }
                ResourceFile res = mResources[gifScript.resID];
                gifScript.fileName = res.fileName;
                mOnReadCommandListener.onGif((Movie) res.resource, gifScript);
            },
            (chunk, currentOffset, argSize, textConfig) -> {
                ScriptResourceFile sResFile = ScriptDefinerUtils.SFX(chunk,currentOffset);
                if (sResFile == null) {
                    return;
                }
                ResourceFile res = mResources[sResFile.resID];
                Log.d(TAG, "read: SFX: " + res + " " + sResFile.resID);
                mOnReadCommandListener.onSFX((Byte) res.resource,mSFXPool, res.fileName);
            },
            (chunk, currentOffset, argSize, textConfig) -> {
                ScriptResourceFile sResFile = ScriptDefinerUtils.Ambient(chunk,currentOffset);
                if (sResFile == null) {
                    return;
                }
                ResourceFile res = mResources[sResFile.resID];
                mOnReadCommandListener.onAmbient((MediaPlayer) res.resource,res.fileName);
            },
            (chunk, currentOffset, argSize, textConfig) -> {
                ScriptResourceFile sResFile = ScriptDefinerUtils.Vector(chunk,currentOffset);
                if (sResFile == null) {
                    return;
                }
                ResourceFile res = mResources[sResFile.resID];
                mOnReadCommandListener.onVector((FileSVC) res.resource,textConfig.mAdvancedText, res.fileName);
            },
    };

    public ScriptEngine(@NonNull OnReadCommandListener rr) {
        mOnReadCommandListener = rr;

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

    public void read(byte[] chunk) {

        int offset = 0;

        Log.d(TAG, "read: CHUNK: " + Arrays.toString(chunk));

        Log.d(TAG, "read: CHUNK_LENGTH: " + " CHUNK[0]:" + chunk[offset]);

        short textLength = Utilities.gn(chunk[offset], chunk[offset + 1]);

        offset += 2;

        byte[] textBytes = new byte[textLength];
        System.arraycopy(chunk, offset, textBytes, 0, textLength);

        String text = new String(textBytes, StandardCharsets.UTF_8).trim();
        ScriptTextConfig textConfig = new ScriptTextConfig();

        String[] advancedText = text.split("\\|");

        if (advancedText.length != 1) {
            textConfig.mAdvancedText = advancedText;
        }

        textConfig.spannableString = new SpannableString(advancedText[0]);

        Log.d(TAG, "read: TEXT_BYTES_LENGTH: " + textBytes.length + " TEXT_LENGTH: " + textLength + " TEXT:" + text);

        int i = textLength;

        if (chunk.length == i + offset) { // No script to miss this one
            mOnCreateScriptTextViewListener.onCreate(textConfig);
            return;
        }

        short scriptSize = (short) (chunk[i + offset] & 0xFF);
        i++;
        Log.d(TAG, "read: SCRIPT_SIZE: " + scriptSize);

        for (int j = 0; j < scriptSize; ) {
            int currentOffset = i + j + offset;
            short argSize = (short) (chunk[currentOffset] & 0xFF);
            currentOffset++;
            byte commandIndex = chunk[currentOffset];

            Log.d(TAG, "read: J: " + j + " SCRIPT_SIZE:" + scriptSize +
                    " OFFSET:" + currentOffset + " ARG_SIZE: " + argSize +
                    " COMMAND_INDEX: " + commandIndex);

            if (commandIndex > mExecuteCommands.length) {
                mOnReadCommandListener.onError("Invalid command Ref: " + commandIndex);
                continue;
            }

            mExecuteCommands[commandIndex]
                    .execute(chunk,currentOffset,argSize,textConfig);

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

            int resPosition = (int) (skcFile.length() - resLength - 4);

            fis.skip(resPosition);

            byte resCount = (byte) fis.read();

            mResources = new ResourceFile[resCount];

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

                int ret = fileSectionPos - (i + 1) * 4 + prevPos;

                fis.skip(ret);
                // Read file content
                // Read header file
                byte h = (byte) fis.read();
                Log.d(TAG, "loadResources: HEADER: " + (h & 0xff));
                fis.skip(-1);
                fis.read(file);

                ResourceFile resourceFile = new ResourceFile();

                if (h == 71) { // GIF
                    resourceFile.resource = Movie.decodeByteArray(file, 0, file.length);
                    extension = "gif";
                } else if ((h & 0xff) == 137) { // PNG
                    resourceFile.resource = BitmapFactory.decodeByteArray(file, 0, file.length);
                    extension = "png";
                } else if (h == 73) { // MP3
                    File tempFile = ScriptEngine.createTempFile(file, ".mp3", context);
                    extension = "mp3";
                    if (fileLength <= 1048576) { // 1 MB
                        resourceFile.resource = sfxID;
                        Log.d(TAG, "loadResources: SFX: " + i + " " + sfxID);
                        mSFXPool.load(tempFile.getAbsolutePath(), 1);
                        sfxID++;
                    } else {
                        MediaPlayer player = MediaPlayer.create(context, Uri.fromFile(tempFile));
                        player.setLooping(true);
                        resourceFile.resource = player;
                    }
                } else { // vector content file
                    extension = "svc";
                    resourceFile.resource = FileUtils.retrieveSVCFile(file, context.getResources().getDisplayMetrics().density);
                }

                resourceFile.fileName = i + "." + extension;

                mResources[i] = resourceFile;

                if (mOnFileResourceListener != null) {
                    mOnFileResourceListener.onFileResource(file, i, extension);
                }

                prevPos = currentPos;
                fis.skip(-fileLength - ret);
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

        for (File temp : files) {
            if (temp.delete()) {
                Log.d(TAG, "releaseResources:" + temp.getName() + " IS CLEARED FROM TEMP FOLDER");
            }
        }
    }

    public static File createTempFile(byte[] file, String extension, Context context) throws IOException {
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

    private static class ResourceFile {
        public Object resource;
        public String fileName;
    }

    private interface ExecuteCommand {
        void execute(byte[] chunk,
                     int currentOffset,
                     short argSize,
                     ScriptTextConfig textConfig);
    }
}
