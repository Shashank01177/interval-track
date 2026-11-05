import React, { useState, useEffect, useRef } from 'react';
import {
  BatteryCharging,
  Zap,
  Volume2,
  VolumeX,
  Vibrate,
  Bell,
  StopCircle,
  Play,
  ShieldCheck
} from 'lucide-react';

export const BatterySimulator: React.FC = () => {
  const [currentLevel, setCurrentLevel] = useState<number>(84);
  const [isCharging, setIsCharging] = useState<boolean>(true);
  const [targetPercentage, setTargetPercentage] = useState<number>(100);
  const [isAlarmEnabled, setIsAlarmEnabled] = useState<boolean>(true);
  const [soundEnabled, setSoundEnabled] = useState<boolean>(true);
  const [vibrateEnabled, setVibrateEnabled] = useState<boolean>(true);
  const [isAlarmPlaying, setIsAlarmPlaying] = useState<boolean>(false);

  const audioContextRef = useRef<AudioContext | null>(null);
  const timerRef = useRef<number | null>(null);

  // Play real web audio oscillator alarm chime
  const playChime = () => {
    try {
      const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      const ctx = new AudioCtx();
      audioContextRef.current = ctx;

      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
      osc.frequency.setValueAtTime(880, ctx.currentTime + 0.15); // A5
      osc.frequency.setValueAtTime(1046.5, ctx.currentTime + 0.3); // C6

      gain.gain.setValueAtTime(0.3, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.6);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start();
      osc.stop(ctx.currentTime + 0.6);
    } catch (e) {
      console.log('AudioContext error', e);
    }
  };

  // Trigger alarm loop when target reached
  useEffect(() => {
    if (isAlarmEnabled && isCharging && currentLevel >= targetPercentage) {
      if (!isAlarmPlaying) {
        setIsAlarmPlaying(true);
      }
    } else {
      if (currentLevel < targetPercentage && isAlarmPlaying) {
        setIsAlarmPlaying(false);
      }
    }
  }, [currentLevel, isCharging, targetPercentage, isAlarmEnabled]);

  // Handle repeating sound alarm while isAlarmPlaying is true
  useEffect(() => {
    if (isAlarmPlaying && soundEnabled) {
      playChime();
      const interval = window.setInterval(() => {
        playChime();
      }, 1200);
      return () => clearInterval(interval);
    }
  }, [isAlarmPlaying, soundEnabled]);

  const handleStopAlarm = () => {
    setIsAlarmPlaying(false);
  };

  const handleTestAlarm = () => {
    if (isAlarmPlaying) {
      handleStopAlarm();
    } else {
      setIsAlarmPlaying(true);
    }
  };

  return (
    <div className="flex-1 flex flex-col p-4 space-y-4 animate-in fade-in duration-200">
      {/* Top Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <BatteryCharging className="w-5 h-5 text-emerald-400" />
            Charge Alarm
          </h2>
          <p className="text-xs text-slate-400">
            Full charge & custom target sound notification
          </p>
        </div>

        {/* Master Toggle */}
        <button
          onClick={() => setIsAlarmEnabled(!isAlarmEnabled)}
          className={`px-3 py-1.5 rounded-full text-xs font-bold transition-all duration-200 flex items-center gap-1.5 ${
            isAlarmEnabled
              ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40 shadow-lg shadow-emerald-500/10'
              : 'bg-slate-800 text-slate-400 border border-slate-700'
          }`}
        >
          <span className={`w-2 h-2 rounded-full ${isAlarmEnabled ? 'bg-emerald-400 animate-pulse' : 'bg-slate-500'}`} />
          {isAlarmEnabled ? 'Alarm Active' : 'Disabled'}
        </button>
      </div>

      {/* Alarm Triggered Banner */}
      {isAlarmPlaying && (
        <div className="bg-gradient-to-r from-rose-950/80 to-red-900/60 border border-rose-500 rounded-2xl p-4 shadow-xl shadow-rose-950/50 flex flex-col items-center text-center animate-bounce">
          <div className="text-rose-400 font-extrabold text-sm uppercase tracking-wider mb-1 flex items-center gap-1.5">
            <Bell className="w-4 h-4 animate-spin" />
            Target Reached: {currentLevel}%!
          </div>
          <p className="text-xs text-rose-200 mb-3">
            Your phone has reached {targetPercentage}%. Unplug your charger to protect battery health.
          </p>
          <button
            onClick={handleStopAlarm}
            className="flex items-center gap-2 px-5 py-2 rounded-xl bg-white text-slate-950 font-bold text-xs hover:bg-rose-100 shadow-md transition-transform active:scale-95"
          >
            <StopCircle className="w-4 h-4 text-rose-600" />
            DISMISS ALARM
          </button>
        </div>
      )}

      {/* Hero Circular Battery Ring */}
      <div className="relative flex flex-col items-center justify-center py-6 bg-[#111827] border border-slate-800/80 rounded-3xl">
        <div className="relative w-44 h-44 flex items-center justify-center">
          {/* SVG Progress Circle */}
          <svg className="w-full h-full transform -rotate-90">
            <circle
              cx="88"
              cy="88"
              r="76"
              stroke="#0F172A"
              strokeWidth="12"
              fill="transparent"
            />
            <circle
              cx="88"
              cy="88"
              r="76"
              stroke={currentLevel >= targetPercentage ? '#10B981' : currentLevel >= 80 ? '#06B6D4' : '#6366F1'}
              strokeWidth="12"
              strokeDasharray={477.5}
              strokeDashoffset={477.5 - (477.5 * currentLevel) / 100}
              strokeLinecap="round"
              fill="transparent"
              className="transition-all duration-300 ease-out"
            />
          </svg>

          {/* Center Info */}
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            {isCharging && (
              <div className="flex items-center gap-1 text-[11px] font-bold text-emerald-400 mb-0.5">
                <Zap className="w-3.5 h-3.5 animate-pulse text-emerald-400" />
                CHARGING
              </div>
            )}
            <div className="text-4xl font-extrabold text-white tracking-tight">
              {currentLevel}%
            </div>
            <div className="text-xs text-slate-400 mt-0.5">
              Target: <span className="text-indigo-400 font-bold">{targetPercentage}%</span>
            </div>
          </div>
        </div>

        {/* Battery Simulation Control (Slider to test charging) */}
        <div className="w-full px-6 mt-4">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span>Simulate Level:</span>
            <span className="font-mono text-white font-bold">{currentLevel}%</span>
          </div>
          <input
            type="range"
            min="50"
            max="100"
            value={currentLevel}
            onChange={e => setCurrentLevel(Number(e.target.value))}
            className="w-full accent-emerald-500 h-1.5 bg-slate-800 rounded-lg cursor-pointer"
          />
        </div>
      </div>

      {/* Target Percentage Card */}
      <div className="bg-[#111827] border border-slate-800/80 rounded-2xl p-4">
        <div className="flex items-center justify-between mb-3">
          <span className="text-xs font-bold text-slate-300">
            Set Alert Threshold
          </span>
          <span className="text-sm font-extrabold text-indigo-400">
            {targetPercentage}%
          </span>
        </div>

        {/* Quick Preset Buttons */}
        <div className="grid grid-cols-4 gap-2 mb-3">
          {[80, 85, 90, 100].map(pct => {
            const isSelected = targetPercentage === pct;
            return (
              <button
                key={pct}
                onClick={() => setTargetPercentage(pct)}
                className={`py-2 px-1 rounded-xl text-center border transition-all ${
                  isSelected
                    ? 'bg-indigo-600 border-indigo-500 text-white shadow-lg shadow-indigo-600/30'
                    : 'bg-[#090D16] border-slate-800 text-slate-300 hover:border-slate-700'
                }`}
              >
                <div className="text-xs font-bold">{pct}%</div>
                <div className={`text-[9px] ${isSelected ? 'text-indigo-200' : 'text-slate-500'}`}>
                  {pct === 100 ? 'Full' : 'Protect'}
                </div>
              </button>
            );
          })}
        </div>

        <div className="flex items-center gap-1.5 text-[11px] text-slate-400">
          <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
          <span>Setting to 80% or 85% significantly prolongs smartphone battery lifespan.</span>
        </div>
      </div>

      {/* Alert Preferences (Sound & Vibration) */}
      <div className="bg-[#111827] border border-slate-800/80 rounded-2xl p-4 space-y-3">
        <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">
          Alert Style
        </div>

        {/* Sound toggle */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            {soundEnabled ? (
              <Volume2 className="w-4 h-4 text-indigo-400" />
            ) : (
              <VolumeX className="w-4 h-4 text-slate-500" />
            )}
            <div>
              <div className="text-xs font-semibold text-white">Sound Alarm</div>
              <div className="text-[11px] text-slate-400">Loud audible ringtone</div>
            </div>
          </div>
          <button
            onClick={() => setSoundEnabled(!soundEnabled)}
            className={`w-10 h-5 rounded-full transition-colors relative ${
              soundEnabled ? 'bg-indigo-600' : 'bg-slate-800'
            }`}
          >
            <span
              className={`absolute top-0.5 w-4 h-4 rounded-full bg-white transition-transform ${
                soundEnabled ? 'left-5' : 'left-1'
              }`}
            />
          </button>
        </div>

        {/* Vibration toggle */}
        <div className="flex items-center justify-between border-t border-slate-800/60 pt-3">
          <div className="flex items-center gap-2.5">
            <Vibrate className="w-4 h-4 text-cyan-400" />
            <div>
              <div className="text-xs font-semibold text-white">Vibration Alert</div>
              <div className="text-[11px] text-slate-400">Repeating pulse pattern</div>
            </div>
          </div>
          <button
            onClick={() => setVibrateEnabled(!vibrateEnabled)}
            className={`w-10 h-5 rounded-full transition-colors relative ${
              vibrateEnabled ? 'bg-cyan-600' : 'bg-slate-800'
            }`}
          >
            <span
              className={`absolute top-0.5 w-4 h-4 rounded-full bg-white transition-transform ${
                vibrateEnabled ? 'left-5' : 'left-1'
              }`}
            />
          </button>
        </div>

        {/* Test Alarm Button */}
        <div className="pt-2">
          <button
            onClick={handleTestAlarm}
            className={`w-full py-2.5 px-4 rounded-xl text-xs font-bold flex items-center justify-center gap-2 transition-all ${
              isAlarmPlaying
                ? 'bg-rose-600 hover:bg-rose-700 text-white shadow-lg shadow-rose-600/30'
                : 'bg-[#090D16] hover:bg-slate-800 border border-slate-800 text-indigo-400'
            }`}
          >
            {isAlarmPlaying ? (
              <>
                <StopCircle className="w-4 h-4" />
                Stop Test Sound
              </>
            ) : (
              <>
                <Play className="w-4 h-4" />
                Test Alarm Sound Now
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
