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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import info.servertools.core.util.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.google.common.base.Preconditions.checkNotNull;
import static info.servertools.backup.ServerToolsBackup.LOG;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BackupCleanup {

    private static final Gson gson = new Gson();

    public static void run(final File backupDirectory) {
        final TreeSet<BackupMetadata> backups = scan(backupDirectory);
        checkOldBackups(backups);
        checkNumberBackups(backups);
        checkBackupDirSize(backupDirectory, backups);

    }

    private static void checkOldBackups(final TreeSet<BackupMetadata> backups) {
        if (BackupConfig.lifespanDays == -1) return;
        Iterator<BackupMetadata> iterator = backups.iterator();
        while (iterator.hasNext()) {
            BackupMetadata meta = iterator.next();
            long age = (System.currentTimeMillis() - meta.timestamp) / 86400000;
            LOG.debug("File: {}, Age: {}", meta.backupFile.getName(), age);
            if (age > BackupConfig.lifespanDays) {
                LOG.info("Backup Age: Removing old backup {}", meta.backupFile.getName());
                meta.backupFile.delete();
                iterator.remove();
            }
        }
    }

    private static void checkNumberBackups(final TreeSet<BackupMetadata> backups) {
        if (BackupConfig.maxNumberBackups == -1) return;
        Iterator<BackupMetadata> iterator = backups.iterator();
        while (backups.size() > BackupConfig.maxNumberBackups && iterator.hasNext()) {
            BackupMetadata meta = iterator.next();
            LOG.info("Number Backups: Removing old backup {}", meta.backupFile.getName());
            meta.backupFile.delete();
            iterator.remove();
        }
    }

    private static void checkBackupDirSize(final File backupDirectory, final TreeSet<BackupMetadata> backups) {
        if (BackupConfig.maxFolderSize == -1) return;
        Iterator<BackupMetadata> iterator = backups.iterator();
        while (FileUtils.getFolderSize(backupDirectory) > BackupConfig.maxFolderSize * org.apache.commons.io.FileUtils.ONE_MB && iterator.hasNext()) {
            System.out.println(FileUtils.getFolderSize(backupDirectory));
            BackupMetadata meta = iterator.next();
            LOG.info("Backups Size: Removing old backup {}", meta.backupFile.getName());
            meta.backupFile.delete();
            iterator.remove();
        }
    }

    private static TreeSet<BackupMetadata> scan(File backupDirectory) {
        checkNotNull(backupDirectory);
        TreeSet<BackupMetadata> backups = new TreeSet<>();
        if (!backupDirectory.exists() || !backupDirectory.isDirectory()) return backups;

        File[] files = backupDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".zip");
            }
        });

        if (files != null && files.length > 0) {
            for (final File file : files) {
                try (final ZipFile zip = new ZipFile(file)) {
                    final Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (entry.getName().equals("backupdata.json")) {
                            Backup.DataFile data;
                            try (InputStreamReader reader = new InputStreamReader(zip.getInputStream(entry))) {
                                data = gson.fromJson(reader, new TypeToken<Backup.DataFile>() {}.getType());
                            }
                            if (data != null) {
                                backups.add(new BackupMetadata(file, data.timestamp));
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.warn("Error ocurred while cleaning up backups, Couldn't open file as zipfile: {}", file.getName());
                    LOG.warn("", e);
                }
            }
        }

        return backups;
    }

    public static class BackupMetadata implements Comparable<BackupMetadata> {
        public final File backupFile;
        public final long timestamp;

        public BackupMetadata(File backupFile, long timestamp) {
            this.backupFile = backupFile;
            this.timestamp = timestamp;
        }

        @Override
        public int compareTo(@Nonnull BackupMetadata meta) {
            return Long.compare(this.timestamp, meta.timestamp);
        }
    }
}
