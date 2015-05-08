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

import static info.servertools.backup.ServerToolsBackup.LOG;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BackupConfig {

    private static final Logger log = LogManager.getLogger();

    public static String backupsPath = "backup";
    private static final String defaultDateFormat ="YYYY-MM-dd_HH-mm-ss";
    public static SimpleDateFormat dateFormat = new SimpleDateFormat(defaultDateFormat);

    public static int lifespanDays = -1;
    public static int maxFolderSize = -1;
    public static int maxNumberBackups = -1;

    public static final Set<String> fileBlacklist = new HashSet<>();
    public static final Set<String> directoryBlackList = new HashSet<>();

    public static boolean sendBackupMessageToOps = true;
    public static boolean sendBackupMessageToUsers = true;
    public static final Set<String> backupMessageWhitelist = new HashSet<>();

    public static boolean enableAutoBackup = false;
    public static int autoBackupInterval = 1440;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init(File file) {

        Configuration config;

        try {
            config = new Configuration(file);
        } catch (Exception e) {
            log.fatal("Error loading configuration, deleting and retrying", e);

            if (file.exists()) {
                file.delete();
            }

            config = new Configuration(file);
        }

        config.load();

        String category;
        Property prop;
        String[] array;

        /* General Settings */
        category = "general";

        prop = config.get(category, "backupDir", backupsPath);
        prop.comment = "This is the root location where backups will be stored";
        backupsPath = prop.getString();


        prop = config.get(category, "backupDateFormat", defaultDateFormat);
        prop.comment = "Change the date formate to your wish. Default: "+defaultDateFormat;
        String tmp =prop.getString();
        try {
            dateFormat = new SimpleDateFormat(tmp);
        } catch (Exception e) {
            LOG.error("Unkowen date sequence '"+tmp+".'! Using: "+defaultDateFormat,e);
            dateFormat = new SimpleDateFormat(defaultDateFormat);
        }

        prop = config.get(category, "daysToKeepBackups", lifespanDays);
        prop.comment = "The number of days that the backup will be kept for, " +
                       "Set -1 to disable";
        lifespanDays = clampInt(prop, -1, Integer.MAX_VALUE);

        prop = config.get(category, "maxBackupDirSize", maxFolderSize);
        prop.comment = "The maximum size of the backup directory in Megabytes, " +
                       "Set to -1 to disable";
        maxFolderSize = clampInt(prop, -1, Integer.MAX_VALUE);

        prop = config.get(category, "maxNumberBackups", maxNumberBackups);
        prop.comment = "The maximum number of backups that will be kept in the backup directory, " +
                       "Set to -1 to disable";
        maxNumberBackups = clampInt(prop, -1, Integer.MAX_VALUE);

        prop = config.get(category, "sendBackupMessageToOps", sendBackupMessageToOps);
        prop.comment = "Send backup related messages to server operators";
        sendBackupMessageToOps = prop.getBoolean(sendBackupMessageToOps);

        prop = config.get(category, "sendBackupMessageToUsers", sendBackupMessageToUsers);
        prop.comment = "Send backup related messages to all users";
        sendBackupMessageToUsers = prop.getBoolean(sendBackupMessageToUsers);

        prop = config.get(category, "backupMessageWhitelist", "");
        prop.comment = "A Comma separated list of users to always send backup related messages to";
        array = prop.getString().split(",");
        if (array.length > 0) {
            Collections.addAll(backupMessageWhitelist, array);
        }

        prop = config.get(category, "fileBlackList", "");
        prop.comment = "Comma separated list of files to not back up";
        array = prop.getString().split(",");
        if (array.length > 0) {
            Collections.addAll(fileBlacklist, array);
        }
        fileBlacklist.add("level.dat_new"); /* Minecraft Temp File - Causes Backup Problems */

        prop = config.get(category, "directoryBlackList", "");
        prop.comment = "Comma separated list of directory names to not back up";
        array = prop.getString().split(",");
        if (array.length > 0) {
            Collections.addAll(directoryBlackList, array);
        }


        /* AutoBackup Settings */
        category = "autoBackup";

        prop = config.get(category, "enableAutoBackup", enableAutoBackup);
        prop.comment = "Enable automatic backups at specified intervals";
        enableAutoBackup = prop.getBoolean(enableAutoBackup);

        prop = config.get(category, "autoBackupInterval", autoBackupInterval);
        prop.comment = "The interval in minutes for the auto backup to occur";
        autoBackupInterval = prop.getInt(autoBackupInterval);

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static int clampInt(final Property prop, final int min, final int max) {
        final int intVal = prop.getInt();
        if (intVal < min) {
            log.warn("Property {} was set below the minimum of {}. Resetting to {}", prop.getName(), min, min);
            prop.set(min);
            return min;
        } else if (intVal > max) {
            log.warn("Property {} was set above the maximum of {}. Resetting to {}", prop.getName(), max, max);
            prop.set(max);
            return max;
        }
        return intVal;
    }
}
