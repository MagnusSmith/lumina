package com.lumina.catalogue.defaults;

import com.lumina.catalogue.CataloguePresetService;
import com.lumina.catalogue.model.Level;
import com.lumina.catalogue.model.MeterType;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class CatalogueInitializer implements ApplicationListener<ContextRefreshedEvent> {

  private final CataloguePresetService presetService;

  public CatalogueInitializer(CataloguePresetService presetService) {
    this.presetService = presetService;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {

    var lorawanGatewayIsPresent =
        presetService.findByTypeAndLevel(MeterType.LORAWAN, Level.GATEWAY).isPresent();

    if (!lorawanGatewayIsPresent) {
      presetService.create(LorawanGateway.preset());
    }

    var lorawanDeviceIsPresent =
        presetService.findByTypeAndLevel(MeterType.LORAWAN, Level.DEVICE).isPresent();

    if (!lorawanDeviceIsPresent) {
      presetService.create(LorawanDevice.preset());
    }
  }
}
