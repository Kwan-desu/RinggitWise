import React from 'react';
import { 
  QrCode, 
  Wallet, 
  Smartphone, 
  Monitor, 
  RotateCcw, 
  Plus, 
  Download, 
  Flame, 
  ShieldCheck,
  CheckCircle2,
  Trash2,
  Sparkles
} from 'lucide-react';
import { formatRM } from '../utils/helpers';

export default function Navbar({
  userProfile,
  safeToSpendDaily,
  viewMode,
  setViewMode,
  language,
  setLanguage,
  onOpenAddModal,
  onOpenSetIncome,
  onClearAllData,
  onLoadDemoData,
  onOpenDuitNow
}) {
  const isBM = language === 'bm';
  const hasIncome = userProfile && userProfile.monthlyIncome > 0;

  return (
    <header className="sticky top-0 z-40 bg-slate-900/95 backdrop-blur-md border-b border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Logo & Malaysian Fintech Badge */}
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500 via-teal-500 to-red-500 p-0.5 shadow-lg shadow-emerald-500/20 flex items-center justify-center">
              <div className="w-full h-full bg-slate-950 rounded-[10px] flex items-center justify-center">
                <span className="font-black text-xl bg-gradient-to-r from-emerald-400 to-teal-200 bg-clip-text text-transparent">
                  RW
                </span>
              </div>
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="text-lg font-bold tracking-tight text-white">
                  Ringgit<span className="text-emerald-400">Wise</span>
                </span>
                <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800/60">
                  MYR • RM
                </span>
                <span className="hidden sm:inline-flex items-center text-[10px] font-semibold px-2 py-0.5 rounded-full bg-red-950/80 text-red-300 border border-red-700/50">
                  <span className="w-1.5 h-1.5 rounded-full bg-red-500 mr-1 animate-pulse"></span>
                  DuitNow QR
                </span>
              </div>
              <p className="text-[11px] text-slate-400 hidden sm:block">
                {isBM ? 'Aplikasi Bajet 3-Balang Malaysia • Caleb Hammer Style' : 'Malaysian 3-Bucket Budgeting • Caleb Hammer Style'}
              </p>
            </div>
          </div>

          {/* Quick Safe To Spend Pill or Set Income CTA */}
          <div className="hidden md:flex items-center">
            {hasIncome ? (
              <button 
                onClick={onOpenSetIncome}
                className="flex items-center bg-slate-950 border border-slate-800 hover:border-slate-700 px-3.5 py-1.5 rounded-full shadow-inner transition-colors group"
                title="Click to edit monthly income"
              >
                <span className="text-xs text-slate-400 mr-2 flex items-center">
                  <span className="w-2 h-2 rounded-full bg-emerald-400 mr-1.5 animate-ping"></span>
                  {isBM ? 'Selamat Belanja Hari Ini:' : 'Safe to Spend Today:'}
                </span>
                <span className="text-sm font-bold text-emerald-400 group-hover:underline">
                  {formatRM(safeToSpendDaily)}
                </span>
              </button>
            ) : (
              <button
                onClick={onOpenSetIncome}
                className="flex items-center space-x-1.5 bg-emerald-950/60 border border-emerald-800/60 hover:bg-emerald-900/60 text-emerald-300 px-3 py-1.5 rounded-full text-xs font-semibold transition-all"
              >
                <Wallet className="w-3.5 h-3.5 text-emerald-400" />
                <span>{isBM ? 'Tetapkan Pendapatan Bulanan' : 'Set Monthly Income'}</span>
              </button>
            )}
          </div>

          {/* Controls: View Mode, Language, Actions */}
          <div className="flex items-center space-x-2 sm:space-x-2.5">
            
            {/* View Mode Switcher (Desktop PC vs Mobile Phone Frame vs Auto) */}
            <div className="hidden lg:flex items-center bg-slate-950 p-1 rounded-lg border border-slate-800 text-xs">
              <button
                onClick={() => setViewMode('auto')}
                className={`px-2.5 py-1 rounded font-medium transition-all ${
                  viewMode === 'auto'
                    ? 'bg-slate-800 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
                title="Responsive View"
              >
                Auto
              </button>
              <button
                onClick={() => setViewMode('pc')}
                className={`flex items-center px-2.5 py-1 rounded font-medium transition-all ${
                  viewMode === 'pc'
                    ? 'bg-emerald-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
                title="PC Dashboard View"
              >
                <Monitor className="w-3.5 h-3.5 mr-1" />
                PC UI
              </button>
              <button
                onClick={() => setViewMode('mobile')}
                className={`flex items-center px-2.5 py-1 rounded font-medium transition-all ${
                  viewMode === 'mobile'
                    ? 'bg-emerald-600 text-white shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
                title="Mobile Simulator View"
              >
                <Smartphone className="w-3.5 h-3.5 mr-1" />
                Mobile UI
              </button>
            </div>

            {/* Quick DuitNow Pay Button */}
            <button
              onClick={onOpenDuitNow}
              className="flex items-center space-x-1.5 bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-500 hover:to-rose-500 text-white text-xs font-semibold px-3 py-2 rounded-lg shadow-md shadow-red-600/20 transition-all active:scale-95"
            >
              <QrCode className="w-4 h-4" />
              <span className="hidden xs:inline">DuitNow QR</span>
            </button>

            {/* Add Transaction Button */}
            <button
              onClick={onOpenAddModal}
              className="flex items-center space-x-1 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold px-3 py-2 rounded-lg shadow-md shadow-emerald-600/20 transition-all active:scale-95"
            >
              <Plus className="w-4 h-4" />
              <span className="hidden sm:inline">{isBM ? 'Rekod RM' : 'Add RM'}</span>
            </button>

            {/* Language Toggle (EN / BM) */}
            <button
              onClick={() => setLanguage(language === 'en' ? 'bm' : 'en')}
              className="px-2 py-1.5 rounded-lg border border-slate-700 bg-slate-800 text-xs font-bold text-slate-300 hover:text-white hover:border-slate-600 transition-colors"
              title="Toggle English / Bahasa Melayu"
            >
              {language === 'en' ? '🇲🇾 BM' : '🇬🇧 EN'}
            </button>

            {/* Clear All Data */}
            <button
              onClick={onClearAllData}
              className="p-2 rounded-lg border border-slate-800 bg-slate-900 text-slate-400 hover:text-rose-400 hover:border-rose-900/50 transition-colors"
              title={isBM ? 'Kosongkan Semua Data' : 'Clear All Data (Start Fresh)'}
            >
              <Trash2 className="w-4 h-4" />
            </button>

            {/* Load Demo Data option */}
            {onLoadDemoData && (
              <button
                onClick={onLoadDemoData}
                className="hidden xl:flex items-center space-x-1 p-2 rounded-lg border border-slate-800 bg-slate-900 text-slate-400 hover:text-amber-400 hover:border-amber-900/50 transition-colors text-xs"
                title="Load sample preview data"
              >
                <Sparkles className="w-3.5 h-3.5" />
                <span className="text-[10px]">Contoh</span>
              </button>
            )}
          </div>

        </div>
      </div>
    </header>
  );
}
