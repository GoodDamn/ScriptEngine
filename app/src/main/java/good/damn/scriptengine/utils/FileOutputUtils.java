package good.damn.scriptengine.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import good.damn.scriptengine.models.Piece;

public class FileOutputUtils {

    private static final String TAG = "FileOutputUtils";
    
    public static boolean makeSKCFile(ArrayList<Piece> arrayList, Context context) {

        FileOutputStream fileOutputStream;
        
        try {

            String path = context.getCacheDir()
                    + FileUtils.DUMB_DIR;

            String fileName= "/dumb.skc";

            File file = new File(path);

            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return false;
                }

                Log.d(TAG, "makeSKCFile: DUMB DIR HAS BEEN CREATED!");
                File skcFile = new File(file + fileName);
                if (!skcFile.createNewFile()) {
                    return false;
                }
            }

            Log.d(TAG, "makeSKCFile: DUMB FILE HAS BEEN CREATED!");

            fileOutputStream = new FileOutputStream(file+fileName);

            for (Piece piece : arrayList) {
                fileOutputStream.write(piece.getChunk());
            }

        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }

        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
