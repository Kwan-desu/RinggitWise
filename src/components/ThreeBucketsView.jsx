import React from 'react';
import { ShoppingBag, Coffee, PiggyBank, ArrowUpRight, AlertCircle, CheckCircle2, Wallet } from 'lucide-react';
import { formatRM } from '../utils/helpers';

export default function ThreeBucketsView({
  monthlyIncome,
  totalNeedsSpent,
  totalWantsSpent,
  totalSavingsSpent,
  onOpenSetIncome,
  language
}) {
  const isBM = language === 'bm';

  const hasIncome = monthlyIncome && monthlyIncome > 0;
  const needsBudget = hasIncome ? monthlyIncome * 0.50 : 0;
  const wantsBudget = hasIncome ? monthlyIncome * 0.30 : 0;
  const savingsBudget = hasIncome ? monthlyIncome * 0.20 : 0;

  const buckets = [
    {
      id: 'needs',
      title: isBM ? 'Keperluan (Needs)' : 'Needs Bucket',
      percentage: '50%',
      targetAmount: needsBudget,
      spentAmount: totalNeedsSpent,
      icon: ShoppingBag,
      gradient: 'from-emerald-500/20 to-teal-500/10',
      border: 'border-emerald-500/30',
      barColor: 'bg-emerald-500',
      badgeColor: 'text-emerald-400 bg-emerald-950/80 border-emerald-800',
      examples: isBM 
        ? 'Sewa rumah, TNB, Air Selangor, Petrol RON95, Tol RFID, 99 Speedmart, Ubat' 
        : 'Rent/Mortgage, TNB, Water, RON95 Petrol, RFID Toll, 99 Speedmart, Clinic',
      advice: isBM 
        ? 'Pastikan di bawah 50% gaji anda untuk mengelakkan beban tetap melampau.' 
        : 'Keep under 50% of take-home pay so living costs never choke you.'
    },
    {
      id: 'wants',
      title: isBM ? 'Kehendak (Wants)' : 'Wants Bucket',
      percentage: '30%',
      targetAmount: wantsBudget,
      spentAmount: totalWantsSpent,
      icon: Coffee,
      gradient: 'from-amber-500/20 to-orange-500/10',
      border: 'border-amber-500/30',
      barColor: 'bg-amber-500',
      badgeColor: 'text-amber-400 bg-amber-950/80 border-amber-800',
      examples: isBM 
        ? 'Boba tea, Lepak Cafe, Shopee/TikTok Shop, Tiket Wayang GSC, Steam game' 
        : 'Boba tea, Cafe hunting, Shopee flash sales, Cinema tickets, Gaming, Travel',
      advice: isBM 
        ? 'Hadkan kepada 30%. Inilah punca utama kebocoran wang gaji rakyat Malaysia!' 
        : 'Caleb Hammer rule: Cut frivolous wants when drowning in credit card debt!'
    },
    {
      id: 'savings',
      title: isBM ? 'Simpanan & Hutang (Savings/Debt)' : 'Savings & Debt Bucket',
      percentage: '20%',
      targetAmount: savingsBudget,
      spentAmount: totalSavingsSpent,
      icon: PiggyBank,
      gradient: 'from-blue-500/20 to-indigo-500/10',
      border: 'border-blue-500/30',
      barColor: 'bg-blue-500',
      badgeColor: 'text-blue-400 bg-blue-950/80 border-blue-800',
      examples: isBM 
        ? 'Bayaran PTPTN, Ansuran Kereta Myvi, Kad Kredit, Caruman Sukarela KWSP, ASB' 
        : 'PTPTN student loan, Car loan, Credit cards, EPF voluntary top-up, ASB tabung',
      advice: isBM 
        ? 'Keutamaan: Selesaikan hutang faedah tinggi sebelum mula melabur!' 
        : 'Prioritize toxic high-interest debt first before luxury investing!'
    }
  ];

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 sm:p-6 shadow-xl">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 mb-6 border-b border-slate-800/80 pb-4">
        <div>
          <h2 className="text-xl font-bold text-white tracking-tight flex items-center">
            {isBM ? 'Peraturan 3 Balang Kewangan (50 / 30 / 20)' : 'The 3-Bucket System (50 / 30 / 20 Rule)'}
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            {isBM 
              ? 'Formulanya mudah: 50% keperluan asas, 30% kehendak peribadi, 20% bebas hutang & simpanan.'
              : 'Every single Ringgit automatically partitioned into three crystal-clear categories.'}
          </p>
        </div>
        
        {hasIncome ? (
          <div className="flex items-center space-x-2">
            <div className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-slate-950 border border-slate-800 text-slate-300">
              {isBM ? 'Gaji Bersih: ' : 'Net Monthly: '} 
              <span className="text-emerald-400 font-bold">{formatRM(monthlyIncome)}</span>
            </div>
            <button
              onClick={onOpenSetIncome}
              className="text-xs text-slate-400 hover:text-white bg-slate-950 px-2 py-1.5 rounded-lg border border-slate-800 transition-colors"
            >
              Ubah
            </button>
          </div>
        ) : (
          <button
            onClick={onOpenSetIncome}
            className="text-xs font-bold px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white shadow-md shadow-emerald-600/20 transition-all"
          >
            + {isBM ? 'Tetapkan Pendapatan' : 'Set Income'}
          </button>
        )}
      </div>

      {/* 3 Bucket Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {buckets.map((b) => {
          const Icon = b.icon;
          const pct = b.targetAmount > 0 
            ? Math.min(100, Math.round((b.spentAmount / b.targetAmount) * 100))
            : 0;
          const isOver = b.targetAmount > 0 && b.spentAmount > b.targetAmount;

          return (
            <div 
              key={b.id}
              className={`rounded-xl border ${b.border} bg-gradient-to-b ${b.gradient} p-5 flex flex-col justify-between relative overflow-hidden transition-all hover:border-slate-600`}
            >
              <div>
                {/* Header */}
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2.5">
                    <div className="w-9 h-9 rounded-lg bg-slate-950/80 border border-slate-800 flex items-center justify-center text-slate-200">
                      <Icon className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="text-sm font-bold text-white">{b.title}</h3>
                      <span className="text-[10px] text-slate-400 font-medium">
                        {b.percentage} {isBM ? 'Peruntukan' : 'Allocation'}
                      </span>
                    </div>
                  </div>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded border ${b.badgeColor}`}>
                    {b.percentage}
                  </span>
                </div>

                {/* Amount figures */}
                <div className="mt-4 flex items-baseline justify-between">
                  <div>
                    <span className="text-2xl font-black text-white">
                      {formatRM(b.spentAmount)}
                    </span>
                    <span className="text-xs text-slate-400 block mt-0.5">
                      {hasIncome ? `${isBM ? 'dari sasaran' : 'of target'} ${formatRM(b.targetAmount)}` : (isBM ? 'Tiada had ditetapkan' : 'Set income to calculate')}
                    </span>
                  </div>
                  {hasIncome && (
                    <div className="text-right">
                      <span className={`text-sm font-extrabold ${isOver ? 'text-rose-400' : 'text-slate-300'}`}>
                        {pct}%
                      </span>
                    </div>
                  )}
                </div>

                {/* Progress bar */}
                <div className="w-full bg-slate-950 h-2.5 rounded-full overflow-hidden mt-3 border border-slate-800">
                  <div 
                    className={`h-full rounded-full transition-all duration-500 ${
                      isOver ? 'bg-rose-500' : b.barColor
                    }`}
                    style={{ width: `${pct}%` }}
                  ></div>
                </div>

                {/* Status Indicator */}
                <div className="mt-2.5 flex items-center justify-between text-[11px]">
                  {hasIncome ? (
                    isOver ? (
                      <span className="flex items-center text-rose-400 font-semibold">
                        <AlertCircle className="w-3.5 h-3.5 mr-1" />
                        {isBM ? 'Lebih had sebanyak ' : 'Over by '} {formatRM(b.spentAmount - b.targetAmount)}
                      </span>
                    ) : (
                      <span className="flex items-center text-emerald-400 font-medium">
                        <CheckCircle2 className="w-3.5 h-3.5 mr-1" />
                        {formatRM(b.targetAmount - b.spentAmount)} {isBM ? 'baki selamat' : 'remaining'}
                      </span>
                    )
                  ) : (
                    <span className="text-slate-500 text-[10px]">
                      {isBM ? 'Masukkan gaji bulanan untuk lihat baki' : 'Input salary to see remaining'}
                    </span>
                  )}
                </div>

                {/* Typical Malaysian Examples */}
                <div className="mt-4 pt-3 border-t border-slate-800/80">
                  <span className="text-[10px] uppercase font-bold text-slate-400 block tracking-wider">
                    {isBM ? 'Contoh Gaya Hidup MY:' : 'MY Lifestyle Items:'}
                  </span>
                  <p className="text-[11px] text-slate-300 mt-1 leading-relaxed">
                    {b.examples}
                  </p>
                </div>
              </div>

              {/* Advice */}
              <div className="mt-4 text-[10px] text-slate-400 italic bg-slate-950/40 p-2 rounded-lg border border-slate-800/60">
                "{b.advice}"
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
