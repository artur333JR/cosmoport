package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/rest/ships")
public class ShipController {

    private final ShipService shipService;

    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping
    public ResponseEntity<List<Ship>> getShipsList(@RequestParam(required = false) String name,
                                                   @RequestParam(required = false) String planet,
                                                   @RequestParam(required = false) ShipType shipType,
                                                   @RequestParam(required = false) Long after,
                                                   @RequestParam(required = false) Long before,
                                                   @RequestParam(required = false) Boolean isUsed,
                                                   @RequestParam(required = false) Double minSpeed,
                                                   @RequestParam(required = false) Double maxSpeed,
                                                   @RequestParam(required = false) Integer minCrewSize,
                                                   @RequestParam(required = false) Integer maxCrewSize,
                                                   @RequestParam(required = false) Double minRating,
                                                   @RequestParam(required = false) Double maxRating,
                                                   @RequestParam(required = false) ShipOrder order,
                                                   @RequestParam(required = false) Integer pageNumber,
                                                   @RequestParam(required = false) Integer pageSize) {

        List<Ship> shipList = this.shipService.getFilteredShipList(name, planet, shipType, after, before,
                isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);

        shipList = this.shipService.getShipsPerPage(shipList, pageNumber, pageSize, order);

        return shipList == null || shipList.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(shipList, HttpStatus.OK);
    }

    @GetMapping(value = "/count")
    public ResponseEntity<Integer> getShipsCount(@RequestParam(required = false) String name,
                                                 @RequestParam(required = false) String planet,
                                                 @RequestParam(required = false) ShipType shipType,
                                                 @RequestParam(required = false) Long after,
                                                 @RequestParam(required = false) Long before,
                                                 @RequestParam(required = false) Boolean isUsed,
                                                 @RequestParam(required = false) Double minSpeed,
                                                 @RequestParam(required = false) Double maxSpeed,
                                                 @RequestParam(required = false) Integer minCrewSize,
                                                 @RequestParam(required = false) Integer maxCrewSize,
                                                 @RequestParam(required = false) Double minRating,
                                                 @RequestParam(required = false) Double maxRating) {
        Integer shipCount = this.shipService.getFilteredShipList(name, planet, shipType, after, before,
                isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating).size();

        return new ResponseEntity<>(shipCount, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable Long id) {
        if (isIdInvalid(id))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Ship ship = this.shipService.read(id);
        return ship == null
                ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        return ship == null
                || isNameInvalid(ship.getName())
                || isPlanetInvalid(ship.getPlanet())
                || ship.getShipType() == null
                || isProdDateInvalid(ship.getProdDate())
                || isSpeedInvalid(ship.getSpeed())
                || isCrewSizeInvalid(ship.getCrewSize())
                ?
                new ResponseEntity<>(HttpStatus.BAD_REQUEST)
                :
                new ResponseEntity<>(this.shipService.create(ship), HttpStatus.OK);
    }

    @PostMapping(value = "/{id}")
    public ResponseEntity<Ship> updateShip(@RequestBody Ship ship, @PathVariable Long id) {
        if (ship == null
                || isIdInvalid(id)
                || (ship.getName() != null && isNameInvalid(ship.getName()))
                || (ship.getPlanet() != null && isPlanetInvalid(ship.getPlanet()))
                || (ship.getProdDate() != null && isProdDateInvalid(ship.getProdDate()))
                || (ship.getSpeed() != null && isSpeedInvalid(ship.getSpeed()))
                || (ship.getCrewSize() != null && isCrewSizeInvalid(ship.getCrewSize())))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        else if (this.shipService.update(ship, id) == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(this.shipService.update(ship, id), HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteShip(@PathVariable Long id) {
        if (isIdInvalid(id))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return this.shipService.delete(id) == false
                ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean isIdInvalid(Long id) {
        return id == null || id != Math.floor(id) || id <= 0;
    }

    private boolean isNameInvalid(String name) {
        return name == null || name.isEmpty() || name.length() > 50;
    }

    private boolean isPlanetInvalid(String planet) {
        return isNameInvalid(planet);
    }

    private boolean isProdDateInvalid(Date date) {
        return date == null
                || date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() < 2800
                || date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() > 3019;
    }

    private boolean isSpeedInvalid(Double speed) {
        return speed == null || speed < 0.01d || speed > 0.99d;
    }

    private boolean isCrewSizeInvalid(Integer crewSize) {
        return crewSize == null || crewSize < 1 || crewSize > 9999;
    }
}
