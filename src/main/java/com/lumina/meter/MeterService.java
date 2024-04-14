package com.lumina.meter;

import com.lumina.NotFoundException;
import com.lumina.catalogue.ItemRepository;
import com.lumina.catalogue.model.CatalogueItem;
import com.lumina.meter.model.Meter;
import com.lumina.meter.model.info.LineInfo;
import com.lumina.meter.model.info.MeterInfo;
import com.lumina.meter.model.info.MeterInfoBuilder;
import java.util.List;
import java.util.Optional;

import com.lumina.meter.validation.MeterValidator;
import com.lumina.validation.Errors;
import com.lumina.validation.LuminaValidationException;
import org.springframework.stereotype.Service;

@Service
public class MeterService {

  private final MeterRepository repository;
  private final ItemRepository itemRepository;
  private final MeterValidator meterValidator;

  public MeterService(MeterRepository repository, ItemRepository itemRepository, MeterValidator meterValidator) {
    this.repository = repository;
    this.itemRepository = itemRepository;
    this.meterValidator = meterValidator;
  }

  public MeterInfo create(Meter meter) {
    var model = meter.model();
    var catalogueItem = getCatalogueItem(model);

    Errors errors = new Errors("meter");
    meterValidator.validate(meter, errors);

    if(errors.getErrorCount() > 0 ){
      throw new LuminaValidationException(errors);
    }


    var saved = repository.save(meter);
    return toMeterInfo(catalogueItem, saved);

  }


  MeterInfo toMeterInfo(CatalogueItem item, Meter meter){


    List<LineInfo> infoLines =
        item.constraints().stream()
            .map(
                c ->
                    meter.lines().stream()
                        .filter(l -> l.name().equalsIgnoreCase(c.name()))
                        .findAny()
                        .map(sl -> new LineInfo(sl, c))
                        .orElseGet(() -> new LineInfo(null, c)))
            .toList();

    var info =
        MeterInfoBuilder.builder()
            .id(meter.id())
            .locationId(meter.locationId())
            .model(meter.model())
            .name(item.name())
            .description(item.description())
            .level(item.level())
            .type(item.type())
            .manufacturer(item.manufacturer())
            .lines(infoLines)
            .build();

    return info;
  }

  CatalogueItem getCatalogueItem(String model){
  return
        itemRepository
            .findByModel(model)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "The meter model %s could not be found in the catalogue!"
                            .formatted(model)));
  }

  public Optional<MeterInfo> findById(String id) {
    return repository.findById(id).map(m -> toMeterInfo(getCatalogueItem(m.model()), m));
  }


  public List<MeterInfo> findByLocationId(String locationId){
    return repository.findByLocationId(locationId).stream()
        .map( m -> toMeterInfo(getCatalogueItem(m.model()), m)).toList();
  }
}
