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

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static info.servertools.backup.ServerToolsBackup.LOG;

public class UploadFTP {


    public UploadFTP(File backupFile){

        FTPClient ftpClient = new FTPClient();
        try {
            long start = System.currentTimeMillis();
            ftpClient.connect(BackupConfig.ftpServer, BackupConfig.ftpPort);
            ftpClient.login(BackupConfig.ftpUser, BackupConfig.ftpPass);
            ftpClient.enterLocalPassiveMode();  //TODO:FTP passivemode configurable

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            if(makeDirectories(ftpClient,BackupConfig.ftpDir)) {
                if(ftpClient.changeWorkingDirectory(BackupConfig.ftpDir)) {

                    InputStream inputStream = new FileInputStream(backupFile);

                    BackupManager.getInstance().sendMessage("Uploading to remote server");
                    LOG.info("Upload {} to remote server", backupFile.getName());
                    boolean done = ftpClient.storeFile(backupFile.getName(), inputStream);
                    inputStream.close();
                    if (done) {
                        long duration = (System.currentTimeMillis() - start) / 1000;
                        LOG.info("Upload completed in {} seconds", duration);
                        double speed = (backupFile.length() / duration) / 1048576.0F;
                        BackupManager.getInstance().sendMessage("Upload finished after " + duration + " seconds with " +
                                String.format("%.2f", speed) + " MB/s");
                    } else {
                        LOG.error("Error uploading backup :(");
                        LOG.error(ftpClient.getReplyString());
                        BackupManager.getInstance().sendMessage("Error uploading backup " + backupFile.getName());
                    }
                } else{
                    LOG.error("Error switching directory: "+ftpClient.getReplyString());
                }
            } else {
                LOG.error("Error creating directory: "+ ftpClient.getReplyString());
            }
        } catch (IOException ex) {
            LOG.error("Error uploading file",ex);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                LOG.error("Error closing remote connection",ex);
                //ex.printStackTrace();
            }
        }
    }

    /**
     * Creates a nested directory structure on a FTP server
     * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param dirPath Path of the directory, i.e /projects/java/ftp/demo
     * @return true if the directory was created successfully, false otherwise
     * @throws IOException if any error occurred during client-server communication
     */
    public static boolean makeDirectories(FTPClient ftpClient, String dirPath)
            throws IOException {
        String[] pathElements = dirPath.split("/");
        if (pathElements.length > 0) {
            for (String singleDir : pathElements) {
                if (!singleDir.isEmpty()) {
                    boolean existed = ftpClient.changeWorkingDirectory(singleDir);
                    if (!existed) {
                        boolean created = ftpClient.makeDirectory(singleDir);
                        if (created) {
                            //System.out.println("CREATED directory: " + singleDir);
                            ftpClient.changeWorkingDirectory(singleDir);
                        } else {
                            //System.out.println("COULD NOT create directory: " + singleDir);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
