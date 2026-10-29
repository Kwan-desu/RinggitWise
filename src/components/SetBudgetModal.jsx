import React, { useState } from 'react';
import { X, Wallet, Check } from 'lucide-react';
import { playSound, fireCelebration, formatRM } from '../utils/helpers';

export default function SetBudgetModal({
  isOpen,
  onClose,
  currentBudget = 3500,
  currentName = '',
  onSaveBudget,
  language = 'en'
}) {
  const isBM = language === 'bm';
  const [budget, setBudget] = useState(currentBudget > 0 ? String(currentBudget) : '3500');
  const [name, setName] = useState(currentName || '');

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    const val = Number(budget);
    if (val <= 0) return;

    onSaveBudget({
      name: name.trim() || 'My Finances',
      monthlyBudget: val,
    });

    playSound('approve');
    fireCelebration();
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-md bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-2xl p-6 relative transition-colors">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-3 mb-4">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <Wallet className="w-4 h-4" />
            </div>
            <h3 className="text-base font-bold text-slate-900 dark:text-white">
              {isBM ? 'Tetapkan Bajet Bulanan (RM)' : 'Set Monthly Budget (RM)'}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-700 dark:hover:text-white p-1 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          
          <div>
            <label className="text-slate-700 dark:text-slate-300 font-semibold block mb-1">
              {isBM ? 'Nama Profil Kewangan' : 'Profile / Account Name'}
            </label>
            <input
              type="text"
              placeholder="e.g. Kwan / Family Budget"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-xl px-3 py-2 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div>
            <label className="text-slate-700 dark:text-slate-300 font-semibold block mb-1">
              {isBM ? 'Had Bajet Bulanan (RM)' : 'Monthly Budget Limit (RM)'}
            </label>
            <div className="relative">
              <span className="absolute left-3 top-2 text-slate-500 dark:text-slate-400 font-bold">RM</span>
              <input
                type="number"
                step="50"
                required
                placeholder="3500"
                value={budget}
                onChange={(e) => setBudget(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-xl pl-11 pr-3 py-2 text-slate-900 dark:text-white font-bold text-base focus:outline-none focus:border-emerald-500"
              />
            </div>
          </div>

          <p className="text-[11px] text-slate-500 dark:text-slate-400 leading-relaxed">
            {isBM 
              ? 'Bajet ini digunakan untuk memantau perbelanjaan bulanan dan baki peruntukan harian anda.'
              : 'This budget limit is used to calculate your remaining balance and daily safe spending allowance.'}
          </p>

          <button
            type="submit"
            className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-2.5 rounded-xl transition-all shadow-md shadow-emerald-600/20"
          >
            {isBM ? 'Simpan Bajet RM' : 'Save Budget (RM)'}
          </button>
        </form>

      </div>
    </div>
  );
}
