import React, { useState } from 'react';
import { Edit2, Check, ArrowUpRight } from 'lucide-react';
import { formatRM, formatMonthYear } from '../utils/helpers';

export default function BudgetSummaryCards({
  monthlyBudget = 3500,
  onUpdateBudget,
  totalSpent = 0,
  transactionCount = 0,
  selectedMonth = '2026-10',
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
      
      {/* 1. Total Spent (M3 Filled Card) */}
      <div className="p-4 rounded-3xl transition-all" style={{
        backgroundColor: 'var(--md-sys-color-surface-container-high)',
        color: 'var(--md-sys-color-on-surface)'
      }}>
        <div className="flex items-center justify-between text-xs">
          <span className="font-semibold tracking-wide uppercase text-[11px]" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
            {isBM ? 'Jumlah Belanja' : 'Total Spent'}
          </span>
          <span className="font-bold tabular-nums text-xs px-2 py-0.5 rounded-full" style={{
            backgroundColor: 'var(--md-sys-color-surface-container-highest)',
            color: 'var(--md-sys-color-primary)'
          }}>
            {percentUsed}% {isBM ? 'digunakan' : 'used'}
          </span>
        </div>

        <div className="mt-2.5 text-2xl sm:text-3xl font-extrabold tracking-tight tabular-nums">
          {formatRM(totalSpent)}
        </div>

        {/* M3 Linear Progress Bar */}
        <div className="w-full h-1.5 rounded-full overflow-hidden mt-3" style={{ backgroundColor: 'var(--md-sys-color-surface-container-lowest)' }}>
          <div 
            className="h-full rounded-full transition-all duration-500"
            style={{ 
              width: `${percentUsed}%`,
              backgroundColor: isOverBudget ? 'var(--md-sys-color-error)' : 'var(--md-sys-color-primary)'
            }}
          />
        </div>

        <div className="mt-2.5 flex items-center justify-between text-[11px]" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
          <span>{transactionCount} {isBM ? 'transaksi' : 'transactions'}</span>
          <span className="font-semibold">{formatMonthYear(selectedMonth, isBM)}</span>
        </div>
      </div>

      {/* 2. Monthly Budget (M3 Filled Card) */}
      <div className="p-4 rounded-3xl transition-all" style={{
        backgroundColor: 'var(--md-sys-color-surface-container-high)',
        color: 'var(--md-sys-color-on-surface)'
      }}>
        <div className="flex items-center justify-between text-xs">
          <span className="font-semibold tracking-wide uppercase text-[11px]" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
            {isBM ? 'Had Bajet Bulanan' : 'Monthly Budget'}
          </span>
          <button
            onClick={() => { setIsEditing(!isEditing); setTempBudget(monthlyBudget.toString()); }}
            className="p-1 rounded-full hover:opacity-80 transition-colors"
            style={{ color: 'var(--md-sys-color-primary)' }}
            title="Edit Budget"
          >
            <Edit2 className="w-3.5 h-3.5" />
          </button>
        </div>

        {isEditing ? (
          <form onSubmit={handleSaveBudget} className="mt-2 flex items-center space-x-2">
            <input
              type="number"
              step="50"
              value={tempBudget}
              onChange={(e) => setTempBudget(e.target.value)}
              className="w-full rounded-xl px-2.5 py-1 text-base font-bold tabular-nums focus:outline-none"
              style={{
                backgroundColor: 'var(--md-sys-color-surface)',
                border: '1.5px solid var(--md-sys-color-primary)',
                color: 'var(--md-sys-color-on-surface)'
              }}
              autoFocus
            />
            <button
              type="submit"
              className="px-3 py-1 rounded-xl text-xs font-bold shadow-xs"
              style={{
                backgroundColor: 'var(--md-sys-color-primary)',
                color: 'var(--md-sys-color-on-primary)'
              }}
            >
              <Check className="w-3.5 h-3.5" />
            </button>
          </form>
        ) : (
          <div className="mt-2.5 text-2xl sm:text-3xl font-extrabold tracking-tight tabular-nums">
            {formatRM(monthlyBudget)}
          </div>
        )}

        <div className="mt-4 text-[11px] flex items-center justify-between" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
          <span>{isBM ? 'Had siling Ringgit' : 'Ceiling limit'}</span>
          <span className="font-semibold">RM</span>
        </div>
      </div>

      {/* 3. Remaining Balance (M3 Filled Card) */}
      <div className="p-4 rounded-3xl transition-all" style={{
        backgroundColor: 'var(--md-sys-color-surface-container-high)',
        color: 'var(--md-sys-color-on-surface)'
      }}>
        <div className="flex items-center justify-between text-xs">
          <span className="font-semibold tracking-wide uppercase text-[11px]" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
            {isBM ? 'Baki Bersih' : 'Remaining Balance'}
          </span>
          <span className="text-[10px] font-bold px-2.5 py-0.5 rounded-full" style={{
            backgroundColor: isOverBudget ? 'var(--md-sys-color-error-container)' : 'var(--md-sys-color-secondary-container)',
            color: isOverBudget ? 'var(--md-sys-color-on-error-container)' : 'var(--md-sys-color-on-secondary-container)'
          }}>
            {isOverBudget ? (isBM ? 'Lebih Had' : 'Over Limit') : (isBM ? 'Terkawal' : 'On Track')}
          </span>
        </div>

        <div className="mt-2.5 text-2xl sm:text-3xl font-extrabold tracking-tight tabular-nums" style={{
          color: isOverBudget ? 'var(--md-sys-color-error)' : 'var(--md-sys-color-on-surface)'
        }}>
          {isOverBudget ? `-${formatRM(totalSpent - monthlyBudget)}` : formatRM(remaining)}
        </div>

        <div className="mt-4 text-[11px] flex items-center justify-between" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
          <span>{isBM ? 'Baki sehingga hujung bulan' : 'Safe to end of month'}</span>
          <span className="font-semibold">{daysRemaining} {isBM ? 'hari' : 'days left'}</span>
        </div>
      </div>

      {/* 4. Daily Allowance (M3 Filled Card) */}
      <div className="p-4 rounded-3xl transition-all" style={{
        backgroundColor: 'var(--md-sys-color-surface-container-high)',
        color: 'var(--md-sys-color-on-surface)'
      }}>
        <div className="flex items-center justify-between text-xs">
          <span className="font-semibold tracking-wide uppercase text-[11px]" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
            {isBM ? 'Peruntukan Harian' : 'Daily Allowance'}
          </span>
          <span className="text-[11px] font-bold" style={{ color: 'var(--md-sys-color-primary)' }}>
            {daysRemaining}d
          </span>
        </div>

        <div className="mt-2.5 text-2xl sm:text-3xl font-extrabold tracking-tight tabular-nums" style={{ color: 'var(--md-sys-color-primary)' }}>
          {formatRM(dailySafeSpend)}
        </div>

        <div className="mt-4 text-[11px] flex items-center justify-between" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
          <span>{isBM ? 'Kadar belanja harian selamat' : 'Guilt-free daily burn rate'}</span>
          <span className="font-semibold">RM/day</span>
        </div>
      </div>

    </div>
  );
}
