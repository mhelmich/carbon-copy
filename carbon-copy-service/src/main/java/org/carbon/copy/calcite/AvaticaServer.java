package org.carbon.copy.calcite;

public interface AvaticaServer {
    void start();
    void stop();
    int getPort();
    void join() throws InterruptedException;
}
