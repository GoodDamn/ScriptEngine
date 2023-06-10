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
    
    public static String makeSKCFile(ArrayList<Piece> arrayList, Context context) {

        FileOutputStream fileOutputStream;

        String path = context.getCacheDir()
                + FileUtils.DUMB_DIR;

        try {

            String fileName = "/dumb.skc";

            File file = new File(path);

            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }

                Log.d(TAG, "makeSKCFile: DUMB DIR HAS BEEN CREATED!");
                File skcFile = new File(file + fileName);
                if (!skcFile.createNewFile()) {
                    return null;
                }
            }

            Log.d(TAG, "makeSKCFile: DUMB FILE HAS BEEN CREATED!");

            path += fileName;

            fileOutputStream = new FileOutputStream(path);

            for (Piece piece : arrayList) {
                fileOutputStream.write(piece.getChunk());
            }

        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }

        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }
}
