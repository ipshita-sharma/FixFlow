export function getAssetCategories(assets = []) {
  return [...new Set(assets.map(asset => asset.category).filter(Boolean))].sort()
}

export function filterAssets(assets = [], query = '', category = '') {
  const normalizedQuery = query.trim().toLowerCase()
  return assets.filter(asset => {
    const matchesQuery = !normalizedQuery || `${asset.assetCode || ''} ${asset.assetName || ''}`.toLowerCase().includes(normalizedQuery)
    return matchesQuery && (!category || asset.category === category)
  })
}

export function summarizeAssets(assets = []) {
  const healthValues = assets.map(asset => Number(asset.healthScore)).filter(Number.isFinite)
  return {
    total: assets.length,
    needsAttention: assets.filter(asset => asset.condition === 'Needs attention').length,
    underMaintenance: assets.filter(asset => asset.condition === 'Under maintenance').length,
    averageHealth: healthValues.length ? Math.round(healthValues.reduce((sum, value) => sum + value, 0) / healthValues.length) : null,
  }
}
