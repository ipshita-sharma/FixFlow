package com.fixflow.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixflow.model.Location;
import com.fixflow.service.LocationService;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService locations;

    public LocationController(LocationService locations) { this.locations = locations; }

    @GetMapping
    public List<Location> list() { return locations.findAll(); }
}
