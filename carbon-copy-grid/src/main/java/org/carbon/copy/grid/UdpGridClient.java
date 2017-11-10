/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.carbon.copy.grid;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.Closeable;
import java.io.IOException;

class UdpGridClient implements Closeable, AutoCloseable {
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final ChannelFuture channelFuture;

    UdpGridClient(String host, int port, Cache cache) {
        Bootstrap b = new Bootstrap();
        b.group(workerGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new MessageDecoder(),
                            new MessageEncoder(),
                            new GridClientHandler(cache)
                    );
                }
            });

        channelFuture = b.bind(host, port);
    }

    void send(Message request) {
        Channel ch = channelFuture.channel();
        ch.writeAndFlush(request);
    }

    @Override
    public void close() throws IOException {
        try {
            channelFuture.channel().close();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
