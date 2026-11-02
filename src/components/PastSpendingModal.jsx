import React from 'react';
import { 
  X, 
  Calendar, 
  ArrowUpRight, 
  ChevronRight, 
  TrendingUp, 
  Layers, 
  CheckCircle2,
  Receipt
} from 'lucide-react';
import { formatRM, formatMonthYear, groupTransactionsByMonth } from '../utils/helpers';

export default function PastSpendingModal({
  isOpen,
  onClose,
  transactions = [],
  categories = [],
  monthlyBudget = 3500,
  selectedMonth,
  onSelectMonth,
  language = 'en'
}) {
  if (!isOpen) return null;
  const isBM = language === 'bm';

  // Strictly filter for real expenses only
  const expensesOnly = transactions.filter(t => 
    Number(t.amount) > 0 && 
    t.type !== 'income' && 
    t.bucket !== 'income' && 
    t.category?.toLowerCase() !== 'income'
  );

  // Group by month
  const monthlyGroups = groupTransactionsByMonth(expensesOnly);
  const totalLifetimeSpent = expensesOnly.reduce((acc, t) => acc + Number(t.amount || 0), 0);
  const avgMonthlySpend = monthlyGroups.length > 0 ? (totalLifetimeSpent / monthlyGroups.length) : 0;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div 
        className="rounded-[28px] w-full max-w-2xl max-h-[88vh] flex flex-col shadow-2xl overflow-hidden transition-all"
        style={{
          backgroundColor: 'var(--md-sys-color-surface-container-high)',
          color: 'var(--md-sys-color-on-surface)'
        }}
      >
        
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b" style={{ borderColor: 'var(--md-sys-color-outline-variant)' }}>
          <div className="flex items-center space-x-3">
            <div className="p-2.5 rounded-full" style={{
              backgroundColor: 'var(--md-sys-color-primary-container)',
              color: 'var(--md-sys-color-on-primary-container)'
            }}>
              <Calendar className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold" style={{ color: 'var(--md-sys-color-on-surface)' }}>
                {isBM ? 'Sejarah Perbelanjaan Mengikut Bulan' : 'Past Spending by Month'}
              </h3>
              <p className="text-xs" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
                {isBM ? 'Lihat dan bandingkan rekod perbelanjaan bulan-bulan lepas' : 'Review and compare historical monthly spending'}
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:opacity-80 transition-colors"
            style={{
              backgroundColor: 'var(--md-sys-color-surface-container)',
              color: 'var(--md-sys-color-on-surface-variant)'
            }}
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Lifetime Summary KPI Banner */}
        <div className="grid grid-cols-3 gap-2 px-6 py-3.5 border-b text-xs" style={{
          backgroundColor: 'var(--md-sys-color-surface-container)',
          borderColor: 'var(--md-sys-color-outline-variant)'
        }}>
          <div>
            <span className="text-[10px] uppercase font-bold block tracking-wider" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
              {isBM ? 'JUMLAH KESELURUHAN' : 'LIFETIME SPENT'}
            </span>
            <span className="text-base font-extrabold tabular-nums" style={{ color: 'var(--md-sys-color-primary)' }}>
              {formatRM(totalLifetimeSpent)}
            </span>
          </div>

          <div>
            <span className="text-[10px] uppercase font-bold block tracking-wider" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
              {isBM ? 'PURATA BULANAN' : 'MONTHLY AVERAGE'}
            </span>
            <span className="text-base font-extrabold tabular-nums">
              {formatRM(avgMonthlySpend)}
            </span>
          </div>

          <div>
            <span className="text-[10px] uppercase font-bold block tracking-wider" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
              {isBM ? 'BULAN DIREKOD' : 'RECORDED MONTHS'}
            </span>
            <span className="text-base font-extrabold tabular-nums">
              {monthlyGroups.length} {isBM ? 'Bulan' : 'Months'}
            </span>
          </div>
        </div>

        {/* Scrollable Month Cards */}
        <div className="flex-1 overflow-y-auto p-6 space-y-3">
          {monthlyGroups.length === 0 ? (
            <div className="py-12 text-center text-zinc-400 text-xs space-y-2">
              <Receipt className="w-8 h-8 mx-auto text-zinc-300 dark:text-zinc-700" />
              <p>{isBM ? 'Belum ada sejarah perbelanjaan lepas direkodkan.' : 'No past spending records found yet.'}</p>
            </div>
          ) : (
            monthlyGroups.map((group) => {
              const isCurrentSelected = selectedMonth === group.month;
              const percentUsed = monthlyBudget > 0 ? Math.min(100, Math.round((group.totalSpent / monthlyBudget) * 100)) : 0;
              const isOver = group.totalSpent > monthlyBudget;

              // Top 3 categories for this month
              const catTotals = {};
              group.transactions.forEach(t => {
                const cat = t.category || 'Other';
                catTotals[cat] = (catTotals[cat] || 0) + Number(t.amount || 0);
              });

              const sortedCats = Object.entries(catTotals)
                .sort((a, b) => b[1] - a[1])
                .slice(0, 3);

              return (
                <div
                  key={group.month}
                  className="rounded-2xl p-4 transition-all"
                  style={{
                    backgroundColor: isCurrentSelected ? 'var(--md-sys-color-surface-container-highest)' : 'var(--md-sys-color-surface)',
                    border: isCurrentSelected ? '2px solid var(--md-sys-color-primary)' : '1px solid var(--md-sys-color-outline-variant)'
                  }}
                >
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                    
                    {/* Left: Month title & spend */}
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="text-base font-bold" style={{ color: 'var(--md-sys-color-on-surface)' }}>
                          {formatMonthYear(group.month, isBM)}
                        </span>
                        {isCurrentSelected && (
                          <span className="text-[10px] font-bold px-2 py-0.5 rounded-full" style={{
                            backgroundColor: 'var(--md-sys-color-primary)',
                            color: 'var(--md-sys-color-on-primary)'
                          }}>
                            {isBM ? 'Aktif' : 'Viewing Now'}
                          </span>
                        )}
                      </div>

                      <div className="flex items-center space-x-3 mt-1 text-xs">
                        <span className="text-lg font-black tabular-nums" style={{ color: 'var(--md-sys-color-primary)' }}>
                          {formatRM(group.totalSpent)}
                        </span>
                        <span style={{ color: 'var(--md-sys-color-outline)' }}>•</span>
                        <span className="font-medium" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
                          {group.count} {isBM ? 'transaksi' : 'expenses'}
                        </span>
                        <span style={{ color: 'var(--md-sys-color-outline)' }}>•</span>
                        <span className="font-bold tabular-nums" style={{
                          color: isOver ? 'var(--md-sys-color-error)' : 'var(--md-sys-color-on-surface-variant)'
                        }}>
                          {percentUsed}% {isBM ? 'bajet' : 'of budget'}
                        </span>
                      </div>
                    </div>

                    {/* Right: Quick Switch CTA */}
                    <div>
                      <button
                        onClick={() => {
                          onSelectMonth(group.month);
                          onClose();
                        }}
                        className="flex items-center space-x-1.5 px-4 py-1.5 rounded-full text-xs font-bold transition-all shadow-xs active:scale-95"
                        style={{
                          backgroundColor: isCurrentSelected ? 'var(--md-sys-color-surface-container)' : 'var(--md-sys-color-primary)',
                          color: isCurrentSelected ? 'var(--md-sys-color-on-surface-variant)' : 'var(--md-sys-color-on-primary)'
                        }}
                      >
                        <span>{isCurrentSelected ? (isBM ? 'Sedang Dipaparkan' : 'Active View') : (isBM ? 'Papar Bulan Ini' : 'View This Month')}</span>
                        {!isCurrentSelected && <ChevronRight className="w-3.5 h-3.5" />}
                      </button>
                    </div>

                  </div>

                  {/* Top Spending Categories breakdown bar */}
                  {sortedCats.length > 0 && (
                    <div className="mt-3 pt-2.5 border-t flex flex-wrap items-center gap-1.5 text-[11px]" style={{ borderColor: 'var(--md-sys-color-outline-variant)' }}>
                      <span className="font-medium" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
                        {isBM ? 'Perbelanjaan Utama:' : 'Top Categories:'}
                      </span>
                      {sortedCats.map(([cat, amt]) => {
                        const pct = group.totalSpent > 0 ? Math.round((amt / group.totalSpent) * 100) : 0;
                        return (
                          <span
                            key={cat}
                            className="px-2.5 py-0.5 rounded-full text-[10.5px] font-semibold flex items-center space-x-1"
                            style={{
                              backgroundColor: 'var(--md-sys-color-surface-container)',
                              color: 'var(--md-sys-color-on-surface)'
                            }}
                          >
                            <span>{cat}:</span>
                            <span className="font-bold tabular-nums">{formatRM(amt)}</span>
                            <span style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>({pct}%)</span>
                          </span>
                        );
                      })}
                    </div>
                  )}

                </div>
              );
            })
          )}
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-3 border-t border-zinc-100 dark:border-zinc-800 flex items-center justify-between text-xs text-zinc-500">
          <button
            onClick={() => {
              onSelectMonth('all');
              onClose();
            }}
            className="text-zinc-700 dark:text-zinc-300 hover:underline font-semibold"
          >
            {isBM ? 'Papar Semua Transaksi (Semua Masa)' : 'View All Expenses (All Time)'}
          </button>

          <button
            onClick={onClose}
            className="px-3.5 py-1.5 border border-zinc-200 dark:border-zinc-800 rounded-lg text-zinc-700 dark:text-zinc-300 hover:text-zinc-900 dark:hover:text-zinc-100 font-medium"
          >
            {isBM ? 'Tutup' : 'Close'}
          </button>
        </div>

      </div>
    </div>
  );
}
