import React from 'react';
import { 
  QrCode, 
  Plus, 
  Sun, 
  Moon, 
  FolderPlus, 
  Trash2, 
  Sparkles,
  Wallet
} from 'lucide-react';
import { formatRM } from '../utils/helpers';

export default function Navbar({
  userProfile,
  theme = 'dark',
  onToggleTheme,
  language = 'en',
  setLanguage,
  onOpenAddModal,
  onOpenCategoryManager,
  onOpenBudgetModal,
  onClearAllData,
  onLoadSampleData,
  onOpenDuitNow
}) {
  const isBM = language === 'bm';
  const isDark = theme === 'dark';

  return (
    <header className="sticky top-0 z-40 bg-white/90 dark:bg-slate-900/90 backdrop-blur-md border-b border-slate-200 dark:border-slate-800 transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Logo & Malaysian Ringgit Branding */}
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500 via-teal-500 to-rose-500 p-0.5 shadow-md shadow-emerald-500/20 flex items-center justify-center">
              <div className="w-full h-full bg-white dark:bg-slate-950 rounded-[10px] flex items-center justify-center transition-colors">
                <span className="font-black text-xl bg-gradient-to-r from-emerald-500 to-teal-400 bg-clip-text text-transparent">
                  RM
                </span>
              </div>
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="text-lg font-bold tracking-tight text-slate-900 dark:text-white">
                  Ringgit<span className="text-emerald-500">Wise</span>
                </span>
                <span className="text-[10px] uppercase font-black tracking-wider px-1.5 py-0.5 rounded bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-400 border border-emerald-300 dark:border-emerald-800/60">
                  MYR • RM
                </span>
                <span className="hidden sm:inline-flex items-center text-[10px] font-semibold px-2 py-0.5 rounded-full bg-rose-50 dark:bg-rose-950/80 text-rose-700 dark:text-rose-300 border border-rose-200 dark:border-rose-800/50">
                  <span className="w-1.5 h-1.5 rounded-full bg-rose-500 mr-1 animate-pulse"></span>
                  DuitNow OCR
                </span>
              </div>
              <p className="text-[11px] text-slate-500 dark:text-slate-400 hidden sm:block">
                {isBM ? 'Aplikasi Pengurusan Kewangan Peribadi Malaysia' : 'Malaysian Personal Finance & Expense Tracker'}
              </p>
            </div>
          </div>

          {/* Quick Monthly Budget Pill */}
          <div className="hidden md:flex items-center">
            <button 
              onClick={onOpenBudgetModal}
              className="flex items-center space-x-2 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 hover:border-emerald-500 dark:hover:border-emerald-500 px-3.5 py-1.5 rounded-full shadow-inner transition-colors group"
              title="Click to edit monthly budget"
            >
              <Wallet className="w-3.5 h-3.5 text-emerald-500" />
              <span className="text-xs text-slate-500 dark:text-slate-400">
                {isBM ? 'Bajet Bulanan:' : 'Monthly Budget:'}
              </span>
              <span className="text-xs font-bold text-slate-900 dark:text-white group-hover:text-emerald-500 transition-colors">
                {formatRM(userProfile?.monthlyBudget || 3500)}
              </span>
            </button>
          </div>

          {/* Right Action Controls */}
          <div className="flex items-center space-x-1.5 sm:space-x-2">
            
            {/* Dark Mode / Light Mode Toggle */}
            <button
              onClick={onToggleTheme}
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:text-emerald-600 dark:hover:text-emerald-400 transition-all active:scale-95"
              title={isDark ? (isBM ? 'Tukar ke Mod Cerah' : 'Switch to Light Mode') : (isBM ? 'Tukar ke Mod Gelap' : 'Switch to Dark Mode')}
              aria-label="Toggle theme"
            >
              {isDark ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-slate-700" />}
            </button>

            {/* Category Manager Modal Button */}
            <button
              onClick={onOpenCategoryManager}
              className="flex items-center space-x-1 p-2 sm:px-3 sm:py-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:text-emerald-600 dark:hover:text-emerald-400 text-xs font-semibold transition-colors"
              title={isBM ? 'Urus Kategori Perbelanjaan' : 'Manage Spending Categories'}
            >
              <FolderPlus className="w-4 h-4" />
              <span className="hidden sm:inline">{isBM ? 'Kategori' : 'Categories'}</span>
            </button>

            {/* Quick DuitNow Pay / OCR Button */}
            <button
              onClick={onOpenDuitNow}
              className="flex items-center space-x-1.5 bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-500 hover:to-rose-500 text-white text-xs font-bold px-3 py-2 rounded-xl shadow-md shadow-red-600/20 transition-all active:scale-95"
            >
              <QrCode className="w-4 h-4" />
              <span className="hidden xs:inline">DuitNow OCR</span>
            </button>

            {/* Add Transaction Button */}
            <button
              onClick={onOpenAddModal}
              className="flex items-center space-x-1 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold px-3 py-2 rounded-xl shadow-md shadow-emerald-600/20 transition-all active:scale-95"
            >
              <Plus className="w-4 h-4" />
              <span>{isBM ? 'Rekod RM' : 'Add RM'}</span>
            </button>

            {/* Language Toggle (EN / BM) */}
            <button
              onClick={() => setLanguage(language === 'en' ? 'bm' : 'en')}
              className="px-2 py-1.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 text-xs font-bold text-slate-700 dark:text-slate-300 hover:border-slate-400 transition-colors"
              title="Toggle English / Bahasa Melayu"
            >
              {language === 'en' ? '🇲🇾 BM' : '🇬🇧 EN'}
            </button>

            {/* Clear All Data */}
            <button
              onClick={onClearAllData}
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-100 dark:bg-slate-900 text-slate-400 hover:text-rose-500 transition-colors"
              title={isBM ? 'Kosongkan Semua Data' : 'Clear All Data'}
            >
              <Trash2 className="w-4 h-4" />
            </button>

            {/* Load Sample Data */}
            {onLoadSampleData && (
              <button
                onClick={onLoadSampleData}
                className="hidden xl:flex items-center space-x-1 p-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-100 dark:bg-slate-900 text-slate-500 dark:text-slate-400 hover:text-amber-500 dark:hover:text-amber-400 transition-colors text-xs"
                title="Load sample preview data matching Image 1"
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
