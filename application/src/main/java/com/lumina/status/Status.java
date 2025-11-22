package com.lumina.status;

import java.time.LocalDateTime;

/**
 * Domain model for tracking meter communication status and signal strength.
 *
 * <p>This interface is currently unused but represents a planned feature for monitoring meter
 * health and connectivity.
 *
 * <p>Future implementation should include:
 *
 * <ul>
 *   <li>Status repository and service layer
 *   <li>REST endpoints for querying meter status
 *   <li>Background job to update status from meter communications
 *   <li>Alerts/notifications when meters go offline or have weak signal
 * </ul>
 *
 * @see <a href="https://github.com/yourorg/lumina/issues/XX">GitHub Issue #XX</a> for tracking
 *     TODO: Create GitHub issue for Status feature implementation
 */
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
