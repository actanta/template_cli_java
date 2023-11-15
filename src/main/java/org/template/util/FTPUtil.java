package org.template.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.template.core.FTPLogin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
public class FTPUtil {

    public static String LOCAL_CHARSET = "GBK";
    public static String SERVER_CHARSET = StandardCharsets.ISO_8859_1.name();

    /**
     * Get FTP connection object
     *
     * @param host       FTP address
     * @param port       FTP port
     * @param username   FTP access username
     * @param password   FTP access password
     * @param bufferSize cache size
     * @return FTP connection object
     */
    public static FTPClient getFTPClient(String host, Integer port, String username, String password, Integer bufferSize) {
        if (StringUtils.isEmpty(host) || port == null || StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            log.error("Error ftp init arguments");
            return null;
        }
        log.info("ftp init {}:{} username:{} bufferSize:{}", host, port, username, bufferSize);
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.setDefaultTimeout(30000);
            ftpClient.setConnectTimeout(30000);
            ftpClient.setControlKeepAliveTimeout(Duration.ofSeconds(300));
            ftpClient.setDataTimeout(Duration.ofMillis(60000L));
            ftpClient.connect(host, port);
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                if (ftpClient.login(username, password)) {
                    if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
                        LOCAL_CHARSET = StandardCharsets.UTF_8.name();
                    }
                    ftpClient.setControlEncoding(LOCAL_CHARSET);
                    ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                    if("PORT".equals(FTPLogin.getModel())) {
                        ftpClient.setActivePortRange(50000,65000);
                        ftpClient.enterLocalActiveMode();//主动
                    }else {
                        ftpClient.enterLocalPassiveMode();//被动
                    }
                    if (bufferSize == null || bufferSize < 0) {
                        bufferSize = 1024 * 1024 * 128; //128MBytes
                    }
                    ftpClient.setBufferSize(bufferSize);
                    ftpClient.setSendDataSocketBufferSize(bufferSize);
                    ftpClient.setSendBufferSize(bufferSize);
                    log.info("SUCCESS connect ftp {}:{} {}", host, port, LOCAL_CHARSET);
                    return ftpClient;
                } else {
                    log.error("ftp login failed{}", getFTPErrorMsg(ftpClient));
                }
            } else {
                log.error("ftp connection failed{}", getFTPErrorMsg(ftpClient));
                disconnect(ftpClient);
            }
        } catch (Exception e) {
            log.error("ftp connection exception {} - {}", getFTPErrorMsg(ftpClient), e.getMessage());
            log.error("", e);
        }
        return null;
    }

    /**
     * disconnect
     *
     * @param client FTPClient
     */
    public static void disconnect(FTPClient client) {
        if (client != null) {
            if (client.isConnected()) {
                try {
                    client.logout();
                } catch (IOException e) {
                    log.error("ftp logout error: {} {}", getFTPErrorMsg(client), e.getMessage());
                }
                try {
                    client.disconnect();
                } catch (IOException e) {
                    log.error("ftp disconnect error: {} {}", getFTPErrorMsg(client), e.getMessage());
                }
            }
        }
    }

    /**
     * get ftp error msg
     *
     * @param client FTPClient
     * @return ftp error msg
     */
    public static String getFTPErrorMsg(FTPClient client) {
        String msg = "[ftpclient is null]";
        if (client != null) {
            msg = "[" + client.getReplyCode() + " " + client.getReplyString() + "]";
        }
        return msg;
    }


    /**
     * get encoded Path
     *
     * @param path unencoded path
     * @return encoded Path
     */
    public static String getEncodedPath(String path) {
        try {
            return new String(path.getBytes(LOCAL_CHARSET), SERVER_CHARSET);
        } catch (UnsupportedEncodingException e) {
            log.error("character encoding conversion exception:{} path:{}", e.getMessage(), path);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return path;
    }

}
