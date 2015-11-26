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

import info.servertools.core.util.FileUtils;
import info.servertools.core.util.GsonUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static info.servertools.backup.ServerToolsBackup.LOG;

@SuppressWarnings("ResultOfMethodCallIgnored")
class Backup {

    private static final Object LOCK = new Object(); // Only run one backup at a time

    private final File sourceDir;
    private final File backupDir;

    private final String fileName;

    public Backup(File sourceDir, File backupDir, String fileName) throws IOException {
        this.sourceDir = sourceDir;
        this.backupDir = backupDir;
        this.fileName = fileName;

        if (!sourceDir.exists()) throw new FileNotFoundException("The given backup source path doesn't exist");
        if (!sourceDir.isDirectory()) throw new IOException("The given backup source path is a file");
    }

    public void run() throws IOException {
        synchronized (LOCK) {
            final File backupFileTmp = new File(backupDir, "tmp");
            LOG.info("Starting backup {}", backupFileTmp.getAbsolutePath());
            BackupManager.getInstance().sendMessage("Starting Server Backup");
            long start = System.currentTimeMillis();

            final File dataFile = new File(sourceDir, "backupdata.json");

            GsonUtils.writeToFile(new DataFile(), dataFile, ServerToolsBackup.LOG, true);
            FileUtils.zipDirectory(sourceDir, backupFileTmp, BackupConfig.fileBlacklist, BackupConfig.directoryBlackList);
            dataFile.delete();
            BackupManager.unlockSaving();

            long duration = (System.currentTimeMillis() - start) / 1000;
            LOG.info("Backup completed in {} seconds", duration);
            BackupManager.getInstance().sendMessage("Backup finished after " + duration + " seconds");

            final File backupFile = new File(backupDir, fileName);
            LOG.info("Rename backup {}", backupFile.getAbsolutePath());
            backupFileTmp.renameTo(backupFile);

            BackupCleanup.run(backupDir);
        }
    }

    public static class DataFile {
        @SuppressWarnings("CanBeFinal")
        public long timestamp = System.currentTimeMillis();
    }
}
