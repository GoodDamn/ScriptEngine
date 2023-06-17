package good.damn.scriptengine.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.models.ResourceBuildResult;
import good.damn.scriptengine.models.ResourceReference;

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

        // It needs to surrounds by try/catch because this shit
        // doesn't close your streams after throwing IOException
        FileOutputStream fos = new FileOutputStream(skresFile);
        FileInputStream fis;
        int n;
        byte[] buffer = new byte[2048];

        String[] fileNames = new String[resDir.length];

        fos.write(resDir.length);

        // O(2n)
        // write file's begin positions
        int currentPosition = 0;
        for (byte i = 0; i < resDir.length; i++) {
            fos.write(Utilities.gbInt(currentPosition));
            currentPosition += resDir[i].length();
        }

        //write each file's content
        for (byte i = 0; i < resDir.length; i++) {

            fis = new FileInputStream(resDir[i]);

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

        FileOutputStream fosSKC;
        FileInputStream fisRes;

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

            fosSKC = new FileOutputStream(path);

            // Compile resource section
            ResourceBuildResult result = mkSKResFile(context);

            String[] compiledRes = result.getCompiledResources();

            int resSectionLength = (int) result.getOutFile().length();

            fosSKC.write(Utilities.gbInt(resSectionLength));

            for (Piece piece : arrayList) {

                LinkedList<ResourceReference> references = piece.getResRef();

                byte[] chunk = piece.getChunk();

                Log.d(TAG, "mkSKCFile: COMPILED RESOURCES: " + Arrays.toString(compiledRes));
                if (references != null) {
                    for (ResourceReference ref: references) {
                        int index = ArrayUtils.bruteForceSearch(compiledRes,ref.getResName());
                        short textLength = Utilities.gn(chunk[4],chunk[5]);
                        int resPosition = 6+textLength+ref.getResPosition(); // 7 = 4(chunkLength) + 1(scriptSize) + 2(textLength)
                        Log.d(TAG, "mkSKCFile: RES_NAME: " + ref.getResName());
                        chunk[resPosition] = (byte) index;
                        Log.d(TAG, "mkSKCFile: RES_POSITION: " + resPosition + " INDEX: " + index + " CHUNK: " + Arrays.toString(chunk));
                    }
                }

                fosSKC.write(chunk);
            }

            // Write resources to .skc file

            fisRes = new FileInputStream(result.getOutFile());
            byte[] buffer = new byte[8192];

            int n;
            while ((n = fisRes.read(buffer)) != -1) {
                fosSKC.write(buffer,0,n);
            }

        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }

        try {
            fosSKC.close();
            fisRes.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }
}
