import React, { useRef, useEffect, useState, useCallback } from 'react';
import * as THREE from 'three';
import { useKeycloak } from '@react-keycloak/web';
import { useNavigate } from 'react-router-dom';
import {
  Activity, Users, Target, BrainCircuit, ShieldAlert, ArrowRight,
  Heart, Flame, Trophy, ChevronRight, Send, Bike, Timer, Zap,
  Dumbbell, MessageCircle, Sparkles, TrendingUp, Shield
} from 'lucide-react';
import { motion, useScroll, useTransform, AnimatePresence, useInView } from 'framer-motion';
import './Landing.css';

/* ═══════════════════════════════════════════════
   RAW THREE.JS 3D BACKGROUND
   ═══════════════════════════════════════════════ */

function ThreeBackground() {
  const canvasRef = useRef(null);
  const animRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    // ─── Renderer ───
    const renderer = new THREE.WebGLRenderer({ canvas, alpha: true, antialias: true });
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 1.5));

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 100);
    camera.position.set(0, 0, 8);

    // ─── Lights ───
    scene.add(new THREE.AmbientLight(0xffffff, 0.15));
    const voltLight = new THREE.PointLight(0xccff00, 0.8, 50);
    voltLight.position.set(10, 10, 10);
    scene.add(voltLight);
    const cyanLight = new THREE.PointLight(0x00f2fe, 0.5, 50);
    cyanLight.position.set(-10, -5, 5);
    scene.add(cyanLight);
    const violetLight = new THREE.PointLight(0xbd00ff, 0.5, 50);
    violetLight.position.set(0, 5, -10);
    scene.add(violetLight);

    // ─── Stars (2000 particles) ───
    const starCount = 2000;
    const starPositions = new Float32Array(starCount * 3);
    const starColors = new Float32Array(starCount * 3);
    for (let i = 0; i < starCount; i++) {
      starPositions[i * 3] = (Math.random() - 0.5) * 60;
      starPositions[i * 3 + 1] = (Math.random() - 0.5) * 60;
      starPositions[i * 3 + 2] = (Math.random() - 0.5) * 60;
      const r = Math.random();
      if (r < 0.4) { starColors[i*3]=0.8; starColors[i*3+1]=1; starColors[i*3+2]=0; }
      else if (r < 0.7) { starColors[i*3]=0; starColors[i*3+1]=0.95; starColors[i*3+2]=1; }
      else { starColors[i*3]=0.74; starColors[i*3+1]=0; starColors[i*3+2]=1; }
    }
    const starGeo = new THREE.BufferGeometry();
    starGeo.setAttribute('position', new THREE.BufferAttribute(starPositions, 3));
    starGeo.setAttribute('color', new THREE.BufferAttribute(starColors, 3));
    const starMat = new THREE.PointsMaterial({ size: 0.05, vertexColors: true, transparent: true, opacity: 0.7, sizeAttenuation: true });
    const stars = new THREE.Points(starGeo, starMat);
    scene.add(stars);

    // ─── Floating Rings ───
    const createRing = (pos, color, size) => {
      const geo = new THREE.TorusGeometry(size, 0.04, 16, 100);
      const mat = new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 0.6, transparent: true, opacity: 0.4 });
      const mesh = new THREE.Mesh(geo, mat);
      mesh.position.set(...pos);
      scene.add(mesh);
      return mesh;
    };
    const ring1 = createRing([-5, 2, -8], 0xccff00, 2.5);
    const ring2 = createRing([5, -1, -6], 0x00f2fe, 1.8);
    const ring3 = createRing([0, 3, -10], 0xbd00ff, 3);

    // ─── Glowing Spheres ───
    const createSphere = (pos, color, size) => {
      const geo = new THREE.SphereGeometry(size, 32, 32);
      const mat = new THREE.MeshStandardMaterial({ color, emissive: color, emissiveIntensity: 1.5, transparent: true, opacity: 0.35 });
      const mesh = new THREE.Mesh(geo, mat);
      mesh.position.set(...pos);
      scene.add(mesh);
      return mesh;
    };
    const sphere1 = createSphere([-3, -2, -4], 0xccff00, 0.4);
    const sphere2 = createSphere([4, 1, -5], 0x00f2fe, 0.3);
    const sphere3 = createSphere([0, -3, -7], 0xbd00ff, 0.35);

    // ─── DNA Helix ───
    const helixGroup = new THREE.Group();
    helixGroup.position.set(6, 0, -5);
    for (let i = 0; i < 80; i++) {
      const t = (i / 80) * Math.PI * 6;
      const y = (i / 80) * 12 - 6;
      const s1Geo = new THREE.SphereGeometry(0.06, 8, 8);
      const s1Mat = new THREE.MeshStandardMaterial({ color: 0xccff00, emissive: 0xccff00, emissiveIntensity: 0.8 });
      const s1 = new THREE.Mesh(s1Geo, s1Mat);
      s1.position.set(Math.cos(t) * 1.5, y, Math.sin(t) * 1.5);
      helixGroup.add(s1);
      const s2Geo = new THREE.SphereGeometry(0.06, 8, 8);
      const s2Mat = new THREE.MeshStandardMaterial({ color: 0x00f2fe, emissive: 0x00f2fe, emissiveIntensity: 0.8 });
      const s2 = new THREE.Mesh(s2Geo, s2Mat);
      s2.position.set(Math.cos(t + Math.PI) * 1.5, y, Math.sin(t + Math.PI) * 1.5);
      helixGroup.add(s2);
    }
    scene.add(helixGroup);

    // ─── Nebula background particles ───
    const nebulaCount = 500;
    const nebPositions = new Float32Array(nebulaCount * 3);
    for (let i = 0; i < nebulaCount; i++) {
      nebPositions[i*3] = (Math.random()-0.5) * 80;
      nebPositions[i*3+1] = (Math.random()-0.5) * 80;
      nebPositions[i*3+2] = -20 - Math.random() * 40;
    }
    const nebGeo = new THREE.BufferGeometry();
    nebGeo.setAttribute('position', new THREE.BufferAttribute(nebPositions, 3));
    const nebMat = new THREE.PointsMaterial({ size: 0.15, color: 0xccff00, transparent: true, opacity: 0.1, sizeAttenuation: true });
    const nebula = new THREE.Points(nebGeo, nebMat);
    scene.add(nebula);

    // ─── Mouse tracking ───
    let mouseX = 0, mouseY = 0;
    const onMouseMove = (e) => {
      mouseX = (e.clientX / window.innerWidth - 0.5) * 2;
      mouseY = (e.clientY / window.innerHeight - 0.5) * 2;
    };
    window.addEventListener('mousemove', onMouseMove);

    // ─── Scroll tracking ───
    let scrollY = 0;
    const onScroll = () => { scrollY = window.scrollY; };
    window.addEventListener('scroll', onScroll);

    // ─── Resize ───
    const onResize = () => {
      camera.aspect = window.innerWidth / window.innerHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(window.innerWidth, window.innerHeight);
    };
    window.addEventListener('resize', onResize);

    // ─── Animation loop ───
    const clock = new THREE.Clock();
    const animate = () => {
      animRef.current = requestAnimationFrame(animate);
      const t = clock.getElapsedTime();

      // Camera follows mouse subtly
      camera.position.x += (mouseX * 0.5 - camera.position.x) * 0.02;
      camera.position.y += (-mouseY * 0.5 - camera.position.y) * 0.02;
      camera.lookAt(0, 0, 0);

      // Stars rotate
      stars.rotation.y = t * 0.02;
      stars.rotation.x = Math.sin(t * 0.01) * 0.1;

      // Rings rotate & bob
      ring1.rotation.x = t * 0.24;
      ring1.rotation.z = t * 0.16;
      ring1.position.y = 2 + Math.sin(t * 0.4) * 0.5;

      ring2.rotation.x = t * 0.18;
      ring2.rotation.z = t * 0.12;
      ring2.position.y = -1 + Math.sin(t * 0.3) * 0.5;

      ring3.rotation.x = t * 0.36;
      ring3.rotation.z = t * 0.24;
      ring3.position.y = 3 + Math.sin(t * 0.6) * 0.5;

      // Spheres bob & pulse
      sphere1.position.y = -2 + Math.sin(t * 0.8) * 0.8;
      sphere1.scale.setScalar(1 + Math.sin(t * 1.2) * 0.15);
      sphere2.position.y = 1 + Math.sin(t * 0.7) * 0.6;
      sphere2.scale.setScalar(1 + Math.sin(t * 1.0) * 0.12);
      sphere3.position.y = -3 + Math.sin(t * 0.9) * 0.7;
      sphere3.scale.setScalar(1 + Math.sin(t * 1.1) * 0.13);

      // Helix rotates
      helixGroup.rotation.y = t * 0.15;

      // Parallax on scroll
      const scrollOffset = scrollY * 0.001;
      stars.position.y = -scrollOffset * 3;
      helixGroup.position.y = -scrollOffset * 5;

      renderer.render(scene, camera);
    };
    animate();

    return () => {
      cancelAnimationFrame(animRef.current);
      window.removeEventListener('mousemove', onMouseMove);
      window.removeEventListener('scroll', onScroll);
      window.removeEventListener('resize', onResize);
      renderer.dispose();
    };
  }, []);

  return <canvas ref={canvasRef} className="three-canvas" />;
}

/* ═══════════════════════════════════════════════
   ANIMATED COUNTER
   ═══════════════════════════════════════════════ */

function AnimatedCounter({ target, duration = 2000, suffix = '' }) {
  const [count, setCount] = useState(0);
  const ref = useRef(null);
  const inView = useInView(ref, { once: true });

  useEffect(() => {
    if (!inView) return;
    let start = 0;
    const step = Math.ceil(target / (duration / 16));
    const timer = setInterval(() => {
      start += step;
      if (start >= target) { setCount(target); clearInterval(timer); }
      else setCount(start);
    }, 16);
    return () => clearInterval(timer);
  }, [inView, target, duration]);

  return <span ref={ref}>{count.toLocaleString()}{suffix}</span>;
}

/* ═══════════════════════════════════════════════
   ANIMATION VARIANTS
   ═══════════════════════════════════════════════ */

const sectionVariants = {
  hidden: { opacity: 0, y: 60 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.8, ease: [0.25, 0.46, 0.45, 0.94] } },
};

const staggerContainer = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.12, delayChildren: 0.1 } },
};

const fadeUpItem = {
  hidden: { opacity: 0, y: 30 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.6 } },
};

/* ═══════════════════════════════════════════════
   SOCIAL TICKER
   ═══════════════════════════════════════════════ */

function SocialTicker() {
  const items = [
    { text: 'Priya finished a 10K', icon: '🏃‍♀️' },
    { text: 'Dev hit a squat PR: 100 kg', icon: '🏋️' },
    { text: 'Maya joined 100 km cycling', icon: '🚴' },
    { text: 'Karan completed a 20 min HIIT', icon: '⚡' },
    { text: 'Alex is on a 14 day streak', icon: '🔥' },
    { text: 'Sam cheered Maya\'s run', icon: '👏' },
  ];

  return (
    <div className="ticker-container">
      <div className="ticker-track">
        {[...items, ...items].map((item, i) => (
          <div key={i} className="ticker-item glass">
            <span className="ticker-icon">{item.icon}</span>
            <span>{item.text}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

/* ═══════════════════════════════════════════════
   QUIZ SECTION
   ═══════════════════════════════════════════════ */

function QuizSection({ onLogin }) {
  const [step, setStep] = useState(0);
  const [answers, setAnswers] = useState({});
  const questions = [
    { q: 'What\'s your primary fitness goal?', opts: ['Lose weight', 'Build muscle', 'Improve endurance', 'Stay active'] },
    { q: 'How often do you work out?', opts: ['Just starting', '2-3x/week', '4-5x/week', 'Daily'] },
    { q: 'What\'s your preferred activity?', opts: ['Running', 'Weight lifting', 'Cycling', 'Mix of everything'] },
  ];

  const handleAnswer = (answer) => {
    setAnswers({ ...answers, [step]: answer });
    if (step < 2) setStep(step + 1);
    else setStep(3);
  };

  return (
    <motion.section id="quiz" className="quiz-section" variants={sectionVariants} initial="hidden" whileInView="visible" viewport={{ once: true }}>
      <div className="quiz-wrapper">
        <motion.div className="quiz-text-side" variants={fadeUpItem}>
          <h2 className="section-title">Find your <span className="text-volt">7-day plan</span></h2>
          <p className="section-subtitle">Three quick questions. No sign-up needed.</p>
          {step === 3 && (
            <motion.div initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }} className="quiz-result card">
              <Sparkles size={28} color="#ccff00" />
              <h3>Your Plan is Ready!</h3>
              <p>Based on your answers, we've crafted a personalized 7-day plan focused on {answers[0]?.toLowerCase()} with {answers[2]?.toLowerCase()}.</p>
              <button className="btn-primary" onClick={onLogin}>Start my plan, free <ArrowRight size={16} style={{ marginLeft: 8 }} /></button>
            </motion.div>
          )}
        </motion.div>
        <motion.div className="quiz-card-side" variants={fadeUpItem}>
          <AnimatePresence mode="wait">
            {step < 3 && (
              <motion.div
                key={step}
                initial={{ opacity: 0, x: 50, rotateY: -10 }}
                animate={{ opacity: 1, x: 0, rotateY: 0 }}
                exit={{ opacity: 0, x: -50, rotateY: 10 }}
                transition={{ duration: 0.4 }}
                className="quiz-card card"
              >
                <div className="quiz-progress">
                  {[0,1,2].map(i => (
                    <div key={i} className={`quiz-dot ${i <= step ? 'active' : ''}`} />
                  ))}
                </div>
                <h3 className="quiz-question">{questions[step].q}</h3>
                <div className="quiz-options">
                  {questions[step].opts.map((opt, i) => (
                    <motion.button
                      key={i}
                      whileHover={{ scale: 1.03, x: 8 }}
                      whileTap={{ scale: 0.97 }}
                      className="quiz-option"
                      onClick={() => handleAnswer(opt)}
                    >
                      {opt}
                      <ChevronRight size={16} />
                    </motion.button>
                  ))}
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </motion.div>
      </div>
    </motion.section>
  );
}

/* ═══════════════════════════════════════════════
   APP PREVIEW WITH PHONE MOCKUP
   ═══════════════════════════════════════════════ */

function AppPreview() {
  return (
    <motion.section id="app" className="app-section" variants={sectionVariants} initial="hidden" whileInView="visible" viewport={{ once: true }}>
      <motion.h2 className="section-title" variants={fadeUpItem}>
        Your day, <span className="text-volt">at a glance</span>
      </motion.h2>
      <motion.p className="section-subtitle" variants={fadeUpItem}>
        Weekly goal ring, activity chart, and recovery score live on one home screen.
      </motion.p>
      <motion.div className="app-preview-container" variants={staggerContainer} initial="hidden" whileInView="visible" viewport={{ once: true }}>
        <motion.div className="phone-mockup" variants={fadeUpItem}>
          <div className="phone-frame">
            <div className="phone-notch"></div>
            <div className="phone-screen">
              <div className="app-header-bar">
                <span className="app-day">Saturday</span>
                <span className="app-greeting">Evening, <strong className="text-volt">Alex</strong></span>
              </div>
              <div className="goal-ring-container">
                <svg viewBox="0 0 120 120" className="goal-ring">
                  <circle cx="60" cy="60" r="52" fill="none" stroke="rgba(255,255,255,0.06)" strokeWidth="8" />
                  <circle cx="60" cy="60" r="52" fill="none" stroke="#ccff00" strokeWidth="8"
                    strokeDasharray="326.7" strokeDashoffset="91.5" strokeLinecap="round"
                    style={{ filter: 'drop-shadow(0 0 8px rgba(204,255,0,0.5))' }} />
                  <text x="60" y="56" textAnchor="middle" fill="white" fontSize="22" fontWeight="800" fontFamily="Outfit">72%</text>
                  <text x="60" y="72" textAnchor="middle" fill="rgba(255,255,255,0.5)" fontSize="9" fontFamily="Manrope">WEEKLY GOAL</text>
                </svg>
              </div>
              <div className="app-stats-row">
                <div className="app-stat">
                  <ShieldAlert size={16} color="#ffb4ab" />
                  <div>
                    <div className="app-stat-label">Burnout risk</div>
                    <div className="app-stat-value">6.4</div>
                  </div>
                </div>
                <div className="app-stat">
                  <Flame size={16} color="#00f2fe" />
                  <div>
                    <div className="app-stat-label">Streak</div>
                    <div className="app-stat-value text-cyan">6 days</div>
                  </div>
                </div>
                <div className="app-stat">
                  <Bike size={16} color="#bd00ff" />
                  <div>
                    <div className="app-stat-label">Cycling</div>
                    <div className="app-stat-value text-violet">64%</div>
                  </div>
                </div>
              </div>
              <div className="app-chart">
                {[40,60,35,80,55,70,45].map((h, i) => (
                  <div key={i} className="chart-bar-wrap">
                    <motion.div
                      className="chart-bar"
                      initial={{ height: 0 }}
                      whileInView={{ height: `${h}%` }}
                      viewport={{ once: true }}
                      transition={{ delay: i * 0.1, duration: 0.6 }}
                      style={{ background: i === 3 ? '#ccff00' : 'rgba(204,255,0,0.25)' }}
                    />
                    <span className="chart-label">{['M','T','W','T','F','S','S'][i]}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </motion.div>
      </motion.div>
    </motion.section>
  );
}

/* ═══════════════════════════════════════════════
   CHALLENGES / LEADERBOARD
   ═══════════════════════════════════════════════ */

function ChallengesSection() {
  const leaderboard = [
    { rank: 1, name: 'Priya N.', time: '412 min', highlight: false },
    { rank: 2, name: 'Karan M.', time: '388 min', highlight: false },
    { rank: 3, name: 'Alex K. (you)', time: '371 min', highlight: true },
    { rank: 4, name: 'Maya R.', time: '352 min', highlight: false },
  ];

  return (
    <motion.section id="compete" className="challenges-section" variants={sectionVariants} initial="hidden" whileInView="visible" viewport={{ once: true }}>
      <motion.h2 className="section-title" variants={fadeUpItem}>
        Race your friends <span className="text-cyan">in real time</span>
      </motion.h2>
      <motion.p className="section-subtitle" variants={fadeUpItem}>
        Pick a challenge, watch the leaderboard move.
      </motion.p>
      <motion.div className="challenge-container" variants={staggerContainer} initial="hidden" whileInView="visible" viewport={{ once: true }}>
        <motion.div className="challenge-card card" variants={fadeUpItem}>
          <div className="challenge-header">
            <div className="challenge-icon-wrap">
              <Bike size={28} color="#00f2fe" />
            </div>
            <div>
              <h3>100 km cycling in October</h3>
              <span className="challenge-meta">9 days left · 128 riders</span>
            </div>
          </div>
          <div className="challenge-progress-bar">
            <motion.div
              className="challenge-progress-fill"
              initial={{ width: 0 }}
              whileInView={{ width: '64%' }}
              viewport={{ once: true }}
              transition={{ duration: 1.2, ease: 'easeOut' }}
            />
          </div>
          <div className="challenge-progress-text">You: <span className="text-volt">64</span> of 100 km</div>
        </motion.div>
        <motion.div className="leaderboard-card card" variants={fadeUpItem}>
          <div className="leaderboard-header">
            <Trophy size={20} color="#ccff00" />
            <span>Live Leaderboard</span>
          </div>
          {leaderboard.map((entry, i) => (
            <motion.div
              key={i}
              className={`leaderboard-row ${entry.highlight ? 'highlight' : ''}`}
              initial={{ opacity: 0, x: 20 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.1 }}
              whileHover={{ x: 4 }}
            >
              <div className="lb-rank">{entry.rank}</div>
              <div className="lb-name">{entry.name}</div>
              <div className="lb-time text-volt">{entry.time}</div>
            </motion.div>
          ))}
        </motion.div>
      </motion.div>
    </motion.section>
  );
}

/* ═══════════════════════════════════════════════
   AI COACH CHAT
   ═══════════════════════════════════════════════ */

function AICoachSection() {
  const [messages, setMessages] = useState([
    { role: 'ai', text: "You've trained hard three days running. How are your legs?" },
    { role: 'user', text: "Pretty heavy honestly." },
    { role: 'ai', text: "Then keep tomorrow easy: 30 minutes zone 2, then mobility." },
  ]);
  const [input, setInput] = useState('');
  const [typing, setTyping] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => { scrollToBottom(); }, [messages, typing]);

  const handleSend = () => {
    if (!input.trim()) return;
    const userMsg = input.trim();
    setMessages(prev => [...prev, { role: 'user', text: userMsg }]);
    setInput('');
    setTyping(true);
    setTimeout(() => {
      let response = "Great question! Based on your training data, I'd recommend adjusting your recovery and nutrition plan. Let me create a personalized schedule for you.";
      if (userMsg.toLowerCase().includes('meal') || userMsg.toLowerCase().includes('food') || userMsg.toLowerCase().includes('eat')) {
        response = "Based on your 6.4 burnout score and today's heavy legs, I'd add 20g extra protein tonight — grilled chicken with quinoa. Tomorrow morning, oatmeal with berries and a scoop of whey.";
      } else if (userMsg.toLowerCase().includes('rest') || userMsg.toLowerCase().includes('sleep')) {
        response = "Your recovery is key right now. Aim for 8+ hours tonight. Try a 10-minute guided meditation before bed — it's shown to improve sleep quality by 23% for athletes.";
      } else if (userMsg.toLowerCase().includes('run') || userMsg.toLowerCase().includes('jog')) {
        response = "Given your current fatigue level, I'd skip the run tomorrow. A brisk 30-minute walk instead will keep your aerobic base without stressing those heavy legs.";
      }
      setMessages(prev => [...prev, { role: 'ai', text: response }]);
      setTyping(false);
    }, 1500);
  };

  return (
    <motion.section id="coach" className="coach-section" variants={sectionVariants} initial="hidden" whileInView="visible" viewport={{ once: true }}>
      <motion.h2 className="section-title" variants={fadeUpItem}>
        A coach that <span className="text-violet">remembers you</span>
      </motion.h2>
      <motion.p className="section-subtitle" variants={fadeUpItem}>
        Ask anything. Get answers based on your sleep, load, and recent sessions.
      </motion.p>
      <motion.div className="coach-container" variants={fadeUpItem}>
        <div className="chat-window card">
          <div className="chat-header glass">
            <BrainCircuit size={22} color="#ccff00" />
            <span>Wefit AI Coach</span>
            <div className="online-dot" />
          </div>
          <div className="chat-messages">
            {messages.map((msg, i) => (
              <motion.div
                key={i}
                className={`chat-bubble ${msg.role}`}
                initial={{ opacity: 0, y: 10, scale: 0.95 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                transition={{ delay: i < 3 ? i * 0.15 : 0 }}
              >
                {msg.role === 'ai' && <BrainCircuit size={14} className="bubble-icon" />}
                {msg.text}
              </motion.div>
            ))}
            {typing && (
              <motion.div className="chat-bubble ai typing" initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
                <BrainCircuit size={14} className="bubble-icon" />
                <div className="typing-dots">
                  <span /><span /><span />
                </div>
              </motion.div>
            )}
            <div ref={messagesEndRef} />
          </div>
          <div className="chat-input-area">
            <input
              type="text"
              placeholder="Can you adjust my meals too?"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSend()}
            />
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
              className="send-btn"
              onClick={handleSend}
            >
              <Send size={18} />
            </motion.button>
          </div>
        </div>
      </motion.div>
    </motion.section>
  );
}

/* ═══════════════════════════════════════════════
   MAIN LANDING COMPONENT
   ═══════════════════════════════════════════════ */

const Landing = () => {
  let keycloakObj = null;
  let navigate = null;

  try {
    const kc = useKeycloak();
    keycloakObj = kc?.keycloak;
  } catch (e) {
    // Keycloak not available — page still renders
  }

  try {
    navigate = useNavigate();
  } catch (e) {
    // Router not available
  }

  const handleLogin = useCallback(() => {
    if (keycloakObj?.authenticated) {
      navigate?.('/dashboard');
    } else if (keycloakObj) {
      keycloakObj.login();
    } else {
      document.getElementById('join')?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [keycloakObj, navigate]);

  const { scrollYProgress } = useScroll();
  const navBg = useTransform(scrollYProgress, [0, 0.05], ['rgba(10,12,16,0)', 'rgba(10,12,16,0.92)']);

  const features = [
    { icon: Activity, title: 'Activity tracking', desc: 'Log runs, rides and lifts with duration, calories, and distance. Browse your full workout timeline.', color: 'var(--color-primary-container)' },
    { icon: Users, title: 'Social feed', desc: 'Follow friends, share workouts and photos, and cheer each other on.', color: 'var(--color-secondary-container)' },
    { icon: Target, title: 'Live challenges', desc: 'Join public challenges and climb real-time leaderboards.', color: 'var(--color-tertiary-container)' },
    { icon: BrainCircuit, title: 'AI coach', desc: 'Advice grounded in your own history, plus training and nutrition plans.', color: 'var(--color-primary-container)' },
    { icon: ShieldAlert, title: 'Burnout watch', desc: 'A 0-10 risk score warns you before overtraining turns into injury.', color: 'var(--color-error)' },
    { icon: Shield, title: 'Safe community', desc: 'Report tools and admin moderation keep the feed friendly.', color: 'var(--color-secondary-container)' },
  ];

  return (
    <div className="landing-page">
      {/* ─── 3D CANVAS BACKGROUND ─── */}
      <ThreeBackground />

      {/* ─── NAVBAR ─── */}
      <motion.nav
        initial={{ y: -80, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.8, ease: [0.25, 0.46, 0.45, 0.94] }}
        className="navbar"
        style={{ backgroundColor: navBg }}
      >
        <div className="nav-brand text-volt">WEFIT</div>
        <div className="nav-links">
          <a href="#features">Features</a>
          <a href="#app">App</a>
          <a href="#compete">Challenges</a>
          <a href="#quiz">Your plan</a>
          <a href="#coach">AI Coach</a>
        </div>
        <div className="nav-actions">
          <button className="btn-secondary" onClick={handleLogin}>Log In</button>
          <button className="btn-primary" onClick={handleLogin}>Join Free</button>
        </div>
        <button className="mobile-menu-btn" onClick={() => document.querySelector('.nav-links')?.classList.toggle('open')}>
          <span /><span /><span />
        </button>
      </motion.nav>

      <main>
        {/* ─── HERO ─── */}
        <section className="hero">
          <motion.div
            initial={{ opacity: 0, x: -60 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 1, delay: 0.3, ease: [0.25, 0.46, 0.45, 0.94] }}
            className="hero-content"
          >
            <motion.div
              initial={{ opacity: 0, scale: 0.5 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className="hero-badge glass"
            >
              <Zap size={14} color="#ccff00" />
              <span>AI-Powered Fitness Platform</span>
            </motion.div>
            <motion.h1
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.8, delay: 0.5 }}
              className="hero-title"
            >
              Every workout<br /><span className="text-volt">deserves cheers.</span>
            </motion.h1>
            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.8, delay: 0.7 }}
              className="hero-subtitle"
            >
              Log it in seconds. Friends cheer, challenges heat up, and your AI coach keeps you going. Tap the demo and see how it feels.
            </motion.p>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.9 }}
              className="hero-ctas"
            >
              <motion.button
                whileHover={{ scale: 1.05, boxShadow: '0 0 40px rgba(204,255,0,0.5)' }}
                whileTap={{ scale: 0.95 }}
                className="btn-primary btn-lg"
                onClick={handleLogin}
              >
                Start for free <ArrowRight size={20} style={{ marginLeft: 8 }} />
              </motion.button>
              <motion.button
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="btn-secondary btn-lg"
                onClick={() => document.getElementById('app')?.scrollIntoView({ behavior: 'smooth' })}
              >
                See the app
              </motion.button>
            </motion.div>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 1.2 }}
              className="hero-stats"
            >
              <div className="hero-stat">
                <span className="hero-stat-number text-volt"><AnimatedCounter target={12480} /></span>
                <span className="hero-stat-label">workouts logged</span>
              </div>
              <div className="hero-stat-divider" />
              <div className="hero-stat">
                <span className="hero-stat-number text-cyan"><AnimatedCounter target={342} /></span>
                <span className="hero-stat-label">active challenges</span>
              </div>
              <div className="hero-stat-divider" />
              <div className="hero-stat">
                <span className="hero-stat-number text-violet"><AnimatedCounter target={89} suffix="%" /></span>
                <span className="hero-stat-label">hit weekly goals</span>
              </div>
            </motion.div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, scale: 0.8, rotateY: -15 }}
            animate={{ opacity: 1, scale: 1, rotateY: 0 }}
            transition={{ duration: 1.2, delay: 0.5 }}
            className="hero-graphic"
          >
            <motion.div
              animate={{ y: [0, -20, 0] }}
              transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut' }}
              className="floating-card glass"
            >
              <div className="card-header label text-volt">Today's Pulse</div>
              <div className="metrics-list">
                <div className="metric-row"><Dumbbell size={16} color="#ccff00" /><span><span className="text-volt">3</span> workouts logged</span></div>
                <div className="metric-row"><Target size={16} color="#00f2fe" /><span><span className="text-cyan">2</span> active challenges</span></div>
                <div className="metric-row"><TrendingUp size={16} color="#bd00ff" /><span><span className="text-violet">72%</span> hit weekly goals</span></div>
                <div className="metric-row"><Flame size={16} color="#ccff00" /><span>Day <span className="text-volt">6</span> streak</span></div>
                <div className="metric-row"><Zap size={16} color="#00f2fe" /><span><span className="text-cyan">180</span> of 600 kcal</span></div>
              </div>
              <motion.button
                whileHover={{ scale: 1.03 }}
                className="btn-primary full-width mt-4"
                onClick={handleLogin}
              >
                Save your streak <ArrowRight size={14} style={{ marginLeft: 4 }} />
              </motion.button>
            </motion.div>

            <motion.div
              animate={{ y: [0, 18, 0] }}
              transition={{ duration: 7, repeat: Infinity, ease: 'easeInOut' }}
              className="floating-feed glass"
            >
              <div className="feed-header label text-cyan">Live Activity</div>
              {[
                { text: 'Priya finished a 10K', icon: '🏃‍♀️' },
                { text: 'Dev hit a squat PR: 100 kg', icon: '🏋️', cls: 'text-volt' },
                { text: 'Maya joined 100 km cycling', icon: '🚴', cls: 'text-cyan' },
                { text: 'Karan completed 20 min HIIT', icon: '⚡' },
                { text: 'Alex is on a 14 day streak', icon: '🔥', cls: 'text-violet' },
                { text: "Sam cheered Maya's run", icon: '👏' },
              ].map((item, i) => (
                <motion.div
                  key={i}
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 1 + i * 0.15 }}
                  className={`feed-item ${item.cls || ''}`}
                >
                  <span className="feed-icon">{item.icon}</span>
                  {item.text}
                </motion.div>
              ))}
            </motion.div>
          </motion.div>
        </section>

        {/* ─── SOCIAL TICKER ─── */}
        <SocialTicker />

        {/* ─── FEATURES ─── */}
        <section id="features" className="features">
          <motion.h2
            variants={fadeUpItem}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            className="section-title"
          >
            Everything your <span className="text-volt">training needs</span>
          </motion.h2>
          <motion.p
            variants={fadeUpItem}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            className="section-subtitle"
          >
            Track, connect, compete, improve.
          </motion.p>
          <motion.div
            className="features-grid"
            variants={staggerContainer}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
          >
            {features.map((f, i) => (
              <motion.div
                key={i}
                variants={fadeUpItem}
                whileHover={{ scale: 1.04, y: -8, transition: { duration: 0.3 } }}
                className="feature-card card"
              >
                <div className="feature-icon-wrap" style={{ '--glow-color': f.color }}>
                  <f.icon size={28} color={f.color} />
                </div>
                <h3>{f.title}</h3>
                <p>{f.desc}</p>
              </motion.div>
            ))}
          </motion.div>
        </section>

        {/* ─── QUIZ ─── */}
        <QuizSection onLogin={handleLogin} />

        {/* ─── APP PREVIEW ─── */}
        <AppPreview />

        {/* ─── CHALLENGES ─── */}
        <ChallengesSection />

        {/* ─── AI COACH ─── */}
        <AICoachSection />

        {/* ─── CTA ─── */}
        <motion.section
          id="join"
          className="cta-section"
          variants={sectionVariants}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true }}
        >
          <div className="cta-glow" />
          <motion.h2 variants={fadeUpItem}>
            Your next personal best <span className="text-volt">starts today</span>
          </motion.h2>
          <motion.p variants={fadeUpItem}>
            Free to join. Bring a friend and make it a challenge.
          </motion.p>
          <motion.button
            variants={fadeUpItem}
            whileHover={{ scale: 1.05, boxShadow: '0 0 50px rgba(204,255,0,0.5)' }}
            whileTap={{ scale: 0.95 }}
            className="btn-primary btn-lg"
            onClick={handleLogin}
          >
            Create your account <ArrowRight size={20} style={{ marginLeft: 8 }} />
          </motion.button>
        </motion.section>
      </main>

      {/* ─── FOOTER ─── */}
      <footer className="footer">
        <div className="footer-content">
          <div className="nav-brand text-volt">WEFIT</div>
          <div className="footer-links">
            <a href="#features">Features</a>
            <a href="#app">App</a>
            <a href="#compete">Challenges</a>
            <a href="#coach">AI Coach</a>
          </div>
          <div className="label">© 2026 Wefit. Movement, made exceptional.</div>
        </div>
      </footer>
    </div>
  );
};

export default Landing;
