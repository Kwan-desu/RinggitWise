import React, { useState } from 'react';
import { X, CreditCard, PlusCircle } from 'lucide-react';
import { playSound, fireCelebration } from '../utils/helpers';

export default function AddDebtModal({
  isOpen,
  onClose,
  onAddDebt,
  language
}) {
  const isBM = language === 'bm';

  const [name, setName] = useState('');
  const [category, setCategory] = useState('Education');
  const [totalAmount, setTotalAmount] = useState('');
  const [remainingAmount, setRemainingAmount] = useState('');
  const [interestRate, setInterestRate] = useState('1.0');
  const [minimumMonthly, setMinimumMonthly] = useState('');

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!name || !remainingAmount) return;

    const total = Number(totalAmount) || Number(remainingAmount);
    const rem = Number(remainingAmount);
    const min = Number(minimumMonthly) || Math.round(rem * 0.05);

    const newDebt = {
      id: `debt-${Date.now()}`,
      name,
      category,
      totalAmount: total,
      remainingAmount: rem,
      interestRate: Number(interestRate) || 0,
      minimumMonthly: min,
      currentPayment: min,
    };

    onAddDebt(newDebt);
    playSound('approve');
    fireCelebration();

    setName('');
    setTotalAmount('');
    setRemainingAmount('');
    setMinimumMonthly('');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fade-in">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6 relative">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-800 pb-3 mb-4">
          <div className="flex items-center space-x-2">
            <CreditCard className="w-5 h-5 text-blue-400" />
            <h3 className="text-base font-bold text-white">
              {isBM ? 'Tambah Hutang / Pinjaman' : 'Add Loan / Debt to Track'}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
          
          <div>
            <label className="text-slate-300 font-semibold block mb-1">
              {isBM ? 'Nama Pinjaman / Hutang' : 'Debt / Loan Name'}
            </label>
            <input
              type="text"
              required
              placeholder={isBM ? 'cth. PTPTN, Ansuran Myvi, Kad Kredit Maybank' : 'e.g. PTPTN Loan, Myvi Car Loan'}
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-white placeholder-slate-500 focus:outline-none focus:border-blue-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-slate-300 font-semibold block mb-1">
                {isBM ? 'Kategori' : 'Category'}
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-2.5 py-2 text-white focus:outline-none focus:border-blue-500"
              >
                <option value="Education">PTPTN / Education</option>
                <option value="Vehicle">Car Loan (Kenderaan)</option>
                <option value="Credit Card">Credit Card</option>
                <option value="BNPL">SPayLater / BNPL</option>
                <option value="Personal">Personal Loan</option>
                <option value="Mortgage">Housing Loan</option>
              </select>
            </div>

            <div>
              <label className="text-slate-300 font-semibold block mb-1">
                {isBM ? 'Kadar Faedah (% setahun)' : 'Interest Rate (% APR)'}
              </label>
              <input
                type="number"
                step="0.1"
                placeholder="cth. 1.0 (PTPTN) atau 15.0"
                value={interestRate}
                onChange={(e) => setInterestRate(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:border-blue-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-slate-300 font-semibold block mb-1">
                {isBM ? 'Baki Semasa (RM)' : 'Remaining Balance (RM)'}
              </label>
              <div className="relative">
                <span className="absolute left-3 top-2 text-slate-400 font-bold">RM</span>
                <input
                  type="number"
                  step="1"
                  required
                  placeholder="12000"
                  value={remainingAmount}
                  onChange={(e) => setRemainingAmount(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-10 pr-2 py-2 text-white font-bold focus:outline-none focus:border-blue-500"
                />
              </div>
            </div>

            <div>
              <label className="text-slate-300 font-semibold block mb-1">
                {isBM ? 'Bayaran Min Bulanan (RM)' : 'Min Monthly Pay (RM)'}
              </label>
              <div className="relative">
                <span className="absolute left-3 top-2 text-slate-400 font-bold">RM</span>
                <input
                  type="number"
                  step="1"
                  placeholder="150"
                  value={minimumMonthly}
                  onChange={(e) => setMinimumMonthly(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-10 pr-2 py-2 text-white font-bold focus:outline-none focus:border-blue-500"
                />
              </div>
            </div>
          </div>

          <div className="pt-2">
            <button
              type="submit"
              className="w-full bg-blue-600 hover:bg-blue-500 text-white font-bold py-2.5 rounded-xl transition-all shadow-lg shadow-blue-600/30"
            >
              {isBM ? 'Simpan Maklumat Hutang' : 'Save Debt Information'}
            </button>
          </div>

        </form>

      </div>
    </div>
  );
}
