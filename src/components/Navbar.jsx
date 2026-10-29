import React from 'react';
import { 
  Plus, 
  Sun, 
  Moon, 
  Folder, 
  QrCode, 
  Calendar, 
  ChevronLeft, 
  ChevronRight,
  Trash2
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
  onOpenDuitNow
}) {
  const isBM = language === 'bm';
  const isDark = theme === 'dark';

  return (
    <header className="sticky top-0 z-40 bg-white/80 dark:bg-zinc-950/80 backdrop-blur-md border-b border-zinc-200/80 dark:border-zinc-800/80 transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-14">
          
          {/* Left: Minimalist Brand Identity */}
          <div className="flex items-center space-x-3">
            <div className="flex items-center space-x-2">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              <span className="text-base font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">
                RinggitWise
              </span>
            </div>
            <span className="text-[11px] font-medium px-2 py-0.5 rounded-md bg-zinc-100 dark:bg-zinc-900 text-zinc-600 dark:text-zinc-400 border border-zinc-200/60 dark:border-zinc-800/60">
              MYR (RM)
            </span>
          </div>

          {/* Middle: Month & Budget Quick Status */}
          <div className="hidden md:flex items-center space-x-4">
            <div className="flex items-center space-x-1.5 text-xs text-zinc-600 dark:text-zinc-400 bg-zinc-100/70 dark:bg-zinc-900/70 px-3 py-1.5 rounded-full border border-zinc-200/60 dark:border-zinc-800/60">
              <Calendar className="w-3.5 h-3.5 text-zinc-400" />
              <span className="font-medium">October 2026</span>
            </div>

            <button
              onClick={onOpenBudgetModal}
              className="text-xs text-zinc-500 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 transition-colors"
            >
              <span>{isBM ? 'Bajet: ' : 'Budget: '}</span>
              <span className="font-semibold text-zinc-900 dark:text-zinc-100 tabular-nums">
                {formatRM(userProfile?.monthlyBudget || 3500)}
              </span>
            </button>
          </div>

          {/* Right: Functional Action Bar */}
          <div className="flex items-center space-x-2">
            
            {/* Quick DuitNow OCR Scanner Button */}
            <button
              onClick={onOpenDuitNow}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-full text-xs font-medium border border-rose-200/80 dark:border-rose-900/50 bg-rose-50/50 dark:bg-rose-950/20 text-rose-700 dark:text-rose-400 hover:bg-rose-100/60 dark:hover:bg-rose-950/40 transition-colors active:scale-95"
            >
              <QrCode className="w-3.5 h-3.5" />
              <span>DuitNow OCR</span>
            </button>

            {/* Category Manager */}
            <button
              onClick={onOpenCategoryManager}
              className="p-1.5 sm:px-2.5 sm:py-1.5 rounded-lg text-xs font-medium text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors flex items-center space-x-1"
              title="Categories"
            >
              <Folder className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">{isBM ? 'Kategori' : 'Categories'}</span>
            </button>

            {/* Add Expense (High-contrast Primary Button) */}
            <button
              onClick={onOpenAddModal}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-full text-xs font-semibold bg-zinc-900 hover:bg-zinc-800 dark:bg-zinc-100 dark:hover:bg-white text-white dark:text-zinc-950 transition-all shadow-xs active:scale-95"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>{isBM ? 'Rekod RM' : 'Add Expense'}</span>
            </button>

            {/* Theme Toggle */}
            <button
              onClick={onToggleTheme}
              className="p-1.5 rounded-lg text-zinc-500 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors"
              title={isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
              aria-label="Toggle theme"
            >
              {isDark ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-zinc-700" />}
            </button>

            {/* Language Switcher */}
            <button
              onClick={() => setLanguage(language === 'en' ? 'bm' : 'en')}
              className="px-2 py-1 rounded-md text-[11px] font-semibold text-zinc-500 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 transition-colors"
            >
              {language === 'en' ? 'BM' : 'EN'}
            </button>

            {/* Clear All Data */}
            <button
              onClick={onClearAllData}
              className="p-1.5 rounded-lg text-zinc-400 hover:text-rose-500 transition-colors"
              title={isBM ? 'Kosongkan Data' : 'Clear Data'}
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>

          </div>

        </div>
      </div>
    </header>
  );
}
