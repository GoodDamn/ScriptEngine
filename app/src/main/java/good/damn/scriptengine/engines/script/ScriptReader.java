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

    private final byte[] mBuffer = new byte[2048];

    public ScriptReader(ScriptEngine scriptEngine, File file) {
        mScriptEngine = scriptEngine;
        mScriptEngine.setFileScriptListener(new OnFileScriptListener() {
            @Override
            public void onResource(int resID) {
                try {
                    FileChannel fileChannel = mChunkStream.getChannel();
                    long savedPos = fileChannel.position();

                    Log.d(TAG, "onResource: SKIP: " +mChunkStream.skip(mChunkLength-savedPos)); // move to resource section

                    Log.d(TAG, "onResource: RESOURCE POSITION: " + fileChannel.position());

                    byte[] currentResID = new byte[1];

                    byte[] fileSize = new byte[4];

                    for (int p = 0; p <= resID;p++ ) {
                        mChunkStream.read(currentResID);
                        Log.d(TAG, "onResource: RESOURCE_ID: " + currentResID[0]);

                        mChunkStream.read(fileSize);

                        int fileLength = Utilities.gn(fileSize,0);

                        Log.d(TAG, "onResource: FILE_SIZE: " + Arrays.toString(fileSize) + " DECODED_LENGTH: " + fileLength);

                        if (currentResID[0] == resID) {
                            byte[] img = new byte[fileLength];
                            mChunkStream.read(img);

                            mChunkStream.skip(-fileChannel.position()+savedPos);// return to chunk position

                            Log.d(TAG, "onResource: RETURN TO POSITION: " + fileChannel.position());

                            break;
                        }


                        mChunkStream.skip(fileLength);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            mChunkStream = new FileInputStream(file);
            mChunkStream.read(mBuffer, 0, 4); // dismiss resource length
            mChunkLength = (int) (file.length() - Utilities.gn(mBuffer,0));

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
            e.printStackTrace();
        }
    }
}
