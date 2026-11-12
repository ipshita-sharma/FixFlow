package com.fixflow.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fixflow.model.Location;

@Repository
public class LocationDao {
    private final JdbcTemplate jdbc;

    public LocationDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Location> findAll() {
        return jdbc.query(
                "SELECT id, building, floor_name, room_name FROM locations ORDER BY building, floor_name, room_name",
                (rs, row) -> new Location(rs.getLong("id"), rs.getString("building"),
                        rs.getString("floor_name"), rs.getString("room_name")));
    }
}
