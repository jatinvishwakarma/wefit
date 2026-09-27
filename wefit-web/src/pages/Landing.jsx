import React from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { useNavigate } from 'react-router-dom';
import { BrainCircuit, Activity, Users, ArrowRight } from 'lucide-react';
import './Landing.css';

const Landing = () => {
  const { keycloak } = useKeycloak();
  const navigate = useNavigate();

  const handleLogin = () => {
    if (keycloak.authenticated) {
      navigate('/dashboard');
    } else {
      keycloak.login();
    }
  };

  return (
    <div className="landing-page">
      <nav className="navbar glass">
        <div className="nav-brand text-gold">Wefit</div>
        <div className="nav-links">
          <a href="#features">Features</a>
          <a href="#how-it-works">How it Works</a>
          <a href="#community">Community</a>
        </div>
        <div className="nav-actions">
          <button className="btn-secondary" onClick={handleLogin}>Member Access</button>
          <button className="btn-primary" onClick={handleLogin}>Join Club</button>
        </div>
      </nav>

      <main>
        <section className="hero">
          <div className="hero-content">
            <h1 className="hero-title">Your Elite Fitness <br/><span className="text-gold">Journey Starts Here</span></h1>
            <p className="hero-subtitle">
              AI-powered training. Social accountability. Built for those who demand excellence.
            </p>
            <div className="hero-ctas">
              <button className="btn-primary" onClick={handleLogin}>Start Your Journey <ArrowRight size={18} style={{marginLeft: '8px', verticalAlign: 'middle'}} /></button>
              <button className="btn-secondary" onClick={() => document.getElementById('features').scrollIntoView()}>Explore Features</button>
            </div>
          </div>
          <div className="hero-graphic">
            <div className="floating-card glass">
              <div className="card-header label">Platinum Syndicate</div>
              <div className="card-metric">VO₂ Max: Elite 99th Percentile</div>
              <div className="card-footer text-gold">AI Adaptive Protocol Active</div>
            </div>
          </div>
        </section>

        <section id="features" className="features">
          <h4 className="section-eyebrow text-gold label">THE PINNACLE OF PERFORMANCE</h4>
          <div className="features-grid">
            <div className="feature-card card">
              <BrainCircuit className="feature-icon" size={32} color="var(--color-primary)" />
              <h3>AI-Powered Insights</h3>
              <p>Real-time biometric adaptation and predictive recovery protocols tailored to your physiology.</p>
            </div>
            <div className="feature-card card">
              <Activity className="feature-icon" size={32} color="var(--color-primary)" />
              <h3>Track Every Move</h3>
              <p>High-precision metric tracking, wearable sync, and velocity tracking for the ultimate edge.</p>
            </div>
            <div className="feature-card card">
              <Users className="feature-icon" size={32} color="var(--color-primary)" />
              <h3>Social Fitness</h3>
              <p>Private leaderboards, verified executive circles, and curated workout duels.</p>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
};

export default Landing;
