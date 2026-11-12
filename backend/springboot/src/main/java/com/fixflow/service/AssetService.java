package com.fixflow.service;

import com.fixflow.dao.AssetDao;
import com.fixflow.dao.MaintenanceHistoryDao;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.model.Asset;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AssetService {
    private final AssetDao assets;
    private final MaintenanceHistoryDao history;
    public AssetService(AssetDao assets, MaintenanceHistoryDao history) { this.assets = assets; this.history = history; }
    public List<Asset> findAll() { return assets.findAll(); }
    public List<Asset> findAllForTechnician(long technicianId) { return assets.findAllForTechnician(technicianId); }
    public Asset find(long id) { return assets.findById(id).orElseThrow(() -> new ResourceNotFoundException("Asset " + id + " was not found.")); }
    public Asset findForTechnician(long id, long technicianId) { return assets.findByIdForTechnician(id, technicianId).orElseThrow(() -> new ResourceNotFoundException("Asset " + id + " was not found.")); }
    public Map<String, Object> history(long id) { find(id); return Map.of("asset", find(id), "maintenance", history.findByAsset(id)); }
    public Map<String, Object> historyForTechnician(long id, long technicianId) { Asset asset = findForTechnician(id, technicianId); return Map.of("asset", asset, "maintenance", history.findByAsset(id)); }
}
