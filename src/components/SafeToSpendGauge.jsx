import React from 'react';
import { ShieldCheck, AlertTriangle, Sparkles, TrendingUp, HelpCircle, Wallet, Plus } from 'lucide-react';
import { formatRM } from '../utils/helpers';

export default function SafeToSpendGauge({
  monthlyIncome,
  totalNeedsSpent,
  totalWantsSpent,
  totalSavingsSpent,
  safeToSpendDaily,
  safeToSpendWeekly,
  daysLeftInMonth,
  onOpenSetIncome,
  language
}) {
  const isBM = language === 'bm';

  // If user has not input income yet
  if (!monthlyIncome || monthlyIncome <= 0) {
    return (
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl relative overflow-hidden flex flex-col sm:flex-row items-center justify-between gap-5">
        <div className="flex items-center space-x-4">
          <div className="w-14 h-14 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
            <Wallet className="w-7 h-7" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h2 className="text-lg font-bold text-white tracking-tight">
                {isBM ? 'Tetapkan Gaji Bersih Bulanan Anda' : 'Set Your Monthly Take-Home Pay'}
              </h2>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800">
                Langkah 1
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-1 max-w-lg leading-relaxed">
              {isBM 
                ? 'Masukkan pendapatan bulanan anda untuk mengaktifkan metrik Selamat Belanja (Safe to Spend) dan formula 3 Balang Kewangan 50/30/20.'
                : 'Enter your monthly salary to calculate your guilt-free daily Safe to Spend metric and activate your 3-Bucket budget.'}
            </p>
          </div>
        </div>

        <button
          onClick={onOpenSetIncome}
          className="w-full sm:w-auto shrink-0 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs px-5 py-3 rounded-xl shadow-lg shadow-emerald-600/30 flex items-center justify-center space-x-2 transition-all active:scale-95"
        >
          <Plus className="w-4 h-4" />
          <span>{isBM ? 'Tetapkan Gaji (RM)' : 'Enter Income (RM)'}</span>
        </button>
      </div>
    );
  }

  // Dollarwise logic: Wants allocation is 30% of monthly income
  const totalWantsBudget = monthlyIncome * 0.30;
  const remainingWantsPool = Math.max(0, totalWantsBudget - totalWantsSpent);
  const wantsPercentUsed = Math.min(100, Math.round((totalWantsSpent / totalWantsBudget) * 100));

  let statusColor = 'emerald';
  let statusText = isBM ? 'Dalam Kawalan Bajet' : 'Comfortably on Track';
  let hammerTip = isBM 
    ? 'Bagus! Anda ada wang baki harian tanpa mengusik duit sewa atau bayaran PTPTN.' 
    : 'Great discipline! You have guilt-free money left without compromising bills or PTPTN.';

  if (wantsPercentUsed > 80 && wantsPercentUsed <= 100) {
    statusColor = 'amber';
    statusText = isBM ? 'Amaran: Bajet Menipis' : 'Budget Warning: Running Low';
    hammerTip = isBM
      ? 'Awas! Baki belanja santai anda tinggal sedikit. Kurangkan lepak cafe mahal minggu ini!'
      : 'Caution! Wants budget is tight. Stick to mamak roti canai instead of high-end cafes!';
  } else if (wantsPercentUsed > 100) {
    statusColor = 'rose';
    statusText = isBM ? 'Amaran Caleb Hammer: Lebih Had!' : 'Caleb Hammer Alert: Over Budget!';
    hammerTip = isBM
      ? 'Aiyo! Anda dah terlebih belanja kehendak! Hentikan Shopee checkout sekarang juga!'
      : 'Stop spending! You blew past your Wants allocation. Cut non-essentials immediately!';
  }

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 sm:p-6 shadow-xl relative overflow-hidden">
      {/* Background ambient glow */}
      <div className={`absolute -right-10 -top-10 w-44 h-44 rounded-full blur-3xl opacity-20 pointer-events-none ${
        statusColor === 'emerald' ? 'bg-emerald-500' : statusColor === 'amber' ? 'bg-amber-500' : 'bg-rose-500'
      }`}></div>

      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 border-b border-slate-800/80 pb-5">
        <div>
          <div className="flex items-center space-x-2">
            <h2 className="text-xl font-bold text-white tracking-tight">
              {isBM ? 'Selamat Belanja (Safe to Spend)' : 'Safe to Spend Metric'}
            </h2>
            <span className={`text-[11px] font-bold px-2 py-0.5 rounded-full border ${
              statusColor === 'emerald' 
                ? 'bg-emerald-950/70 text-emerald-400 border-emerald-800/60'
                : statusColor === 'amber'
                ? 'bg-amber-950/70 text-amber-400 border-amber-800/60'
                : 'bg-rose-950/70 text-rose-400 border-rose-800/60'
            }`}>
              {statusText}
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            {isBM 
              ? 'Berapa banyak ringgit yang anda boleh belanja hari ini tanpa mengorbankan sewa, bil TNB atau PTPTN.'
              : 'How much Ringgit you can spend right now after guaranteeing rent, TNB bills & loan repayments.'}
          </p>
        </div>

        {/* Days Left in Month indicator */}
        <div className="flex items-center space-x-3">
          <div className="text-right flex items-center md:flex-col justify-between md:justify-center bg-slate-950/60 px-3.5 py-2 rounded-xl border border-slate-800">
            <span className="text-[11px] text-slate-400">
              {isBM ? 'Baki Hari Bulan Ini' : 'Days Left in Month'}
            </span>
            <span className="text-sm font-bold text-slate-200">
              {daysLeftInMonth} {isBM ? 'Hari' : 'Days'}
            </span>
          </div>
          <button
            onClick={onOpenSetIncome}
            className="text-xs text-slate-400 hover:text-white bg-slate-950 p-2.5 rounded-xl border border-slate-800 transition-colors"
            title="Edit Monthly Income"
          >
            <Wallet className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Primary Figures: Daily vs Weekly Safe-To-Spend */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mt-5">
        
        {/* Daily Allowance Card */}
        <div className="bg-slate-950/80 border border-slate-800/80 rounded-xl p-4 flex items-center justify-between">
          <div>
            <span className="text-xs text-slate-400 font-medium block">
              {isBM ? 'Peruntukan Selamat Hari Ini' : 'Safe to Spend Today'}
            </span>
            <div className="text-2xl sm:text-3xl font-black text-emerald-400 tracking-tight mt-1">
              {formatRM(safeToSpendDaily)}
            </div>
            <span className="text-[10px] text-slate-500 mt-1 block">
              {isBM ? 'Boleh belanja tanpa rasa bersalah' : 'Daily guilt-free allowance'}
            </span>
          </div>
          <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <Sparkles className="w-6 h-6" />
          </div>
        </div>

        {/* Weekly Allowance Card */}
        <div className="bg-slate-950/80 border border-slate-800/80 rounded-xl p-4 flex items-center justify-between">
          <div>
            <span className="text-xs text-slate-400 font-medium block">
              {isBM ? 'Baki Selamat Minggu Ini' : 'Safe to Spend This Week'}
            </span>
            <div className="text-2xl sm:text-3xl font-black text-teal-400 tracking-tight mt-1">
              {formatRM(safeToSpendWeekly)}
            </div>
            <span className="text-[10px] text-slate-500 mt-1 block">
              {isBM ? 'Peruntukan 7 hari' : 'Next 7 days rolling budget'}
            </span>
          </div>
          <div className="w-12 h-12 rounded-xl bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400">
            <TrendingUp className="w-6 h-6" />
          </div>
        </div>

        {/* Wants Budget Pool Card */}
        <div className="bg-slate-950/80 border border-slate-800/80 rounded-xl p-4 sm:col-span-2 lg:col-span-1">
          <div className="flex justify-between items-center mb-1.5">
            <span className="text-xs text-slate-400 font-medium">
              {isBM ? 'Baki Balang Kehendak (30%)' : 'Wants Bucket Remaining'}
            </span>
            <span className="text-xs font-bold text-slate-300">
              {formatRM(remainingWantsPool)} / {formatRM(totalWantsBudget)}
            </span>
          </div>

          {/* Progress bar */}
          <div className="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden mt-2">
            <div 
              className={`h-full transition-all duration-500 rounded-full ${
                wantsPercentUsed > 100 
                  ? 'bg-rose-500' 
                  : wantsPercentUsed > 80 
                  ? 'bg-amber-500' 
                  : 'bg-emerald-500'
              }`}
              style={{ width: `${Math.min(100, wantsPercentUsed)}%` }}
            ></div>
          </div>
          
          <div className="flex justify-between items-center mt-2 text-[10px] text-slate-500">
            <span>{wantsPercentUsed}% {isBM ? 'digunakan' : 'used'}</span>
            <span>{formatRM(Math.max(0, totalWantsBudget - totalWantsSpent))} {isBM ? 'baki' : 'left'}</span>
          </div>
        </div>

      </div>

      {/* Malaysian Financial Audit Tip Footer */}
      <div className="mt-4 p-3 bg-slate-950/40 border border-slate-800/60 rounded-xl flex items-start space-x-2.5 text-xs text-slate-300">
        <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
        <div>
          <span className="font-semibold text-emerald-400 mr-1.5">
            {isBM ? 'Audit Ringgit:' : 'Ringgit Audit Insight:'}
          </span>
          <span>{hammerTip}</span>
        </div>
      </div>

    </div>
  );
}
