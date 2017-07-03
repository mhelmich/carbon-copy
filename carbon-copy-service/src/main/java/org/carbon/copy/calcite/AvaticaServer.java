package org.carbon.copy.calcite;

public interface AvaticaServer {
    void start();
    void stop();
    void join() throws InterruptedException;
}
