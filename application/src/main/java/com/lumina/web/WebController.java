package com.lumina.web;

import com.lumina.catalogue.CatalogueItemService;
import com.lumina.client.ClientService;
import com.lumina.location.LocationService;
import com.lumina.meter.MeterService;
import com.lumina.project.ProjectService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {

  private final CatalogueItemService catalogueItemService;
  private final MeterService meterService;
  private final ClientService clientService;
  private final ProjectService projectService;
  private final LocationService locationService;

  public WebController(
      CatalogueItemService catalogueItemService,
      MeterService meterService,
      ClientService clientService,
      ProjectService projectService,
      LocationService locationService) {
    this.catalogueItemService = catalogueItemService;
    this.meterService = meterService;
    this.clientService = clientService;
    this.projectService = projectService;
    this.locationService = locationService;
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

  @GetMapping("/catalogue/item/{model}")
  public String catalogueItemDetail(@PathVariable String model, Model viewModel) {
    var item =
        catalogueItemService
            .findByModel(model)
            .orElseThrow(() -> new IllegalArgumentException("Catalogue item not found: " + model));
    viewModel.addAttribute("item", item);
    return "catalogue-detail :: detail";
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

  @GetMapping("/clients/{clientId}/projects")
  public String projects(@PathVariable String clientId, Model model) {
    var client =
        clientService
            .findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));
    var projects = projectService.findByClientId(clientId);

    model.addAttribute("title", "Projects - " + client.name());
    model.addAttribute("page", "projects");
    model.addAttribute("client", client);
    model.addAttribute("projects", projects);
    return "projects";
  }

  @GetMapping("/clients/{clientId}/projects/{projectId}/locations")
  public String locations(
      @PathVariable String clientId, @PathVariable String projectId, Model model) {
    var client =
        clientService
            .findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));
    var project =
        projectService
            .findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
    var locations = locationService.findByProjectId(projectId);

    model.addAttribute("title", "Locations - " + project.name());
    model.addAttribute("page", "locations");
    model.addAttribute("client", client);
    model.addAttribute("project", project);
    model.addAttribute("locations", locations);
    return "locations";
  }

  // JSON API endpoints for cascading dropdowns
  @GetMapping("/web/api/clients")
  @ResponseBody
  public List<Map<String, String>> getClients() {
    return clientService.findAll().stream()
        .map(client -> Map.of("id", client.id(), "name", client.name()))
        .toList();
  }

  @GetMapping("/web/api/projects/{clientId}")
  @ResponseBody
  public List<Map<String, String>> getProjectsByClient(@PathVariable String clientId) {
    return projectService.findByClientId(clientId).stream()
        .map(project -> Map.of("id", project.id(), "name", project.name()))
        .toList();
  }

  @GetMapping("/web/api/locations/{projectId}")
  @ResponseBody
  public List<Map<String, String>> getLocationsByProject(@PathVariable String projectId) {
    return locationService.findByProjectId(projectId).stream()
        .map(location -> Map.of("id", location.id(), "name", location.name()))
        .toList();
  }
}
