package com.lumina.status;

import java.time.LocalDateTime;

/** Domain model for tracking meter communication status and signal strength. */
public sealed interface Status permits Status.Lorawan, Status.Modbus {
  record Lorawan(
      String id,
      String meterId,
      LocalDateTime lastCommunication,
      SignalStrength.Lorawan signalStrength,
      double batteryLevel)
      implements Status {}

  record Modbus(
      String id,
      String meterId,
      LocalDateTime lastCommunication,
      SignalStrength.Modbus signalStrength)
      implements Status {}

  sealed interface SignalStrength permits SignalStrength.Lorawan, SignalStrength.Modbus {
    record Lorawan(int rssi, int snr) implements SignalStrength {}
    ;

    record Modbus(int dbm) implements SignalStrength {}
    ;
  }
}
