package com.nathan818.polus.protocol.util;

import com.nathan818.polus.protocol.exception.IllegalProtocolStateException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProtocolPreconditions {
    public static void checkState(String errorMessage, boolean expression) {
        if (!expression) {
            throw new IllegalProtocolStateException(errorMessage);
        }
    }
}
