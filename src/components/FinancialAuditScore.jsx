import React from 'react';
import { ShieldCheck, AlertOctagon, TrendingUp, CheckCircle2, Award, Zap } from 'lucide-react';
import { formatRM } from '../utils/helpers';

export default function FinancialAuditScore({
  monthlyIncome,
  totalNeedsSpent,
  totalWantsSpent,
  debts,
  savingsGoals,
  language
}) {
  const isBM = language === 'bm';

  // Calculate Emergency Fund
  const emergencyGoal = savingsGoals.find(g => g.id === 'sav-1') || savingsGoals[0];
  const emergencyAmount = emergencyGoal ? emergencyGoal.current : 0;
  const estimatedMonthlyLivingCosts = Math.max(1500, totalNeedsSpent * 1.2);
  const emergencyMonths = (emergencyAmount / estimatedMonthlyLivingCosts).toFixed(1);

  // Total Debt calculation
  const totalDebtBalance = debts.reduce((acc, d) => acc + d.remainingAmount, 0);
  const totalMonthlyDebtRepay = debts.reduce((acc, d) => acc + d.currentPayment, 0);
  const debtToIncomePct = Math.round((totalMonthlyDebtRepay / (monthlyIncome || 1)) * 100);

  // Score algorithm (0-100)
  let score = 50;

  // Emergency fund component (+/- 25)
  if (emergencyMonths >= 6) score += 25;
  else if (emergencyMonths >= 3) score += 15;
  else if (emergencyMonths >= 1) score += 5;
  else score -= 15;

  // Debt to income component (+/- 20)
  if (debtToIncomePct < 15) score += 20;
  else if (debtToIncomePct <= 30) score += 10;
  else if (debtToIncomePct <= 45) score -= 5;
  else score -= 20;

  // Wants ratio component (+/- 15)
  const wantsRatio = (totalWantsSpent / (monthlyIncome || 1)) * 100;
  if (wantsRatio <= 30) score += 15;
  else if (wantsRatio <= 40) score += 5;
  else score -= 15;

  // Clamp 0 to 100
  const finalScore = Math.min(100, Math.max(10, score));

  let grade = 'B';
  let gradeColor = 'text-amber-400';
  let verdictTitle = isBM ? 'Sederhana • Ada Risiko' : 'Fair • Room for Discipline';
  let roast = isBM
    ? 'Anda masih hidup tapi bayaran Myvi dan kad kredit sedang menghakis simpanan masa depan anda!'
    : 'You are floating, but car loan and credit card debt are devouring your wealth creation!';

  if (finalScore >= 80) {
    grade = 'A';
    gradeColor = 'text-emerald-400';
    verdictTitle = isBM ? 'Cemerlang • Kebal Kewangan' : 'Hammer Approved • Financially Resilient';
    roast = isBM 
      ? 'Hebat! Dana kecemasan kukuh dan tiada hutang toksik bernilai tinggi. Teruskan!' 
      : 'Caleb Hammer smiles: Solid emergency reserves, controlled wants, no toxic debt!';
  } else if (finalScore < 60) {
    grade = 'D';
    gradeColor = 'text-rose-400';
    verdictTitle = isBM ? 'BAHAYA • Sesi Audit Diperlukan' : 'CRITICAL • Financial Audit Emergency!';
    roast = isBM
      ? 'Aiyo! Dana kecemasan tak cukup 3 bulan dan anda masih belanja di cafe mahal dan SPayLater?! Berhenti!'
      : 'HAMMER ALARM! You are one minor car breakdown away from debt disaster. Cut the wants to ZERO!';
  }

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 sm:p-6 shadow-xl">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-slate-800 pb-4 mb-5">
        <div>
          <div className="flex items-center space-x-2">
            <Award className="w-5 h-5 text-amber-400" />
            <h2 className="text-xl font-bold text-white tracking-tight">
              {isBM ? 'Skor Kesihatan Kewangan (Financial Audit Score)' : 'Ringgit Health Audit Score'}
            </h2>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            {isBM 
              ? 'Penilaian ketat gaya Caleb Hammer berdasarkan dana kecemasan, beban hutang dan tabiat belanja.'
              : 'Ruthless Caleb Hammer-style audit score based on cash reserves, DTI and impulse wants.'}
          </p>
        </div>

        {/* Grade Badge */}
        <div className="flex items-center space-x-3 bg-slate-950 px-4 py-2 rounded-xl border border-slate-800 self-start sm:self-auto">
          <span className="text-xs text-slate-400 font-semibold">{isBM ? 'Gred:' : 'Grade:'}</span>
          <span className={`text-2xl font-black ${gradeColor}`}>{grade}</span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
        
        {/* Main Score Dial / Number */}
        <div className="lg:col-span-4 bg-slate-950 border border-slate-800 rounded-2xl p-6 text-center">
          <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block">
            {isBM ? 'Skor Audit Keseluruhan' : 'Overall Health Score'}
          </span>
          <div className="text-5xl font-black text-white mt-2 tracking-tight">
            {finalScore}<span className="text-xl text-slate-500 font-normal">/100</span>
          </div>

          <div className={`mt-3 inline-block px-3 py-1 rounded-full text-xs font-bold border ${
            finalScore >= 80 
              ? 'bg-emerald-950 text-emerald-400 border-emerald-800' 
              : finalScore >= 60 
              ? 'bg-amber-950 text-amber-400 border-amber-800' 
              : 'bg-rose-950 text-rose-400 border-rose-800'
          }`}>
            {verdictTitle}
          </div>

          <p className="text-xs text-slate-400 mt-4 italic bg-slate-900/80 p-3 rounded-xl border border-slate-800">
            "{roast}"
          </p>
        </div>

        {/* Audit Pillars Breakdown */}
        <div className="lg:col-span-8 space-y-4">
          
          {/* Pillar 1: Emergency Fund */}
          <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-4">
            <div className="flex justify-between items-center mb-1.5">
              <span className="text-xs font-bold text-slate-200">
                1. {isBM ? 'Dana Kecemasan (Emergency Fund)' : 'Emergency Fund Reserves'}
              </span>
              <span className="text-xs font-extrabold text-emerald-400">
                {emergencyMonths} / 6.0 {isBM ? 'Bulan' : 'Months'}
              </span>
            </div>
            <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
              <div 
                className="bg-emerald-500 h-full rounded-full transition-all duration-500"
                style={{ width: `${Math.min(100, (emergencyMonths / 6) * 100)}%` }}
              ></div>
            </div>
            <span className="text-[10px] text-slate-400 mt-1.5 block">
              {formatRM(emergencyAmount)} {isBM ? 'tersedia dalam ASB / Simpanan Tetap' : 'liquid cash in ASB / Versa'}
            </span>
          </div>

          {/* Pillar 2: Debt to Income (DTI) */}
          <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-4">
            <div className="flex justify-between items-center mb-1.5">
              <span className="text-xs font-bold text-slate-200">
                2. {isBM ? 'Nisbah Hutang Berbanding Pendapatan (DTI)' : 'Debt-to-Income (DTI) Ratio'}
              </span>
              <span className={`text-xs font-extrabold ${debtToIncomePct > 35 ? 'text-rose-400' : 'text-slate-300'}`}>
                {debtToIncomePct}% {isBM ? 'dari gaji' : 'of monthly pay'}
              </span>
            </div>
            <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
              <div 
                className={`h-full rounded-full transition-all duration-500 ${debtToIncomePct > 35 ? 'bg-rose-500' : 'bg-blue-500'}`}
                style={{ width: `${Math.min(100, debtToIncomePct * 2)}%` }}
              ></div>
            </div>
            <span className="text-[10px] text-slate-400 mt-1.5 block">
              {formatRM(totalMonthlyDebtRepay)}/bulan ({isBM ? 'Baki keseluruhan hutang:' : 'Total debt principal:'} {formatRM(totalDebtBalance)})
            </span>
          </div>

          {/* Pillar 3: Wants Discipline */}
          <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-4">
            <div className="flex justify-between items-center mb-1.5">
              <span className="text-xs font-bold text-slate-200">
                3. {isBM ? 'Disiplin Kehendak (Wants Discipline)' : 'Wants Discipline (< 30%)'}
              </span>
              <span className={`text-xs font-extrabold ${wantsRatio > 30 ? 'text-amber-400' : 'text-emerald-400'}`}>
                {wantsRatio.toFixed(1)}% {isBM ? 'digunakan' : 'used'}
              </span>
            </div>
            <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
              <div 
                className={`h-full rounded-full transition-all duration-500 ${wantsRatio > 30 ? 'bg-amber-500' : 'bg-emerald-500'}`}
                style={{ width: `${Math.min(100, (wantsRatio / 30) * 100)}%` }}
              ></div>
            </div>
            <span className="text-[10px] text-slate-400 mt-1.5 block">
              {isBM ? 'Mengekalkan gaya hidup tanpa membazir pada boba & Shopee 3 pagi.' : 'Strict limit on entertainment, dining out & impulse orders.'}
            </span>
          </div>

        </div>

      </div>
    </div>
  );
}
