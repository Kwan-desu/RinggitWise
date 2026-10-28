import React from 'react';
import { LayoutDashboard, PieChart, QrCode, Flame, CreditCard } from 'lucide-react';

export default function MobileNavigation({
  activeTab,
  setActiveTab,
  onOpenDuitNow,
  language
}) {
  const isBM = language === 'bm';

  return (
    <nav className="fixed bottom-0 inset-x-0 z-40 bg-slate-900/95 backdrop-blur-lg border-t border-slate-800 pb-safe">
      <div className="max-w-md mx-auto px-4 h-16 flex items-center justify-around relative">
        
        {/* Dashboard */}
        <button
          onClick={() => setActiveTab('dashboard')}
          className={`flex flex-col items-center justify-center flex-1 h-full py-1 transition-colors ${
            activeTab === 'dashboard' ? 'text-emerald-400' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <LayoutDashboard className="w-5 h-5" />
          <span className="text-[10px] font-semibold mt-1">
            {isBM ? 'Utama' : 'Home'}
          </span>
        </button>

        {/* 3 Buckets */}
        <button
          onClick={() => setActiveTab('buckets')}
          className={`flex flex-col items-center justify-center flex-1 h-full py-1 transition-colors ${
            activeTab === 'buckets' ? 'text-emerald-400' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <PieChart className="w-5 h-5" />
          <span className="text-[10px] font-semibold mt-1">
            {isBM ? '3 Balang' : 'Buckets'}
          </span>
        </button>

        {/* Center Floating DuitNow QR Pay Button */}
        <div className="flex-1 flex justify-center -mt-6">
          <button
            onClick={onOpenDuitNow}
            className="w-14 h-14 rounded-full bg-gradient-to-tr from-red-600 via-rose-500 to-red-500 text-white shadow-xl shadow-red-600/40 border-4 border-slate-950 flex items-center justify-center transition-all active:scale-95 group"
            title="DuitNow QR Scan"
          >
            <QrCode className="w-7 h-7 group-hover:scale-110 transition-transform" />
          </button>
        </div>

        {/* Swipe Review */}
        <button
          onClick={() => setActiveTab('review')}
          className={`flex flex-col items-center justify-center flex-1 h-full py-1 transition-colors ${
            activeTab === 'review' ? 'text-rose-400' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Flame className="w-5 h-5" />
          <span className="text-[10px] font-semibold mt-1">
            {isBM ? 'Leret' : 'Swipe'}
          </span>
        </button>

        {/* Debts */}
        <button
          onClick={() => setActiveTab('debts')}
          className={`flex flex-col items-center justify-center flex-1 h-full py-1 transition-colors ${
            activeTab === 'debts' ? 'text-blue-400' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <CreditCard className="w-5 h-5" />
          <span className="text-[10px] font-semibold mt-1">
            {isBM ? 'Hutang' : 'Debts'}
          </span>
        </button>

      </div>
    </nav>
  );
}
