package server;

import lib.message.KVMessage;

public interface IMessageHandler {
    KVMessage handleRequest(KVMessage request, ServerState state);
}
