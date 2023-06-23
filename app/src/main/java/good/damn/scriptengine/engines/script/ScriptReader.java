package good.damn.scriptengine.engines.script;

import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import good.damn.scriptengine.interfaces.OnFileScriptListener;
import good.damn.scriptengine.utils.Utilities;
import good.damn.scriptengine.views.TextViewPhrase;

public class ScriptReader {

    private static final String TAG = "ScriptReader";

    private final ScriptEngine mScriptEngine;

    private FileInputStream mChunkStream;

    private int mChunkLength;
    private int mFileLength;

    private final byte[] mResourceCount = new byte[1];
    private final byte[] bFilePosition = new byte[4];

    private final byte[] mBuffer = new byte[2048];

    public ScriptReader(ScriptEngine scriptEngine, File file) {
        mScriptEngine = scriptEngine;
        mScriptEngine.setFileScriptListener(new OnFileScriptListener() {
            @Override
            public byte[] onResource(int resID) {
                try {
                    FileChannel fileChannel = mChunkStream.getChannel();
                    long savedPos = fileChannel.position();

                    Log.d(TAG, "onResource: SKIP: " +mChunkStream.skip(mChunkLength-savedPos)); // move to resource section

                    Log.d(TAG, "onResource: RESOURCE POSITION: " + fileChannel.position() + " RES_ID: " + resID);


                    mChunkStream.read(mResourceCount);
                    Log.d(TAG, "onResource: RESOURCE_COUNT: " + mResourceCount[0]);


                    int nextFilePosition;

                    int sk;

                    if (resID != mResourceCount[0]) {
                        nextFilePosition = mFileLength;
                    } else {
                        sk = (resID+1)*4;
                        mChunkStream.skip(sk);
                        mChunkStream.read(bFilePosition);
                        nextFilePosition = Utilities.gn(bFilePosition,0);
                        mChunkStream.skip(-sk-4); // return to begin of res-section
                        Log.d(TAG, "onResource: NEXT_FILE_POS: " + nextFilePosition + " POSITION: " + fileChannel.position());
                    }

                    sk = resID* 4;
                    mChunkStream.skip(sk);
                    mChunkStream.read(bFilePosition);
                    int filePosition = Utilities.gn(bFilePosition,0);
                    int fileDLength = nextFilePosition-filePosition;

                    Log.d(TAG, "onResource: FILE_POSITION: " + Arrays.toString(bFilePosition) + " DECODED_LENGTH: " + fileDLength);

                    byte[] file = new byte[fileDLength];

                    mChunkStream.skip(-sk-4 + mResourceCount[0]*4 + filePosition); // move to begin position of files
                    mChunkStream.read(file);
                    Log.d(TAG, "onResource: FILE_TITLE: " + file[0] + " " + file[1] + " " + file[2]);

                    mChunkStream.skip(-fileChannel.position()+savedPos);// return to chunk position
                    Log.d(TAG, "onResource: RETURN TO POSITION: " + fileChannel.position());

                    return file;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        });

        try {
            mChunkStream = new FileInputStream(file);
            mChunkStream.read(mBuffer, 0, 4); // dismiss resource length
            mFileLength = (int) file.length();
            mChunkLength = (int) (mFileLength - Utilities.gn(mBuffer,0));

            Log.d(TAG, "ScriptReader: LENGTH: FILE: " + file.length() + " CHUNK LENGTH: " + mChunkLength);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void next() {
        if (mChunkStream == null) {
            Log.d(TAG, "next: CHUNK FILE INPUT STREAM IS NULL!");
            return;
        }

        byte[] chunkLengthBytes = new byte[4];

        try {

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
