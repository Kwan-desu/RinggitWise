import React, { useState } from 'react';
import { X } from 'lucide-react';
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
      name: name.trim() || 'User',
      monthlyBudget: val,
    });

    playSound('approve');
    fireCelebration();
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-sm bg-white dark:bg-zinc-900 border border-zinc-200/80 dark:border-zinc-800/80 rounded-2xl shadow-xl p-5 relative transition-colors">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-zinc-100 dark:border-zinc-800/80 pb-3 mb-4">
          <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
            {isBM ? 'Tetapkan Had Bajet' : 'Set Monthly Budget'}
          </h3>
          <button
            onClick={onClose}
            className="text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 p-1"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          <div>
            <label className="text-[11px] font-medium text-zinc-500 block mb-1 uppercase tracking-wider">
              {isBM ? 'Had Bajet Bulanan (RM)' : 'Monthly Budget (RM)'}
            </label>
            <div className="relative">
              <span className="absolute left-3 top-2.5 text-zinc-400 font-semibold">RM</span>
              <input
                type="number"
                step="50"
                required
                placeholder="3500"
                value={budget}
                onChange={(e) => setBudget(e.target.value)}
                className="w-full bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-lg pl-10 pr-3 py-2 text-zinc-900 dark:text-zinc-100 font-semibold text-base tabular-nums focus:outline-none focus:border-zinc-900 dark:focus:border-zinc-100"
                autoFocus
              />
            </div>
          </div>

          <button
            type="submit"
            className="w-full bg-zinc-900 hover:bg-zinc-800 dark:bg-zinc-100 dark:hover:bg-white text-white dark:text-zinc-900 font-semibold py-2 rounded-lg transition-colors"
          >
            {isBM ? 'Simpan Bajet' : 'Save Budget'}
          </button>
        </form>

      </div>
    </div>
  );
}
