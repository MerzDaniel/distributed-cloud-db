package server;

import lib.message.kv.KVMessage;

public interface IMessageHandler {
    KVMessage handleRequest(KVMessage request, ServerState state);
}
