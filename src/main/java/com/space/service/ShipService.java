package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.List;

public interface ShipService {
    Ship create(Ship ship);
    Ship read(Long id);
    List<Ship> readAll();
    List<Ship> getFilteredShipList(String name, String planet, ShipType shipType, Long after, Long before, Boolean isUsed,
                                   Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                                   Double minRating, Double maxRating);
    List<Ship> getShipsPerPage(List<Ship> ships, Integer pageNumber, Integer pageSize, ShipOrder shipOrder);
    Ship update(Ship ship, Long id);
    boolean delete(Long id);

}
