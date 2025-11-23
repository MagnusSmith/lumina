package com.lumina.meter;

import com.lumina.meter.model.Meter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
    value = "lumina.meter.status.simulator.enabled",
    havingValue = "true",
    matchIfMissing = false)
public class StatusSimulatorTask {

  private final StatusService statusService;
  private final MeterRepository meterRepository;
  private final Random random;

  public StatusSimulatorTask(StatusService statusService, MeterRepository meterRepository) {
    this.statusService = statusService;
    this.meterRepository = meterRepository;
    this.random = new Random();
  }

  /**
   * Simulates status updates for all meters at configured intervals. The interval is controlled by
   * the spring property: lumina.meter.status.simulator.interval (in milliseconds)
   */
  @Scheduled(fixedDelayString = "${lumina.meter.status.simulator.interval:60000}")
  public void simulateStatusUpdates() {
    List<Meter> meters = meterRepository.findAll();

    if (meters.isEmpty()) {
      log.debug("No meters found. Skipping status simulation.");
      return;
    }

    log.info("Simulating status updates for {} meter(s)", meters.size());

    for (Meter meter : meters) {
      try {
        Map<String, Object> statusData = generateSimulatedData(meter);
        statusService.createStatus(meter.id(), statusData);
        log.debug("Created simulated status for meter: {}", meter.id());
      } catch (Exception e) {
        log.error("Failed to create status for meter {}: {}", meter.id(), e.getMessage(), e);
      }
    }
  }

  /**
   * Generates simulated status data for a meter. This includes random values for common meter
   * metrics.
   *
   * @param meter the meter to generate data for
   * @return map of status data
   */
  private Map<String, Object> generateSimulatedData(Meter meter) {
    Map<String, Object> data = new HashMap<>();

    // Common meter metrics
    data.put("batteryLevel", 50 + random.nextInt(50)); // 50-100%
    data.put("signalStrength", -120 + random.nextInt(70)); // -120 to -50 dBm
    data.put("temperature", 15 + random.nextInt(20)); // 15-35°C
    data.put("humidity", 30 + random.nextInt(50)); // 30-80%

    // Connection status
    data.put("connected", random.nextBoolean());
    data.put("lastSeen", System.currentTimeMillis());

    // Model-specific data
    data.put("model", meter.model());
    data.put("stage", meter.stage().name());

    // Random readings based on meter type
    if (meter.model().contains("LORAWAN")) {
      data.put("spreadingFactor", 7 + random.nextInt(6)); // SF7-SF12
      data.put("frameCounter", random.nextInt(10000));
    } else if (meter.model().contains("MODBUS")) {
      data.put("registerCount", 10 + random.nextInt(90)); // 10-100 registers
      data.put("errorCount", random.nextInt(5));
    }

    return data;
  }
}
