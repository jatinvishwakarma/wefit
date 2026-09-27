import React from 'react';
import Sidebar from '../components/Sidebar';
import { Bell, Search, TrendingUp, Flame, BrainCircuit, Activity, ChevronRight } from 'lucide-react';
import './Dashboard.css';

const Dashboard = () => {
  return (
    <div className="dashboard-layout">
      <Sidebar />
      <main className="dashboard-content">
        <header className="dashboard-header">
          <div>
            <h1 className="greeting">Good Evening, Jatin</h1>
            <span className="date label">September 27, 2026</span>
          </div>
          <div className="header-actions">
            <button className="icon-btn"><Search size={20} /></button>
            <button className="icon-btn notification-btn">
              <Bell size={20} />
              <span className="badge">3</span>
            </button>
          </div>
        </header>

        <section className="stats-grid">
          <div className="stat-card card">
            <div className="stat-header">
              <span className="label">This Week</span>
              <TrendingUp size={20} color="var(--color-success)" />
            </div>
            <div className="stat-value">12</div>
            <div className="stat-footer text-gold">Workouts</div>
          </div>
          <div className="stat-card card">
            <div className="stat-header">
              <span className="label">Calories Burned</span>
              <Activity size={20} color="var(--color-primary)" />
            </div>
            <div className="stat-value">4,820</div>
            <div className="stat-footer">kcal</div>
          </div>
          <div className="stat-card card">
            <div className="stat-header">
              <span className="label">Active Streak</span>
              <Flame size={20} color="#F97316" />
            </div>
            <div className="stat-value">15 Days</div>
            <div className="stat-footer text-gold">Keep it up!</div>
          </div>
          <div className="stat-card card insight-stat">
            <div className="stat-header">
              <span className="label">AI Insights</span>
              <BrainCircuit size={20} color="var(--color-primary)" />
            </div>
            <div className="stat-value text-gold">3 New</div>
            <div className="stat-footer">Ready for review</div>
          </div>
        </section>

        <div className="dashboard-body">
          <div className="recent-activities">
            <div className="section-header">
              <h3>Recent Activities</h3>
              <button className="btn-secondary btn-sm">View All</button>
            </div>
            <div className="activity-list">
              {[1, 2, 3].map((_, i) => (
                <div key={i} className="activity-item card">
                  <div className="activity-icon">
                    <Activity size={24} color="var(--color-primary)" />
                  </div>
                  <div className="activity-details">
                    <h4>Morning Run</h4>
                    <span className="label">Today, 6:00 AM • 45 min • 420 kcal</span>
                  </div>
                  <button className="text-btn text-gold">
                    View AI Insight <ChevronRight size={16} style={{verticalAlign:'middle'}}/>
                  </button>
                </div>
              ))}
            </div>
          </div>

          <div className="ai-preview">
            <div className="section-header">
              <h3>Latest Insight</h3>
            </div>
            <div className="insight-card card">
              <div className="insight-header">
                <BrainCircuit size={24} color="var(--color-primary)" />
                <h4>Running Analysis</h4>
              </div>
              <p className="insight-summary">
                Excellent pacing! The AI suggests increasing your cadence slightly to reduce impact. 
                Your Vo2 Max shows a 2% improvement from last week.
              </p>
              <button className="btn-primary full-width">View Full Analysis</button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
