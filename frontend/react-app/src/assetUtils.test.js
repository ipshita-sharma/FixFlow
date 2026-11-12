import test from 'node:test'
import assert from 'node:assert/strict'
import { filterAssets, getAssetCategories, summarizeAssets } from './assetUtils.js'

const assets = [
  { assetCode: 'HVAC-302', assetName: 'Conference Room HVAC', category: 'HVAC', condition: 'Needs attention', healthScore: 68 },
  { assetCode: 'SRV-02', assetName: 'Application server', category: 'Server', condition: 'Healthy', healthScore: 94 },
  { assetCode: 'UPS-04', assetName: 'Server room power backup', category: 'Power', condition: 'Under maintenance', healthScore: 78 },
]

test('summarizes the returned asset records using stored health scores', () => {
  assert.deepEqual(summarizeAssets(assets), { total: 3, needsAttention: 1, underMaintenance: 1, averageHealth: 80 })
  assert.deepEqual(summarizeAssets([]), { total: 0, needsAttention: 0, underMaintenance: 0, averageHealth: null })
})

test('filters actual asset codes, names, and categories', () => {
  assert.deepEqual(filterAssets(assets, 'server').map(asset => asset.assetCode), ['SRV-02', 'UPS-04'])
  const hvacAssets = filterAssets(assets, '', 'HVAC')
  assert.deepEqual(hvacAssets.map(asset => asset.assetCode), ['HVAC-302'])
  assert.deepEqual(summarizeAssets(hvacAssets), { total: 1, needsAttention: 1, underMaintenance: 0, averageHealth: 68 })
  assert.deepEqual(getAssetCategories(assets), ['HVAC', 'Power', 'Server'])
})
