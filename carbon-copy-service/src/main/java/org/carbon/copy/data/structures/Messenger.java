package org.carbon.copy.data.structures;

import java.util.UUID;
import java.util.concurrent.Future;

/**
 * This is mostly a facade around the galaxy messenger.
 * The added value by this implementation is bookkeeping around requests and responses
 * which are embedded in more handy and native java primitives (such as Futures).
 * Right now this messenger assumes you only do request-response-style communication.
 * More complicated protocols are not supported.
 */
interface Messenger {
    /**
     * This method is supposed to be used only when sending out a request.
     * Never send out responses using this method!
     */
    <T> Future<T> send(short toNode, BaseMessage message);

    /**
     * This method is only supposed to be used when replying to a request.
     * Never use this to send a request only send responses!
     */
    void replyTo(short toNode, UUID requestId, BaseMessage messageToSend);

    /**
     * This marks a particular request as completed and releases the holder
     * of the corresponding Future.
     */
    void complete(UUID requestId, Object result);
}
