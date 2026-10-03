import React, { useState } from 'react';
import Sidebar from '../components/Sidebar';
import { Bell, Search, Activity, Target, Flame, BrainCircuit, ShieldAlert, Users, Send } from 'lucide-react';
import { useKeycloak } from '@react-keycloak/web';
import { motion, AnimatePresence } from 'framer-motion';
import { useQuery, useMutation } from '@tanstack/react-query';
import { getUserProfile, getUserLevel, getLeaderboard, getActivities, chatWithAi } from '../api/services';
import './Dashboard.css';

const Dashboard = () => {
  const { keycloak } = useKeycloak();
  const userId = keycloak.tokenParsed?.sub;

  const [aiMessage, setAiMessage] = useState('');
  const [chatHistory, setChatHistory] = useState([]);
  const [isChatOpen, setIsChatOpen] = useState(false);

  // Queries
  const { data: userProfile } = useQuery({
    queryKey: ['userProfile', userId],
    queryFn: () => getUserProfile(userId),
    enabled: !!userId,
  });

  const { data: gamification } = useQuery({
    queryKey: ['gamification', userId],
    queryFn: getUserLevel,
    enabled: !!userId,
  });

  const { data: leaderboard } = useQuery({
    queryKey: ['leaderboard'],
    queryFn: getLeaderboard,
  });

  const { data: activities } = useQuery({
    queryKey: ['activities', userId],
    queryFn: getActivities,
    enabled: !!userId,
  });

  // AI Chat Mutation
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
    e.preventDefault();
    if (!aiMessage.trim()) return;
    
    setChatHistory(prev => [...prev, { sender: 'user', text: aiMessage }]);
    aiMutation.mutate(aiMessage);
    setAiMessage('');
  };

  const username = userProfile?.firstName || keycloak.tokenParsed?.preferred_username || 'Athlete';
  const level = gamification?.level || 1;
  const xp = gamification?.xp || 0;
  const streak = gamification?.currentStreak || 0;

  return (
    <div className="dashboard-layout">
      <Sidebar />
      <main className="dashboard-content">
        <header className="dashboard-header">
          <div>
            <h1 className="greeting">Welcome back, <br/><span className="text-volt">{username}</span> (Level {level})</h1>
          </div>
          <div className="header-actions">
            <button className="icon-btn"><Search size={20} /></button>
            <button className="icon-btn notification-btn">
              <Bell size={20} />
            </button>
          </div>
        </header>

        <motion.section 
          initial="hidden"
          animate="visible"
          variants={{
            hidden: { opacity: 0 },
            visible: { opacity: 1, transition: { staggerChildren: 0.1 } }
          }}
          className="stats-grid"
        >
          <motion.div variants={{ hidden: { y: 20, opacity: 0 }, visible: { y: 0, opacity: 1 } }} whileHover={{ y: -5 }} className="stat-card card">
            <div className="stat-header">
              <span className="label">Total XP</span>
              <Target size={20} color="var(--color-primary-container)" />
            </div>
            <div className="stat-value text-volt">{xp}</div>
            <div className="stat-footer">Level {level}</div>
          </motion.div>
          <motion.div variants={{ hidden: { y: 20, opacity: 0 }, visible: { y: 0, opacity: 1 } }} whileHover={{ y: -5 }} className="stat-card card">
            <div className="stat-header">
              <span className="label">Activity Count</span>
              <Activity size={20} color="var(--color-error)" />
            </div>
            <div className="stat-value">{activities?.length || 0}</div>
            <div className="stat-footer">Total workouts</div>
          </motion.div>
          <motion.div variants={{ hidden: { y: 20, opacity: 0 }, visible: { y: 0, opacity: 1 } }} whileHover={{ y: -5 }} className="stat-card card">
            <div className="stat-header">
              <span className="label">Active Streak</span>
              <Flame size={20} color="var(--color-secondary-container)" />
            </div>
            <div className="stat-value text-cyan">{streak} Days</div>
            <div className="stat-footer">Keep it up!</div>
          </motion.div>
        </motion.section>

        <div className="dashboard-body">
          <motion.div 
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className="recent-activities"
          >
            <div className="section-header">
              <h3>Global Leaderboard <span className="label text-cyan ml-2">Top 10 XP</span></h3>
            </div>
            <div className="activity-list">
              {leaderboard?.map((user, index) => (
                <motion.div key={user.userId} whileHover={{ scale: 1.02 }} className={`activity-item card ${user.userId === userId ? 'highlight-card' : ''}`}>
                  <div className="activity-rank text-cyan">{index + 1}</div>
                  <div className="activity-details">
                    <h4 className={user.userId === userId ? 'text-volt' : ''}>{user.userId === userId ? 'You' : `Athlete ${user.userId.substring(0, 5)}`}</h4>
                  </div>
                  <div className="text-volt stat-value-sm">Lvl {user.level} ({user.xp} XP)</div>
                </motion.div>
              ))}
              {(!leaderboard || leaderboard.length === 0) && (
                <div className="empty-state">No users on the leaderboard yet.</div>
              )}
            </div>
          </motion.div>

          <motion.div 
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, delay: 0.6 }}
            className="ai-preview"
          >
            <div className="section-header">
              <h3>AI Coach</h3>
            </div>
            <div className="insight-card card" style={{ display: 'flex', flexDirection: 'column', height: '100%', maxHeight: '400px' }}>
              <div className="insight-header" style={{ marginBottom: '16px' }}>
                <BrainCircuit size={24} color="var(--color-primary-container)" />
                <h4 className="text-volt">Chat with Coach</h4>
              </div>
              
              <div className="chat-window" style={{ flexGrow: 1, overflowY: 'auto', marginBottom: '16px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {chatHistory.length === 0 ? (
                  <p className="insight-summary" style={{ textAlign: 'center', color: 'rgba(255,255,255,0.5)', marginTop: '20px' }}>
                    Ask me anything about your training, diet, or recovery!
                  </p>
                ) : (
                  chatHistory.map((msg, i) => (
                    <div key={i} style={{ 
                      alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                      backgroundColor: msg.sender === 'user' ? 'var(--color-primary-container)' : 'var(--color-surface-elevated)',
                      color: msg.sender === 'user' ? '#000' : '#fff',
                      padding: '8px 12px',
                      borderRadius: '8px',
                      maxWidth: '80%',
                      fontSize: '0.9rem'
                    }}>
                      {msg.text}
                    </div>
                  ))
                )}
                {aiMutation.isPending && (
                  <div style={{ alignSelf: 'flex-start', color: 'rgba(255,255,255,0.5)', fontSize: '0.8rem' }}>Coach is typing...</div>
                )}
              </div>

              <form onSubmit={handleAiSubmit} style={{ display: 'flex', gap: '8px' }}>
                <input 
                  type="text" 
                  value={aiMessage}
                  onChange={(e) => setAiMessage(e.target.value)}
                  placeholder="Ask a question..."
                  style={{ 
                    flexGrow: 1, 
                    padding: '10px', 
                    borderRadius: '8px', 
                    border: '1px solid rgba(255,255,255,0.1)',
                    background: 'rgba(255,255,255,0.05)',
                    color: 'white'
                  }}
                  disabled={aiMutation.isPending}
                />
                <button type="submit" className="icon-btn" disabled={aiMutation.isPending} style={{ background: 'var(--color-primary-container)', color: '#000' }}>
                  <Send size={18} />
                </button>
              </form>
            </div>
          </motion.div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
