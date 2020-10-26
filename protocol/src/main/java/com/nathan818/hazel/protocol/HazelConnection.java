package com.nathan818.hazel.protocol;

import com.nathan818.hazel.protocol.event.HazelDisconnectedEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HazelConnection {
    private static final int RELIABLE_TICKS_INTERVAL = 100;
    private static final int PING_MILLIS = 1500;
    private static final int TIMEOUT_MILLIS = PING_MILLIS * 6;

    private final Channel channel;
    private final Reliable reliable = new Reliable();
    private boolean isClosing;

    public void onRegister() {
        channel.eventLoop().scheduleWithFixedDelay(
                reliable::tick, RELIABLE_TICKS_INTERVAL, RELIABLE_TICKS_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void onRead(ByteBuf in, Consumer<Object> list) {
        if (isClosing) {
            return;
        }

        HazelPacket packet = HazelEncoder.decode(in);

        switch (packet.type().isReliable()) {
            case IN: {
                channel.writeAndFlush(
                        new HazelPacket(HazelPacketType.ACKNOWLEDGEMENT, packet.reliableId()),
                        channel.voidPromise());
                if (!reliable.handleReceived(packet.reliableId())) {
                    return;
                }
                break;
            }

            case OUT: {
                if (!reliable.handleAck(packet.reliableId())) {
                    return;
                }
                break;
            }
        }

        list.accept(packet.retain());
    }

    public void onWrite(HazelPacket out, Consumer<ByteBuf> list) {
        int reliableId;
        if (out.type().isReliable() == HazelPacketType.ReliableType.IN && out.reliableId() == -1) {
            reliableId = reliable.nextSendId();
        } else {
            reliableId = -1;
        }

        ByteBuf buf = null;
        try {
            try {
                if (reliableId != -1) {
                    out.reliableId(reliableId);
                }
                buf = HazelEncoder.encode(out, (size) -> channel.alloc().buffer(size));
            } catch (Throwable t) {
                reliable.rollbackSentId();
                throw t;
            }

            if (reliableId != -1) {
                reliable.registerSent(reliableId, out.type() == HazelPacketType.PING ? null : buf);
            }

            list.accept(buf);
            buf = null;
        } finally {
            if (buf != null) {
                buf.release();
            }
        }
    }

    public void onClose() {
        isClosing = true;
        reliable.clear();
    }

    private void disconnect(String reason) {
        if (!isClosing && channel.isActive()) {
            isClosing = true;
            channel.pipeline().fireUserEventTriggered(new HazelDisconnectedEvent(reason));
            channel.close();
        }
    }

    private class Reliable {
        private int ticksSinceLastSent;
        private int ticksSinceLastReceivedAck;
        private float avgPingMillis = 500.0F;

        private short sentCounter;
        private final Short2ObjectMap<ReliablePacket> sentPackets = new Short2ObjectLinkedOpenHashMap<>(256, 0.75F);
        private final ObjectCollection<ReliablePacket> sentPacketsValues = sentPackets.values();

        private final ShortSet missingPackets = new ShortOpenHashSet(256, 0.75F);
        private int lastReceivedId = -1;

        public int nextSendId() {
            ++sentCounter;
            return (sentCounter & 0xFFFF);
        }

        public void rollbackSentId() {
            --sentCounter;
        }

        public void registerSent(int reliableId, ByteBuf buf) {
            ticksSinceLastSent = 0;
            sentPackets.put((short) reliableId, new ReliablePacket(
                    ReferenceCountUtil.retain(buf), System.currentTimeMillis()));
            // TODO: disconnect if too many non-acknowledged outPackets?
        }

        public boolean handleAck(int reliableId) {
            ticksSinceLastReceivedAck = 0;

            ReliablePacket packet = sentPackets.remove((short) reliableId);
            if (packet == null) {
                return false;
            }
            packet.release();

            float elapsedTime = Math.max(0, Math.min(PING_MILLIS, packet.elapsedTimeMillis(System.currentTimeMillis())));
            avgPingMillis = avgPingMillis * 0.7F + elapsedTime * 0.3F;

            // TODO: add a callback mechanism?
            return true;
        }

        public boolean handleReceived(int reliableId) {
            boolean isLast;
            if (lastReceivedId == -1) {
                isLast = true;
                lastReceivedId = 0;
            } else {
                int overwritePointer = (short) (lastReceivedId - 32768) & 0xFFFF;
                if (overwritePointer < lastReceivedId) {
                    isLast = reliableId > lastReceivedId || reliableId <= overwritePointer;
                } else {
                    isLast = reliableId > lastReceivedId && reliableId <= overwritePointer;
                }
            }

            if (isLast) {
                int missingId = lastReceivedId + 1;
                int missingLen = reliableId - missingId;
                if (missingLen > 0) {
                    if (missingPackets.size() + missingLen > 16384) {
                        disconnect("Too many missing packets");
                        return false;
                    }
                    for (; missingId < reliableId; ++missingId) {
                        missingPackets.add((short) missingId);
                    }
                }

                lastReceivedId = reliableId;
                return true;
            }

            return missingPackets.remove((short) reliableId);
        }

        @SuppressWarnings("Java8CollectionRemoveIf")
        public void tick() {
            ++ticksSinceLastReceivedAck;
            if (ticksSinceLastReceivedAck >= TIMEOUT_MILLIS / RELIABLE_TICKS_INTERVAL) {
                disconnect("Connection timed out"); // TODO: details
                return;
            }

            ++ticksSinceLastSent;
            if (ticksSinceLastSent >= PING_MILLIS / RELIABLE_TICKS_INTERVAL) {
                channel.writeAndFlush(new HazelPacket(HazelPacketType.PING));
                return;
            }

            if (!sentPacketsValues.isEmpty()) {
                long time = System.currentTimeMillis();
                for (ObjectIterator<ReliablePacket> it = sentPacketsValues.iterator(); it.hasNext(); ) {
                    ReliablePacket packet = it.next();
                    if (!packet.tick(time)) {
                        it.remove();
                    }
                }
            }
        }

        public void clear() {
            for (ObjectIterator<ReliablePacket> it = sentPacketsValues.iterator(); it.hasNext(); ) {
                ReliablePacket packet = it.next();
                it.remove();
                packet.release();
            }
        }
    }

    @RequiredArgsConstructor
    private class ReliablePacket {
        private final ByteBuf buf;
        private final long sentTimeMillis;
        private long nextTimeoutMillis = computeTimeoutMillis();

        public boolean tick(long currentTimeMillis) {
            long elapsedTime = elapsedTimeMillis(currentTimeMillis);

            if (buf == null) {
                return elapsedTime < TIMEOUT_MILLIS;
            }

            if (elapsedTime >= TIMEOUT_MILLIS) {
                disconnect("Connection timed out"); // TODO: details
                return false;
            }

            if (elapsedTime >= nextTimeoutMillis) {
                nextTimeoutMillis += computeTimeoutMillis();
                channel.writeAndFlush(buf.retain());
            }
            return true;
        }

        public long elapsedTimeMillis(long currentTimeMillis) {
            return currentTimeMillis - sentTimeMillis;
        }

        private long computeTimeoutMillis() {
            return Math.max(50, Math.min(300, (long) (reliable.avgPingMillis * 1.5F)));
        }

        public void release() {
            ReferenceCountUtil.safeRelease(buf);
        }
    }
}
