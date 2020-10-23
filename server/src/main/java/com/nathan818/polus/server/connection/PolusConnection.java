package com.nathan818.polus.server.connection;

import com.nathan818.polus.api.connection.Connection;
import com.nathan818.polus.protocol.packet.PolusPacket;

public interface PolusConnection extends Connection {
    void setProtocolVersion(int protocolVersion);

    void setName(String name);

    void sendPacket(PolusPacket packet);
}
