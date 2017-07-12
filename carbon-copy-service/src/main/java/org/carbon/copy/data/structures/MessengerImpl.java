package org.carbon.copy.data.structures;

import com.google.inject.Inject;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

class MessengerImpl implements Messenger {
    private static final ConcurrentHashMap<UUID, CarbonCopyFuture> inProgressRequests = new ConcurrentHashMap<>();
    private co.paralleluniverse.galaxy.Messenger messenger;

    @Inject
    MessengerImpl(co.paralleluniverse.galaxy.Messenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public <T> Future<T> send(short toNode, BaseMessage messageToSend) {
        CarbonCopyFuture<T> f = new CarbonCopyFuture<>();

        UUID requestId;
        CarbonCopyFuture tempF;
        do {
            requestId = UUID.randomUUID();
            tempF = inProgressRequests.putIfAbsent(requestId, f);
        } while(tempF != null);

        messageToSend.setRequestId(requestId);
        messageToSend.send(messenger, toNode);
        return f;
    }

    @Override
    public void replyTo(short toNode, UUID requestId, BaseMessage messageToSend) {
        messageToSend.setRequestId(requestId);
        messageToSend.send(messenger, toNode);
    }

    @Override
    public void complete(UUID requestId, Object result) {
        CarbonCopyFuture f = inProgressRequests.remove(requestId);
        if (f != null) {
            f.complete(result);
        }
    }
}
