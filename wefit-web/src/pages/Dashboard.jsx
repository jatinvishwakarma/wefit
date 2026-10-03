import React, { useState } from 'react';
import Sidebar from '../components/Sidebar';
import { 
  Bell, Search, Activity, Target, Flame, BrainCircuit, ShieldAlert, 
  Users, Send, Plus, CheckCircle, Trophy, ThumbsUp, MessageSquare, 
  Share2, Award, Calendar, Heart, Dumbbell, Bike, Clock, ChevronRight, UserCheck
} from 'lucide-react';
import { useKeycloak } from '@react-keycloak/web';
import { motion, AnimatePresence } from 'framer-motion';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useLocation, Link, useNavigate } from 'react-router-dom';
import { 
  getUserProfile, getUserLevel, getLeaderboard, getActivities, 
  logActivity, getFeed, chatWithAi 
} from '../api/services';
import './Dashboard.css';

const Dashboard = ({ initialView }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // Determine current active view from URL path
  const path = location.pathname.replace('/', '') || initialView || 'dashboard';
  const currentView = ['dashboard', 'log', 'history', 'insights', 'feed', 'notifications', 'friends', 'profile'].includes(path) 
    ? path 
    : 'dashboard';

  let keycloakObj = null;
  try {
    const kc = useKeycloak();
    keycloakObj = kc?.keycloak;
  } catch (e) {}

  const isGuest = localStorage.getItem('wefit_guest_mode') === 'true';
  const userId = keycloakObj?.tokenParsed?.sub || 'demo-user-1';

  // React Queries
  const { data: userProfile } = useQuery({
    queryKey: ['userProfile', userId],
    queryFn: () => getUserProfile(userId),
  });

  const { data: gamification } = useQuery({
    queryKey: ['gamification', userId],
    queryFn: getUserLevel,
  });

  const { data: leaderboard } = useQuery({
    queryKey: ['leaderboard'],
    queryFn: getLeaderboard,
  });

  const { data: activities = [] } = useQuery({
    queryKey: ['activities', userId],
    queryFn: getActivities,
  });

  const { data: feedData = [] } = useQuery({
    queryKey: ['feed'],
    queryFn: getFeed,
  });

  // State for AI Chat
  const [aiMessage, setAiMessage] = useState('');
  const [chatHistory, setChatHistory] = useState([
    { sender: 'ai', text: "Hello! I am your Wefit AI Coach. How are your legs feeling after your recent sessions?" }
  ]);

  const aiMutation = useMutation({
    mutationFn: chatWithAi,
    onSuccess: (data) => {
      setChatHistory(prev => [...prev, { sender: 'ai', text: data.response }]);
    },
    onError: () => {
      setChatHistory(prev => [...prev, { sender: 'ai', text: 'Error connecting to AI Coach.' }]);
    }
  });

  const handleAiSubmit = (e) => {
    e?.preventDefault();
    if (!aiMessage.trim()) return;
    const msg = aiMessage.trim();
    setChatHistory(prev => [...prev, { sender: 'user', text: msg }]);
    setAiMessage('');
    aiMutation.mutate(msg);
  };

  // State for Activity Logging
  const [actType, setActType] = useState('Running');
  const [actName, setActName] = useState('');
  const [actDuration, setActDuration] = useState('');
  const [actDistance, setActDistance] = useState('');
  const [actCalories, setActCalories] = useState('');
  const [logSuccess, setLogSuccess] = useState(false);

  const logMutation = useMutation({
    mutationFn: logActivity,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['activities'] });
      setLogSuccess(true);
      setActName('');
      setActDuration('');
      setActDistance('');
      setActCalories('');
      setTimeout(() => setLogSuccess(false), 4000);
    }
  });

  const handleLogSubmit = (e) => {
    e.preventDefault();
    if (!actDuration) return;
    logMutation.mutate({
      type: actType,
      name: actName || `${actType} Session`,
      duration: Number(actDuration),
      distance: actDistance ? Number(actDistance) : null,
      calories: actCalories ? Number(actCalories) : Number(actDuration) * 8,
    });
  };

  // State for Social Feed Cheers
  const [feedItems, setFeedItems] = useState(feedData);
  React.useEffect(() => {
    if (feedData.length > 0 && feedItems.length === 0) setFeedItems(feedData);
  }, [feedData]);

  const handleCheer = (id) => {
    setFeedItems(prev => prev.map(item => {
      if (item.id === id) {
        return {
          ...item,
          cheers: item.cheered ? item.cheers - 1 : item.cheers + 1,
          cheered: !item.cheered
        };
      }
      return item;
    }));
  };

  // State for History Filters
  const [historyFilter, setHistoryFilter] = useState('All');

  // State for Notifications
  const [notifications, setNotifications] = useState([
    { id: 1, text: 'Priya N. cheered your 5K Trail Run! 👏', time: '10m ago', unread: true },
    { id: 2, text: 'Active Streak milestone: 12 days achieved! 🔥', time: '2h ago', unread: true },
    { id: 3, text: 'Dev S. joined the 100km October cycling challenge.', time: '1d ago', unread: false },
    { id: 4, text: 'AI Coach generated your optimal recovery nutrition plan.', time: '2d ago', unread: false },
  ]);

  const markAllNotificationsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, unread: false })));
  };

  // State for Friends
  const [friendsList, setFriendsList] = useState([
    { id: 'f1', name: 'Priya N.', level: 11, xp: 4820, following: true, status: 'Completed 10K road race' },
    { id: 'f2', name: 'Karan M.', level: 9, xp: 3950, following: true, status: 'Resting today' },
    { id: 'f3', name: 'Dev S.', level: 5, xp: 1980, following: true, status: 'In gym: Heavy squats' },
    { id: 'f4', name: 'Maya R.', level: 6, xp: 2410, following: false, status: 'Cycling enthusiast' },
  ]);

  const toggleFollow = (id) => {
    setFriendsList(prev => prev.map(f => f.id === id ? { ...f, following: !f.following } : f));
  };

  const username = userProfile?.firstName || keycloakObj?.tokenParsed?.preferred_username || (isGuest ? 'Alex (Guest)' : 'Athlete');
  const level = gamification?.level || 7;
  const xp = gamification?.xp || 2840;
  const streak = gamification?.currentStreak || 12;

  return (
    <div className="dashboard-layout">
      <Sidebar />
      <main className="dashboard-content">
        {/* Header Bar */}
        <header className="dashboard-header">
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
              <span className="badge" style={{ background: 'rgba(204,255,0,0.15)', color: '#ccff00', border: '1px solid rgba(204,255,0,0.3)' }}>
                {isGuest ? '🎮 Demo / Guest Mode' : '⚡ Keycloak Authenticated'}
              </span>
              {isGuest && (
                <button 
                  onClick={() => { localStorage.removeItem('wefit_guest_mode'); keycloakObj?.login ? keycloakObj.login() : navigate('/'); }}
                  style={{ background: 'none', border: 'none', color: '#00f2fe', cursor: 'pointer', fontSize: '0.85rem', textDecoration: 'underline' }}
                >
                  Log in with credentials
                </button>
              )}
            </div>
            <h1 className="greeting">
              Welcome back, <br/><span className="text-volt">{username}</span> (Level {level})
            </h1>
          </div>
          <div className="header-actions">
            <Link to="/log" className="btn-primary" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 18px' }}>
              <Plus size={18} /> Log Workout
            </Link>
            <Link to="/notifications" className="icon-btn notification-btn" title="Notifications">
              <Bell size={20} />
              {notifications.some(n => n.unread) && <div className="badge">{notifications.filter(n => n.unread).length}</div>}
            </Link>
          </div>
        </header>

        {/* Dynamic View Rendering based on active route */}
        {currentView === 'dashboard' && (
          <div>
            {/* Top Metric Cards */}
            <motion.section 
              initial="hidden"
              animate="visible"
              variants={{ hidden: { opacity: 0 }, visible: { opacity: 1, transition: { staggerChildren: 0.1 } } }}
              className="stats-grid"
            >
              <motion.div variants={{ hidden: { y: 20, opacity: 0 }, visible: { y: 0, opacity: 1 } }} whileHover={{ y: -5 }} className="stat-card card">
                <div className="stat-header">
                  <span className="label">Total XP</span>
                  <Target size={20} color="var(--color-primary-container)" />
                </div>
                <div className="stat-value text-volt">{xp.toLocaleString()}</div>
                <div className="stat-footer">Level {level} Athlete</div>
              </motion.div>

              <motion.div variants={{ hidden: { y: 20, opacity: 0 }, visible: { y: 0, opacity: 1 } }} whileHover={{ y: -5 }} className="stat-card card">
                <div className="stat-header">
                  <span className="label">Activity Count</span>
                  <Activity size={20} color="var(--color-error)" />
                </div>
                <div className="stat-value">{activities.length}</div>
                <div className="stat-footer">Workouts logged</div>
              </motion.div>

              <motion.div variants={{ hidden: { y: 20, opacity: 0 }, visible: { y: 0, opacity: 1 } }} whileHover={{ y: -5 }} className="stat-card card">
                <div className="stat-header">
                  <span className="label">Active Streak</span>
                  <Flame size={20} color="var(--color-secondary-container)" />
                </div>
                <div className="stat-value text-cyan">{streak} Days</div>
                <div className="stat-footer">Keep the fire burning!</div>
              </motion.div>

              <motion.div variants={{ hidden: { y: 20, opacity: 0 }, visible: { y: 0, opacity: 1 } }} whileHover={{ y: -5 }} className="stat-card card">
                <div className="stat-header">
                  <span className="label">Burnout Watch</span>
                  <ShieldAlert size={20} color="#ffb4ab" />
                </div>
                <div className="stat-value" style={{ color: '#ffb4ab' }}>6.4 / 10</div>
                <div className="stat-footer">Moderate fatigue (Zone 2 advised)</div>
              </motion.div>
            </motion.section>

            {/* Split Body: Leaderboard & AI Chat Widget */}
            <div className="dashboard-body">
              <motion.div initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.5 }} className="recent-activities">
                <div className="section-header">
                  <h3>Global Leaderboard <span className="label text-cyan ml-2">Top 5 Athletes</span></h3>
                  <Link to="/history" style={{ color: '#ccff00', fontSize: '0.9rem', textDecoration: 'none' }}>View History →</Link>
                </div>
                <div className="activity-list">
                  {leaderboard?.slice(0, 5).map((user, index) => (
                    <motion.div key={user.userId} whileHover={{ scale: 1.01 }} className={`activity-item card ${user.name.includes('you') ? 'highlight-card' : ''}`}>
                      <div className="activity-rank text-cyan">#{index + 1}</div>
                      <div className="activity-details" style={{ flexGrow: 1 }}>
                        <h4 className={user.name.includes('you') ? 'text-volt' : ''}>{user.name}</h4>
                        <span style={{ fontSize: '0.85rem', color: 'rgba(255,255,255,0.5)' }}>Training Time: {user.time || '320 min'}</span>
                      </div>
                      <div className="text-volt stat-value-sm">Lvl {user.level} ({user.xp} XP)</div>
                    </motion.div>
                  ))}
                </div>
              </motion.div>

              <motion.div initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.5 }} className="ai-preview">
                <div className="section-header">
                  <h3>AI Coach Quick Chat</h3>
                  <Link to="/insights" style={{ color: '#ccff00', fontSize: '0.9rem', textDecoration: 'none' }}>Full Studio →</Link>
                </div>
                <div className="insight-card card" style={{ display: 'flex', flexDirection: 'column', height: '380px' }}>
                  <div className="insight-header">
                    <BrainCircuit size={24} color="var(--color-primary-container)" />
                    <h4 className="text-volt">Your Adaptive Trainer</h4>
                  </div>
                  <div className="chat-window" style={{ flexGrow: 1, overflowY: 'auto', marginBottom: '16px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    {chatHistory.map((msg, i) => (
                      <div key={i} style={{ 
                        alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                        backgroundColor: msg.sender === 'user' ? 'var(--color-primary-container)' : 'rgba(255,255,255,0.08)',
                        color: msg.sender === 'user' ? '#000' : '#fff',
                        padding: '8px 12px',
                        borderRadius: '8px',
                        maxWidth: '85%',
                        fontSize: '0.88rem'
                      }}>
                        {msg.text}
                      </div>
                    ))}
                    {aiMutation.isPending && (
                      <div style={{ color: 'rgba(255,255,255,0.5)', fontSize: '0.8rem' }}>Coach is thinking...</div>
                    )}
                  </div>
                  <form onSubmit={handleAiSubmit} style={{ display: 'flex', gap: '8px' }}>
                    <input 
                      type="text" 
                      value={aiMessage}
                      onChange={(e) => setAiMessage(e.target.value)}
                      placeholder="Ask coach for tips..."
                      style={{ 
                        flexGrow: 1, padding: '10px 14px', borderRadius: '8px', 
                        border: '1px solid rgba(255,255,255,0.1)', background: 'rgba(255,255,255,0.05)', color: 'white' 
                      }}
                      disabled={aiMutation.isPending}
                    />
                    <button type="submit" className="icon-btn" disabled={aiMutation.isPending} style={{ background: '#ccff00', color: '#000' }}>
                      <Send size={16} />
                    </button>
                  </form>
                </div>
              </motion.div>
            </div>
          </div>
        )}

        {/* Activity Log View */}
        {currentView === 'log' && (
          <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} style={{ maxWidth: '700px' }}>
            <div className="section-header">
              <h2>Log a New Workout</h2>
            </div>
            {logSuccess && (
              <div style={{ background: 'rgba(204,255,0,0.15)', border: '1px solid #ccff00', color: '#ccff00', padding: '14px 20px', borderRadius: '10px', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                <CheckCircle size={20} />
                <span>Workout Logged Successfully! <strong>+100 XP</strong> awarded. Active streak continued! 🔥</span>
              </div>
            )}
            <form onSubmit={handleLogSubmit} className="card" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div>
                <label className="label" style={{ display: 'block', marginBottom: '8px' }}>Select Activity Type</label>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px' }}>
                  {['Running', 'Cycling', 'Weightlifting', 'HIIT', 'Swimming', 'Yoga'].map(type => (
                    <button
                      key={type}
                      type="button"
                      onClick={() => setActType(type)}
                      style={{
                        padding: '12px',
                        borderRadius: '8px',
                        border: actType === type ? '2px solid #ccff00' : '1px solid rgba(255,255,255,0.1)',
                        background: actType === type ? 'rgba(204,255,0,0.1)' : 'rgba(255,255,255,0.03)',
                        color: actType === type ? '#ccff00' : '#fff',
                        fontWeight: 600,
                        cursor: 'pointer'
                      }}
                    >
                      {type}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="label" style={{ display: 'block', marginBottom: '8px' }}>Workout Title</label>
                <input 
                  type="text" 
                  value={actName} 
                  onChange={(e) => setActName(e.target.value)} 
                  placeholder="e.g. 5K Sunrise Run" 
                  style={{ width: '100%', padding: '12px', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#fff' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label className="label" style={{ display: 'block', marginBottom: '8px' }}>Duration (Minutes) *</label>
                  <input 
                    type="number" 
                    required 
                    value={actDuration} 
                    onChange={(e) => setActDuration(e.target.value)} 
                    placeholder="e.g. 35" 
                    style={{ width: '100%', padding: '12px', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#fff' }}
                  />
                </div>
                <div>
                  <label className="label" style={{ display: 'block', marginBottom: '8px' }}>Distance (km - optional)</label>
                  <input 
                    type="number" 
                    step="0.1" 
                    value={actDistance} 
                    onChange={(e) => setActDistance(e.target.value)} 
                    placeholder="e.g. 6.2" 
                    style={{ width: '100%', padding: '12px', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#fff' }}
                  />
                </div>
              </div>

              <div>
                <label className="label" style={{ display: 'block', marginBottom: '8px' }}>Calories Burned (kcal)</label>
                <input 
                  type="number" 
                  value={actCalories} 
                  onChange={(e) => setActCalories(e.target.value)} 
                  placeholder="e.g. 420 (auto-calculated if blank)" 
                  style={{ width: '100%', padding: '12px', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#fff' }}
                />
              </div>

              <button type="submit" className="btn-primary" style={{ padding: '14px', fontSize: '1rem', marginTop: '10px' }}>
                Save Activity & Collect +100 XP
              </button>
            </form>
          </motion.div>
        )}

        {/* History View */}
        {currentView === 'history' && (
          <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }}>
            <div className="section-header">
              <h2>Workout History ({activities.length})</h2>
              <div style={{ display: 'flex', gap: '8px' }}>
                {['All', 'Running', 'Cycling', 'Weightlifting', 'HIIT'].map(cat => (
                  <button 
                    key={cat}
                    onClick={() => setHistoryFilter(cat)}
                    style={{
                      padding: '8px 16px',
                      borderRadius: '20px',
                      border: historyFilter === cat ? '1px solid #ccff00' : '1px solid rgba(255,255,255,0.1)',
                      background: historyFilter === cat ? 'rgba(204,255,0,0.15)' : 'transparent',
                      color: historyFilter === cat ? '#ccff00' : 'rgba(255,255,255,0.6)',
                      cursor: 'pointer',
                      fontSize: '0.85rem'
                    }}
                  >
                    {cat}
                  </button>
                ))}
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {activities
                .filter(act => historyFilter === 'All' || act.type.toLowerCase() === historyFilter.toLowerCase())
                .map(act => (
                  <div key={act.id} className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '18px 24px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                      <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: 'rgba(204,255,0,0.1)', color: '#ccff00', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        {act.type === 'Cycling' ? <Bike size={24} /> : act.type === 'Weightlifting' ? <Dumbbell size={24} /> : <Activity size={24} />}
                      </div>
                      <div>
                        <h4 style={{ margin: '0 0 4px 0', fontSize: '1.1rem' }}>{act.name}</h4>
                        <span style={{ fontSize: '0.85rem', color: 'rgba(255,255,255,0.4)' }}>{act.date || 'Recent workout'}</span>
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: '24px', alignItems: 'center' }}>
                      {act.distance && <div style={{ textAlign: 'right' }}><div className="text-volt" style={{ fontWeight: 700 }}>{act.distance} km</div><div style={{ fontSize: '0.75rem', color: 'rgba(255,255,255,0.4)' }}>Distance</div></div>}
                      <div style={{ textAlign: 'right' }}><div style={{ fontWeight: 700 }}>{act.duration} min</div><div style={{ fontSize: '0.75rem', color: 'rgba(255,255,255,0.4)' }}>Duration</div></div>
                      <div style={{ textAlign: 'right' }}><div className="text-cyan" style={{ fontWeight: 700 }}>{act.calories} kcal</div><div style={{ fontSize: '0.75rem', color: 'rgba(255,255,255,0.4)' }}>Burned</div></div>
                    </div>
                  </div>
                ))}
            </div>
          </motion.div>
        )}

        {/* AI Coach Studio View */}
        {currentView === 'insights' && (
          <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px', height: '620px' }}>
            <div className="card" style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
              <div style={{ borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '16px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '12px' }}>
                <BrainCircuit size={28} color="#ccff00" />
                <div>
                  <h3 style={{ margin: 0 }}>AI Coach Studio</h3>
                  <span style={{ fontSize: '0.85rem', color: 'rgba(255,255,255,0.4)' }}>Personalized physiological guidance</span>
                </div>
              </div>

              <div style={{ flexGrow: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '12px', paddingRight: '8px' }}>
                {chatHistory.map((msg, i) => (
                  <div key={i} style={{
                    alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                    background: msg.sender === 'user' ? '#ccff00' : 'rgba(255,255,255,0.06)',
                    color: msg.sender === 'user' ? '#000' : '#fff',
                    padding: '12px 18px',
                    borderRadius: '12px',
                    maxWidth: '80%',
                    lineHeight: '1.5'
                  }}>
                    {msg.text}
                  </div>
                ))}
                {aiMutation.isPending && (
                  <div style={{ color: 'rgba(255,255,255,0.5)', fontStyle: 'italic' }}>AI Coach is composing recovery recommendations...</div>
                )}
              </div>

              <div style={{ marginTop: '16px' }}>
                <div style={{ display: 'flex', gap: '8px', marginBottom: '12px', overflowX: 'auto', paddingBottom: '4px' }}>
                  {["Suggest post-workout meal", "My legs are sore, can I train?", "Give me a 3-day running split"].map((prompt, i) => (
                    <button
                      key={i}
                      type="button"
                      onClick={() => { setAiMessage(prompt); }}
                      style={{
                        padding: '6px 12px',
                        borderRadius: '16px',
                        background: 'rgba(255,255,255,0.05)',
                        border: '1px solid rgba(255,255,255,0.1)',
                        color: 'rgba(255,255,255,0.7)',
                        fontSize: '0.8rem',
                        whiteSpace: 'nowrap',
                        cursor: 'pointer'
                      }}
                    >
                      {prompt}
                    </button>
                  ))}
                </div>
                <form onSubmit={handleAiSubmit} style={{ display: 'flex', gap: '10px' }}>
                  <input
                    type="text"
                    value={aiMessage}
                    onChange={(e) => setAiMessage(e.target.value)}
                    placeholder="Ask your coach anything..."
                    style={{ flexGrow: 1, padding: '12px', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', color: '#fff' }}
                  />
                  <button type="submit" className="btn-primary" style={{ padding: '0 20px' }}>
                    Send
                  </button>
                </form>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="card">
                <div className="stat-header">
                  <span className="label">Fatigue & Recovery Index</span>
                  <ShieldAlert size={20} color="#ffb4ab" />
                </div>
                <div style={{ fontSize: '2rem', fontWeight: 800, color: '#ffb4ab', marginBottom: '8px' }}>6.4 / 10</div>
                <p style={{ fontSize: '0.85rem', color: 'rgba(255,255,255,0.6)', lineHeight: '1.4' }}>
                  Your training load has peaked over 3 consecutive active days. Muscle glycogen recovery is at 74%. Keep tomorrow's session light.
                </p>
              </div>

              <div className="card">
                <div className="stat-header">
                  <span className="label">Daily Nutrition Target</span>
                  <Heart size={20} color="#00f2fe" />
                </div>
                <div style={{ fontSize: '1.4rem', fontWeight: 700, color: '#00f2fe', marginBottom: '4px' }}>140g Protein</div>
                <div style={{ fontSize: '0.85rem', color: 'rgba(255,255,255,0.6)' }}>Target: 2,400 kcal (820 kcal burned)</div>
              </div>
            </div>
          </motion.div>
        )}

        {/* Social Feed View */}
        {currentView === 'feed' && (
          <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} style={{ maxWidth: '750px' }}>
            <div className="section-header">
              <h2>Club Social Feed</h2>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
              {feedItems.map(item => (
                <div key={item.id} className="card" style={{ padding: '24px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <div className="avatar">{item.avatar}</div>
                      <div>
                        <h4 style={{ margin: 0 }}>{item.author}</h4>
                        <span style={{ fontSize: '0.8rem', color: 'rgba(255,255,255,0.4)' }}>{item.time}</span>
                      </div>
                    </div>
                  </div>
                  <h3 style={{ margin: '0 0 8px 0', fontSize: '1.2rem' }}>{item.title}</h3>
                  <p style={{ color: '#00f2fe', fontWeight: 600, fontSize: '0.9rem', marginBottom: '18px' }}>{item.stats}</p>
                  <div style={{ display: 'flex', gap: '16px', borderTop: '1px solid rgba(255,255,255,0.06)', paddingTop: '14px' }}>
                    <button
                      onClick={() => handleCheer(item.id)}
                      style={{
                        background: item.cheered ? 'rgba(204,255,0,0.2)' : 'rgba(255,255,255,0.04)',
                        border: item.cheered ? '1px solid #ccff00' : '1px solid rgba(255,255,255,0.1)',
                        color: item.cheered ? '#ccff00' : '#fff',
                        padding: '8px 16px',
                        borderRadius: '20px',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px'
                      }}
                    >
                      👏 {item.cheers} Cheers
                    </button>
                    <button style={{ background: 'transparent', border: 'none', color: 'rgba(255,255,255,0.5)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <MessageSquare size={16} /> {item.comments} Comments
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </motion.div>
        )}

        {/* Notifications View */}
        {currentView === 'notifications' && (
          <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} style={{ maxWidth: '650px' }}>
            <div className="section-header">
              <h2>Notifications</h2>
              <button onClick={markAllNotificationsRead} style={{ background: 'none', border: 'none', color: '#ccff00', cursor: 'pointer', fontSize: '0.9rem' }}>
                Mark all as read
              </button>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {notifications.map(n => (
                <div key={n.id} className="card" style={{ padding: '16px 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderLeft: n.unread ? '4px solid #ccff00' : '1px solid rgba(255,255,255,0.08)' }}>
                  <div>
                    <div style={{ color: n.unread ? '#fff' : 'rgba(255,255,255,0.6)', fontWeight: n.unread ? 600 : 400 }}>{n.text}</div>
                    <span style={{ fontSize: '0.8rem', color: 'rgba(255,255,255,0.35)' }}>{n.time}</span>
                  </div>
                </div>
              ))}
            </div>
          </motion.div>
        )}

        {/* Friends View */}
        {currentView === 'friends' && (
          <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} style={{ maxWidth: '700px' }}>
            <div className="section-header">
              <h2>Athletes & Friends ({friendsList.length})</h2>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {friendsList.map(friend => (
                <div key={friend.id} className="card" style={{ padding: '18px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                    <div className="avatar">{friend.name.charAt(0)}</div>
                    <div>
                      <h4 style={{ margin: '0 0 2px 0' }}>{friend.name} <span className="label text-volt ml-2">Lvl {friend.level}</span></h4>
                      <span style={{ fontSize: '0.85rem', color: 'rgba(255,255,255,0.4)' }}>{friend.status}</span>
                    </div>
                  </div>
                  <button
                    onClick={() => toggleFollow(friend.id)}
                    style={{
                      padding: '8px 18px',
                      borderRadius: '20px',
                      border: friend.following ? '1px solid rgba(255,255,255,0.2)' : '1px solid #ccff00',
                      background: friend.following ? 'rgba(255,255,255,0.05)' : '#ccff00',
                      color: friend.following ? 'white' : 'black',
                      fontWeight: 600,
                      cursor: 'pointer'
                    }}
                  >
                    {friend.following ? 'Following' : '+ Follow'}
                  </button>
                </div>
              ))}
            </div>
          </motion.div>
        )}

        {/* Profile View */}
        {currentView === 'profile' && (
          <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} style={{ maxWidth: '650px' }}>
            <div className="section-header">
              <h2>Athlete Profile</h2>
            </div>
            <div className="card" style={{ padding: '32px', textAlign: 'center', marginBottom: '24px' }}>
              <div className="avatar" style={{ width: '80px', height: '80px', fontSize: '2rem', margin: '0 auto 16px auto' }}>
                {username.charAt(0).toUpperCase()}
              </div>
              <h2 style={{ margin: '0 0 4px 0' }}>{username}</h2>
              <p style={{ color: '#00f2fe', margin: '0 0 16px 0' }}>Level {level} Pro Athlete · {xp} XP</p>
              <div style={{ display: 'flex', justifyContent: 'center', gap: '32px', borderTop: '1px solid rgba(255,255,255,0.1)', borderBottom: '1px solid rgba(255,255,255,0.1)', padding: '16px 0', margin: '16px 0' }}>
                <div><div style={{ fontSize: '1.4rem', fontWeight: 800 }}>{activities.length}</div><div style={{ fontSize: '0.8rem', color: 'rgba(255,255,255,0.4)' }}>Workouts</div></div>
                <div><div style={{ fontSize: '1.4rem', fontWeight: 800 }} className="text-volt">{streak}</div><div style={{ fontSize: '0.8rem', color: 'rgba(255,255,255,0.4)' }}>Streak Days</div></div>
                <div><div style={{ fontSize: '1.4rem', fontWeight: 800 }} className="text-cyan">24.5k</div><div style={{ fontSize: '0.8rem', color: 'rgba(255,255,255,0.4)' }}>Calories</div></div>
              </div>

              <div style={{ textAlign: 'left', marginTop: '24px' }}>
                <h4 style={{ marginBottom: '12px' }}>Unlocked Badges</h4>
                <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                  {['🏅 Early Bird', '🔥 Streak Master', '🚴 Century Cyclist', '🏋️ Iron Lifter'].map((b, i) => (
                    <span key={i} style={{ padding: '6px 14px', borderRadius: '16px', background: 'rgba(204,255,0,0.1)', border: '1px solid rgba(204,255,0,0.3)', color: '#ccff00', fontSize: '0.85rem' }}>
                      {b}
                    </span>
                  ))}
                </div>
              </div>
            </div>

            <div className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h4 style={{ margin: '0 0 4px 0' }}>Sign Out</h4>
                <span style={{ fontSize: '0.85rem', color: 'rgba(255,255,255,0.4)' }}>End session on this device</span>
              </div>
              <button 
                onClick={() => { localStorage.removeItem('wefit_guest_mode'); if (keycloakObj?.authenticated) { keycloakObj.logout({ redirectUri: window.location.origin }); } else { navigate('/'); } }} 
                className="btn-secondary" 
                style={{ color: '#ff4d4d', borderColor: 'rgba(255,77,77,0.4)' }}
              >
                Log Out
              </button>
            </div>
          </motion.div>
        )}
      </main>
    </div>
  );
};

export default Dashboard;
