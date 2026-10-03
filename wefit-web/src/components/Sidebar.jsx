import React from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Activity, 
  History, 
  BrainCircuit, 
  MessageSquare, 
  Bell, 
  Users, 
  User,
  LogOut,
  Home
} from 'lucide-react';
import { useKeycloak } from '@react-keycloak/web';
import './Sidebar.css';

const Sidebar = () => {
  let keycloakObj = null;
  try {
    const kc = useKeycloak();
    keycloakObj = kc?.keycloak;
  } catch (e) {}

  const navigate = useNavigate();
  const isGuest = localStorage.getItem('wefit_guest_mode') === 'true';
  const username = keycloakObj?.tokenParsed?.preferred_username || (isGuest ? 'Alex (Guest)' : 'Athlete');

  const handleLogout = () => {
    localStorage.removeItem('wefit_guest_mode');
    if (keycloakObj?.authenticated) {
      keycloakObj.logout({ redirectUri: window.location.origin });
    } else {
      navigate('/');
    }
  };

  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Activity Log', path: '/log', icon: Activity },
    { name: 'History', path: '/history', icon: History },
    { name: 'AI Coach', path: '/insights', icon: BrainCircuit },
    { name: 'Feed', path: '/feed', icon: MessageSquare },
    { name: 'Notifications', path: '/notifications', icon: Bell, badge: 1 },
    { name: 'Friends', path: '/friends', icon: Users },
    { name: 'Profile', path: '/profile', icon: User },
  ];

  return (
    <aside className="sidebar glass">
      <div className="sidebar-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Link to="/dashboard" style={{ textDecoration: 'none' }}>
          <h2 className="text-volt" style={{ margin: 0, cursor: 'pointer' }}>WEFIT</h2>
        </Link>
        <Link to="/" title="Go to Landing Page" style={{ color: 'rgba(255,255,255,0.4)', display: 'flex', alignItems: 'center' }}>
          <Home size={18} />
        </Link>
      </div>
      <nav className="sidebar-nav">
        {navItems.map((item) => (
          <NavLink 
            key={item.name} 
            to={item.path}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <item.icon className="nav-icon" size={20} />
            <span>{item.name}</span>
            {item.badge && <span className="nav-badge">{item.badge}</span>}
          </NavLink>
        ))}
      </nav>
      <div className="sidebar-footer">
        <div className="user-info" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div className="avatar">{username.charAt(0).toUpperCase()}</div>
            <div className="user-details">
              <span className="user-name">{username}</span>
              <span className="user-role">{isGuest ? 'Demo Athlete' : 'Pro Member'}</span>
            </div>
          </div>
          <button 
            onClick={handleLogout} 
            title="Log Out" 
            style={{ 
              background: 'none', 
              border: 'none', 
              color: 'rgba(255,255,255,0.4)', 
              cursor: 'pointer',
              padding: '6px',
              borderRadius: '6px',
              display: 'flex',
              alignItems: 'center'
            }}
            onMouseEnter={(e) => e.currentTarget.style.color = '#ff4d4d'}
            onMouseLeave={(e) => e.currentTarget.style.color = 'rgba(255,255,255,0.4)'}
          >
            <LogOut size={18} />
          </button>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;

