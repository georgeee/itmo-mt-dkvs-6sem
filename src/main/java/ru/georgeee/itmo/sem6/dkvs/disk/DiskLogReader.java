package ru.georgeee.itmo.sem6.dkvs.disk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConverter;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConvertible;

import java.io.*;

public class DiskLogReader<T extends ArgsConvertible> {
    private static final Logger log = LoggerFactory.getLogger(DiskLogReader.class);
    private final Class<T> clazz;
    private final BufferedReader reader;

    public DiskLogReader(Class<T> clazz, File file) throws FileNotFoundException {
        this(clazz, createBufferedReader(file));
    }

    public DiskLogReader(Class<T> clazz, BufferedReader reader) {
        this.clazz = clazz;
        this.reader = reader;
    }

    private static BufferedReader createBufferedReader(File file) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }

    public T readEntry() throws IOException {
        String line = reader.readLine();
        if (line != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                try {
                    return ArgsConverter.parse(clazz, line);
                } catch (RuntimeException e) {
                    log.error("Error parsing log entry, line " + line, e);
                    return readEntry();
                }
            }
        }
        return null;
    }

}
