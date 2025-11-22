package com.lumina.web;

import com.lumina.catalogue.CatalogueItemService;
import com.lumina.client.ClientService;
import com.lumina.meter.MeterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

  private final CatalogueItemService catalogueItemService;
  private final MeterService meterService;
  private final ClientService clientService;

  public WebController(
      CatalogueItemService catalogueItemService,
      MeterService meterService,
      ClientService clientService) {
    this.catalogueItemService = catalogueItemService;
    this.meterService = meterService;
    this.clientService = clientService;
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("title", "Home - Lumina Meter Config");
    model.addAttribute("page", "home");
    return "index";
  }

  @GetMapping("/catalogue")
  public String catalogue(Model model) {
    model.addAttribute("title", "Catalogue - Lumina Meter Config");
    model.addAttribute("page", "catalogue");
    model.addAttribute("catalogueItems", catalogueItemService.findAll());
    return "catalogue";
  }

  @GetMapping("/meters")
  public String meters(Model model) {
    model.addAttribute("title", "Meters - Lumina Meter Config");
    model.addAttribute("page", "meters");
    model.addAttribute("meters", meterService.findAll());
    return "meters";
  }

  @GetMapping("/clients")
  public String clients(Model model) {
    model.addAttribute("title", "Clients - Lumina Meter Config");
    model.addAttribute("page", "clients");
    model.addAttribute("clients", clientService.findAll());
    return "clients";
  }
}
