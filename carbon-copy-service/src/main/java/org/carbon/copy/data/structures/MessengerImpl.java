package org.carbon.copy.data.structures;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

class MessengerImpl implements Messenger {
    private static final Logger logger = LoggerFactory.getLogger(MessengerImpl.class);
    // this concurrency hashmap does all the bookkeeping for requests that are in-flight
    // each request gets a UUID associated to it and then sent off
    // the response is expected to have the same request id in it's body
    // this way we can look up the corresponding future in the map and release the caller
    private static final ConcurrentHashMap<UUID, CarbonCopyFuture> inProgressRequests = new ConcurrentHashMap<>(16, 0.75f, 8);
    private co.paralleluniverse.galaxy.Messenger messenger;

    @Inject
    MessengerImpl(co.paralleluniverse.galaxy.Messenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public <T> Future<T> send(short toNode, BaseMessage messageToSend) {
        CarbonCopyFuture<T> f = new CarbonCopyFuture<>();

        // spin in a loop until you get a UUID that isn't in the map already
        // unlikely this ever runs more often than once ... but you never know
        UUID requestId;
        CarbonCopyFuture tempF;
        do {
            requestId = UUID.randomUUID();
            tempF = inProgressRequests.putIfAbsent(requestId, f);
        } while(tempF != null);

        // set the request id
        // this property of a message is managed here
        // because only now we know that the UUID doesn't exist already
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
        } else {
            logger.warn("Couldn't find future with id " + requestId);
        }
    }
}
