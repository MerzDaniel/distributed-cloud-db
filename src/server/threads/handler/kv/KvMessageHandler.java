package server.threads.handler.kv;

import lib.message.kv.KVMessage;
import lib.message.kv.KvMessageFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;
import server.threads.handler.MessageHandlerUtils;

import static lib.message.MessageUtil.isValidKey;
import static lib.message.MessageUtil.isValidValue;

public final class KvMessageHandler {
    static Logger logger = LogManager.getLogger(KvMessageHandler.class);

    public static KVMessage handleKvMessage(KVMessage kvMessage, ServerState state) {

        if (!MessageHandlerUtils.isResponsible(state, kvMessage.getKey(), kvMessage.getStatus())) {
            return KvMessageFactory.createServerNotResponsibleMessage(kvMessage.getKey(), state.meta.marshall());
        }

        if (!isValidKeyValueLength(kvMessage)) {
            logger.info(String.format("Key or Value are too long. Only a size for key/value of 20/120kb is allowed. key=%S | value=%s", kvMessage.getKey(), kvMessage.getValue()));
            return KvMessageFactory.createInvalidMessage();
        }

        switch (kvMessage.getStatus()) {
            case GET:
                return new GetHandler().handleRequest(kvMessage, state);
            case PUT:
                logger.debug(String.format("New PUT message from client: <%s,%s>", kvMessage.getKey(), kvMessage.getValue()));
                return new PutHandler().handleRequest(kvMessage, state);
            case DELETE:
                return KvMessageFactory.createDeleteErrorMessage();
            default:
                return KvMessageFactory.createInvalidMessage();
        }
    }

    private static boolean isValidKeyValueLength(KVMessage message) {
        switch (message.getStatus()) {
            case GET:
                return isValidKey(message.getKey());
            case PUT:
                return isValidKey(message.getKey()) && isValidValue(message.getValue());
            case DELETE:
                return isValidKey(message.getKey());
        }

        return true;
    }
}
