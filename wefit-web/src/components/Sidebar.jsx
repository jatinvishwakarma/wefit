import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Activity, 
  History, 
  BrainCircuit, 
  MessageSquare, 
  Bell, 
  Users, 
  User 
} from 'lucide-react';
import { useKeycloak } from '@react-keycloak/web';
import './Sidebar.css';

const Sidebar = () => {
  const { keycloak } = useKeycloak();
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
      <div className="sidebar-header">
        <h2 className="text-volt">WEFIT</h2>
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
        <div className="user-info">
          <div className="avatar">{keycloak.tokenParsed?.preferred_username?.charAt(0).toUpperCase() || 'A'}</div>
          <div className="user-details">
            <span className="user-name">{keycloak.tokenParsed?.preferred_username || 'Alex K.'}</span>
            <span className="user-role">Free Member</span>
          </div>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
