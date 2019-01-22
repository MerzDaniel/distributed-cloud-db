package server;

import lib.message.KvMessage.KVMessage;

public interface IMessageHandler {
    KVMessage handleRequest(KVMessage request, ServerState state);
}
