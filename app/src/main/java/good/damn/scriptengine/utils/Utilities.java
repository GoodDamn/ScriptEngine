package good.damn.scriptengine.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Utilities {

    private static final String TAG = "Utilities";

    public static byte[] gb(int number){ // big-endian (getBytesFromNumber)
        byte[] b = new byte[2];
        b[0] = (byte) (number/255);
        b[1] = (byte) (number%255);
        return b;
    }

    public static short gn(byte a, byte b){
        return (short) ((a & 0xFF) * 255 + (b & 0xFF));
    } // get number from bytes

    public static byte[] getBytesFromIS(String userPath)
            throws IOException {
        FileInputStream inputStream = new FileInputStream("storage/emulated/0/"+userPath);
        byte[] buffer = new byte[512];
        int n;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while ((n = inputStream.read(buffer)) != -1){
            byteArrayOutputStream.write(buffer);
        }
        inputStream.close();
        byte[] file = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return file;
    }

    public static void showMessage(String m, Context context) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(context, m, Toast.LENGTH_SHORT).show());
    }
}
