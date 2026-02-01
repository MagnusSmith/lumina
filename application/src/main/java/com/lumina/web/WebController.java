package com.lumina.web;

import com.lumina.catalogue.CatalogueItemService;
import com.lumina.client.ClientService;
import com.lumina.location.LocationService;
import com.lumina.meter.MeterService;
import com.lumina.project.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * MVC Controller for rendering HTML views via Thymeleaf.
 *
 * <p>This controller is responsible only for server-side rendering of HTML pages. All JSON API
 * endpoints are handled by dedicated REST controllers in their respective domain packages (e.g.,
 * ClientController, ProjectController, LocationController, CatalogueController).
 *
 * <p>HTMX and JavaScript in templates should call the REST API endpoints directly (e.g.,
 * /api/client, /api/project/client/{clientId}) rather than having duplicate endpoints here.
 */
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
    model.addAttribute("meters", meterService.findAllForView());
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
}
