import React, { useState } from 'react';
import { X, DollarSign, Wallet, Check } from 'lucide-react';
import { playSound, fireCelebration, formatRM } from '../utils/helpers';

export default function SetIncomeModal({
  isOpen,
  onClose,
  currentIncome,
  currentName,
  onSaveProfile,
  language
}) {
  const isBM = language === 'bm';
  const [income, setIncome] = useState(currentIncome > 0 ? String(currentIncome) : '');
  const [name, setName] = useState(currentName || '');

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    const val = Number(income);
    if (val <= 0) return;

    onSaveProfile({
      name: name.trim() || 'My Finances',
      monthlyIncome: val,
    });

    playSound('approve');
    fireCelebration();
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fade-in">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6 relative">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-800 pb-3 mb-4">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center">
              <Wallet className="w-4 h-4" />
            </div>
            <h3 className="text-base font-bold text-white">
              {isBM ? 'Tetapkan Pendapatan Bulanan (Gaji Bersih)' : 'Set Monthly Take-Home Pay'}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          
          <div>
            <label className="text-slate-300 font-semibold block mb-1">
              {isBM ? 'Nama Anda / Profil' : 'Your Name / Profile'}
            </label>
            <input
              type="text"
              placeholder={isBM ? 'cth. Brian / Ahmad' : 'e.g. Kwan / Alex'}
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div>
            <label className="text-slate-300 font-semibold block mb-1">
              {isBM ? 'Gaji Bersih Bulanan (RM) *' : 'Monthly Net Take-Home Salary (RM) *'}
            </label>
            <div className="relative">
              <span className="absolute left-3 top-2.5 text-slate-400 font-bold text-sm">RM</span>
              <input
                type="number"
                step="50"
                required
                placeholder="4000"
                value={income}
                onChange={(e) => setIncome(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-11 pr-3 py-2.5 text-white font-black text-base focus:outline-none focus:border-emerald-500"
              />
            </div>
            <span className="text-[11px] text-slate-400 mt-1 block">
              {isBM ? 'Gaji selepas tolak KWSP, SOCSO dan PCB.' : 'Your actual bank account deposit after EPF & tax deductions.'}
            </span>
          </div>

          {/* Quick preset buttons */}
          <div>
            <span className="text-[10px] text-slate-500 uppercase tracking-wider font-bold block mb-1.5">
              {isBM ? 'Pilihan Pantas (Contoh Gaji):' : 'Quick Presets:'}
            </span>
            <div className="grid grid-cols-4 gap-2">
              {[2500, 3500, 4800, 6500].map(p => (
                <button
                  key={p}
                  type="button"
                  onClick={() => setIncome(String(p))}
                  className="py-1.5 px-2 bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded-lg text-slate-300 text-center font-semibold text-[11px] transition-colors"
                >
                  RM {p.toLocaleString()}
                </button>
              ))}
            </div>
          </div>

          <div className="pt-2">
            <button
              type="submit"
              disabled={!income || Number(income) <= 0}
              className="w-full bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-bold py-2.5 rounded-xl transition-all shadow-lg shadow-emerald-600/30 flex items-center justify-center space-x-1.5"
            >
              <Check className="w-4 h-4" />
              <span>{isBM ? 'Simpan & Aktifkan Formula 3 Balang' : 'Save & Calculate 3-Bucket Budget'}</span>
            </button>
          </div>

        </form>

      </div>
    </div>
  );
}
