const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

export async function apiRequest(path, options = {}) {
  const response = await fetch(`${API}${path}`, { credentials: 'include', headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }, ...options })
  if (!response.ok) throw new Error((await response.json().catch(() => ({}))).message || 'The FixFlow API is unavailable.')
  return response.status === 204 ? null : response.json()
}

const SESSION_KEY = 'fixflow.session'

export function getSession() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY) || 'null')
  } catch {
    return null
  }
}

export function saveSession(user) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(user))
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY)
}

export const mockIssues = [
  { id: 'FF-1042', title: 'AC not cooling', category: 'HVAC', location: 'Conference Room 302', priority: 'HIGH', status: 'IN PROGRESS', technician: 'Arjun Mehta', reported: '17 Aug 2026' },
  { id: 'FF-1051', title: 'Printer offline', category: 'IT / Network', location: 'Finance Bay', priority: 'MEDIUM', status: 'ASSIGNED', technician: 'Rahul Verma', reported: '16 Aug 2026' },
  { id: 'FF-1057', title: 'Water pressure low', category: 'Plumbing', location: 'Pantry · Floor 2', priority: 'LOW', status: 'OPEN', technician: 'Unassigned', reported: '15 Aug 2026' },
  { id: 'FF-1061', title: 'Access card reader', category: 'Security', location: 'North Entrance', priority: 'CRITICAL', status: 'AT RISK', technician: 'Priya Sharma', reported: '15 Aug 2026' },
]

export const mockAssets = [
  { id: 'HVAC-302', name: 'Conference Room HVAC', category: 'HVAC', location: 'Block A · Floor 3', condition: 'Needs attention', health: 68, warranty: 'Expired', maintenance: '21 July 2026', failures: 12 },
  { id: 'SRV-02', name: 'Application server', category: 'Server', location: 'Block B · Basement', condition: 'Healthy', health: 94, warranty: 'Active', maintenance: '02 Aug 2026', failures: 2 },
  { id: 'UPS-04', name: 'Server room power backup', category: 'Power', location: 'Block B · Basement', condition: 'Under maintenance', health: 78, warranty: 'Active', maintenance: '29 June 2026', failures: 5 },
  { id: 'PRN-14', name: 'Finance printer', category: 'Printing', location: 'Block B · Floor 2', condition: 'Needs attention', health: 68, warranty: 'Expired', maintenance: '09 July 2026', failures: 8 },
]
