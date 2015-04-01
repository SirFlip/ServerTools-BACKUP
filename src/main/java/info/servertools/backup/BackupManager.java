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

import info.servertools.core.util.ServerUtils;

import com.google.common.base.Strings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class BackupManager {

    private static BackupManager instance = null;

    private final File worldDirectory;
    private final File backupDirectory;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-YYYY_HH-mm-ss");

    private static final Map<Integer, Boolean> worldSavingCache = new HashMap<>(MinecraftServer.getServer().worldServers.length);

    private BackupManager() {
        LOG.info("Initializing ServerTools Backup Handler");

        if (Strings.isNullOrEmpty(BackupConfig.backupsPath)) {
            throw new RuntimeException("Backup path must be configured");
        }
        worldDirectory = DimensionManager.getWorld(0).getChunkSaveLocation();
        backupDirectory = new File(BackupConfig.backupsPath);

        LOG.trace("Backup directory is at {}", backupDirectory.getAbsolutePath());

        if (backupDirectory.exists() && !backupDirectory.isDirectory()) {
            throw new RuntimeException("A file exists with the same name as the configured backup directory");
        }

        //noinspection ResultOfMethodCallIgnored
        backupDirectory.mkdirs();
    }

    /** Start a server backup */
    public void doBackup() {
        saveChunks();
        lockSaving(); // Will be unlocked in the Backup thread

        new Thread() {
            @Override
            public void run() {
                try {
                    new Backup(worldDirectory, backupDirectory, getBackupName()).run();
                } catch (IOException e) {
                    LOG.error("Failed to run server backup", e);
                    sendMessage("Failed to run server backup: " + e.getMessage());
                }
            }
        }.start();
    }

    /**
     * Get the backup filename that should be used for the current backup. <i>Time dependent</i>
     *
     * @return the backup filename
     */
    String getBackupName() {
        return dateFormat.format(Calendar.getInstance().getTime()) + ".zip";
    }

    /**
     * Send a messsage to users who are configured to receive backup related messages
     *
     * @param message the message
     */
    public void sendMessage(@Nullable Object message) {
        ChatComponentText text = new ChatComponentText(String.valueOf(message));
        for (EntityPlayerMP player : ServerUtils.getAllPlayers()) {
            if (BackupConfig.sendBackupMessageToOps && ServerUtils.isOP(player.getGameProfile())) {
                player.addChatComponentMessage(text);
            } else if (BackupConfig.sendBackupMessageToUsers) {
                player.addChatComponentMessage(text);
            } else if (BackupConfig.backupMessageWhitelist.contains(player.getGameProfile().getName())) {
                player.addChatComponentMessage(text);
            }
        }
    }

    /** Save all chunks to disk. <b>WARNING: This must be done in the main thread!</b> */
    private static void saveChunks() {
        for (final WorldServer worldServer : MinecraftServer.getServer().worldServers) {
            try {
                worldServer.saveAllChunks(true, null);
                LOG.trace("Saved world: {}", worldServer.provider.dimensionId);
            } catch (MinecraftException e) {
                LOG.warn("Failed to save all chunk data to disk", e);
            }
        }
    }

    /** Prevent worlds from being saved */
    private static void lockSaving() {
        for (final WorldServer worldServer : MinecraftServer.getServer().worldServers) {
            worldSavingCache.put(worldServer.provider.dimensionId, worldServer.disableLevelSaving);
            worldServer.disableLevelSaving = true;
            LOG.trace("Disabled saving of world: {}", worldServer.provider.dimensionId);
        }
    }

    /** Remove the lock from world saving */
    public static void unlockSaving() {
        for (final WorldServer worldServer : MinecraftServer.getServer().worldServers) {
            if (worldSavingCache.containsKey(worldServer.provider.dimensionId)) {
                worldServer.disableLevelSaving = worldSavingCache.get(worldServer.provider.dimensionId);
                LOG.trace("Resetting world saving disable flag of world: {} to {}", worldServer.provider.dimensionId, worldServer.disableLevelSaving);
            }
        }
    }

    public static synchronized BackupManager getInstance() {
        if (instance == null)
            instance = new BackupManager();
        return instance;
    }

}
