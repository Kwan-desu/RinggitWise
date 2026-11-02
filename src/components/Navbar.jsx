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
  Trash2,
  History
} from 'lucide-react';
import { formatRM, formatMonthYear } from '../utils/helpers';

export default function Navbar({
  userProfile,
  theme = 'dark',
  onToggleTheme,
  language = 'en',
  setLanguage,
  selectedMonth = '2026-10',
  onPrevMonth,
  onNextMonth,
  onSelectMonth,
  onOpenPastSpending,
  availableMonths = [],
  yearGroups = [],
  onOpenAddModal,
  onOpenCategoryManager,
  onOpenBudgetModal,
  onClearAllData,
  onOpenDuitNow
}) {
  const isBM = language === 'bm';
  const isDark = theme === 'dark';

  return (
    <header className="sticky top-0 z-40 transition-colors backdrop-blur-md" style={{
      backgroundColor: 'color-mix(in srgb, var(--md-sys-color-surface) 92%, transparent)',
      borderBottom: '1px solid var(--md-sys-color-outline-variant)'
    }}>
      <div className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Left: Material You Brand Identity */}
          <div className="flex items-center space-x-2 sm:space-x-3">
            <div className="flex items-center space-x-2.5">
              <div className="w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shadow-xs" style={{
                backgroundColor: 'var(--md-sys-color-primary-container)',
                color: 'var(--md-sys-color-on-primary-container)'
              }}>
                RM
              </div>
              <div>
                <span className="text-base font-bold tracking-tight block leading-tight" style={{ color: 'var(--md-sys-color-on-surface)' }}>
                  RinggitWise
                </span>
                <span className="text-[10px] font-medium hidden sm:block" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
                  Personal Finance • Malaysia
                </span>
              </div>
            </div>
          </div>

          {/* Middle: M3 Chip Month Navigator */}
          <div className="flex items-center space-x-1 sm:space-x-2">
            
            {/* Month & Year Stepper Chip */}
            <div className="flex items-center space-x-0.5 text-xs px-2 py-1.5 rounded-full transition-all" style={{
              backgroundColor: 'var(--md-sys-color-surface-container-high)',
              border: '1px solid var(--md-sys-color-outline-variant)'
            }}>
              <button
                type="button"
                onClick={onPrevMonth}
                className="p-1 rounded-full transition-colors hover:opacity-80 active:scale-95"
                style={{ color: 'var(--md-sys-color-on-surface-variant)' }}
                title={isBM ? "Bulan / Tahun Sebelumnya" : "Previous Month / Year"}
                aria-label="Previous Month"
              >
                <ChevronLeft className="w-3.5 h-3.5" />
              </button>

              <div className="flex items-center space-x-1 px-1">
                <Calendar className="w-3.5 h-3.5 shrink-0" style={{ color: 'var(--md-sys-color-primary)' }} />
                <select
                  value={selectedMonth}
                  onChange={(e) => onSelectMonth && onSelectMonth(e.target.value)}
                  className="bg-transparent border-none text-xs font-semibold cursor-pointer focus:outline-none max-w-[120px] sm:max-w-[180px] truncate"
                  style={{ color: 'var(--md-sys-color-on-surface)' }}
                >
                  <option value="all" className="font-bold" style={{ backgroundColor: 'var(--md-sys-color-surface-container)' }}>
                    {isBM ? '🕒 Semua Masa (All Time)' : '🕒 All Time'}
                  </option>
                  {yearGroups.map(({ year, months }) => (
                    <optgroup key={year} label={isBM ? `── Tahun ${year} ──` : `── Year ${year} ──`} className="font-bold">
                      <option value={String(year)} className="font-semibold" style={{ backgroundColor: 'var(--md-sys-color-surface-container)' }}>
                        {isBM ? `Sepanjang Tahun ${year}` : `Full Year ${year}`}
                      </option>
                      {months.map((mStr) => (
                        <option key={mStr} value={mStr} style={{ backgroundColor: 'var(--md-sys-color-surface-container)' }}>
                          {formatMonthYear(mStr, isBM)}
                        </option>
                      ))}
                    </optgroup>
                  ))}
                </select>
              </div>

              <button
                type="button"
                onClick={onNextMonth}
                className="p-1 rounded-full transition-colors hover:opacity-80 active:scale-95"
                style={{ color: 'var(--md-sys-color-on-surface-variant)' }}
                title={isBM ? "Bulan / Tahun Seterusnya" : "Next Month / Year"}
                aria-label="Next Month"
              >
                <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>

            {/* Past Spending Overview Button (M3 Assist Chip) */}
            <button
              type="button"
              onClick={onOpenPastSpending}
              className="hidden lg:flex items-center space-x-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition-all hover:opacity-90 active:scale-95"
              style={{
                backgroundColor: 'var(--md-sys-color-surface-container)',
                color: 'var(--md-sys-color-on-surface-variant)',
                border: '1px solid var(--md-sys-color-outline-variant)'
              }}
              title={isBM ? "Lihat Sejarah Perbelanjaan Mengikut Bulan" : "View Historical Spending by Month"}
            >
              <History className="w-3.5 h-3.5" style={{ color: 'var(--md-sys-color-primary)' }} />
              <span>{isBM ? 'Rekod Lepas' : 'History'}</span>
            </button>

            {/* Budget status quick link */}
            <button
              onClick={onOpenBudgetModal}
              className="hidden sm:inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-medium transition-colors"
              style={{
                backgroundColor: 'var(--md-sys-color-surface-container-low)',
                color: 'var(--md-sys-color-on-surface)'
              }}
            >
              <span style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>{isBM ? 'Bajet: ' : 'Budget: '}</span>
              <span className="font-bold tabular-nums" style={{ color: 'var(--md-sys-color-primary)' }}>
                {formatRM(userProfile?.monthlyBudget || 3500)}
              </span>
            </button>
          </div>

          {/* Right: Functional Action Bar (Material 3 Buttons) */}
          <div className="flex items-center space-x-1.5 sm:space-x-2">
            
            {/* DuitNow OCR Scanner Button */}
            <button
              onClick={onOpenDuitNow}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-full text-xs font-semibold transition-all active:scale-95 shadow-2xs"
              style={{
                backgroundColor: 'var(--md-sys-color-secondary-container)',
                color: 'var(--md-sys-color-on-secondary-container)'
              }}
            >
              <QrCode className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">DuitNow OCR</span>
              <span className="sm:hidden">OCR</span>
            </button>

            {/* Category Manager */}
            <button
              onClick={onOpenCategoryManager}
              className="p-2 sm:px-3 sm:py-1.5 rounded-full text-xs font-medium transition-colors flex items-center space-x-1"
              style={{
                backgroundColor: 'var(--md-sys-color-surface-container)',
                color: 'var(--md-sys-color-on-surface)'
              }}
              title="Categories"
            >
              <Folder className="w-3.5 h-3.5" />
              <span className="hidden md:inline">{isBM ? 'Kategori' : 'Categories'}</span>
            </button>

            {/* Add Expense (Primary Filled Button) */}
            <button
              onClick={onOpenAddModal}
              className="hidden sm:flex items-center space-x-1.5 px-3.5 py-1.5 rounded-full text-xs font-semibold transition-all shadow-xs active:scale-95"
              style={{
                backgroundColor: 'var(--md-sys-color-primary)',
                color: 'var(--md-sys-color-on-primary)'
              }}
            >
              <Plus className="w-3.5 h-3.5" />
              <span>{isBM ? 'Rekod RM' : 'Add Expense'}</span>
            </button>

            {/* Theme Toggle (Icon Button) */}
            <button
              onClick={onToggleTheme}
              className="p-2 rounded-full transition-colors hover:opacity-80 active:scale-95"
              style={{
                backgroundColor: 'var(--md-sys-color-surface-container)',
                color: 'var(--md-sys-color-on-surface)'
              }}
              title={isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
              aria-label="Toggle theme"
            >
              {isDark ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-indigo-500" />}
            </button>

            {/* Language Switcher */}
            <button
              onClick={() => setLanguage(language === 'en' ? 'bm' : 'en')}
              className="px-2.5 py-1 rounded-full text-[11px] font-bold transition-colors"
              style={{
                backgroundColor: 'var(--md-sys-color-surface-container-high)',
                color: 'var(--md-sys-color-on-surface)'
              }}
            >
              {language === 'en' ? 'BM' : 'EN'}
            </button>

            {/* Clear All Data */}
            <button
              onClick={onClearAllData}
              className="p-2 rounded-full transition-colors hover:opacity-80 active:scale-95 text-rose-500"
              style={{ backgroundColor: 'var(--md-sys-color-surface-container)' }}
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
