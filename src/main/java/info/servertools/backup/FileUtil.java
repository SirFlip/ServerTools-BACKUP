/*
 * Copyright 2014 ServerTools
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.servertools.backup;

import java.io.File;
import java.text.DecimalFormat;

import static info.servertools.backup.ServerToolsBackup.LOG;

public class FileUtil {
    public static final int KB = 1024;
    public static final int MB = KB * 1024;
    public static final int GB = MB * 1024;

    public static final double KB_D = 1024D;
    public static final double MB_D = KB_D * 1024D;
    public static final double GB_D = MB_D * 1024D;


    private static String form(double num) {
        DecimalFormat nf = new DecimalFormat();
        if (num >= GB_D) {
            num /= GB_D;
            num = (long) (num * 10D) / 10D;
            return nf.format(num) + " GB";
        } else if (num >= MB_D) {
            num /= MB_D;
            num = (long) (num * 10D) / 10D;
            return nf.format(num) + " MB";
        } else if (num >= KB_D) {
            num /= KB_D;
            num = (long) (num * 10D) / 10D;
            return nf.format(num) + " kB";
        }
        return nf.format(num) + " B";
    }

    public static long fileSize(File f){
        long length = 0;
        try {
            if (f == null || !f.exists()) { return 0;}
            if (f.isFile()) { return f.length();}
            else if (f.isDirectory()) {
                File[] files = f.listFiles();
                if (files != null && files.length > 0){
                    for (File file: files){
                         length += fileSize(file);
                    }
                }
            }
        }
        catch (Exception e){
            LOG.error(e);
        }
        return length;
    }


    public static String getSizeS(File f){
        return form(fileSize(f));
    }
}
