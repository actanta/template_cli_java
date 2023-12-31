package org.template;

import com.beust.jcommander.JCommander;
import lombok.extern.slf4j.Slf4j;
import org.template.core.FTPTest;


@Slf4j
public class Main {

    public static void main(String[] args) {
        JCommander.newBuilder()
                .addObject(new FTPTest())
                .build()
                .parse(args);

        FTPTest.main(args);
    }

}
