package org.carbon.copy.data.structures;

import co.paralleluniverse.galaxy.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic listener wrapper that adds error handling.
 */
abstract class BaseMessageListener implements MessageListener {
    private static Logger logger = LoggerFactory.getLogger(BaseMessageListener.class);
    @Override
    public void messageReceived(short fromNode, byte[] bytes) {
        try {
            handle(fromNode, bytes);
        } catch (Exception xcp) {
            logger.error("Handling a message failed", xcp);
        }
    }

    protected abstract String getTopic();
    protected abstract void handle(short fromNode, byte[] bytes) throws Exception;
}
