package info.servertools.backup;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import info.servertools.core.ServerTools;
import info.servertools.core.command.CommandManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = ServerToolsBackup.MOD_ID, name = ServerToolsBackup.MOD_ID)
public class ServerToolsBackup {

    public static final String MOD_ID = "ServerTools-BACKUP";
    public static final Logger LOG = LogManager.getLogger(MOD_ID);
    public static final File BACKUP_DIR = new File(ServerTools.serverToolsDir, "backup");

    static { //noinspection ResultOfMethodCallIgnored
        BACKUP_DIR.mkdirs();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        BackupConfig.init(new File(BACKUP_DIR, "backup.cfg"));

        if (BackupConfig.enableAutoBackup)
            new AutoBackupManager();

        CommandManager.registerSTCommand(new CommandBackup("backup"));
    }
}
