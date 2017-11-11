/*
 * Copyright 2017 Marco Helmich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.carbon.copy.grid;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class ServerClientTest {
    public void testBasic() throws IOException {
        int serverPort1 = getNewPort();
        int serverPort2 = getNewPort();
        Cache serverCache = new CacheImpl(serverPort1);
        Cache clientCache = new CacheImpl(serverPort2);
        try (UdpGridServer server = new UdpGridServer(serverPort1, serverCache)) {
            try (UdpGridClient client = new UdpGridClient("localhost", serverPort1, clientCache)) {
                client.send(new Message.GET(99, 123));
//                assertEquals(Message.MessageType.ACK, resp.type);

            }
        }
    }

    private int getNewPort() {
        Random r = new Random();
        int newPort = r.nextInt(65535 - 2048) + 2048;
        while (!isPortAvailable(newPort)) {
            newPort = r.nextInt(65535 - 2048) + 2048;
        }
        return newPort;
    }

    private boolean isPortAvailable(int port) {
        try {
            (new Socket("localhost", port)).close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
