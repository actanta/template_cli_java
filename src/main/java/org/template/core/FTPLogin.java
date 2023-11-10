package org.template.core;

import com.beust.jcommander.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.template.util.FTPUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FTPLogin {

    public static void main(String[] args) {
        checkArgs(args);
        try {
            FTPClient ftpClient = FTPUtil.getFTPClient(host, port, username, password, null);
            if (ftpClient != null) {
                log.info("working directory:{}", ftpClient.printWorkingDirectory());
                FTPUtil.disconnect(ftpClient);
                log.info("ftp disconnect!");
            }
        } catch (Exception e) {
            log.error("FTPLogin error", e);
        }
    }


    public static void checkArgs(String[] args) {
        if (args.length == 0) {
            log.error("Error program arguments. e.g.:\n{}", "  java -jar cli.jar -h 10.10.10.10 -P 21 -u root -d /test/ --log -p");
            System.exit(0);
        }
        log.info("PROGRAM ARGUMENTS: host:{} port:{} username:{} len(password):{} directory:{} doLog:{}", host, port, username, password.length(), directory, doLog);
    }


    @Parameter
    private static List<String> parameters = new ArrayList<>();

    @Parameter(names = {"-h", "--host"}, description = "host")
    private static String host;

    @Parameter(names = {"-P", "--port"}, description = "port")
    private static Integer port = 21;

    @Parameter(names = {"-u", "--username"}, description = "username")
    private static String username;

    @Parameter(names = {"-p", "--password"}, description = "password", password = true)
    private static String password = "123456";

    @Parameter(names = {"-d", "--directory"}, description = "directory")
    private static String directory;

    @Parameter(names = {"-l", "--log"}, description = "doLog")
    private static Boolean doLog = false;
}
