package good.damn.traceview.utils;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;

import good.damn.traceview.animators.ParallelAnimator;
import good.damn.traceview.animators.SequenceAnimator;
import good.damn.traceview.graphics.Circle;
import good.damn.traceview.graphics.Entity;
import good.damn.traceview.graphics.Line;
import good.damn.traceview.graphics.editor.CircleEditor;
import good.damn.traceview.graphics.editor.EntityEditor;
import good.damn.traceview.models.FileSVC;

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static void mkSVCFile(LinkedList<EntityEditor> entities,
                                 byte fileType,
                                 String path,
                                 byte animator,
                                 Context context) {

        FileOutputStream fos;

        try {
            File file = new File(context.getCacheDir() + path);

            if (file.createNewFile()) {
                Log.d(TAG, "mkSVCFile: SVC_FILE HAS BEEN CREATED !");
            }

            fos = new FileOutputStream(file);

            byte fileConfig = (byte)
                    ((fileType << 4) // .svc type (0 - interactive, 1 - animation)
                     | animator); // animator (0 - Parallel, 1 - Sequence)
            fos.write(fileConfig);

            fos.write(entities.size()); // vectors size

            byte vectorType;

            for (EntityEditor entity : entities) {
                vectorType = 0; // line by default
                if (entity instanceof CircleEditor) {
                    vectorType = 1;
                }

                fos.write(vectorType);

                fos.write(ByteUtils.fixedPointNumber(entity.getStartNormalX()));
                fos.write(ByteUtils.fixedPointNumber(entity.getStartNormalY()));
                fos.write(ByteUtils.fixedPointNumber(entity.getEndNormalX()));
                fos.write(ByteUtils.fixedPointNumber(entity.getEndNormalY()));

                fos.write(ByteUtils.integer(entity.getColor()));
                fos.write(entity.getStrokeWidth());

                if (fileType == FileSVC.TYPE_ANIMATION) {
                    Log.d(TAG, "mkSVCFile: DURATION: " + entity.getDuration());
                    fos.write(ByteUtils.Short((short) entity.getDuration()));
                }
            }

            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileSVC retrieveSVCFile(InputStream fis, float density)
            throws IOException {
        Entity[] entities;
        byte fileConfig = (byte) fis.read();

        FileSVC fileSVC = new FileSVC();

        byte svcType = (byte) ((fileConfig >> 4) & 0xf);
        byte animationType = (byte) (fileConfig & 0xf);

        fileSVC.isInteractive = svcType == 0;

        switch (animationType) {
            case FileSVC.ANIMATOR_SEQUENCE:
                fileSVC.animator = new SequenceAnimator();
                break;
            case FileSVC.ANIMATOR_PARALLEL:
                fileSVC.animator = new ParallelAnimator();
                break;
        }

        Log.d(TAG, "retrieveSVCFile: SVC_TYPE: " + svcType + " ANIMATION_TYPE: " + animationType);

        byte countVectors = (byte) fis.read();

        byte[] shortBuffer = new byte[2];
        byte[] intBuffer = new byte[4];

        entities = new Entity[countVectors];

        float tempX;
        float tempY;

        for (byte i = 0; i < entities.length; i++) {

            Entity entity;

            switch (fis.read()) {
                case 1:
                    entity = new Circle(density);
                    break;
                default:
                    entity = new Line(density);
                    break;
            }

            entities[i] = entity;

            fis.read(shortBuffer);
            tempX = ByteUtils.fixedPointNumber(shortBuffer);

            fis.read(shortBuffer);
            tempY = ByteUtils.fixedPointNumber(shortBuffer);

            entity.setStartNormalPoint(tempX, tempY);

            fis.read(shortBuffer);
            tempX = ByteUtils.fixedPointNumber(shortBuffer);

            fis.read(shortBuffer);
            tempY = ByteUtils.fixedPointNumber(shortBuffer);

            entity.setEndNormalPoint(tempX,tempY);

            fis.read(intBuffer);
            entity.setColor(ByteUtils.integer(intBuffer));
            Log.d(TAG, "retrieveSVCFile: COLOR: FROM: " + Arrays.toString(intBuffer) + " TO: " + entity.getColor());

            entity.setStrokeWidth((byte) fis.read());

            if (fileSVC.isInteractive) {
                continue;
            }

            fis.read(shortBuffer);
            entity.setDuration(ByteUtils.Short(shortBuffer,0));
        }

        fis.close();

        fileSVC.entities = entities;

        return fileSVC;
    }

    public static FileSVC retrieveSVCFile(byte[] in, float density) {
        try {
            return retrieveSVCFile(new ByteArrayInputStream(in),density);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static FileSVC retrieveSVCFile(String path, Context context, float density) {
        try {
            return retrieveSVCFile(new FileInputStream(context.getCacheDir() + path),density);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
