package good.damn.scriptengine.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.models.ResourceBuildResult;
import good.damn.scriptengine.models.ResourceReference;
import good.damn.traceview.utils.ByteUtils;

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
            currentPosition += resDir[i].length();
            fos.write(Utilities.gbInt(currentPosition));
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

    public static String mkSKCFile(ArrayList<Piece> arrayList, Activity activity) {
        return mkSKCFile("dumb.skc",
                activity.getCacheDir() + FileUtils.DUMB_DIR,
                arrayList,
                activity);
    }

    public static String mkSKCFile(String fileName,
                                   String dir,
                                   ArrayList<Piece> arrayList,
                                   Activity activity) {

        FileOutputStream fosSKC;
        FileInputStream fisRes = null;

        String path = dir;

        try {

            File file = new File(path);

            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }

                Log.d(TAG, "makeSKCFile: DUMB DIR HAS BEEN CREATED!");
                File skcFile = new File(file,fileName);
                if (!skcFile.createNewFile()) {
                    return null;
                }
            }

            Log.d(TAG, "makeSKCFile: DUMB FILE HAS BEEN CREATED!");

            path = dir+"/"+fileName;

            fosSKC = new FileOutputStream(path);

            // Compile resource section
            ResourceBuildResult result = mkSKResFile(activity);

            String[] compiledRes = null;
            int resSectionLength = 0;

            if (result != null) {
                compiledRes = result.getCompiledResources();
                resSectionLength = (int) result.getOutFile().length();
            }
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
            if (result != null) {
                fisRes = new FileInputStream(result.getOutFile());
                byte[] buffer = new byte[8192];

                int n;
                while ((n = fisRes.read(buffer)) != -1) {
                    fosSKC.write(buffer, 0, n);
                }
            }

        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }

        try {
            fosSKC.close();
            if (fisRes != null) {
                fisRes.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }
/*
    public static void mkSSEFile(String name, ArrayList<Piece> mPieces, Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    200);

            return;
        }

        try {
            File dir = new File();

            Log.d(TAG, "onClick: DIR: " + dir);

            if (dir.mkdirs()) {
                Log.d(TAG, "onClick: ScriptProjects DIR IS CREATED!");
            }

            File file = new File(dir,name);

            if (file.createNewFile()) {
                Log.d(TAG, "onClick: "+name+" IS CREATED!");
            }

            FileOutputStream fos = new FileOutputStream(file);

            fos.write(mPieces.size()); //0-255 pieces

            for (Piece piece: mPieces) {
                byte[] textPiece = piece.getString().toString()
                        .getBytes(StandardCharsets.UTF_8);

                String source = piece.getSourceCode();
                byte[] sourceCode = new byte[0];
                if (source != null) {
                    sourceCode = source
                            .getBytes(StandardCharsets.UTF_8);
                }

                fos.write(ByteUtils.Short((short) textPiece.length));
                fos.write(ByteUtils.Short((short) sourceCode.length));

                fos.write(textPiece);
                fos.write(sourceCode);

            }

            fos.close();

            Utilities.showMessage("SAVED " + name, activity);
        } catch (IOException e) {
            e.printStackTrace();
            Utilities.showMessage("ERROR: " + e.getMessage(), activity);
        }
    }*/
}
