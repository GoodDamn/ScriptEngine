package good.damn.scriptengine.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
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
        return (short) (
                (a & 0xff) << 8 |
                (b & 0xff)
        );
    } // get number from bytes

    public static int gn(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) |
               ((bytes[offset+1] & 0xFF) << 16) |
               ((bytes[offset+2] & 0xFF) << 8) |
               ((bytes[offset+3] & 0xFF));
    }

    public static byte[] getBytesFromResources(String resourceName, Context context)
            throws IOException {
        return getBytesFromStorage(context.getCacheDir()+FileUtils.RES_DIR+"/"+resourceName);
    }

    public static byte[] getBytesFromExternalStorage(String userPath)
            throws IOException {
        return getBytesFromStorage("storage/emulated/0/"+userPath);
    }

    public static byte[] getBytesFromStorage(String path)
            throws IOException{
        FileInputStream inputStream = new FileInputStream(path);
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
        handler.post(() -> Toast.makeText(context, m, Toast.LENGTH_LONG).show());
    }
}
