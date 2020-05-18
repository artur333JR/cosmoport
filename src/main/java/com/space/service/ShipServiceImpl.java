package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipServiceImpl implements ShipService {
    private final ShipRepository shipRepository;

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public Ship read(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public List<Ship> readAll() {
        return shipRepository.findAll();
    }

    @Override
    public List<Ship> getFilteredShipList(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed,
                                          Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                                          Double minRating, Double maxRating) {

        return readAll().stream()
                .filter(o -> name == null || o.getName().contains(name))
                .filter(o -> planet == null || o.getPlanet().contains(planet))
                .filter(o -> shipType == null || o.getShipType().equals(shipType))
                .filter(o -> after == null || o.getProdDate().getTime() >= new Date(after).getTime())
                .filter(o -> before == null || o.getProdDate().getTime() <= new Date(before).getTime())
                .filter(o -> isUsed == null || o.isUsed().equals(isUsed))
                .filter(o -> minSpeed == null || o.getSpeed() >= minSpeed)
                .filter(o -> maxSpeed == null || o.getSpeed() <= maxSpeed)
                .filter(o -> minCrewSize == null || o.getCrewSize() >= minCrewSize)
                .filter(o -> maxCrewSize == null || o.getCrewSize() <= maxCrewSize)
                .filter(o -> minRating == null || o.getRating() >= minRating)
                .filter(o -> maxRating == null || o.getRating() <= maxRating)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ship> getShipsPerPage(List<Ship> ships, Integer pageNumber, Integer pageSize, ShipOrder shipOrder) {
        if (ships == null || ships.isEmpty())
            return ships;

        return ships.stream()
                .sorted(getComparator(shipOrder))
                .skip(pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    @Override
    public Ship create(Ship ship) {
        if (ship.isUsed() == null) {
            ship.setUsed(false);
        }

        ship.setSpeed((double) Math.round(ship.getSpeed() * 100) / 100);
        ship.setRating(calculateRating(ship));
        return shipRepository.save(ship);
    }

    @Override
    public Ship update(Ship ship, Long id) {
        Ship shipForUpdate = read(id);
        if (shipForUpdate == null)
            return null;

        shipForUpdate.setName(ship.getName() != null ? ship.getName() : shipForUpdate.getName());
        shipForUpdate.setPlanet(ship.getPlanet() != null ? ship.getPlanet() : shipForUpdate.getPlanet());
        shipForUpdate.setShipType(ship.getShipType() != null ? ship.getShipType() : shipForUpdate.getShipType());
        shipForUpdate.setProdDate(ship.getProdDate() != null ? ship.getProdDate() : shipForUpdate.getProdDate());
        shipForUpdate.setUsed(ship.isUsed() != null ? ship.isUsed() : shipForUpdate.isUsed());
        shipForUpdate.setSpeed(ship.getSpeed() != null ? ship.getSpeed() : shipForUpdate.getSpeed());
        shipForUpdate.setCrewSize(ship.getCrewSize() != null ? ship.getCrewSize() : shipForUpdate.getCrewSize());

        shipForUpdate.setRating(calculateRating(shipForUpdate));

        return shipRepository.save(shipForUpdate);
    }

    @Override
    public boolean delete(Long id) {
        if (shipRepository.existsById(id)) {
            shipRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Comparator<Ship> getComparator(ShipOrder shipOrder) {
        if (shipOrder == null) {
            return Comparator.comparingLong(Ship::getId);
        }

        Comparator<Ship> comparator = null;

        switch (shipOrder) {
            case ID:
                comparator = Comparator.comparingLong(Ship::getId);
                break;
            case SPEED:
                comparator = Comparator.comparingDouble(Ship::getSpeed);
                break;
            case DATE:
                comparator = Comparator.comparing(Ship::getProdDate);
                break;
            case RATING:
                comparator = Comparator.comparingDouble(Ship::getRating);
                break;
        }

        return comparator;
    }

    private Double calculateRating(Ship ship) {
        double wearFactor = ship.isUsed() ? 0.5 : 1.0;
        int prodYear = ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();
        double rating = (80 * ship.getSpeed() * wearFactor) / (3019 - prodYear + 1d);
        return (double) Math.round(rating * 100) / 100;
    }
}
