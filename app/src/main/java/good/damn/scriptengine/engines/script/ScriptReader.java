package good.damn.scriptengine.engines.script;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import good.damn.scriptengine.interfaces.ScriptReaderListener;
import good.damn.scriptengine.utils.Utilities;

public class ScriptReader {

    private static final String TAG = "ScriptReader";

    private ScriptReaderListener mScriptReaderListener;

    private final ScriptEngine mScriptEngine;

    private FileInputStream mChunkStream;

    private int mChunkLength;
    private int mFileLength;

    private final byte[] mBuffer = new byte[2048];

    public ScriptReader(ScriptEngine scriptEngine, File file) {
        mScriptEngine = scriptEngine;
        try {
            mChunkStream = new FileInputStream(file);
            mChunkStream.read(mBuffer, 0, 4); // dismiss resource length
            mFileLength = (int) file.length();
            mChunkLength = mFileLength - Utilities.gn(mBuffer,0);

            Log.d(TAG, "ScriptReader: LENGTH: FILE: " + file.length() + " CHUNK LENGTH: " + mChunkLength);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setScriptReaderListener(ScriptReaderListener listener) {
        mScriptReaderListener = listener;
    }

    public void next() {
        if (mChunkStream == null) {
            Log.d(TAG, "next: CHUNK FILE INPUT STREAM IS NULL!");
            return;
        }

        byte[] chunkLengthBytes = new byte[4];

        try {

            if (mChunkStream.getChannel().position() >= mChunkLength) { // eof
                Log.d(TAG, "next: END OF SCRIPTS");
                if (mScriptReaderListener != null) {
                    mScriptReaderListener.onReadFinish();
                }
                return;
            }
            
            mChunkStream.read(chunkLengthBytes);

            int chunkLength = Utilities.gn(chunkLengthBytes,0);
            Log.d(TAG, "next: CHUNK_LENGTH: " + chunkLength + " BYTES:" + Arrays.toString(chunkLengthBytes));
            
            mChunkStream.read(mBuffer,0,chunkLength+2);

            Log.d(TAG, "next: BUFFER: " + Arrays.toString(mBuffer));
            mScriptEngine.read(mBuffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
