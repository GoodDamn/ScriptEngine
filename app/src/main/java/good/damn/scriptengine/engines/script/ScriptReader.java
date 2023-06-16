package good.damn.scriptengine.engines.script;

import android.security.FileIntegrityManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import good.damn.scriptengine.utils.Utilities;
import good.damn.scriptengine.views.TextViewPhrase;

public class ScriptReader {

    private static final String TAG = "ScriptReader";

    private final ScriptEngine mScriptEngine;

    private FileInputStream mChunkStream;
    private FileInputStream mResourceStream;


    private byte[] mBuffer = new byte[2048];

    public ScriptReader(ScriptEngine scriptEngine, File file) {
        mScriptEngine = scriptEngine;
        try {
            mChunkStream = new FileInputStream(file);
            mChunkStream.read(mBuffer, 0, 4); // dismiss resource length
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void next() {

        if (mChunkStream == null) {
            Log.d(TAG, "next: CHUNK FILE INPUT STREAM IS NULL!");
            return;
        }

        TextViewPhrase textViewPhrase = new TextViewPhrase(mScriptEngine.getContext());

        byte[] chunkLengthBytes = new byte[4];

        try {
            mChunkStream.read(chunkLengthBytes);
            int chunkLength = Utilities.gn(chunkLengthBytes,0);
            Log.d(TAG, "next: CHUNK_LENGTH: " + chunkLength + " BYTES:" + Arrays.toString(chunkLengthBytes));

            mChunkStream.read(mBuffer,0,chunkLength+2);

            Log.d(TAG, "next: BUFFER: " + Arrays.toString(mBuffer));
            mScriptEngine.read(mBuffer,textViewPhrase);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
