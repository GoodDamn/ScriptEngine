package good.damn.scriptengine.utils;

import android.text.SpannableString;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import good.damn.scriptengine.models.Piece;

public class FileReaderUtils {

    private static final String TAG = "FileReaderUtils";

    public static ArrayList<Piece> Txt(InputStream inputStream) throws IOException {
        ArrayList<Piece> arrayList = new ArrayList<>();

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line;

        while((line = bufferedReader.readLine()) != null) {
            byte[] textChunk = line.getBytes(StandardCharsets.UTF_8);
            arrayList.add(new Piece(
                    ArrayUtils.concatByteArrays(textChunk, new byte[]{0}),
                    line)
            );
        }

        inputStreamReader.close();
        bufferedReader.close();

        return arrayList;
    }

    public static byte[] getBytesFromFile(InputStream inputStream)
            throws IOException {

        byte[] buffer = new byte[512];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int n;

        do {
            n = inputStream.read(buffer);
            baos.write(buffer, 0, n);
        } while(n != -1);

        byte[] total = baos.toByteArray();

        inputStream.close();

        return total;
    }
}
