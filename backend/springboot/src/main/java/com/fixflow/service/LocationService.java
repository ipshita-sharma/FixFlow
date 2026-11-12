package com.fixflow.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fixflow.dao.LocationDao;
import com.fixflow.model.Location;

@Service
public class LocationService {
    private final LocationDao locations;

    public LocationService(LocationDao locations) { this.locations = locations; }

    public List<Location> findAll() { return locations.findAll(); }
}
