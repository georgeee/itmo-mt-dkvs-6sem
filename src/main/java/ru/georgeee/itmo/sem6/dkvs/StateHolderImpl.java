package ru.georgeee.itmo.sem6.dkvs;

import ru.georgeee.itmo.sem6.dkvs.msg.Op;
import ru.georgeee.itmo.sem6.dkvs.msg.OpResult;

import java.util.HashMap;
import java.util.Map;

public class StateHolderImpl implements StateHolder {
    private final Map<String, String> map;

    public StateHolderImpl() {
        this.map = new HashMap<>();
    }

    @Override
    public OpResult applyOp(Op op) {
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
