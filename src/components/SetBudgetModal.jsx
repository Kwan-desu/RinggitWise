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
      <div 
        className="w-full max-w-sm rounded-[28px] shadow-2xl p-6 relative transition-all"
        style={{
          backgroundColor: 'var(--md-sys-color-surface-container-high)',
          color: 'var(--md-sys-color-on-surface)'
        }}
      >
        
        {/* Header */}
        <div className="flex items-center justify-between border-b pb-3.5 mb-4" style={{ borderColor: 'var(--md-sys-color-outline-variant)' }}>
          <h3 className="text-base font-bold" style={{ color: 'var(--md-sys-color-on-surface)' }}>
            {isBM ? 'Tetapkan Had Bajet' : 'Set Monthly Budget'}
          </h3>
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

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          <div>
            <label className="text-[11px] font-bold block mb-1 uppercase tracking-wider" style={{ color: 'var(--md-sys-color-primary)' }}>
              {isBM ? 'Had Bajet Bulanan (RM)' : 'Monthly Budget (RM)'}
            </label>
            <div className="relative">
              <span className="absolute left-3.5 top-2.5 font-black text-lg" style={{ color: 'var(--md-sys-color-primary)' }}>RM</span>
              <input
                type="number"
                step="50"
                required
                placeholder="3500"
                value={budget}
                onChange={(e) => setBudget(e.target.value)}
                className="w-full rounded-2xl pl-12 pr-4 py-2.5 font-black text-2xl tabular-nums focus:outline-none transition-all"
                style={{
                  backgroundColor: 'var(--md-sys-color-surface)',
                  border: '2px solid var(--md-sys-color-primary)',
                  color: 'var(--md-sys-color-on-surface)'
                }}
                autoFocus
              />
            </div>
          </div>

          <button
            type="submit"
            className="w-full font-bold py-3 rounded-full transition-all active:scale-[0.98] shadow-md text-sm mt-2"
            style={{
              backgroundColor: 'var(--md-sys-color-primary)',
              color: 'var(--md-sys-color-on-primary)'
            }}
          >
            {isBM ? 'Simpan Bajet' : 'Save Budget'}
          </button>
        </form>

      </div>
    </div>
  );
}
