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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

class MessageDecoder extends ReplayingDecoder<Message> {

    private boolean haveReadType = false;
    private Message.MessageType messageType;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        // read the message type first
        if (!haveReadType && in.readableBytes() > 0) {
            messageType = Message.MessageType.fromByte(in.readByte());
            haveReadType = true;
        } else if (!haveReadType) {
            checkpoint();
        }

        // then read the rest of the message
        if (in.readableBytes() >= messageType.byteSize) {
            Message m = Message.newMessage(messageType, in);
            list.add(m);
        } else {
            checkpoint();
        }
    }
}
