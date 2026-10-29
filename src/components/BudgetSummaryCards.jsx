import React, { useState } from 'react';
import { Wallet, TrendingUp, Sparkles, CreditCard, Edit3, ArrowUpRight, ArrowDownRight, CheckCircle2 } from 'lucide-react';
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
    <div className="space-y-4">
      {/* 4 Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        
        {/* Card 1: Total Spent */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 shadow-xs transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              {isBM ? 'Jumlah Dibelanja' : 'Total Spent'}
            </span>
            <div className="w-8 h-8 rounded-lg bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <TrendingUp className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-black text-slate-900 dark:text-white mt-2 tracking-tight">
            {formatRM(totalSpent)}
          </div>
          <div className="flex items-center space-x-1.5 mt-2 text-[11px] text-slate-500 dark:text-slate-400">
            <span className="font-bold text-slate-700 dark:text-slate-300">{transactionCount}</span>
            <span>{isBM ? 'transaksi direkodkan' : 'transactions logged'}</span>
          </div>
        </div>

        {/* Card 2: Monthly Budget Limit */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 shadow-xs transition-colors relative">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              {isBM ? 'Bajet Bulanan (RM)' : 'Monthly Budget (RM)'}
            </span>
            <button
              onClick={() => { setIsEditing(!isEditing); setTempBudget(monthlyBudget.toString()); }}
              className="text-slate-400 hover:text-emerald-600 dark:hover:text-emerald-400 p-1 rounded-md transition-colors"
              title="Edit Budget"
            >
              <Edit3 className="w-3.5 h-3.5" />
            </button>
          </div>

          {isEditing ? (
            <form onSubmit={handleSaveBudget} className="mt-2 flex items-center space-x-2">
              <input
                type="number"
                step="50"
                value={tempBudget}
                onChange={(e) => setTempBudget(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-950 border border-emerald-500 rounded-lg px-2.5 py-1 text-xs font-bold text-slate-900 dark:text-white focus:outline-none"
                autoFocus
              />
              <button
                type="submit"
                className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-xs font-bold"
              >
                Save
              </button>
            </form>
          ) : (
            <div className="text-2xl font-black text-slate-900 dark:text-white mt-2 tracking-tight">
              {formatRM(monthlyBudget)}
            </div>
          )}

          <div className="flex items-center space-x-1.5 mt-2 text-[11px] text-slate-500 dark:text-slate-400">
            <span>{percentUsed}% {isBM ? 'daripada bajet' : 'of budget used'}</span>
          </div>
        </div>

        {/* Card 3: Remaining Balance */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 shadow-xs transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              {isBM ? 'Baki Bajet Bersih' : 'Remaining Balance'}
            </span>
            <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${
              isOverBudget 
                ? 'bg-rose-50 dark:bg-rose-950/60 text-rose-600 dark:text-rose-400' 
                : 'bg-teal-50 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400'
            }`}>
              <Wallet className="w-4 h-4" />
            </div>
          </div>
          <div className={`text-2xl font-black mt-2 tracking-tight ${
            isOverBudget ? 'text-rose-600 dark:text-rose-400' : 'text-teal-600 dark:text-teal-400'
          }`}>
            {isOverBudget ? `-${formatRM(totalSpent - monthlyBudget)}` : formatRM(remaining)}
          </div>
          <div className="flex items-center space-x-1.5 mt-2 text-[11px]">
            {isOverBudget ? (
              <span className="text-rose-600 dark:text-rose-400 font-semibold">
                {isBM ? 'Lebih bajet!' : 'Budget exceeded!'}
              </span>
            ) : (
              <span className="text-emerald-600 dark:text-emerald-400 font-semibold flex items-center">
                <CheckCircle2 className="w-3.5 h-3.5 mr-1" />
                {isBM ? 'Dalam sasaran selamat' : 'Safe on track'}
              </span>
            )}
          </div>
        </div>

        {/* Card 4: Daily Safe Spend */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 shadow-xs transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              {isBM ? 'Boleh Belanja Hari Ini' : 'Daily Safe Allowance'}
            </span>
            <div className="w-8 h-8 rounded-lg bg-amber-50 dark:bg-amber-950/60 text-amber-600 dark:text-amber-400 flex items-center justify-center">
              <Sparkles className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-black text-amber-600 dark:text-amber-400 mt-2 tracking-tight">
            {formatRM(dailySafeSpend)}
          </div>
          <div className="flex items-center space-x-1.5 mt-2 text-[11px] text-slate-500 dark:text-slate-400">
            <span>{daysRemaining} {isBM ? 'hari baki bulan ini' : 'days left in month'}</span>
          </div>
        </div>

      </div>
    </div>
  );
}
