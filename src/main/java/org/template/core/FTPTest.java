package org.template.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.template.util.FTPUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Slf4j
public class FTPTest {

    public static void main(String[] args) {
        checkArgs(args);
        try {
            FTPClient ftpClient = FTPUtil.getFTPClient(host, port, username, password, null);
            if (ftpClient != null) {
                ftpClient.changeWorkingDirectory(FTPUtil.getEncodedPath(directory));
                log.info("working directory:{}", ftpClient.printWorkingDirectory());

                //上传
                String remote = "";
                byte[] data = null;
                if (StringUtils.isNotBlank(file)) {
                    remote = file;
                    data = FileUtil.readBytes(file);
                } else {
                    remote = StringUtils.isBlank(url) ? "/smkdata_20231112.csv.test" : url;
                }
                remote = directory + FileNameUtil.getName(remote);
                log.info("storeFileStream->{}", remote);
                OutputStream out = ftpClient.storeFileStream(FTPUtil.getEncodedPath(remote));
                if (out != null) {
                    log.info("write data->{}", remote);
                    out.write(data != null ? data : remote.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    out.close();
                    log.info("completePendingCommand->{}", remote);
                    boolean b = ftpClient.completePendingCommand();
                    log.info("upload end: {} {}", remote, b);
                } else {
                    log.warn("outputStream is null：{}", remote);
                }
                //打印文件信息
                FTPFile[] ftpFiles = ftpClient.listFiles(FTPUtil.getEncodedPath("/1.tgz"));
                if(ftpFiles!=null && ftpFiles.length == 1){
                    log.info("listFiles {}",ftpFiles[0]);
                    Thread.sleep(3000);
                }
                //下载
                InputStream inputStream = ftpClient.retrieveFileStream(FTPUtil.getEncodedPath("/1.tgz"));
                OutputStream outputStream = Files.newOutputStream(Paths.get(System.getProperty("user.dir") + "/" + UUID.randomUUID().toString()));
                long count;
                long writeCount=0L;
                byte[] buffer = new byte[8192];
                int n;
                for(count = 0L; -1 != (n = inputStream.read(buffer)); count += (long)n) {
                    log.info("retrieveCount:{} n:{} count:{}",count+n,n,count);
                    outputStream.write(buffer, 0, n);
                    writeCount+=n;
                    log.info("writeCount {}",writeCount);
                }
                IOUtils.copy(inputStream, outputStream);
                IOUtils.close(inputStream);
                IOUtils.close(outputStream);

                FTPUtil.disconnect(ftpClient);
                log.info("ftp disconnect!");
            }
        } catch (Exception e) {
            log.error("FTPLogin error", e);
        }
    }


    public static void checkArgs(String[] args) {
        if (args.length == 0) {
            log.error("Error program arguments. e.g.:\n{}", "  java -jar cli.jar -h 10.10.10.10 -P 21 -u root -d /test/ -m PORT -p");
            System.exit(0);
        }
        log.info("PROGRAM ARGUMENTS: host:{} port:{} username:{} len(password):{} directory:{} model:{}", host, port, username, password.length(), directory, model);
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

    @Getter
    @Parameter(names = {"-m", "--mode"}, description = "mode")
    private static String model;

    @Parameter(names = {"-url", "--url"}, description = "url")
    private static String url;

    @Parameter(names = {"-f", "--file"}, description = "local file")
    private static String file;
}
