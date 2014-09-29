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

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class AutoBackupManager {

    private long lastBackup = 0;
    private int tick;

    public AutoBackupManager() {
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END)) return;

        tick++;
        if (tick >= 20) {
            if ((System.currentTimeMillis() - lastBackup) / 60000 >= BackupConfig.autoBackupInterval) {
                ServerToolsBackup.LOG.info("Starting AutoBackup");
                BackupManager.getInstance().doBackup();
                lastBackup = System.currentTimeMillis();
            }
            tick = 0;
        }
    }
}
