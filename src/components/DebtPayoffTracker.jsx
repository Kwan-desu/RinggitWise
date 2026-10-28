import React, { useState } from 'react';
import { CreditCard, TrendingDown, Calendar, ShieldAlert, Plus, CheckCircle2 } from 'lucide-react';
import { formatRM, fireCelebration } from '../utils/helpers';

export default function DebtPayoffTracker({
  debts,
  onUpdateDebts,
  language
}) {
  const isBM = language === 'bm';
  const [strategy, setStrategy] = useState('avalanche'); // 'avalanche' or 'snowball'
  const [extraPayment, setExtraPayment] = useState(250); // Extra RM allocated to debt

  const totalRemaining = debts.reduce((sum, d) => sum + d.remainingAmount, 0);
  const totalMinMonthly = debts.reduce((sum, d) => sum + d.minimumMonthly, 0);

  // Sort debts based on strategy
  const sortedDebts = [...debts].sort((a, b) => {
    if (strategy === 'avalanche') {
      return b.interestRate - a.interestRate; // Highest interest first
    } else {
      return a.remainingAmount - b.remainingAmount; // Smallest balance first
    }
  });

  // Calculate approximate debt-free timeline
  const effectiveMonthly = totalMinMonthly + Number(extraPayment || 0);
  const estimatedMonths = effectiveMonthly > 0 ? Math.ceil(totalRemaining / effectiveMonthly) : 48;
  const debtFreeDate = new Date();
  debtFreeDate.setMonth(debtFreeDate.getMonth() + estimatedMonths);
  const dateFormatted = debtFreeDate.toLocaleDateString(isBM ? 'ms-MY' : 'en-US', {
    month: 'short',
    year: 'numeric'
  });

  const handleMakePayment = (debtId, amount) => {
    const updated = debts.map(d => {
      if (d.id === debtId) {
        const newRem = Math.max(0, d.remainingAmount - amount);
        return { ...d, remainingAmount: newRem };
      }
      return d;
    });
    onUpdateDebts(updated);
    fireCelebration();
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 sm:p-6 shadow-xl">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-slate-800 pb-4 mb-5">
        <div>
          <div className="flex items-center space-x-2">
            <CreditCard className="w-5 h-5 text-blue-400" />
            <h2 className="text-xl font-bold text-white tracking-tight">
              {isBM ? 'Pelan Pelupusan Hutang Malaysia (Debt Payoff Planner)' : 'Malaysian Debt Payoff Planner'}
            </h2>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            {isBM 
              ? 'Hapuskan pinjaman PTPTN, ansuran Myvi, kad kredit dan SPayLater dengan strategi matematik.'
              : 'Crush PTPTN student loans, car installments, credit cards and BNPL with ruthless math.'}
          </p>
        </div>

        {/* Strategy Switcher */}
        <div className="flex items-center bg-slate-950 p-1 rounded-xl border border-slate-800 text-xs self-start sm:self-auto">
          <button
            onClick={() => setStrategy('avalanche')}
            className={`px-3 py-1.5 rounded-lg font-bold transition-all ${
              strategy === 'avalanche' 
                ? 'bg-blue-600 text-white shadow' 
                : 'text-slate-400 hover:text-white'
            }`}
          >
            {isBM ? 'Longsoran (Avalanche - Jimat Faedah)' : 'Avalanche (Save Interest)'}
          </button>
          <button
            onClick={() => setStrategy('snowball')}
            className={`px-3 py-1.5 rounded-lg font-bold transition-all ${
              strategy === 'snowball' 
                ? 'bg-blue-600 text-white shadow' 
                : 'text-slate-400 hover:text-white'
            }`}
          >
            {isBM ? 'Bebola Salji (Snowball - Pantas)' : 'Snowball (Fast Wins)'}
          </button>
        </div>
      </div>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <div className="bg-slate-950 p-4 rounded-xl border border-slate-800">
          <span className="text-xs text-slate-400 font-medium block">
            {isBM ? 'Baki Jumlah Hutang' : 'Total Remaining Debt'}
          </span>
          <div className="text-2xl font-black text-rose-400 mt-1">
            {formatRM(totalRemaining)}
          </div>
          <span className="text-[10px] text-slate-500 mt-0.5 block">
            {debts.length} {isBM ? 'Akaun pembiayaan aktif' : 'Active loan accounts'}
          </span>
        </div>

        <div className="bg-slate-950 p-4 rounded-xl border border-slate-800">
          <span className="text-xs text-slate-400 font-medium block">
            {isBM ? 'Bayaran Bulanan Wajib' : 'Minimum Monthly Total'}
          </span>
          <div className="text-2xl font-black text-white mt-1">
            {formatRM(totalMinMonthly)}
          </div>
          <span className="text-[10px] text-slate-500 mt-0.5 block">
            {isBM ? 'Komitmen tetap setiap bulan' : 'Monthly non-negotiable'}
          </span>
        </div>

        <div className="bg-slate-950 p-4 rounded-xl border border-slate-800">
          <span className="text-xs text-slate-400 font-medium block">
            {isBM ? 'Sasaran Bebas Hutang' : 'Projected Debt-Free Date'}
          </span>
          <div className="text-2xl font-black text-emerald-400 mt-1">
            {dateFormatted}
          </div>
          <span className="text-[10px] text-emerald-500 mt-0.5 block">
            {estimatedMonths} {isBM ? 'bulan lagi' : 'months remaining'}
          </span>
        </div>
      </div>

      {/* Debt Cards Ordered by Strategy */}
      <div className="space-y-3">
        {sortedDebts.map((debt, index) => {
          const progress = Math.min(100, Math.round(((debt.totalAmount - debt.remainingAmount) / debt.totalAmount) * 100));
          const isTargetDebt = index === 0 && debt.remainingAmount > 0;

          return (
            <div 
              key={debt.id}
              className={`p-4 rounded-xl border transition-all ${
                isTargetDebt 
                  ? 'bg-blue-950/30 border-blue-500/60 shadow-lg shadow-blue-950/20' 
                  : 'bg-slate-950/60 border-slate-800/80 hover:border-slate-700'
              }`}
            >
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                <div className="flex items-center space-x-3">
                  <div className={`w-8 h-8 rounded-lg flex items-center justify-center text-xs font-black ${
                    isTargetDebt ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-400'
                  }`}>
                    #{index + 1}
                  </div>
                  <div>
                    <div className="flex items-center space-x-2">
                      <h4 className="text-sm font-bold text-white">{debt.name}</h4>
                      {isTargetDebt && (
                        <span className="text-[9px] uppercase font-extrabold px-1.5 py-0.5 rounded bg-blue-600 text-white animate-pulse">
                          {isBM ? 'Fokus Utama' : 'Attack First'}
                        </span>
                      )}
                    </div>
                    <span className="text-xs text-slate-400">
                      {debt.category} • {debt.interestRate}% {isBM ? 'Kadar Faedah' : 'APR'}
                    </span>
                  </div>
                </div>

                <div className="text-right sm:self-auto self-start">
                  <div className="text-base font-extrabold text-white">
                    {formatRM(debt.remainingAmount)}
                  </div>
                  <span className="text-xs text-slate-400">
                    {isBM ? 'Min: ' : 'Min: '}{formatRM(debt.minimumMonthly)}/bulan
                  </span>
                </div>
              </div>

              {/* Progress bar */}
              <div className="w-full bg-slate-900 h-2 rounded-full overflow-hidden mt-3">
                <div 
                  className="bg-blue-500 h-full rounded-full transition-all duration-500"
                  style={{ width: `${progress}%` }}
                ></div>
              </div>

              <div className="flex items-center justify-between text-[11px] text-slate-500 mt-2">
                <span>{progress}% {isBM ? 'diselesaikan' : 'paid off'}</span>
                {debt.remainingAmount > 0 && (
                  <button
                    onClick={() => handleMakePayment(debt.id, debt.minimumMonthly)}
                    className="text-xs font-bold text-blue-400 hover:text-blue-300 transition-colors"
                  >
                    + {isBM ? 'Rekod Bayaran' : 'Record'} {formatRM(debt.minimumMonthly)}
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

    </div>
  );
}
