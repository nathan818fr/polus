package com.nathan818.hazel.protocol;

import com.nathan818.netty.statefuludp.StatefulUdpServerChannel;
import com.nathan818.netty.statefuludp.StatefulUdpServerChildChannel;
import java.net.InetSocketAddress;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

public class HazelServerChildChannel extends StatefulUdpServerChildChannel<HazelServerChildChannel.Id> {
    private final @Getter HazelConnection connection = new HazelConnection(this);

    public HazelServerChildChannel(StatefulUdpServerChannel parent, Id udpId) {
        super(parent, udpId);
    }

    @Override
    protected InetSocketAddress remoteAddress0() {
        return udpId().sender();
    }

    @Accessors(fluent = true)
    @ToString(exclude = {"hashCode"})
    public static final class Id {
        private final @Getter InetSocketAddress sender;
        private final int hashCode;

        public Id(InetSocketAddress sender) {
            this.sender = sender;
            this.hashCode = sender.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Id id = (Id) o;
            return hashCode == id.hashCode && sender.equals(id.sender);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
