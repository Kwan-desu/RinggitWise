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
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-2xl w-full max-w-2xl max-h-[88vh] flex flex-col shadow-xl overflow-hidden transition-colors">
        
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-zinc-100 dark:border-zinc-800">
          <div className="flex items-center space-x-2.5">
            <div className="p-2 rounded-xl bg-zinc-100 dark:bg-zinc-800 text-zinc-800 dark:text-zinc-200">
              <Calendar className="w-4 h-4 text-zinc-600 dark:text-zinc-300" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                {isBM ? 'Sejarah Perbelanjaan Mengikut Bulan' : 'Past Spending by Month'}
              </h3>
              <p className="text-[11px] text-zinc-500 dark:text-zinc-400">
                {isBM ? 'Lihat dan bandingkan rekod perbelanjaan bulan-bulan lepas' : 'Review and compare historical monthly spending'}
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-1 rounded-lg text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Lifetime Summary KPI Banner */}
        <div className="grid grid-cols-3 gap-2 px-6 py-3.5 bg-zinc-50/60 dark:bg-zinc-950/40 border-b border-zinc-100 dark:border-zinc-800/80 text-xs">
          <div>
            <span className="text-[10px] uppercase font-semibold text-zinc-400 block tracking-wider">
              {isBM ? 'JUMLAH KESELURUHAN' : 'LIFETIME SPENT'}
            </span>
            <span className="text-base font-bold text-zinc-900 dark:text-zinc-100 tabular-nums">
              {formatRM(totalLifetimeSpent)}
            </span>
          </div>

          <div>
            <span className="text-[10px] uppercase font-semibold text-zinc-400 block tracking-wider">
              {isBM ? 'PURATA BULANAN' : 'MONTHLY AVERAGE'}
            </span>
            <span className="text-base font-bold text-zinc-900 dark:text-zinc-100 tabular-nums">
              {formatRM(avgMonthlySpend)}
            </span>
          </div>

          <div>
            <span className="text-[10px] uppercase font-semibold text-zinc-400 block tracking-wider">
              {isBM ? 'BULAN DIREKOD' : 'RECORDED MONTHS'}
            </span>
            <span className="text-base font-bold text-zinc-900 dark:text-zinc-100 tabular-nums">
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
                  className={`border rounded-xl p-4 transition-all ${
                    isCurrentSelected 
                      ? 'border-zinc-900 dark:border-zinc-100 bg-zinc-50/80 dark:bg-zinc-800/50 shadow-xs' 
                      : 'border-zinc-200/80 dark:border-zinc-800/80 bg-white dark:bg-zinc-900 hover:border-zinc-400 dark:hover:border-zinc-600'
                  }`}
                >
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                    
                    {/* Left: Month title & spend */}
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="text-sm font-bold text-zinc-900 dark:text-zinc-100">
                          {formatMonthYear(group.month, isBM)}
                        </span>
                        {isCurrentSelected && (
                          <span className="text-[10px] bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 px-2 py-0.5 rounded-full font-semibold">
                            {isBM ? 'Aktif' : 'Viewing Now'}
                          </span>
                        )}
                      </div>

                      <div className="flex items-center space-x-3 mt-1 text-xs">
                        <span className="text-lg font-bold text-zinc-900 dark:text-zinc-100 tabular-nums">
                          {formatRM(group.totalSpent)}
                        </span>
                        <span className="text-zinc-400">·</span>
                        <span className="text-zinc-500 dark:text-zinc-400 font-medium">
                          {group.count} {isBM ? 'transaksi' : 'expenses'}
                        </span>
                        <span className="text-zinc-400">·</span>
                        <span className={`font-semibold tabular-nums ${isOver ? 'text-rose-500' : 'text-zinc-600 dark:text-zinc-400'}`}>
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
                        className={`flex items-center space-x-1.5 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                          isCurrentSelected
                            ? 'bg-zinc-200 dark:bg-zinc-700 text-zinc-800 dark:text-zinc-200 cursor-default'
                            : 'bg-zinc-900 hover:bg-zinc-800 dark:bg-zinc-100 dark:hover:bg-white text-white dark:text-zinc-900 shadow-xs'
                        }`}
                      >
                        <span>{isCurrentSelected ? (isBM ? 'Sedang Dipaparkan' : 'Active View') : (isBM ? 'Papar Bulan Ini' : 'View This Month')}</span>
                        {!isCurrentSelected && <ChevronRight className="w-3.5 h-3.5" />}
                      </button>
                    </div>

                  </div>

                  {/* Top Spending Categories breakdown bar */}
                  {sortedCats.length > 0 && (
                    <div className="mt-3 pt-2.5 border-t border-zinc-100 dark:border-zinc-800/60 flex flex-wrap items-center gap-2 text-[11px]">
                      <span className="text-zinc-400 font-medium">
                        {isBM ? 'Perbelanjaan Utama:' : 'Top Categories:'}
                      </span>
                      {sortedCats.map(([cat, amt]) => {
                        const pct = group.totalSpent > 0 ? Math.round((amt / group.totalSpent) * 100) : 0;
                        return (
                          <span
                            key={cat}
                            className="bg-zinc-100 dark:bg-zinc-800 text-zinc-700 dark:text-zinc-300 px-2 py-0.5 rounded-md text-[10.5px] font-medium flex items-center space-x-1"
                          >
                            <span>{cat}:</span>
                            <span className="font-semibold tabular-nums">{formatRM(amt)}</span>
                            <span className="text-zinc-400">({pct}%)</span>
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
