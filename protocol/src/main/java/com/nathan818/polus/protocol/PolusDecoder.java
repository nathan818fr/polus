package com.nathan818.polus.protocol;

import com.nathan818.hazel.protocol.HazelPacket;
import com.nathan818.hazel.protocol.HazelPacketType;
import com.nathan818.polus.protocol.exception.InputNotConsumedException;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.PolusPacket.Recipient;
import com.nathan818.polus.protocol.packet.QuitPacket;
import com.nathan818.polus.protocol.packet.initial.LoginPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class PolusDecoder extends MessageToMessageDecoder<HazelPacket> {
    private static final Logger logger = LoggerFactory.getLogger(PolusDecoder.class);
    private static final boolean LOG_DECODER_FAILURES = Boolean.parseBoolean(System.getProperty("com.nathan818.polus.protocol.logDecoderFailures", "false"));

    private final Recipient recipient;
    private boolean hello;

    @Override
    protected void decode(ChannelHandlerContext ctx, HazelPacket in, List<Object> out) {
        ByteBuf data = in.content();
        int logOffset = 0;
        try {
            switch (in.type()) {
                case HELLO: {
                    logOffset = LOG_DECODER_FAILURES ? data.readerIndex() : 0;
                    PolusPacket packet = new LoginPacket();
                    packet.read(data, recipient);
                    // don't check if it was fully consumed (to improve new protocols support and let implementation
                    // handle them)
                    out.add(packet);
                    break;
                }

                case UNRELIABLE:
                case RELIABLE: {
                    do {
                        logOffset = LOG_DECODER_FAILURES ? data.readerIndex() : 0;
                        int dataLen = data.readUnsignedShortLE();
                        int packetId = data.readUnsignedByte();
                        ByteBuf packetData = data.readSlice(dataLen);

                        PolusPacket packet = PolusProtocol.get(recipient.isServerChannel()).createPacket(packetId);
                        packet.reliable(in.type() == HazelPacketType.RELIABLE);
                        packet.read(packetData, recipient);
                        if (packetData.isReadable()) {
                            throw new InputNotConsumedException(packetData.readableBytes());
                        }
                        out.add(packet);
                    } while (data.isReadable());
                    break;
                }

                case DISCONNECT: {
                    logOffset = LOG_DECODER_FAILURES ? data.readerIndex() : 0;
                    PolusPacket packet = new QuitPacket();
                    packet.read(data, recipient);
                    // don't check if it was fully consumed
                    out.add(packet);
                    break;
                }

                case PING:
                case ACKNOWLEDGEMENT: {
                    // NTD
                    return;
                }

                default:
                    // theoretically unreachable
                    throw new UnsupportedOperationException("Unhandled HazelPacket of type " + in.type());
            }
        } catch (Throwable t) {
            if (LOG_DECODER_FAILURES && data != null) {
                data.readerIndex(logOffset);
                logger.error(recipient + " - Packet decoding failed: " + t.getMessage()
                        + "\nType: " + in.type().name()
                        + ", Data: [" + data.readableBytes() + " bytes, at offset " + logOffset + "]"
                        + "\n" + ByteBufUtil.prettyHexDump(data));
                return;
            }
            throw t;
        }
    }
}
