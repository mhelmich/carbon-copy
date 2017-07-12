package org.carbon.copy.data.structures;

import java.util.UUID;
import java.util.concurrent.Future;

interface Messenger {
    <T> Future<T> send(short toNode, BaseMessage message);
    void replyTo(short toNode, UUID requestId, BaseMessage messageToSend);
    void complete(UUID requestId, Object result);
}
