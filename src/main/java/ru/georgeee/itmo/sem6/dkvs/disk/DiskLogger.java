package ru.georgeee.itmo.sem6.dkvs.disk;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConverter;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConvertible;

import java.io.*;

public class DiskLogger<T extends ArgsConvertible> {
    private static final Logger log = LoggerFactory.getLogger(DiskLogger.class);
    private final BufferedWriter writer;

    public DiskLogger(BufferedWriter writer) {
        this.writer = writer;
    }

    public DiskLogger(File file) throws FileNotFoundException {
        this(createBufferedWriter(file));
    }

    private static BufferedWriter createBufferedWriter(File file) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
    }

    public void writeEntry(T data) {
        try {
            String[] args = ArgsConverter.getArgs(data);
            String line = StringUtils.join(args, ' ');
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            log.error("Error while logging an object " + data, e);
        }
    }

}

