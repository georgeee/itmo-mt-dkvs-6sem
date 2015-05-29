package ru.georgeee.itmo.sem6.dkvs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.disk.DiskLogReader;
import ru.georgeee.itmo.sem6.dkvs.disk.DiskLogger;
import ru.georgeee.itmo.sem6.dkvs.msg.PValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

class DiskLoggingAcceptor extends Acceptor {
    private static final Logger log = LoggerFactory.getLogger(DiskLoggingAcceptor.class);
    private final DiskLogger<PValue> diskLogger;

    public DiskLoggingAcceptor(AbstractController controller) throws FileNotFoundException {
        this(new File("acceptor." + controller.getId() + ".operations.log"), controller);
    }

    public DiskLoggingAcceptor(File file, AbstractController controller) throws FileNotFoundException {
        super(controller);
        try {
            restoreFromLog(file);
        } catch (IOException e) {
            log.warn("Error reading disk log " + file, e);
        }
        diskLogger = new DiskLogger<>(file);
    }

    private void restoreFromLog(File file) throws IOException {
        DiskLogReader<PValue> reader = new DiskLogReader<>(PValue.class, file);
        PValue pValue;
        while ((pValue = reader.readEntry()) != null) {
            super.accept(pValue);
        }
    }

    @Override
    protected void accept(PValue pValue) {
        diskLogger.writeEntry(pValue);
        super.accept(pValue);
    }
}
