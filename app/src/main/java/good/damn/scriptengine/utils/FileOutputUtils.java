package good.damn.scriptengine.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.models.ResourceBuildResult;

public class FileOutputUtils {

    private static final String TAG = "FileOutputUtils";

    private static ResourceBuildResult mkSKResFile(Context context) throws IOException {
        File[] resDir = new File(context.getCacheDir() + FileUtils.RES_DIR)
                .listFiles();

        if (resDir == null || resDir.length == 0) {
            return null;
        }

        File skresFile = new File(context.getCacheDir() + "/ref.skres");

        if (!skresFile.exists() && skresFile.createNewFile()) {
            Log.d(TAG, "mkSCKRefFile: FILE ref.skres HAS BEEN CREATED");
        }

        // It needs to surrounds by try/catch because this shit doesn't close your streams
        FileOutputStream fos = new FileOutputStream(skresFile);
        FileInputStream fis;
        int n;
        byte[] buffer = new byte[2048];

        String[] fileNames = new String[resDir.length];

        for (byte i = 0; i < resDir.length; i++) {

            fis = new FileInputStream(resDir[i]);

            fos.write(i); // write resID
            // write resource length
            fos.write(Utilities.gbInt((int) resDir[i].length()));

            while ((n = fis.read(buffer)) != -1) {
                fos.write(buffer,0,n);
            }

            fis.close();

            fileNames[i] = resDir[i].getName();
        }

        fos.close();

        return new ResourceBuildResult(fileNames,skresFile);
    }

    public static String mkSKCFile(ArrayList<Piece> arrayList, Context context) {

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

            // Compile resource section
            ResourceBuildResult result = mkSKResFile(context);



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
