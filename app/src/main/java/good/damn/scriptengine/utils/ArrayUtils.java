package good.damn.scriptengine.utils;

import java.util.Arrays;

public class ArrayUtils {

    public static byte[] concatByteArrays(byte[]... arrays) {
        if (arrays.length == 0) {
            return new byte[0];
        }

        int var1 = 0;
        int var2;

        for(var2 = 0; var2 < arrays.length; ++var2) {
            var1 += arrays[var2].length;
        }

        byte[] var3 = Arrays.copyOf(arrays[0], var1);
        var1 = arrays[0].length;

        for(var2 = 1; var2 < arrays.length; ++var2) {
            byte[] var4 = arrays[var2];
            int var5 = var4.length;
            System.arraycopy(var4, 0, var3, var1, var5);
            var1 += var5;
        }

        return var3;
    }


    public static int bruteForceSearch(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(val)) {
                return i;
            }
        }

        return -1;
    }
}
