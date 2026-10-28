import React from 'react';
import { Wifi, Battery, Signal, Smartphone } from 'lucide-react';

export default function MobileFrame({ children, onExitMobileView, language }) {
  const isBM = language === 'bm';

  return (
    <div className="py-6 px-4 min-h-screen bg-slate-950 flex flex-col items-center justify-center">
      
      {/* Top Helper Bar for PC user */}
      <div className="mb-4 flex items-center justify-between w-full max-w-[420px] px-2 text-xs text-slate-400">
        <span className="flex items-center font-semibold text-slate-300">
          <Smartphone className="w-4 h-4 mr-1.5 text-emerald-400" />
          {isBM ? 'Mod Simulasi Mudah Alih (Mobile UI)' : 'Mobile Phone Simulator Mode'}
        </span>
        <button
          onClick={onExitMobileView}
          className="text-emerald-400 hover:text-emerald-300 font-bold hover:underline"
        >
          {isBM ? 'Kembali ke PC UI' : 'Back to PC UI'}
        </button>
      </div>

      {/* Realistic Phone Bezel */}
      <div className="w-full max-w-[410px] h-[860px] bg-black rounded-[48px] p-3 shadow-2xl border-4 border-slate-700 relative overflow-hidden flex flex-col shadow-emerald-950/20">
        
        {/* Dynamic Island / Notch */}
        <div className="absolute top-4 inset-x-0 flex justify-center z-50 pointer-events-none">
          <div className="w-28 h-6 bg-black rounded-full flex items-center justify-between px-3 border border-slate-800">
            <div className="w-2.5 h-2.5 rounded-full bg-slate-900 border border-slate-700"></div>
            <div className="w-2.5 h-2.5 rounded-full bg-slate-900"></div>
          </div>
        </div>

        {/* Status Bar */}
        <div className="h-9 px-6 pt-1 flex items-center justify-between text-[11px] text-slate-300 font-medium z-40 bg-slate-900 select-none border-b border-slate-800/40">
          <span>20:58</span>
          <div className="flex items-center space-x-1.5">
            <Signal className="w-3 h-3" />
            <span className="text-[10px] font-bold">5G</span>
            <Battery className="w-3.5 h-3.5" />
          </div>
        </div>

        {/* Screen Content Container with scrolling */}
        <div className="flex-1 overflow-y-auto bg-slate-950 text-slate-100 relative pb-20 custom-scroll">
          {children}
        </div>

        {/* Home Indicator Bar */}
        <div className="h-4 bg-slate-950 flex items-center justify-center pointer-events-none z-50">
          <div className="w-32 h-1 bg-slate-600 rounded-full"></div>
        </div>

      </div>
    </div>
  );
}
