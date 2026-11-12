import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, NavLink, Navigate, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom'
import { apiRequest, clearSession, getSession, mockAssets, mockIssues, saveSession } from './api'
import { filterAssets, getAssetCategories, summarizeAssets } from './assetUtils'

const A = ({ children, className = '', ...props }) => <span className={className} {...props}>{children}</span>

function Brand() { return <Link className="brand" to="/"><span className="brand-mark">↗</span>FixFlow</Link> }
function Button({ children, className = '', ...props }) { return <button className={`btn ${className}`} {...props}>{children}</button> }
function displayLabel(value = '') { return value.replaceAll('_', ' ').replaceAll('-', ' ').toLowerCase().replace(/\b\w/g, character => character.toUpperCase()) }
function StatusBadge({ value = '', type = 'status' }) { const label = displayLabel(value); const key = value.toLowerCase().replaceAll('_', '-').replaceAll(' ', '-'); return <span className={`${type} ${type}-${key}`}>{label}</span> }
function StatCard({ label, value, trend, danger }) { return <article className="kpi-card"><span className="kpi-label">{label}</span><div className="kpi-number">{value ?? '—'}</div>{trend && <span className={danger ? 'trend down' : 'trend'}>{trend}</span>}</article> }
function ProgressBar({ value, tone = '' }) { return <div className="progress-line"><i className={tone} style={{ width: `${value}%` }} /></div> }
function initials(name = '') { return name.split(' ').filter(Boolean).slice(0, 2).map(part => part[0]).join('').toUpperCase() || '??' }
function currentDateLabel(date = new Date(), { weekday = 'long', uppercase = false } = {}) { const label = new Intl.DateTimeFormat('en-IN', { ...(weekday ? { weekday } : {}), day: 'numeric', month: 'long', year: 'numeric' }).format(date); return uppercase ? label.toUpperCase() : label }
function greeting() { const hour = Number(new Intl.DateTimeFormat('en-IN', { timeZone: 'Asia/Kolkata', hour: '2-digit', hour12: false }).format(new Date())); return hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening' }
function relativeTime(value) { if (!value) return 'Time unavailable'; const difference = Date.now() - new Date(value).getTime(); const minutes = Math.max(0, Math.floor(difference / 60000)); if (minutes < 1) return 'Just now'; if (minutes < 60) return `${minutes}m ago`; const hours = Math.floor(minutes / 60); if (hours < 24) return `${hours}h ago`; const days = Math.floor(hours / 24); return `${days}d ago` }
function exactTime(value) { return value ? new Intl.DateTimeFormat('en-IN', { timeZone: 'Asia/Kolkata', dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : 'Time unavailable' }
function accountSessionKey(session) { return session?.id == null ? 'anonymous' : `${session.id}:${String(session.role || '').toUpperCase()}` }
function roleHome(role) { return role === 'ADMIN' ? '/admin' : role === 'TECHNICIAN' ? '/technician' : role === 'USER' ? '/dashboard' : '/login' }
function RequireAuthenticated({ children }) { if (!getSession()) return <Navigate to="/login" replace/>; return children }
function RequireRole({ role, children }) { const session = getSession(); const actualRole = String(session?.role || '').toUpperCase(); if (!session) return <Navigate to="/login" replace/>; if (actualRole !== role) return <Navigate to={roleHome(actualRole)} replace/>; return children }

function Landing() {
  const features = [['✦', 'Smart Issue Prioritization', 'Transparent scoring considers severity, impact, asset criticality, and SLA urgency.'], ['♧', 'Technician Assignment', 'Recommend the right technician using skills, capacity, priority, and location.'], ['◷', 'SLA Monitoring', 'See safe, at-risk, and breached work before missed deadlines become surprises.'], ['⌂', 'Asset Management', 'Track health, warranty, maintenance dates, and the true cost of repeated failures.'], ['▥', 'Maintenance Analytics', 'Understand issue volume, resolution time, workload, and recurring failure patterns.'], ['✓', 'Issue Tracking', 'Give every request an owner, a timeline, a clear status, and a verifiable resolution.']]
  return <><header className="site-header"><nav className="nav container"><Brand/><div className="nav-links"><a href="#features">Product</a><a href="#how-it-works">How It Works</a><a href="#features">Features</a></div><div className="nav-actions"><Link className="btn btn-ghost" to="/login">Login</Link><Link className="btn btn-primary" to="/register">Get Started ↗</Link></div></nav></header><main><section className="hero"><div className="container hero-grid"><div><span className="eyebrow">Intelligent maintenance operations</span><h1>Maintenance, <span>without</span> the chaos.</h1><p className="hero-copy">Report issues, coordinate repairs, monitor maintenance operations, and keep every asset running from one intelligent platform.</p><div className="hero-buttons"><Link className="btn btn-primary" to="/report">Report an Issue ↗</Link><Link className="btn btn-ghost" to="/dashboard">Explore Platform</Link></div><div className="hero-note"><div className="avatar-stack"><A className="avatar">AJ</A><A className="avatar orange">PM</A><A className="avatar purple">RV</A></div><span>Trusted by teams keeping 1,240+ issues moving.</span></div></div><DashboardPreview/></div></section><section className="section section-tint" id="features"><div className="container"><SectionHeading eyebrow="One source of truth" title="Every moving part, finally in sync." copy="FixFlow turns fragmented maintenance requests into a visible, accountable workflow your whole organization can understand."/><div className="feature-grid">{features.map(([icon, title, copy]) => <article className="feature-card" key={title}><div className="feature-icon">{icon}</div><h3>{title}</h3><p>{copy}</p></article>)}</div></div></section><section className="section" id="how-it-works"><div className="container"><SectionHeading eyebrow="How it works" title="A calmer way to keep things running."/><div className="step-grid">{['Report', 'Prioritize', 'Assign', 'Repair', 'Verify', 'Resolve'].map((step, i) => <div className="step" key={step}><span className="step-number">0{i + 1}</span><h3>{step}</h3><p>{['Capture what happened and where.', 'Score urgency with explainable rules.', 'Match the right skill and capacity.', 'Track diagnosis, parts, and progress.', 'Confirm the fix with the reporter.', 'Keep the history for next time.'][i]}</p></div>)}</div></div></section><section className="section section-tint"><div className="container"><SectionHeading eyebrow="Built for the whole team" title="Everyone sees the work that matters to them."/><div className="role-grid"><Role title="USER" heading="Report and track issues." copy="Know what is happening, who owns it, and when it is expected to be resolved."/><Role title="TECHNICIAN" heading="Manage maintenance jobs." copy="Work from a focused queue with the context, skills, and notes to fix things well."/><Role title="ADMIN" heading="Monitor operations." copy="See performance, risk, recurring failures, and the assets that need a decision."/></div></div></section><section className="section"><div className="container"><div className="stats-strip"><StatCell value="1,240+" label="Issues Resolved"/><StatCell value="94%" label="SLA Compliance"/><StatCell value="86" label="Active Assets"/><StatCell value="4.2h" label="Average Resolution"/></div><div className="cta"><h2>Bring order to your maintenance operations.</h2><p>Start with a clearer view of every issue, every technician, and every asset.</p><Link className="btn btn-teal" to="/register">Get started with FixFlow ↗</Link></div></div></section></main><footer className="site-footer"><div className="container footer-row"><Brand/><span>© 2026 Vertex Business Center · Maintenance, without the chaos.</span></div></footer></>
}
function DashboardPreview() { return <div className="hero-visual"><div className="dashboard-preview"><aside className="preview-sidebar"><div className="mini-logo"><span className="brand-mark">↗</span>FixFlow</div><p className="active">▦ Overview</p><p>◌ My Issues</p><p>＋ Report Issue</p><p>◈ Assets</p><p>⌁ Analytics</p></aside><div className="preview-main"><div className="preview-top"><strong>Good morning, Alex</strong><span className="preview-search">⌕ Search anything</span></div><div className="preview-kpis"><div className="mini-card"><span className="tiny">Open issues</span><div className="number">08</div></div><div className="mini-card"><span className="tiny">SLA compliance</span><div className="number">94%</div></div><div className="mini-card"><span className="tiny">Active assets</span><div className="number">86</div></div></div><div className="preview-panels"><div className="preview-panel"><h4>Issue volume</h4>{[['Electrical', 72, 42], ['HVAC', 55, 31], ['IT / Network', 38, 18]].map(([x, y, z], i) => <div className="bar-row" key={x}><span>{x}</span><span className="bar"><i className={i === 1 ? 'orange' : i === 2 ? 'purple' : ''} style={{ width: `${y}%` }}/></span><b>{z}</b></div>)}</div><div className="preview-panel"><h4>Recent issues</h4><ul className="preview-issues"><li><i className="dot red"/><span>AC not cooling</span><b>HIGH</b></li><li><i className="dot"/><span>Printer offline</span><b>MED</b></li><li><i className="dot teal"/><span>Leak in pantry</span><b>LOW</b></li></ul></div></div></div></div></div> }
function SectionHeading({ eyebrow, title, copy }) { return <div className="section-heading"><span className="eyebrow">{eyebrow}</span><h2>{title}</h2>{copy && <p>{copy}</p>}</div> }
function Role({ title, heading, copy }) { return <article className="role-card"><strong>{title}</strong><h3>{heading}</h3><p>{copy}</p></article> }
function StatCell({ value, label }) { return <div className="stat-cell"><strong>{value}</strong><span>{label}</span></div> }

 function Auth({ register = false }) { const navigate = useNavigate(); const [error, setError] = useState(''); const submit = async (event) => { event.preventDefault(); const form = new FormData(event.currentTarget); if (register && form.get('password') !== form.get('confirmPassword')) return setError('Passwords do not match.'); try { const result = await apiRequest(register ? '/auth/register' : '/auth/login', { method: 'POST', body: JSON.stringify(Object.fromEntries(form)) }); saveSession({ ...result, fullName: result.fullName || form.get('fullName') }); navigate(result.role === 'ADMIN' ? '/admin' : result.role === 'TECHNICIAN' ? '/technician' : '/dashboard'); } catch (err) { setError(err.message || 'Could not sign in. Please try again.'); } }; return <main className="auth-page"><aside className="auth-aside"><Brand/><div><span className="eyebrow">{register ? 'A better maintenance rhythm' : 'Your operations cockpit'}</span><h1>{register ? 'Give every issue a clear next step.' : 'Keep every fix moving forward.'}</h1><p>One calm, connected place for issues, people, assets, and the work between them.</p></div><div className="auth-quote">“FixFlow makes it obvious what needs attention next — and why.”<strong>Priya Sharma · Facilities Lead</strong></div></aside><section className="auth-content"><div className="auth-form-wrap"><span className="eyebrow">{register ? 'Get started' : 'Welcome back'}</span><h2>{register ? 'Create your account' : 'Log in to FixFlow'}</h2><p>{register ? 'Set up your workspace in under two minutes.' : 'Pick up where your maintenance operation left off.'}</p><form className="auth-form" onSubmit={submit}>{register && <div className="form-row"><Field label="Full name" name="fullName" placeholder="Alex Johnson"/><Field label="Phone" name="phone" placeholder="+91 98765 43210"/></div>}<Field label="Work email" name="email" type="email" placeholder="alex@vertexbusiness.com"/><Field label="Password" name="password" type="password" placeholder="8+ characters" minLength="8"/>{register && <><Field label="Organization" name="organization" placeholder="Vertex Business Center"/><div className="field"><label htmlFor="role">Your role</label><select id="role" name="role" required><option value="USER">User</option><option value="TECHNICIAN">Technician</option></select><p className="muted small">Administrator? <Link to="/login">Log in</Link> with your authorized account.</p></div><Field label="Confirm password" name="confirmPassword" type="password" placeholder="Repeat password" minLength="8"/></>}{!register && <div className="form-meta"><label className="check"><input type="checkbox"/> Remember me</label><Link to="/login">Forgot password?</Link></div>}{error && <p className="form-error">{error}</p>}<Button className="btn-primary" type="submit">{register ? 'Create account' : 'Login'} ↗</Button></form><div className="auth-switch">{register ? 'Already have an account?' : 'New to FixFlow?'} <Link to={register ? '/login' : '/register'}>{register ? 'Log in' : 'Create an account'}</Link></div></div></section></main> }
function Field({ label, name, type = 'text', placeholder, minLength }) { return <div className="field"><label htmlFor={name}>{label}</label><input id={name} name={name} type={type} placeholder={placeholder} minLength={minLength} required /></div> }

const TECHNICIAN_STATUSES = ['IN_PROGRESS', 'WAITING_FOR_PARTS', 'RESOLVED'];

function StatusUpdateModal({ job, status, note, error, submitting, onStatusChange, onNoteChange, onCancel, onSubmit }) {
  if (!job) return null;
  return <div className="modal-backdrop" role="presentation">
    <section className="status-modal" role="dialog" aria-modal="true" aria-labelledby="status-modal-title">
      <div className="modal-header">
        <div>
          <span className="eyebrow">Update assigned issue</span>
          <h2 id="status-modal-title">{job.issueNumber}</h2>
          <p>{job.title}</p>
        </div>
        <span className="modal-status">{displayLabel(job.status)}</span>
      </div>
      <form onSubmit={onSubmit}>
        <div className="field">
          <label htmlFor="technician-status">Status</label>
          <select id="technician-status" value={status} onChange={event => onStatusChange(event.target.value)} required autoFocus>
            {TECHNICIAN_STATUSES.map(option => <option value={option} key={option}>{option}</option>)}
          </select>
        </div>
        <div className="field">
          <label htmlFor="technician-note">Notes <span className="muted">(optional)</span></label>
          <textarea id="technician-note" value={note} onChange={event => onNoteChange(event.target.value)} placeholder="Record progress, parts needed, or resolution details." rows="4" />
        </div>
        {error && <p className="form-error" role="alert">{error}</p>}
        <div className="form-footer modal-footer">
          <Button className="btn-ghost" type="button" onClick={onCancel} disabled={submitting}>Cancel</Button>
          <Button className="btn-teal" type="submit" disabled={submitting || !status}>{submitting ? 'Updating...' : 'Update Status'}</Button>
        </div>
      </form>
    </section>
  </div>;
}

function IssueDetailsLive() {
  const { id } = useParams();
  const [issue, setIssue] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    setIssue(null);
    setError('');
    apiRequest(`/issues/${id}`)
      .then(setIssue)
      .catch(err => setError(err.message || 'Could not load this issue. Please try again.'));
  }, [id]);

  if (error) return <Shell><main className="page"><p className="form-error">{error}</p></main></Shell>;
  if (!issue) return <Shell><main className="page"><p className="muted">Loading issue details...</p></main></Shell>;
  return <Shell><main className="page"><PageHeader eyebrow="Issue details" title={issue.issueNumber} copy={`Current status: ${displayLabel(issue.status)}`} action={<Button className="btn-primary">Add update</Button>}/><div className="detail-grid"><section className="detail-hero"><div className="detail-meta"><StatusBadge value={issue.priority} type="priority"/><StatusBadge value={issue.status}/></div><h1>{issue.title}</h1><p className="muted small">{issue.description}</p><div className="detail-stat-grid"><div className="detail-stat"><span>Category</span><strong>{issue.category}</strong></div><div className="detail-stat"><span>Location</span><strong>{issue.location}</strong></div><div className="detail-stat"><span>Asset</span><strong>{issue.assetCode || 'No related asset'}</strong></div><div className="detail-stat"><span>Technician</span><strong>{issue.technician || 'Unassigned'}</strong></div></div><div className="detail-section"><h2>Issue status</h2><div className="timeline"><div className="timeline-item"><strong>{displayLabel(issue.status)}</strong><p>The latest status is stored in the FixFlow issue workflow.</p><time>{exactTime(issue.createdAt)}</time></div></div></div></section><aside className="side-stack"><section className="sla-card"><span>SLA deadline</span><strong>{exactTime(issue.slaDeadline)}</strong><p className="small sla-copy">deadline calculated from the issue severity</p><ProgressBar value={issue.status === 'RESOLVED' || issue.status === 'CLOSED' ? 100 : 62}/></section><section className="panel"><div className="panel-header"><h2>Issue information</h2></div><div className="info-list"><div><span>Severity</span><strong>{displayLabel(issue.severity)}</strong></div><div><span>Priority</span><strong>{displayLabel(issue.priority)}</strong></div><div><span>Status</span><strong>{displayLabel(issue.status)}</strong></div><div><span>Priority score</span><strong>{issue.priorityScore}</strong></div></div></section></aside></div></main></Shell>;
}

function AdminAssignmentPanel() {
  const session = getSession();
  const [issues, setIssues] = useState([]);
  const [technicians, setTechnicians] = useState([]);
  const [selected, setSelected] = useState({});
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [issueRows, technicianRows] = await Promise.all([apiRequest('/issues'), apiRequest('/technicians', { cache: 'no-store' })]);
      setIssues(issueRows);
      setTechnicians(technicianRows);
    } catch (err) {
      setError(err.message || 'Could not load issues and technicians. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const assign = async issue => {
    const technicianId = Number(selected[issue.id]);
    if (!technicianId) {
      setError('Choose a registered technician before assigning this issue.');
      return;
    }
    setBusyId(issue.id);
    setError('');
    setNotice('');
    try {
      await apiRequest(`/issues/${issue.id}/assign`, {
        method: 'POST',
        body: JSON.stringify({ technicianId, assignedBy: session?.id })
      });
      await load();
      setNotice(`${issue.issueNumber} was assigned successfully.`);
    } catch (err) {
      setError(err.message || 'Could not assign this issue.');
    } finally {
      setBusyId(null);
    }
  };

  return <section className="panel"><div className="panel-header"><h2>Assign reported issues</h2><span className="muted small">Registered technicians only</span></div>{error && <p className="form-error">{error}</p>}{notice && <p className="success-message">{notice}</p>}{loading ? <p className="muted">Loading the operations queue...</p> : !issues.length ? <p className="muted">No issues have been reported yet.</p> : <div className="table-card"><table className="issue-table"><thead><tr><th>Issue</th><th>Status</th><th>Current technician</th><th>Assign</th></tr></thead><tbody>{issues.map(issue => <tr key={issue.id}><td><strong>{issue.issueNumber}</strong><span className="issue-sub">{issue.title}</span></td><td><StatusBadge value={issue.status}/></td><td>{issue.technician || 'Unassigned'}</td><td><div className="form-row"><select aria-label={`Technician for ${issue.issueNumber}`} value={selected[issue.id] || ''} onChange={event => setSelected(current => ({ ...current, [issue.id]: event.target.value }))}><option value="">Choose technician</option>{technicians.map(technician => <option key={technician.id} value={technician.id}>{technician.fullName}</option>)}</select><Button className="btn-teal" onClick={() => assign(issue)} disabled={busyId !== null}>{busyId === issue.id ? 'Saving...' : 'Assign'}</Button></div></td></tr>)}</tbody></table></div>}</section>;
}

function Shell({ children, role, user }) { const [open, setOpen] = useState(false); const location = useLocation(); const session = user || getSession(); const activeRole = (getSession()?.role || session?.role || 'USER').toLowerCase(); const admin = activeRole === 'admin'; const tech = activeRole === 'technician'; const name = session?.fullName || 'Guest'; const nav = admin ? [['/admin', '▦', 'Overview'], ['/issues', '◌', 'Issues'], ['/technicians', '♧', 'Technicians'], ['/assets', '◈', 'Assets']] : tech ? [['/technician', '▦', 'Assigned jobs'], ['/issues', '◌', 'All issues'], ['/assets', '◈', 'Assigned assets']] : [['/dashboard', '▦', 'Dashboard'], ['/issues', '◌', 'My Issues'], ['/report', '＋', 'Report Issue'], ['/assets', '◈', 'Assets']]; const logout = () => { apiRequest('/auth/logout', { method: 'POST' }).catch(() => {}); clearSession(); }; return <div className="app-shell"><aside className={`sidebar ${open ? 'open' : ''}`}><Brand/><nav aria-label="Workspace"><span className="sidebar-label">{admin ? 'Command center' : tech ? 'My workspace' : 'Workspace'}</span>{nav.map(([to, icon, label]) => <NavLink key={to} to={to} className={({ isActive }) => isActive ? 'active' : ''}><span className="nav-icon">{icon}</span>{label}</NavLink>)}<span className="sidebar-label">Account</span><NavLink to="/profile"><span className="nav-icon">◎</span>{admin ? 'Settings' : 'Profile'}</NavLink><Link to="/" onClick={logout}><span className="nav-icon">↪</span>Logout</Link></nav><div className="sidebar-bottom"><div className="sidebar-user"><A className={`avatar ${tech ? 'orange' : admin ? 'purple' : ''}`}>{initials(name)}</A><div><strong>{name}</strong><span>{tech ? 'Technician' : admin ? 'Administrator' : session ? 'Operations member' : 'Guest'}</span></div></div></div></aside><div className="main-area"><header className="topbar"><Button className="icon-btn" onClick={() => setOpen(!open)} aria-label="Open navigation">☰</Button><span className="topbar-title">{location.pathname === '/admin' ? 'Command center' : location.pathname.slice(1) || 'Dashboard'}</span><label className="topbar-search">⌕<input type="search" placeholder="Search issues, assets..."/></label><div className="topbar-actions"><Button className="icon-btn" aria-label="Notifications">♢<i className="notification-dot"/></Button><div className="top-user"><A className={`avatar ${tech ? 'orange' : admin ? 'purple' : ''}`}>{initials(name)}</A><div><strong>{name}</strong><span>{tech ? 'Technician' : admin ? 'Administrator' : session ? 'Operations member' : 'Guest'}</span></div></div></div></header>{children}</div></div> }

function UserDashboard() {
  const session = getSession();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const query = session?.id ? `?userId=${encodeURIComponent(session.id)}` : '';
    apiRequest(`/dashboard/data${query}`)
      .then(setData)
      .catch(err => setError(err.message || 'Could not load dashboard data. Please try again.'))
      .finally(() => setLoading(false));
  }, [session?.id]);

  const user = data?.user || session;
  const summary = data?.summary || {};
  return <Shell user={user}><main className="page"><PageHeader eyebrow={currentDateLabel()} title={`${greeting()}, ${user?.fullName || 'Guest'}.`} copy="Here’s a focused view of the maintenance work connected to your spaces." action={<Link className="btn btn-primary" to="/report">＋ Report an issue</Link>}/>{loading && <section className="panel"><p className="muted">Loading current dashboard data...</p></section>}{error && <section className="panel"><p className="form-error">{error}</p></section>}{!loading && !error && <><section className="kpi-grid"><StatCard label="Open issues" value={summary.openIssues} trend="Current unresolved workload"/><StatCard label="In progress" value={summary.inProgress} trend="Currently being worked"/><StatCard label="Urgent attention" value={summary.urgentAttention} trend="High and critical unresolved" danger/><StatCard label="Resolved this month" value={summary.resolvedThisMonth} trend="Current calendar month"/></section><div className="dashboard-grid"><IssuePanel rows={data.recentIssues} /><TimelinePanel events={data.activity} /></div><div className="dashboard-grid wide-right"><AnalyticsPanel volume={data.volume} /><NotificationsPanel notifications={data.notifications} /></div></>}<div className="quick-report"><div><h3>Something needs attention?</h3><p>Give the operations team the context they need to move quickly.</p></div><Link className="btn" to="/report">Report an issue ↗</Link></div></main></Shell>
}
function LiveDateEyebrow() { const [today, setToday] = useState(() => new Date()); useEffect(() => { const now = new Date(); const nextDay = new Date(now); nextDay.setHours(24, 0, 0, 0); const timer = setTimeout(() => setToday(new Date()), Math.max(nextDay.getTime() - now.getTime(), 1000)); return () => clearTimeout(timer); }, [today]); return <>OPERATIONS OVERVIEW · {currentDateLabel(today, { weekday: false, uppercase: true })}</> }
function PageHeader({ eyebrow, title, copy, action }) { return <><div className="page-header"><div><span className="eyebrow">{title === 'Maintenance Command Center' ? <LiveDateEyebrow/> : eyebrow}</span><h1>{title}</h1><p>{copy}</p></div>{action}</div>{title === 'Maintenance Command Center' && <AdminAssignmentPanel/>}</> }
function IssuePanel({ rows = [], loading = false, error = '' }) { return <section className="panel"><div className="panel-header"><h2>Recent issues</h2><Link to="/issues">View all issues ↗</Link></div>{loading && <p className="muted">Loading issues...</p>}{error && <p className="form-error">{error}</p>}{!loading && !error && <IssueTable rows={rows}/>}</section> }
function IssueTable({ rows = [] }) { if (!rows.length) return <p className="muted">No issues have been reported yet.</p>; return <div className="table-card"><table className="issue-table"><thead><tr><th>Issue</th><th>Priority</th><th>Status</th><th>Technician</th></tr></thead><tbody>{rows.map(issue => { const id = issue.issueNumber || issue.id; const routeId = issue.numericId || (typeof issue.id === 'number' ? issue.id : null); const content = <><span className="issue-title">{issue.title}</span><span className="issue-sub">{id} · {issue.location}</span></>; return <tr key={id}><td>{routeId ? <Link to={`/issues/${routeId}`}>{content}</Link> : content}</td><td><StatusBadge value={issue.priority} type="priority"/></td><td><StatusBadge value={issue.status} /></td><td>{issue.technician || 'Unassigned'}</td></tr> })}</tbody></table></div> }
function TimelinePanel({ events = [] }) { return <section className="panel"><div className="panel-header"><h2>Activity timeline</h2><Link to="/issues">View issues ↗</Link></div>{!events.length ? <p className="muted">No activity has been recorded yet.</p> : <div className="timeline">{events.map((event, index) => <div className={`timeline-item ${event.type === 'STATUS' && event.title.includes('RESOLVED') ? 'pending' : ''}`} key={`${event.issueNumber}-${event.occurredAt}-${index}`}><strong>{event.title}</strong><p>{event.details}{event.actor ? ` · ${event.actor}` : ''}</p><time title={exactTime(event.occurredAt)}>{relativeTime(event.occurredAt)}</time></div>)}</div>}</section> }
function AnalyticsPanel({ volume = [] }) { const max = Math.max(...volume.map(point => Number(point.count)), 1); return <section className="panel"><div className="panel-header"><h2>Issue volume</h2><Link to="/issues">View issues ↗</Link></div>{!volume.length ? <p className="muted">No issue-volume data is available.</p> : <div className="analytics-row">{volume.map(point => <div className="analytics-bar" key={point.date} title={`${point.label}: ${point.count} issue${point.count === 1 ? '' : 's'}`}><i style={{ height: `${point.count ? Math.max((point.count / max) * 100, 8) : 0}%` }}/><b>{point.count}</b><span>{point.label}</span></div>)}</div>}</section> }
function NotificationsPanel({ notifications = [] }) { return <section className="panel"><div className="panel-header"><h2>Notifications</h2><Link to="/profile">Manage ↗</Link></div>{!notifications.length ? <p className="muted">No current notifications.</p> : <div className="activity-list">{notifications.map((notification, index) => <div className="activity" key={`${notification.id || notification.title}-${notification.createdAt}-${index}`}><A className={`avatar ${notification.type === 'WARNING' || notification.type === 'CRITICAL' ? 'orange' : ''}`}>{notification.type === 'WARNING' || notification.type === 'CRITICAL' ? '!' : '✓'}</A><p><strong>{notification.title}</strong><br/>{notification.message}<time title={exactTime(notification.createdAt)}>{relativeTime(notification.createdAt)}</time></p></div>)}</div>}</section> }


function ReportIssue() {
  const [severity, setSeverity] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [locations, setLocations] = useState([]);
  const [locationsLoading, setLocationsLoading] = useState(true);
  const [locationError, setLocationError] = useState('');
  const [building, setBuilding] = useState('');
  const [floor, setFloor] = useState('');
  const [room, setRoom] = useState('');
  const [locationId, setLocationId] = useState('');
  const [assets, setAssets] = useState([]);
  const [assetsLoading, setAssetsLoading] = useState(true);
  const [assetError, setAssetError] = useState('');
  const [assetCode, setAssetCode] = useState('');

  useEffect(() => {
    let active = true;

    apiRequest('/locations')
      .then(data => {
        if (active) setLocations(data);
      })
      .catch(() => {
        if (active) setLocationError('Could not load available locations. Please refresh and try again.');
      })
      .finally(() => {
        if (active) setLocationsLoading(false);
      });

    return () => { active = false; };
  }, []);

  useEffect(() => {
    let active = true;

    apiRequest('/assets')
      .then(data => {
        if (active) setAssets(data);
      })
      .catch(() => {
        if (active) setAssetError('Related assets could not be loaded. You can continue without selecting one.');
      })
      .finally(() => {
        if (active) setAssetsLoading(false);
      });

    return () => { active = false; };
  }, []);

  const buildings = useMemo(() => [...new Set(locations.map(location => location.building))], [locations]);
  const floors = useMemo(() => [...new Set(
    locations.filter(location => location.building === building).map(location => location.floorName)
  )], [locations, building]);
  const rooms = useMemo(() => locations.filter(location => (
    location.building === building && location.floorName === floor
  )), [locations, building, floor]);

  const selectBuilding = (value) => {
    setBuilding(value);
    setFloor('');
    setRoom('');
    setLocationId('');
  };

  const selectFloor = (value) => {
    setFloor(value);
    setRoom('');
    setLocationId('');
  };

  const selectRoom = (value) => {
    const selected = rooms.find(location => location.roomName === value);
    setRoom(value);
    setLocationId(selected ? String(selected.id) : '');
  };

  const locationReady = !locationsLoading && !locationError && Boolean(locationId);

  const submit = async (event) => {
    event.preventDefault();

    if (!locationReady) {
      setError('Please choose a valid building, floor, and room before submitting.');
      return;
    }

    const formElement = event.currentTarget;
    const form = new FormData(formElement);

    setMessage('');
    setError('');
    setSubmitting(true);

    const payload = {
      title: form.get('title')?.trim(),
      description: form.get('description')?.trim(),
      category: form.get('category'),
      severity: form.get('severity'),
      building,
      floor,
      room,
      locationId: Number(locationId),
      assetCode: assetCode || null,
      impactScore: 1,
      affectedUsers: 1,
      reporterId: getSession()?.id
    };

    try {
      await apiRequest('/issues', {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      formElement.reset();
      setSeverity('');
      setBuilding('');
      setFloor('');
      setRoom('');
      setLocationId('');
      setAssetCode('');
      setMessage('Issue submitted successfully and saved.');
    } catch (err) {
      setError(err.message || 'Could not submit the issue. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Shell>
      <main className="page">
        <PageHeader
          eyebrow="Help us route it right"
          title="Report a maintenance issue."
          copy="Share a few details and FixFlow will calculate priority, SLA, and the next best action."
          action={<Link className="btn btn-ghost" to="/issues">View my issues</Link>}
        />

        <div className="dashboard-grid wide-right">
          <form className="form-card" onSubmit={submit}>
            <section className="form-section">
              <h2>What happened?</h2>
              <Field label="Issue title" name="title" placeholder="e.g. AC not cooling in Conference Room 302" />

              <div className="form-row">
                <div className="field">
                  <label htmlFor="category">Category</label>
                  <select id="category" name="category" required defaultValue="">
                    <option value="">Choose category</option>
                    <option value="Electrical">Electrical</option>
                    <option value="Plumbing">Plumbing</option>
                    <option value="HVAC">HVAC</option>
                    <option value="IT / Network">IT / Network</option>
                    <option value="Furniture">Furniture</option>
                    <option value="Security">Security</option>
                  </select>
                </div>

                <div className="field">
                  <label htmlFor="severity">Severity</label>
                  <select
                    id="severity"
                    name="severity"
                    value={severity}
                    onChange={(e) => setSeverity(e.target.value)}
                    required
                  >
                    <option value="">Choose severity</option>
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                    <option value="CRITICAL">Critical</option>
                  </select>
                </div>
              </div>

              <div className="field">
                <label htmlFor="description">Description</label>
                <textarea
                  id="description"
                  name="description"
                  placeholder="What did you notice? Who or what is affected?"
                  required
                />
              </div>
            </section>

            <section className="form-section">
              <h2>Where is it?</h2>
              <div className="form-row">
                <div className="field">
                  <label htmlFor="building">Building</label>
                  <select
                    id="building"
                    name="building"
                    value={building}
                    onChange={event => selectBuilding(event.target.value)}
                    disabled={locationsLoading || Boolean(locationError)}
                    required
                  >
                    <option value="">{locationsLoading ? 'Loading buildings...' : 'Choose building'}</option>
                    {buildings.map(value => <option key={value} value={value}>{value}</option>)}
                  </select>
                </div>

                <div className="field">
                  <label htmlFor="floor">Floor</label>
                  <select
                    id="floor"
                    name="floor"
                    value={floor}
                    onChange={event => selectFloor(event.target.value)}
                    disabled={!building || locationsLoading || Boolean(locationError)}
                    required
                  >
                    <option value="">Choose floor</option>
                    {floors.map(value => <option key={value} value={value}>{value}</option>)}
                  </select>
                </div>
              </div>

              <div className="field">
                <label htmlFor="room">Room / area</label>
                <select
                  id="room"
                  name="room"
                  value={room}
                  onChange={event => selectRoom(event.target.value)}
                  disabled={!floor || locationsLoading || Boolean(locationError)}
                  required
                >
                  <option value="">Choose room / area</option>
                  {rooms.map(location => (
                    <option key={location.id} value={location.roomName}>{location.roomName}</option>
                  ))}
                </select>
              </div>

              {locationsLoading && <p className="small muted">Loading available locations...</p>}
              {locationError && <p className="form-error">{locationError}</p>}

              <div className="field">
                <label htmlFor="assetCode">Related asset</label>
                <select
                  id="assetCode"
                  name="assetCode"
                  value={assetCode}
                  onChange={event => setAssetCode(event.target.value)}
                  disabled={assetsLoading}
                >
                  <option value="">No related asset</option>
                  {assets.map(asset => (
                    <option key={asset.assetCode} value={asset.assetCode}>
                      {asset.assetCode} · {asset.assetName}
                    </option>
                  ))}
                </select>
                {assetsLoading && <p className="small muted">Loading available assets...</p>}
                {assetError && <p className="form-error">{assetError}</p>}
              </div>
            </section>

            <div className="form-footer">
              <Link className="btn btn-ghost" to="/dashboard">Cancel</Link>
              <Button className="btn-primary" type="submit" disabled={submitting || !locationReady}>
                {submitting ? 'Submitting...' : 'Submit issue ↗'}
              </Button>
            </div>

            {message && <p className="success-message">{message}</p>}
            {error && <p className="form-error">{error}</p>}
          </form>

          <aside className="side-stack">
            <section className="panel">
              <div className="panel-header">
                <h2>What happens next?</h2>
              </div>
              <div className="timeline">
                <div className="timeline-item">
                  <strong>Issue reviewed</strong>
                  <p>We check the context and location.</p>
                </div>
                <div className="timeline-item">
                  <strong>Priority determined</strong>
                  <p>The transparent score sets urgency.</p>
                </div>
                <div className="timeline-item">
                  <strong>Technician assigned</strong>
                  <p>Skills, workload, and location are matched.</p>
                </div>
                <div className="timeline-item pending">
                  <strong>Repair tracked</strong>
                  <p>You can follow every update.</p>
                </div>
              </div>
            </section>

            <section className="sla-card">
              <span>Priority preview</span>
              <strong>
                {['Waiting for severity', 'Low priority', 'Medium priority', 'High priority', 'Critical priority'][
                  { LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 }[severity] || 0
                ]}
              </strong>
              <p className="small sla-copy">Choose a severity to see how the issue may be routed.</p>
              <ProgressBar value={severity ? { LOW: 24, MEDIUM: 48, HIGH: 72, CRITICAL: 96 }[severity] : 18} />
            </section>
          </aside>
        </div>
      </main>
    </Shell>
  );
}

function Issues() {
  const admin = getSession()?.role === 'ADMIN';
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('');
  const [allRows, setAllRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => {
    apiRequest('/issues')
      .then(data => setAllRows(data.map(issue => ({ id: issue.issueNumber, numericId: issue.id, title: issue.title, category: issue.category, location: issue.location, priority: issue.priority, status: issue.status, technician: issue.technician, reported: issue.createdAt }))))
      .catch(err => setError(err.message || 'Could not load issues. Please try again.'))
      .finally(() => setLoading(false));
  }, []);
  const rows = useMemo(() => allRows.filter(i => !query || `${i.id} ${i.title} ${i.location}`.toLowerCase().includes(query.toLowerCase())).filter(i => !status || i.status === status), [allRows, query, status]);
  return <Shell role={admin ? 'admin' : 'user'}><main className="page"><PageHeader eyebrow={admin ? 'Operations queue' : 'Your maintenance history'} title={admin ? 'All issues' : 'My issues'} copy={admin ? 'Filter, assign, and monitor every maintenance request across Vertex Business Center.' : 'Track every report, from first note to verified resolution.'} action={<Link className="btn btn-primary" to="/report">＋ Create issue</Link>}/><div className="filter-bar"><input type="search" placeholder="Search issue ID, title, or location" value={query} onChange={e => setQuery(e.target.value)}/><select value={status} onChange={e => setStatus(e.target.value)}><option value="">All statuses</option><option>OPEN</option><option>ASSIGNED</option><option>IN_PROGRESS</option><option>WAITING_FOR_PARTS</option><option>RESOLVED</option><option>CLOSED</option></select><select><option>All priorities</option><option>Critical</option><option>High</option><option>Medium</option><option>Low</option></select></div>{loading && <p className="muted">Loading issues...</p>}{error && <p className="form-error">{error}</p>}{!loading && !error && <IssueTable rows={rows}/>}</main></Shell>
}
function IssueDetail() { return <Shell><main className="page"><PageHeader eyebrow="Issue details" title="FF-1042" copy="Reported 17 August 2026 by Alex Johnson" action={<Button className="btn-primary">Add update</Button>}/><div className="detail-grid"><section className="detail-hero"><div className="detail-meta"><StatusBadge value="High" type="priority"/><StatusBadge value="In progress"/></div><h1>AC not cooling in Conference Room 302</h1><p className="muted small">The room temperature has risen above 26°C since this morning. The meeting scheduled at 3 PM may be affected.</p><div className="detail-stat-grid"><div className="detail-stat"><span>Category</span><strong>HVAC</strong></div><div className="detail-stat"><span>Location</span><strong>Block A · Floor 3</strong></div><div className="detail-stat"><span>Asset</span><strong>HVAC-302</strong></div><div className="detail-stat"><span>Reporter</span><strong>Alex Johnson</strong></div></div><div className="detail-section"><h2>Assigned technician</h2><div className="tech-card"><A className="avatar orange">AM</A><div><strong>Arjun Mehta</strong><span>HVAC Specialist · 4 active jobs</span></div><Button className="btn-ghost">Message</Button></div></div><div className="detail-section"><h2>Issue timeline</h2><div className="timeline">{[['Reported', 'Issue submitted with room and asset context.', '17 Aug · 09:18'], ['Triaged', 'Priority set to High with a 6-hour SLA.', '17 Aug · 09:24'], ['Technician assigned', 'Arjun Mehta recommended by skill and location.', '17 Aug · 09:31'], ['Accepted', 'Arjun accepted the repair job.', '17 Aug · 09:40'], ['In progress', 'Technician is checking the condenser unit.', '17 Aug · 10:05'], ['Resolved', 'Waiting for repair completion and verification.', ''], ['Verified', 'Reporter confirms that the space is usable.', '']].map(([title, copy, time], i) => <div className={`timeline-item ${i > 4 ? 'pending' : ''}`} key={title}><strong>{title}</strong><p>{copy}</p>{time && <time>{time}</time>}</div>)}</div></div><div className="detail-section"><h2>Technician notes</h2><p>“The filter is clean. I’m testing the thermostat and condenser fan now; a replacement capacitor may be required.”</p></div></section><aside className="side-stack"><section className="sla-card"><span>SLA deadline · Today, 15:18</span><strong>3h 42m</strong><p className="small sla-copy">remaining before the issue is at risk</p><ProgressBar value={62}/></section><section className="panel"><div className="panel-header"><h2>Issue information</h2></div><div className="info-list"><div><span>Impact</span><strong>One meeting room</strong></div><div><span>People affected</span><strong>12</strong></div><div><span>Asset criticality</span><strong>High</strong></div><div><span>Preferred contact</span><strong>Email</strong></div></div></section><section className="panel"><div className="panel-header"><h2>Related asset</h2><Link to="/assets">View ↗</Link></div><div className="asset-health"><div className="health-ring">68%</div><div><strong>HVAC-302</strong><span>Conference Room HVAC · Needs attention</span></div></div></section></aside></div></main></Shell> }

function Technician() {
  const session = getSession();
  const [technician, setTechnician] = useState(null);
  const [metrics, setMetrics] = useState(null);
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [busyId, setBusyId] = useState(null);
  const [selectedJob, setSelectedJob] = useState(null);
  const [updateStatus, setUpdateStatus] = useState('IN_PROGRESS');
  const [updateNote, setUpdateNote] = useState('');
  const [updateError, setUpdateError] = useState('');

  const loadDashboard = async (showLoading = false) => {
    if (showLoading) setLoading(true);
    try {
      const profile = await apiRequest('/technicians/me');
      const [assigned, workload] = await Promise.all([
        apiRequest('/issues'),
        apiRequest('/technicians/me/metrics')
      ]);
      setTechnician(profile);
      setJobs(assigned);
      setMetrics(workload);
      setError('');
    } catch (err) {
      setTechnician(null);
      setMetrics(null);
      setJobs([]);
      throw err;
    } finally {
      if (showLoading) setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard(true).catch(err => {
      setError(err.message === 'Technician profile was not found.'
        ? 'No technician profile is linked to this account.'
        : err.message || 'Could not load your assigned jobs.');
      setLoading(false);
    });
  }, [session?.id]);

  const openUpdateModal = job => {
    setSelectedJob(job);
    setUpdateStatus(TECHNICIAN_STATUSES.includes(job.status) ? job.status : 'IN_PROGRESS');
    setUpdateNote('');
    setUpdateError('');
    setError('');
    setNotice('');
  };

  const closeUpdateModal = () => {
    if (busyId === null) {
      setSelectedJob(null);
      setUpdateError('');
    }
  };

  const runAction = async (job, action) => {
    if (!technician || busyId !== null) return;
    if (action === 'update') {
      openUpdateModal(job);
      return;
    }
    setBusyId(job.id);
    setError('');
    setNotice('');
    try {
      await apiRequest(`/issues/${job.id}/accept`, { method: 'POST', body: JSON.stringify({}) });
      await loadDashboard();
      setNotice(`${job.issueNumber} accepted and moved to In Progress.`);
    } catch (err) {
      setError(err.message || 'The technician action could not be completed.');
    } finally {
      setBusyId(null);
    }
  };

  const submitStatusUpdate = async event => {
    event.preventDefault();
    if (!selectedJob || busyId !== null) return;
    if (!TECHNICIAN_STATUSES.includes(updateStatus)) {
      setUpdateError('Choose a supported technician status.');
      return;
    }
    setBusyId(selectedJob.id);
    setUpdateError('');
    setError('');
    setNotice('');
    try {
      await apiRequest(`/issues/${selectedJob.id}/technician-update`, {
        method: 'PUT',
        body: JSON.stringify({ status: updateStatus, note: updateNote.trim() })
      });
      await loadDashboard();
      setSelectedJob(null);
      setNotice(`${selectedJob.issueNumber} updated successfully.`);
    } catch (err) {
      setUpdateError(err.message || 'The issue status could not be updated. Please try again.');
    } finally {
      setBusyId(null);
    }
  };

  const currentJobs = technician?.currentJobs || 0;
  const capacityLimit = technician?.capacityLimit || 0;
  const capacityPercent = capacityLimit ? Math.min(100, Math.round((currentJobs / capacityLimit) * 100)) : 0;
  const highPriorityJobs = jobs.filter(job => ['HIGH', 'CRITICAL'].includes(job.priority)).length;
  const slaRiskJobs = jobs.filter(job => {
    if (['RESOLVED', 'CLOSED'].includes(job.status) || !job.slaDeadline) return false;
    const deadline = new Date(job.slaDeadline).getTime();
    return Number.isFinite(deadline) && deadline - Date.now() <= 3 * 60 * 60 * 1000;
  }).length;
  const skills = technician?.skills ? technician.skills.split(',').map(skill => skill.trim()).filter(Boolean) : [];
  const averageResolution = metrics?.averageResolutionHours == null ? '—' : `${Number(metrics.averageResolutionHours).toFixed(1)} hours`;
  const onTimeRate = metrics?.onTimeRate == null ? '—' : `${metrics.onTimeRate}%`;

  return <Shell role="technician"><main className="page"><PageHeader eyebrow="Technician workspace" title="Your work queue." copy="Focus on the jobs that need your skills next, with the SLA context to make the right call." action={<Link className="btn btn-ghost" to="/assets">View assigned assets</Link>}/><section className="kpi-grid"><StatCard label="Assigned jobs" value={currentJobs} trend={technician ? `${currentJobs} current assignment${currentJobs === 1 ? '' : 's'}` : 'No profile data'}/><StatCard label="High priority" value={highPriorityJobs} trend={highPriorityJobs ? 'Requires focus' : 'No high-priority jobs'} danger={highPriorityJobs > 0}/><StatCard label="SLA risk" value={slaRiskJobs} trend={slaRiskJobs ? `${slaRiskJobs} due within 3 hours` : 'No immediate risk'} danger={slaRiskJobs > 0}/><StatCard label="Completed this month" value={metrics?.completedThisMonth ?? 0} trend="Current calendar month"/></section><div className="dashboard-grid wide-right"><section><div className="panel-header"><h2>Assigned jobs</h2><Link to="/issues">Open full queue ↗</Link></div>{error && <p className="form-error">{error}</p>}{notice && <p className="success-message">{notice}</p>}{loading ? <p className="muted">Loading assigned jobs...</p> : !jobs.length ? <section className="panel"><p className="muted">{technician ? 'No jobs are currently assigned to this technician.' : 'No technician profile is linked to this account.'}</p></section> : <div className="job-grid">{jobs.map(job => <article className="job-card" key={job.id}><div className="job-card-head"><StatusBadge value={job.priority} type="priority"/><span className="small muted">{job.issueNumber}</span></div><h3>{job.title}</h3><p>{job.location} · {job.category}</p><div className="job-meta"><span>SLA<strong>{job.slaDeadline ? exactTime(job.slaDeadline) : 'Not available'}</strong></span><span>Status<strong>{displayLabel(job.status)}</strong></span></div><div className="job-actions">{!['RESOLVED', 'CLOSED'].includes(job.status) && <Button className="btn-teal" disabled={busyId !== null} onClick={() => runAction(job, job.status === 'ASSIGNED' ? 'accept' : 'update')}>{busyId === job.id ? 'Saving...' : job.status === 'ASSIGNED' ? 'Accept' : 'Update'}</Button>}<Link className="btn btn-ghost" to={`/issues/${job.id}`}>View</Link></div></article>)}</div>}</section><aside className="side-stack"><section className="panel"><div className="panel-header"><h2>My workload</h2><Link to="/profile">Availability ↗</Link></div><div className="metric"><div className="metric-head"><span>Current capacity</span><span>{capacityPercent}%</span></div><ProgressBar value={capacityPercent} tone="orange"/></div><div className="info-list"><div><span>Open assignments</span><strong>{currentJobs} of {capacityLimit || '—'}</strong></div><div><span>Average resolution</span><strong>{averageResolution}</strong></div><div><span>On-time rate</span><strong>{onTimeRate}</strong></div></div></section><section className="panel"><div className="panel-header"><h2>Skills</h2></div>{skills.length ? <div className="detail-meta">{skills.map(skill => <StatusBadge value={skill} key={skill}/>)}</div> : <p className="muted">No skills are recorded for this technician.</p>}</section></aside></div></main><StatusUpdateModal job={selectedJob} status={updateStatus} note={updateNote} error={updateError} submitting={busyId === selectedJob?.id} onStatusChange={setUpdateStatus} onNoteChange={setUpdateNote} onCancel={closeUpdateModal} onSubmit={submitStatusUpdate}/></Shell>
}

function Admin() { return <Shell role="admin"><main className="page"><PageHeader eyebrow="Operations overview · 17 August 2026" title="Maintenance Command Center" copy="A live view of what needs attention, who is working on it, and which assets are asking for a bigger decision." action={<Link className="btn btn-primary" to="/issues">Manage issues ↗</Link>}/><section className="kpi-grid"><StatCard label="Open issues" value="128" trend="↓ 8% this week"/><StatCard label="Critical" value="17" trend="5 need assignment" danger/><StatCard label="Active repairs" value="31" trend="14 due today"/><StatCard label="SLA compliance" value="91%" trend="↑ 3.4% this month"/></section><div className="chart-grid"><section className="panel"><div className="panel-header"><h2>Issues by category</h2><Link to="/issues">View issues ↗</Link></div><div className="donut-wrap"><div className="donut"/><div className="legend"><span><i/>Electrical · 42%</span><span><i className="orange"/>HVAC · 24%</span><span><i className="purple"/>IT / Network · 16%</span><span><i className="gray"/>Other · 18%</span></div></div></section><section className="panel"><div className="panel-header"><h2>Issues by priority</h2></div>{[['Critical', 28, 'red', 17], ['High', 56, 'orange', 39], ['Medium', 72, '', 51], ['Low', 31, '', 21]].map(([label, value, tone, count]) => <div className="metric" key={label}><div className="metric-head"><span>{label}</span><span>{count}</span></div><ProgressBar value={value} tone={tone}/></div>)}</section></div><div className="dashboard-grid"><section className="panel"><div className="panel-header"><h2>Critical issues</h2><Link to="/issues">View all ↗</Link></div><IssueTable rows={mockIssues.filter(i => i.priority === 'CRITICAL' || i.priority === 'HIGH')}/></section><AdminTechnicianWorkload/></div><div className="dashboard-grid wide-right"><section className="panel"><div className="panel-header"><h2>Most failure-prone assets</h2><Link to="/assets">Asset health ↗</Link></div>{mockAssets.slice(0, 3).map(asset => <div className="health-row" key={asset.id}><div><strong>{asset.id} · {asset.name}</strong><span>{asset.failures} incidents · Avg resolution 4.8 hours</span><ProgressBar value={asset.health} tone={asset.health < 70 ? 'orange' : ''}/></div><strong>{asset.health}%</strong></div>)}</section><NotificationsPanel/></div></main></Shell> }

function Assets() {
  const session = getSession();
  const [accountKey, setAccountKey] = useState(() => accountSessionKey(getSession()));
  const [assets, setAssets] = useState(null);
  const [assetsAccountKey, setAssetsAccountKey] = useState(null);
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [errorAccountKey, setErrorAccountKey] = useState(null);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    const syncAccount = () => {
      const nextKey = accountSessionKey(getSession());
      setAccountKey(currentKey => currentKey === nextKey ? currentKey : nextKey);
    };
    window.addEventListener('storage', syncAccount);
    window.addEventListener('focus', syncAccount);
    document.addEventListener('visibilitychange', syncAccount);
    return () => {
      window.removeEventListener('storage', syncAccount);
      window.removeEventListener('focus', syncAccount);
      document.removeEventListener('visibilitychange', syncAccount);
    };
  }, []);

  useEffect(() => {
    let active = true;
    const accountChanged = assetsAccountKey !== accountKey;
    if (accountChanged) {
      setAssets(null);
      setAssetsAccountKey(null);
      setQuery('');
      setCategory('');
    }
    setError('');
    setErrorAccountKey(null);
    setLoading(true);
    const requestedAccountKey = accountKey;
    const stillCurrent = () => active && requestedAccountKey === accountSessionKey(getSession());

    apiRequest('/assets', { cache: 'no-store' })
      .then(rows => {
        if (!stillCurrent()) return;
        if (!Array.isArray(rows)) throw new Error('The assets response was not valid.');
        setAssets(rows);
        setAssetsAccountKey(requestedAccountKey);
      })
      .catch(err => {
        if (!stillCurrent()) return;
        setError(err.message || 'Could not load assets. Please try again.');
        setErrorAccountKey(requestedAccountKey);
      })
      .finally(() => {
        if (stillCurrent()) setLoading(false);
      });

    return () => { active = false; };
  }, [accountKey, retryCount]);

  const accountAssets = assetsAccountKey === accountKey ? assets : null;
  const accountError = errorAccountKey === accountKey ? error : '';
  const categories = useMemo(() => getAssetCategories(accountAssets || []), [accountAssets]);
  const visibleAssets = useMemo(() => filterAssets(accountAssets || [], query, category), [accountAssets, category, query]);
  const metrics = useMemo(() => accountAssets ? summarizeAssets(visibleAssets) : null, [accountAssets, visibleAssets]);
  const role = String(session?.role || '').toUpperCase();
  const technician = role === 'TECHNICIAN';
  const homePath = role === 'ADMIN' ? '/admin' : technician ? '/technician' : '/dashboard';

  return <Shell><main className="page">
    <PageHeader eyebrow="Know what keeps the place running" title={technician ? 'Assigned assets' : 'Asset management'} copy={technician ? 'Review assets associated with your active issue assignments.' : 'See the health, history, and maintenance context behind the spaces you use every day.'} action={<Link className="btn btn-primary" to={homePath}>{role === 'ADMIN' ? 'Open command center ↗' : 'Back to dashboard ↗'}</Link>}/>
    {loading && !accountAssets && <section className="panel"><p className="muted">Loading assets...</p></section>}
    {accountError && <section className="panel"><p className="form-error" role="alert">{accountError}</p><Button className="btn-ghost" onClick={() => setRetryCount(count => count + 1)} disabled={loading}>{loading ? 'Retrying...' : 'Retry'}</Button></section>}
    {metrics && <>
      <section className="kpi-grid">
        <StatCard label="Total assets" value={String(metrics.total).padStart(2, '0')} trend={`${metrics.total} records returned`}/>
        <StatCard label="Needs attention" value={String(metrics.needsAttention).padStart(2, '0')} trend="Condition: Needs attention" danger={metrics.needsAttention > 0}/>
        <StatCard label="Under maintenance" value={String(metrics.underMaintenance).padStart(2, '0')} trend="Condition: Under maintenance"/>
        <StatCard label="Average health" value={metrics.averageHealth === null ? '—' : `${metrics.averageHealth}%`} trend="Average stored health score"/>
      </section>
      <div className="filter-bar">
        <input type="search" placeholder="Search asset ID or name" value={query} onChange={event => setQuery(event.target.value)}/>
        <select aria-label="Filter assets by category" value={category} onChange={event => setCategory(event.target.value)}><option value="">All categories</option>{categories.map(option => <option value={option} key={option}>{option}</option>)}</select>
      </div>
      {!accountAssets.length && <section className="panel"><p className="muted">No assets are available for this account.</p></section>}
      {accountAssets.length > 0 && !visibleAssets.length && <section className="panel"><p className="muted">No assets match the current search and category filter.</p></section>}
      {visibleAssets.length > 0 && <div className="table-card"><table className="issue-table"><thead><tr><th>Asset ID</th><th>Asset</th><th>Category</th><th>Location</th><th>Condition</th><th>Health</th><th>Last maintenance</th><th>Warranty</th></tr></thead><tbody>{visibleAssets.map(asset => <tr key={asset.id}><td><strong>{asset.assetCode}</strong></td><td><span className="issue-title">{asset.assetName}</span><span className="issue-sub">{asset.failureCount} corrective records</span></td><td>{asset.category}</td><td>{asset.location}</td><td><StatusBadge value={asset.condition}/></td><td><div className="asset-health"><div className="health-ring">{Number.isFinite(Number(asset.healthScore)) ? `${asset.healthScore}%` : '—'}</div></div></td><td>{asset.lastMaintenance || '—'}</td><td>{asset.warrantyEnd || '—'}</td></tr>)}</tbody></table></div>}
    </>}
  </main></Shell>;
}
function Profile() { return <Shell><main className="page"><PageHeader eyebrow="Your FixFlow account" title="Profile & settings" copy="Keep your details current and choose how the operations team keeps you informed."/><div className="dashboard-grid wide-right"><form className="form-card" onSubmit={e => e.preventDefault()}><section className="form-section"><h2>Profile</h2><div className="form-row"><Field label="Full name" name="profile-name" placeholder="Alex Johnson"/><Field label="Work email" name="profile-email" type="email" placeholder="alex@vertexbusiness.com"/></div><div className="form-row"><Field label="Phone" name="profile-phone" placeholder="+91 98765 43210"/><Field label="Organization" name="profile-org" placeholder="Vertex Business Center"/></div></section><section className="form-section"><h2>Notification preferences</h2><label className="terms pref"><input type="checkbox" defaultChecked/> Notify me when an issue changes status.</label><label className="terms pref"><input type="checkbox" defaultChecked/> Notify me before an SLA deadline is at risk.</label><label className="terms pref"><input type="checkbox"/> Send a weekly maintenance summary.</label></section><div className="form-footer"><Button className="btn-primary">Save changes</Button></div></form><aside className="side-stack"><section className="panel"><div className="panel-header"><h2>Account settings</h2></div><div className="info-list"><div><span>Role</span><strong>User</strong></div><div><span>Member since</span><strong>May 2026</strong></div><div><span>Workspace</span><strong>Vertex Business Center</strong></div><div><span>Notifications</span><strong>2 unread</strong></div></div></section><NotificationsPanel/></aside></div></main></Shell> }

function useActiveTechnicians({ refreshOnFocus = false, pollIntervalMs = 0 } = {}) {
  const [technicians, setTechnicians] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const loaded = useRef(false);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      const firstLoad = !loaded.current;
      if (firstLoad) setLoading(true);
      try {
        const data = await apiRequest('/technicians', { cache: 'no-store' });
        if (!mounted) return;
        setTechnicians(data);
        setError('');
        loaded.current = true;
      } catch (err) {
        if (mounted) setError(err.message || 'Could not load technicians. Please try again.');
      } finally {
        if (mounted && firstLoad) setLoading(false);
      }
    };

    load();
    const handleFocus = refreshOnFocus ? () => load() : null;
    if (handleFocus) window.addEventListener('focus', handleFocus);
    const timer = pollIntervalMs > 0 ? window.setInterval(load, pollIntervalMs) : null;
    return () => {
      mounted = false;
      if (handleFocus) window.removeEventListener('focus', handleFocus);
      if (timer) window.clearInterval(timer);
    };
  }, [pollIntervalMs, refreshOnFocus]);

  return { technicians, loading, error };
}

function technicianLoadPercent(technician) {
  return technician.capacityLimit ? Math.min(100, Math.round((technician.currentJobs / technician.capacityLimit) * 100)) : 0;
}

function AdminTechnicianWorkload() {
  const { technicians, loading, error } = useActiveTechnicians();
  return <section className="panel"><div className="panel-header"><h2>Technician workload</h2><Link to="/technicians">Manage ↗</Link></div>{loading && <p className="muted">Loading technician workload...</p>}{error && <p className="form-error">{error}</p>}{!loading && !error && !technicians.length && <p className="muted">No active technicians are registered.</p>}{!loading && !error && technicians.map(technician => <div className="health-row" key={technician.id}><div><strong>{technician.fullName}</strong><span>{technician.skills || 'No skills recorded'} · {displayLabel(technician.availabilityStatus)}</span><ProgressBar value={technicianLoadPercent(technician)} tone={technicianLoadPercent(technician) > 70 ? 'orange' : ''}/></div><strong>{technician.currentJobs} / {technician.capacityLimit || '—'}</strong></div>)}</section>;
}

function AdminTechnicians() {
  const { technicians, loading, error } = useActiveTechnicians({ refreshOnFocus: true, pollIntervalMs: 60000 });

  return <Shell role="admin"><main className="page"><PageHeader eyebrow="People and workload" title="Technician management" copy="Review active registered technicians, their availability, and current workload." action={<Link className="btn btn-ghost" to="/admin">← Overview</Link>}/><section className="panel"><div className="panel-header"><h2>Active technicians</h2><span className="muted small">Loaded from registered accounts</span></div>{loading && <p className="muted">Loading technicians...</p>}{error && <p className="form-error">{error}</p>}{!loading && !error && !technicians.length && <p className="muted">No active technicians are registered.</p>}{!loading && !error && technicians.length > 0 && <div className="table-card"><table className="issue-table"><thead><tr><th>Technician</th><th>Availability</th><th>Workload</th><th>Skills</th></tr></thead><tbody>{technicians.map(technician => <tr key={technician.id}><td><strong>{technician.fullName}</strong><span className="issue-sub">Registered technician</span></td><td><StatusBadge value={technician.availabilityStatus || 'UNKNOWN'}/></td><td>{technician.currentJobs} / {technician.capacityLimit} active jobs</td><td>{technician.skills || 'No skills recorded'}</td></tr>)}</tbody></table></div>}</section></main></Shell>;
}

function App() { return <Routes><Route path="/" element={<Landing/>}/><Route path="/login" element={<Auth/>}/><Route path="/register" element={<Auth register/>}/><Route path="/dashboard" element={<RequireRole role="USER"><UserDashboard/></RequireRole>}/><Route path="/report" element={<ReportIssue/>}/><Route path="/issues" element={<Issues/>}/><Route path="/issues/:id" element={<IssueDetailsLive/>}/><Route path="/technician" element={<RequireRole role="TECHNICIAN"><Technician/></RequireRole>}/><Route path="/technicians" element={<RequireRole role="ADMIN"><AdminTechnicians/></RequireRole>}/><Route path="/admin" element={<RequireRole role="ADMIN"><Admin/></RequireRole>}/><Route path="/assets" element={<RequireAuthenticated><Assets/></RequireAuthenticated>}/><Route path="/profile" element={<Profile/>}/></Routes> }

export default App
