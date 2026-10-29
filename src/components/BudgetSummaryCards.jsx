import React, { useState } from 'react';
import { Edit2, Check, ArrowUpRight } from 'lucide-react';
import { formatRM } from '../utils/helpers';

export default function BudgetSummaryCards({
  monthlyBudget = 3500,
  onUpdateBudget,
  totalSpent = 0,
  transactionCount = 0,
  language = 'en',
}) {
  const isBM = language === 'bm';
  const [isEditing, setIsEditing] = useState(false);
  const [tempBudget, setTempBudget] = useState(monthlyBudget.toString());

  const remaining = Math.max(0, monthlyBudget - totalSpent);
  const percentUsed = monthlyBudget > 0 ? Math.min(100, Math.round((totalSpent / monthlyBudget) * 100)) : 0;
  const isOverBudget = totalSpent > monthlyBudget;

  const now = new Date();
  const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
  const currentDay = now.getDate();
  const daysRemaining = Math.max(1, daysInMonth - currentDay);
  const dailySafeSpend = remaining > 0 ? (remaining / daysRemaining) : 0;

  const handleSaveBudget = (e) => {
    e.preventDefault();
    const val = parseFloat(tempBudget);
    if (!isNaN(val) && val >= 0) {
      onUpdateBudget(val);
      setIsEditing(false);
    }
  };

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5">
      
      {/* 1. Total Spent */}
      <div className="bg-white dark:bg-zinc-900/70 border border-zinc-200/80 dark:border-zinc-800/80 rounded-xl p-4 shadow-2xs transition-colors">
        <div className="flex items-center justify-between text-zinc-500 dark:text-zinc-400">
          <span className="text-[11px] font-medium tracking-wide uppercase">
            {isBM ? 'Jumlah Belanja' : 'Total Spent'}
          </span>
          <span className="text-[11px] font-medium tabular-nums">
            {percentUsed}% {isBM ? 'digunakan' : 'used'}
          </span>
        </div>

        <div className="mt-2 text-2xl sm:text-3xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-100 tabular-nums">
          {formatRM(totalSpent)}
        </div>

        {/* Micro progress bar */}
        <div className="w-full bg-zinc-100 dark:bg-zinc-800 h-1 rounded-full overflow-hidden mt-3">
          <div 
            className={`h-full rounded-full transition-all duration-500 ${
              isOverBudget ? 'bg-rose-500' : 'bg-emerald-500'
            }`}
            style={{ width: `${percentUsed}%` }}
          />
        </div>

        <div className="mt-2 flex items-center justify-between text-[11px] text-zinc-500 dark:text-zinc-400">
          <span>{transactionCount} {isBM ? 'transaksi' : 'transactions'}</span>
          <span>{isBM ? 'Bulan Ini' : 'October 2026'}</span>
        </div>
      </div>

      {/* 2. Monthly Budget */}
      <div className="bg-white dark:bg-zinc-900/70 border border-zinc-200/80 dark:border-zinc-800/80 rounded-xl p-4 shadow-2xs transition-colors">
        <div className="flex items-center justify-between text-zinc-500 dark:text-zinc-400">
          <span className="text-[11px] font-medium tracking-wide uppercase">
            {isBM ? 'Had Bajet Bulanan' : 'Monthly Budget'}
          </span>
          <button
            onClick={() => { setIsEditing(!isEditing); setTempBudget(monthlyBudget.toString()); }}
            className="text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors p-0.5"
            title="Edit Budget"
          >
            <Edit2 className="w-3 h-3" />
          </button>
        </div>

        {isEditing ? (
          <form onSubmit={handleSaveBudget} className="mt-2 flex items-center space-x-2">
            <input
              type="number"
              step="50"
              value={tempBudget}
              onChange={(e) => setTempBudget(e.target.value)}
              className="w-full bg-zinc-50 dark:bg-zinc-950 border border-zinc-300 dark:border-zinc-700 rounded-lg px-2.5 py-1 text-base font-semibold text-zinc-900 dark:text-zinc-100 tabular-nums focus:outline-none focus:border-zinc-900 dark:focus:border-zinc-100"
              autoFocus
            />
            <button
              type="submit"
              className="px-2.5 py-1 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-lg text-xs font-semibold"
            >
              <Check className="w-3 h-3" />
            </button>
          </form>
        ) : (
          <div className="mt-2 text-2xl sm:text-3xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-100 tabular-nums">
            {formatRM(monthlyBudget)}
          </div>
        )}

        <div className="mt-4 text-[11px] text-zinc-500 dark:text-zinc-400 flex items-center justify-between">
          <span>{isBM ? 'Had siling Ringgit' : 'Ceiling limit'}</span>
          <span className="text-zinc-400 dark:text-zinc-500">RM</span>
        </div>
      </div>

      {/* 3. Remaining Balance */}
      <div className="bg-white dark:bg-zinc-900/70 border border-zinc-200/80 dark:border-zinc-800/80 rounded-xl p-4 shadow-2xs transition-colors">
        <div className="flex items-center justify-between text-zinc-500 dark:text-zinc-400">
          <span className="text-[11px] font-medium tracking-wide uppercase">
            {isBM ? 'Baki Bersih' : 'Remaining Balance'}
          </span>
          <span className={`text-[10px] font-medium px-2 py-0.5 rounded-full ${
            isOverBudget 
              ? 'bg-rose-100 dark:bg-rose-950/80 text-rose-700 dark:text-rose-400' 
              : 'bg-emerald-100 dark:bg-emerald-950/80 text-emerald-700 dark:text-emerald-400'
          }`}>
            {isOverBudget ? (isBM ? 'Lebih Had' : 'Over Limit') : (isBM ? 'Terkawal' : 'On Track')}
          </span>
        </div>

        <div className={`mt-2 text-2xl sm:text-3xl font-semibold tracking-tight tabular-nums ${
          isOverBudget ? 'text-rose-600 dark:text-rose-400' : 'text-zinc-900 dark:text-zinc-100'
        }`}>
          {isOverBudget ? `-${formatRM(totalSpent - monthlyBudget)}` : formatRM(remaining)}
        </div>

        <div className="mt-4 text-[11px] text-zinc-500 dark:text-zinc-400 flex items-center justify-between">
          <span>{isBM ? 'Baki sehingga hujung bulan' : 'Safe to end of month'}</span>
          <span className="text-zinc-400 dark:text-zinc-500">{daysRemaining} {isBM ? 'hari' : 'days left'}</span>
        </div>
      </div>

      {/* 4. Daily Allowance */}
      <div className="bg-white dark:bg-zinc-900/70 border border-zinc-200/80 dark:border-zinc-800/80 rounded-xl p-4 shadow-2xs transition-colors">
        <div className="flex items-center justify-between text-zinc-500 dark:text-zinc-400">
          <span className="text-[11px] font-medium tracking-wide uppercase">
            {isBM ? 'Peruntukan Harian' : 'Daily Allowance'}
          </span>
          <span className="text-[11px] font-medium text-zinc-500">
            {daysRemaining}d
          </span>
        </div>

        <div className="mt-2 text-2xl sm:text-3xl font-semibold tracking-tight text-emerald-600 dark:text-emerald-400 tabular-nums">
          {formatRM(dailySafeSpend)}
        </div>

        <div className="mt-4 text-[11px] text-zinc-500 dark:text-zinc-400 flex items-center justify-between">
          <span>{isBM ? 'Kadar belanja harian selamat' : 'Guilt-free daily burn rate'}</span>
          <span className="text-zinc-400 dark:text-zinc-500">RM/day</span>
        </div>
      </div>

    </div>
  );
}
