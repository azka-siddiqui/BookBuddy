import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const NAV_ITEMS = [
  { to: '/', label: 'Home', description: 'Search and discover books', end: true, icon: '🏠' },
  { to: '/recommendations', label: 'Recommendations', description: 'Personalized suggestions', icon: '⭐' },
  { to: '/community', label: 'Community', description: 'Connect with other readers', icon: '👥' },
  { to: '/profile', label: 'My Profile', description: 'Your reading journey', icon: '👤' },
];

export default function Layout() {
  const { user, logout } = useAuth();

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <h1>Book Explorer</h1>
          <p>Welcome back, {user?.displayName || 'Reader'}!</p>
        </div>
        <nav className="sidebar-nav">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
            >
              <span className="nav-icon" aria-hidden>{item.icon}</span>
              <span className="nav-text">
                <span className="nav-label">{item.label}</span>
                <span className="nav-description">{item.description}</span>
              </span>
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">Track your reading journey</div>
      </aside>

      <div className="main">
        <header className="topbar">
          <span className="topbar-user">{user?.displayName}</span>
          <button className="btn btn-ghost" onClick={logout}>
            Logout
          </button>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
