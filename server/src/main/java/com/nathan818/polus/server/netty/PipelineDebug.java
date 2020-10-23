package com.nathan818.polus.server.netty;

import com.nathan818.hazel.protocol.HazelPacket;
import com.nathan818.hazel.protocol.HazelPacketType;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.game.GameActionPacket;
import com.nathan818.polus.protocol.packet.game.GameActionToTargetPacket;
import com.nathan818.polus.protocol.packet.game.action.GameAction;
import com.nathan818.polus.protocol.packet.game.action.UnsupportedAction;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.nathan818.polus.server.netty.PipelineUtil.PACKET_DECODER;
import static com.nathan818.polus.server.netty.PipelineUtil.PACKET_ENCODER;

@UtilityClass
public class PipelineDebug {
    private static final Logger logger = LoggerFactory.getLogger(PipelineDebug.class);

    public static void withHazelDebug(ChannelPipeline pipeline, String prefix) {
        pipeline.addBefore(PACKET_DECODER, "hazel-debug-in", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                if (msg instanceof HazelPacket) {
                    HazelPacket packet = (HazelPacket) msg;
                    logger.info(prefix + "< Hazel:" + packet.type().name()
                            + (packet.type().isReliable() != HazelPacketType.ReliableType.NONE ? " (" + packet.reliableId() + ")" : "")
                            + (packet.hasData() ? "\n" + ByteBufUtil.prettyHexDump(packet.data()) : ""));
                }
                ctx.fireChannelRead(msg);
            }
        });
        pipeline.addBefore(PACKET_ENCODER, "hazel-debug-out", new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                if (msg instanceof HazelPacket) {
                    HazelPacket packet = (HazelPacket) msg;
                    logger.info(prefix + "> Hazel:" + packet.type().name()
                            + (packet.type().isReliable() != HazelPacketType.ReliableType.NONE ? " (" + packet.reliableId() + ")" : "")
                            + (packet.hasData() ? "\n" + ByteBufUtil.prettyHexDump(packet.data()) : ""));
                }
                ctx.write(msg, promise);
            }
        });
    }

    public static void withPolusDebug(ChannelPipeline pipeline, String prefix) {
        pipeline.addAfter(PACKET_DECODER, "polus-debug-in", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                if (msg instanceof PolusPacket) {
                    ByteBuf data = null;
                    if (msg instanceof GameActionPacket) {
                        data = getData(((GameActionPacket) msg).action());
                    } else if (msg instanceof GameActionToTargetPacket) {
                        data = getData(((GameActionToTargetPacket) msg).action());
                    }
                    boolean reliable = ((PolusPacket) msg).reliable();
                    logger.debug(prefix + "< "
                            + (!reliable ? "(unreliable) " : "")
                            + msg + (data != null ? "\n" + ByteBufUtil.prettyHexDump(data) : ""));
                }
                ctx.fireChannelRead(msg);
            }

            private ByteBuf getData(GameAction action) {
                if (action instanceof UnsupportedAction) {
                    return Unpooled.wrappedBuffer(((UnsupportedAction) action).data());
                }
                return null;
            }
        });
        pipeline.addAfter(PACKET_ENCODER, "polus-debug-out", new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                if (msg instanceof PolusPacket) {
                    logger.debug(prefix + "> " + msg);
                }
                ctx.write(msg, promise);
            }
        });
    }
}
