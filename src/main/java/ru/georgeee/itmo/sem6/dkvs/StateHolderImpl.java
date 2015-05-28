package ru.georgeee.itmo.sem6.dkvs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.msg.Op;
import ru.georgeee.itmo.sem6.dkvs.msg.OpResult;

import java.util.HashMap;
import java.util.Map;

public class StateHolderImpl implements StateHolder {
    private static final Logger log = LoggerFactory.getLogger(StateHolderImpl.class);
    private final Map<String, String> map;

    public StateHolderImpl() {
        this.map = new HashMap<>();
    }

    @Override
    public OpResult applyOp(Op op) {
        log.info("Applying op to state: {}", op);
        switch (op.getType()) {
            case GET_CONSISTENT:
            case GET:
                String value = map.get(op.getKey());
                if (value == null) {
                    return OpResult.createNotFoundResult();
                } else {
                    return OpResult.createValueResult(op.getKey(), value);
                }
            case SET:
                map.put(op.getKey(), op.getValue());
                return OpResult.createdStoredResult();
            case DELETE:
                String prev = map.remove(op.getKey());
                return OpResult.createDeletedResult(op.getKey(), prev);
        }
        return null;
    }
}
