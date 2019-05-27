package com.sse.ftp.util;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * handy 意思是, 方便, 但不必要, 且不考虑性能, 且依赖使用者正确使用, 不做太多检查
 *
 * @author cgao
 */
public class FtHandyUtils {
    private static byte[] read(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        while (true) {
            int readed = is.read(buf);
            if (readed <= 0) {
                break;
            }
            os.write(buf, 0, readed);
        }
        is.close();
        return os.toByteArray();
    }

    public static String readFile(String file, String charset) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] r = read(fis);
            return new String(r, charset);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * check if last recorded time is up, if yes, record it again
     *
     * @param now
     * @param last
     * @param timeoutInMillis
     * @return
     */
    public static boolean timeIsUp(long now, AtomicLong last, long timeoutInMillis) {
        boolean r = false;
        if (last.get() == 0) {
            r = true;
        } else if (now - last.get() > timeoutInMillis) {
            r = true;
        }
        if (r) {
            last.set(now);
        }
        return r;
    }

    public static long emptyAsL0(String v) {
        return v == null ? 0 : Long.parseLong(v);
    }

    public static int emptyAsI0(Integer v) {
        return v == null ? 0 : v;
    }

    public static boolean emptyAsFalse(Boolean v) {
        return v == null ? false : v;
    }

    public static String trimAndEmpty(String v) {
        if (v == null) {
            return "";
        }
        return v.trim();
    }

    public static int pages(int rows, int rowsPerPage) {
        return rows % rowsPerPage == 0 ? rows / rowsPerPage : rows / rowsPerPage + 1;
    }

    // return how many fields copied
    public static int copyFields(Object to, Object from) {
        int copied = 0;
        for (Field f : from.getClass().getDeclaredFields()) {
            Field tf = null;
            try {
                tf = to.getClass().getDeclaredField(f.getName());
            } catch (NoSuchFieldException | SecurityException e) {
                ;
            }
            if (tf != null) {
                try {
                    tf.set(to, f.get(from));
                    copied++;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    ;
                }
            }
        }
        return copied;
    }

    public static String toDumpStr(Map<String, String> v) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : v.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append('\n');
        }
        return sb.toString();
    }

    public static String fillStrRight(String str, int needLen) {
        String subStr = subStr(str, needLen);
        StringBuilder sb = new StringBuilder();
        sb.append(subStr);
        try {
            for (int i = needLen; i > subStr.getBytes("GB18030").length; i--) {
                sb.append(" ");
            }
        } catch (UnsupportedEncodingException e) {
        }
        return sb.toString();
    }

    private static String subStr(String str, int subSLength) {
        if (str == null) {
            return "";
        } else {
            int tempSubLength = subSLength; // 截取字节数
            String subStr = str.substring(0, str.length() < subSLength ? str.length() : subSLength); //截取的字串
            int subStrBytsl;
            try {
                subStrBytsl = subStr.getBytes("GB18030").length;
                while (subStrBytsl > tempSubLength) {
                    int subSLengthTemp = --subSLength;
                    subStr = str.substring(0, subSLengthTemp > str.length() ? str.length() : subSLengthTemp);
                    subStrBytsl = subStr.getBytes("GB18030").length;
                }
            } catch (UnsupportedEncodingException e) {

            }
            return subStr;
        }

    }

}
