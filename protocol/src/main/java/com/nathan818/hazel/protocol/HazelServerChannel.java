package com.nathan818.hazel.protocol;

import com.nathan818.netty.statefuludp.StatefulUdpServerChannel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HazelServerChannel extends StatefulUdpServerChannel<HazelServerChildChannel.Id, HazelServerChildChannel> {
    private static final Logger logger = LoggerFactory.getLogger(HazelServerChannel.class);

    public HazelServerChannel(ChannelFactory<? extends DatagramChannel> channelFactory, int threadsCount) {
        super(channelFactory, threadsCount);
    }

    @Override
    protected HazelServerChildChannel createChildChannel(HazelServerChildChannel.Id udpId) {
        return new HazelServerChildChannel(this, udpId);
    }

    @Override
    protected void onRegister(HazelServerChildChannel childChannel) {
        childChannel.getConnection().onRegister();
    }

    @Override
    protected void onRead(ChannelHandlerContext ctx, DatagramPacket packet,
            BiConsumer<HazelServerChildChannel, Object> list) {
        ByteBuf in = packet.content();

        if (!in.isReadable()) {
            // TODO: event
            return;
        }

        HazelServerChildChannel childChannel;
        byte typeId = in.getByte(0);
        HazelServerChildChannel.Id id = new HazelServerChildChannel.Id(packet.sender());
        if (typeId == HazelPacketType.HELLO.id()) {
            childChannel = registerChildChannel(ctx.channel(), id);
        } else {
            childChannel = getChildChannel(id);
            if (childChannel == null) {
                // TODO: event
                return;
            }
        }

        try {
            childChannel.getConnection().onRead(in, (p) -> list.accept(childChannel, p));
        } catch (Throwable t) {
            childChannel.pipeline().fireExceptionCaught(t);
        }
    }

    @Override
    protected void onWrite(HazelServerChildChannel childChannel, Object packet, Consumer<ByteBuf> list) {
        if (packet instanceof HazelPacket) {
            childChannel.getConnection().onWrite((HazelPacket) packet, list);
        } else {
            list.accept(((ByteBuf) packet).retain());
        }
    }

    @Override
    protected void onClose(HazelServerChildChannel childChannel) {
        childChannel.getConnection().onClose();
    }
}
