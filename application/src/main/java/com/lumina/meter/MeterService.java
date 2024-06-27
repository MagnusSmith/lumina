package com.lumina.meter;

import com.lumina.NotFoundException;
import com.lumina.catalogue.ItemRepository;
import com.lumina.catalogue.model.CatalogueItem;
import com.lumina.meter.dto.MeterDto;
import com.lumina.meter.model.Meter;
import com.lumina.meter.validation.MeterValidator;
import com.lumina.validation.Errors;
import com.lumina.validation.LuminaValidationException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MeterService {

    private final MeterRepository repository;
    private final ItemRepository catalogueItemRepository;
    private final MeterValidator meterValidator;

    public MeterService(
            MeterRepository repository,
            ItemRepository catalogueItemRepository,
            MeterValidator meterValidator) {
        this.repository = repository;
        this.catalogueItemRepository = catalogueItemRepository;
        this.meterValidator = meterValidator;
    }

    public Meter create(Meter meter) {
        Errors errors = new Errors("meter");
        meterValidator.validate(meter, errors);

        if (errors.getErrorCount() > 0) {
            throw new LuminaValidationException(errors);
        }

        return repository.save(meter);
    }

    public Meter update(Meter meter) {
        Errors errors = new Errors("meter");
        meterValidator.validate(meter, errors);

        if (errors.getErrorCount() > 0) {
            throw new LuminaValidationException(errors);
        }
        var meterToUpdate =
                repository
                        .findById(meter.id())
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "The meter with id %s could not be found!".formatted(meter.id())));

        return repository.save(meter);
    }

    public CatalogueItem findCatalogueItemByModel(String model) {
        return catalogueItemRepository
                .findByModel(model)
                .orElseThrow(
                        () ->
                                new NotFoundException(
                                        "The meter model %s could not be found in the catalogue!".formatted(model)));
    }

    public Optional<Meter> findById(String id) {
        return repository.findById(id);
    }

    public List<Meter> findByLocationId(String locationId) {
        return repository.findByLocationId(locationId);
    }

    MeterDto toMeterDto(Meter meter, boolean withConstraints) {
        var catItem = findCatalogueItemByModel(meter.model());
        return MeterDto.from(catItem, meter, withConstraints);
    }
}
