package good.damn.scriptengine.utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class FileReaderUtils {

    private static final String TAG = "FileReaderUtils";

    public static String[] Txt(InputStream inputStream) throws IOException {

        byte[] buffer = new byte[512];

        while (true) {
            int n = inputStream.read(buffer);
            Log.d(TAG, "Txt: READ_LENGTH: " + n + " BUFFER: " + Arrays.toString(buffer));
            if (n == -1) {
                break;
            }
        }
        return null;
    }
}
