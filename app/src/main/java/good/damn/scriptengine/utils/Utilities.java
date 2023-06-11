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

    public static byte[] gb(short number){ // big-endian (getBytesFromNumber)
        return new byte[] {
                (byte) (number >> 8),
                (byte) (number & 0xFF)
        };
    }

    public static byte[] gbInt(int number) {
        return new byte[] {
                (byte) (number >> 24),
                (byte) ((number >> 16) & 0xFF),
                (byte) ((number >> 8) & 0xFF),
                (byte) (number & 0xFF)
        };
    }

    public static short gn(byte a, byte b){
        return (short) (a << 8 | b);
    } // get number from bytes

    public static int gn(byte[] bytes, int offset) {
        return (bytes[offset] << 24) |
               (bytes[offset+1] << 16) |
               (bytes[offset+2] << 8) |
               (bytes[offset+3]);
    }

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
