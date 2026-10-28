import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import SafeToSpendGauge from './components/SafeToSpendGauge';
import ThreeBucketsView from './components/ThreeBucketsView';
import WeeklySwipeReview from './components/WeeklySwipeReview';
import DuitNowHub from './components/DuitNowHub';
import FinancialAuditScore from './components/FinancialAuditScore';
import DebtPayoffTracker from './components/DebtPayoffTracker';
import TransactionLedger from './components/TransactionLedger';
import AddTransactionModal from './components/AddTransactionModal';
import AddDebtModal from './components/AddDebtModal';
import SetIncomeModal from './components/SetIncomeModal';
import MobileNavigation from './components/MobileNavigation';
import MobileFrame from './components/MobileFrame';
import { 
  EMPTY_USER_PROFILE, 
  EMPTY_TRANSACTIONS, 
  EMPTY_DEBTS, 
  EMPTY_SAVINGS_GOALS, 
  EMPTY_SWIPE_QUEUE,
  DEMO_USER_PROFILE,
  DEMO_TRANSACTIONS,
  DEMO_DEBTS,
  DEMO_SAVINGS_GOALS,
  DEMO_SWIPE_QUEUE
} from './data/initialData';
import { formatRM } from './utils/helpers';
import { QrCode, Plus, Sparkles, Wallet, AlertCircle, ArrowRight } from 'lucide-react';

export default function App() {
  // Persistent State Management - Defaults to 100% EMPTY clean state (No pre-inputted mock data)
  const [userProfile, setUserProfile] = useState(() => {
    const saved = localStorage.getItem('rw_profile');
    return saved ? JSON.parse(saved) : EMPTY_USER_PROFILE;
  });

  const [transactions, setTransactions] = useState(() => {
    const saved = localStorage.getItem('rw_transactions');
    return saved ? JSON.parse(saved) : EMPTY_TRANSACTIONS;
  });

  const [debts, setDebts] = useState(() => {
    const saved = localStorage.getItem('rw_debts');
    return saved ? JSON.parse(saved) : EMPTY_DEBTS;
  });

  const [savingsGoals, setSavingsGoals] = useState(() => {
    const saved = localStorage.getItem('rw_savings');
    return saved ? JSON.parse(saved) : EMPTY_SAVINGS_GOALS;
  });

  const [swipeQueue, setSwipeQueue] = useState(() => {
    const saved = localStorage.getItem('rw_swipe_queue');
    return saved ? JSON.parse(saved) : EMPTY_SWIPE_QUEUE;
  });

  // UI Modals & Navigation
  const [viewMode, setViewMode] = useState('auto'); // 'auto', 'pc', 'mobile'
  const [mobileTab, setMobileTab] = useState('dashboard');
  const [language, setLanguage] = useState('en');
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isAddDebtOpen, setIsAddDebtOpen] = useState(false);
  const [isSetIncomeOpen, setIsSetIncomeOpen] = useState(false);
  const [isDuitNowOpen, setIsDuitNowOpen] = useState(false);

  // Sync to LocalStorage
  useEffect(() => {
    localStorage.setItem('rw_profile', JSON.stringify(userProfile));
  }, [userProfile]);

  useEffect(() => {
    localStorage.setItem('rw_transactions', JSON.stringify(transactions));
  }, [transactions]);

  useEffect(() => {
    localStorage.setItem('rw_debts', JSON.stringify(debts));
  }, [debts]);

  useEffect(() => {
    localStorage.setItem('rw_savings', JSON.stringify(savingsGoals));
  }, [savingsGoals]);

  useEffect(() => {
    localStorage.setItem('rw_swipe_queue', JSON.stringify(swipeQueue));
  }, [swipeQueue]);

  // Financial Calculations
  const monthlyIncome = userProfile?.monthlyIncome || 0;
  const hasIncome = monthlyIncome > 0;

  const expenseTransactions = transactions.filter(t => t.bucket !== 'income');
  
  const totalNeedsSpent = expenseTransactions
    .filter(t => t.bucket === 'needs')
    .reduce((acc, t) => acc + t.amount, 0);

  const totalWantsSpent = expenseTransactions
    .filter(t => t.bucket === 'wants')
    .reduce((acc, t) => acc + t.amount, 0);

  const totalSavingsSpent = expenseTransactions
    .filter(t => t.bucket === 'savings')
    .reduce((acc, t) => acc + t.amount, 0);

  // Safe To Spend Calculation (Dollarwise core metric)
  const now = new Date();
  const lastDayOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
  const daysLeftInMonth = Math.max(1, lastDayOfMonth - now.getDate());

  const totalWantsBudget = monthlyIncome * 0.30;
  const remainingWantsPool = Math.max(0, totalWantsBudget - totalWantsSpent);
  
  const safeToSpendDaily = hasIncome ? Math.max(0, remainingWantsPool / daysLeftInMonth) : 0;
  const safeToSpendWeekly = hasIncome ? Math.min(remainingWantsPool, safeToSpendDaily * 7) : 0;

  // Handlers
  const handleAddTransaction = (newTx) => {
    const tx = {
      id: `tx-${Date.now()}`,
      date: newTx.date || new Date().toISOString().split('T')[0],
      ...newTx,
    };
    setTransactions(prev => [tx, ...prev]);

    // Also add to review queue if not already reviewed
    if (!newTx.reviewed) {
      setSwipeQueue(prev => [
        {
          id: `sq-${Date.now()}`,
          title: newTx.title,
          category: newTx.category,
          amount: newTx.amount,
          method: newTx.method,
          date: 'Just now',
          hammerComment: newTx.bucket === 'wants' 
            ? 'Was this really essential to your Malaysian financial freedom?!' 
            : 'Essential living expenditure logged.',
        },
        ...prev
      ]);
    }
  };

  const handleDeleteTransaction = (id) => {
    setTransactions(prev => prev.filter(t => t.id !== id));
  };

  const handleAddDebt = (newDebt) => {
    setDebts(prev => [newDebt, ...prev]);
  };

  const handleSaveProfile = (newProfile) => {
    setUserProfile(prev => ({ ...prev, ...newProfile }));
  };

  const handleResolveSwipeItem = (id, bucket, isShame) => {
    const item = swipeQueue.find(i => i.id === id);
    if (item) {
      const newTx = {
        title: item.title,
        amount: item.amount,
        bucket: bucket,
        category: item.category,
        method: item.method,
        note: item.hammerComment,
        date: new Date().toISOString().split('T')[0],
        reviewed: true,
        shameFlag: isShame,
      };
      handleAddTransaction(newTx);
      setSwipeQueue(prev => prev.filter(i => i.id !== id));
    }
  };

  const handleClearAllData = () => {
    if (window.confirm(language === 'bm' ? 'Kosongkan semua data dan mula dari awal?' : 'Clear all data and start completely fresh?')) {
      setUserProfile(EMPTY_USER_PROFILE);
      setTransactions(EMPTY_TRANSACTIONS);
      setDebts(EMPTY_DEBTS);
      setSavingsGoals(EMPTY_SAVINGS_GOALS);
      setSwipeQueue(EMPTY_SWIPE_QUEUE);
      localStorage.clear();
    }
  };

  const handleLoadDemoData = () => {
    setUserProfile(DEMO_USER_PROFILE);
    setTransactions(DEMO_TRANSACTIONS);
    setDebts(DEMO_DEBTS);
    setSavingsGoals(DEMO_SAVINGS_GOALS);
    setSwipeQueue(DEMO_SWIPE_QUEUE);
  };

  const handleExportCSV = () => {
    const headers = ['ID', 'Date', 'Merchant / Title', 'Amount (RM)', 'Bucket', 'Category', 'Method', 'Note'];
    const rows = transactions.map(t => [
      t.id,
      t.date,
      `"${t.title.replace(/"/g, '""')}"`,
      t.amount,
      t.bucket,
      `"${t.category}"`,
      `"${t.method}"`,
      `"${(t.note || '').replace(/"/g, '""')}"`
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(e => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `RinggitWise_Transactions_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // Mobile Simulator Content
  const renderMobileContent = () => {
    return (
      <div className="space-y-4 p-4">
        {/* Quick Header in Mobile */}
        <div className="flex items-center justify-between pb-2 border-b border-slate-800">
          <div>
            <span className="text-[11px] text-slate-400">
              {language === 'bm' ? 'Selamat Belanja Hari Ini' : 'Safe to Spend Today'}
            </span>
            <div className="text-2xl font-black text-emerald-400">
              {formatRM(safeToSpendDaily)}
            </div>
          </div>
          <button
            onClick={() => setIsAddModalOpen(true)}
            className="w-9 h-9 rounded-full bg-emerald-600 text-white flex items-center justify-center shadow-lg shadow-emerald-600/30"
          >
            <Plus className="w-5 h-5" />
          </button>
        </div>

        {mobileTab === 'dashboard' && (
          <div className="space-y-4">
            <SafeToSpendGauge
              monthlyIncome={monthlyIncome}
              totalNeedsSpent={totalNeedsSpent}
              totalWantsSpent={totalWantsSpent}
              totalSavingsSpent={totalSavingsSpent}
              safeToSpendDaily={safeToSpendDaily}
              safeToSpendWeekly={safeToSpendWeekly}
              daysLeftInMonth={daysLeftInMonth}
              onOpenSetIncome={() => setIsSetIncomeOpen(true)}
              language={language}
            />

            <ThreeBucketsView
              monthlyIncome={monthlyIncome}
              totalNeedsSpent={totalNeedsSpent}
              totalWantsSpent={totalWantsSpent}
              totalSavingsSpent={totalSavingsSpent}
              onOpenSetIncome={() => setIsSetIncomeOpen(true)}
              language={language}
            />

            <FinancialAuditScore
              monthlyIncome={monthlyIncome}
              totalNeedsSpent={totalNeedsSpent}
              totalWantsSpent={totalWantsSpent}
              debts={debts}
              savingsGoals={savingsGoals}
              onOpenAddModal={() => setIsAddModalOpen(true)}
              language={language}
            />

            <TransactionLedger
              transactions={transactions}
              onDeleteTransaction={handleDeleteTransaction}
              onExportCSV={handleExportCSV}
              onOpenAddModal={() => setIsAddModalOpen(true)}
              onOpenDuitNow={() => setIsDuitNowOpen(true)}
              language={language}
            />
          </div>
        )}

        {mobileTab === 'buckets' && (
          <div className="space-y-4">
            <ThreeBucketsView
              monthlyIncome={monthlyIncome}
              totalNeedsSpent={totalNeedsSpent}
              totalWantsSpent={totalWantsSpent}
              totalSavingsSpent={totalSavingsSpent}
              onOpenSetIncome={() => setIsSetIncomeOpen(true)}
              language={language}
            />
            <SafeToSpendGauge
              monthlyIncome={monthlyIncome}
              totalNeedsSpent={totalNeedsSpent}
              totalWantsSpent={totalWantsSpent}
              totalSavingsSpent={totalSavingsSpent}
              safeToSpendDaily={safeToSpendDaily}
              safeToSpendWeekly={safeToSpendWeekly}
              daysLeftInMonth={daysLeftInMonth}
              onOpenSetIncome={() => setIsSetIncomeOpen(true)}
              language={language}
            />
          </div>
        )}

        {mobileTab === 'review' && (
          <WeeklySwipeReview
            queue={swipeQueue}
            onResolveItem={handleResolveSwipeItem}
            onResetQueue={() => setSwipeQueue([])}
            onLoadDemoQueue={() => setSwipeQueue(DEMO_SWIPE_QUEUE)}
            language={language}
          />
        )}

        {mobileTab === 'debts' && (
          <DebtPayoffTracker
            debts={debts}
            onUpdateDebts={setDebts}
            onOpenAddDebt={() => setIsAddDebtOpen(true)}
            language={language}
          />
        )}

        <MobileNavigation
          activeTab={mobileTab}
          setActiveTab={setMobileTab}
          onOpenDuitNow={() => setIsDuitNowOpen(true)}
          language={language}
        />
      </div>
    );
  };

  // If Mobile Simulator Frame View is selected on PC
  if (viewMode === 'mobile') {
    return (
      <div className="min-h-screen bg-slate-950">
        <Navbar
          userProfile={userProfile}
          safeToSpendDaily={safeToSpendDaily}
          viewMode={viewMode}
          setViewMode={setViewMode}
          language={language}
          setLanguage={setLanguage}
          onOpenAddModal={() => setIsAddModalOpen(true)}
          onOpenSetIncome={() => setIsSetIncomeOpen(true)}
          onClearAllData={handleClearAllData}
          onLoadDemoData={handleLoadDemoData}
          onOpenDuitNow={() => setIsDuitNowOpen(true)}
        />
        <MobileFrame onExitMobileView={() => setViewMode('pc')} language={language}>
          {renderMobileContent()}
        </MobileFrame>

        {/* Modals */}
        <AddTransactionModal
          isOpen={isAddModalOpen}
          onClose={() => setIsAddModalOpen(false)}
          onAddTransaction={handleAddTransaction}
          language={language}
        />

        <AddDebtModal
          isOpen={isAddDebtOpen}
          onClose={() => setIsAddDebtOpen(false)}
          onAddDebt={handleAddDebt}
          language={language}
        />

        <SetIncomeModal
          isOpen={isSetIncomeOpen}
          onClose={() => setIsSetIncomeOpen(false)}
          currentIncome={userProfile?.monthlyIncome || 0}
          currentName={userProfile?.name || ''}
          onSaveProfile={handleSaveProfile}
          language={language}
        />

        {isDuitNowOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md">
            <div className="w-full max-w-3xl max-h-[90vh] overflow-y-auto">
              <div className="relative">
                <DuitNowHub
                  onAddTransaction={handleAddTransaction}
                  userProfile={userProfile}
                  language={language}
                  onClose={() => setIsDuitNowOpen(false)}
                />
                <button
                  onClick={() => setIsDuitNowOpen(false)}
                  className="absolute top-4 right-4 text-slate-400 hover:text-white p-2 rounded-lg bg-slate-900 border border-slate-800"
                >
                  ✕
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  }

  // PC and Responsive Desktop Layout
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 pb-20 selection:bg-emerald-500 selection:text-white">
      {/* Top Navigation */}
      <Navbar
        userProfile={userProfile}
        safeToSpendDaily={safeToSpendDaily}
        viewMode={viewMode}
        setViewMode={setViewMode}
        language={language}
        setLanguage={setLanguage}
        onOpenAddModal={() => setIsAddModalOpen(true)}
        onOpenSetIncome={() => setIsSetIncomeOpen(true)}
        onClearAllData={handleClearAllData}
        onLoadDemoData={handleLoadDemoData}
        onOpenDuitNow={() => setIsDuitNowOpen(true)}
      />

      {/* Main Container */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
        
        {/* First-time Onboarding Alert if user has no income input yet */}
        {!hasIncome && (
          <div className="bg-gradient-to-r from-emerald-950/60 via-slate-900 to-slate-900 border-2 border-emerald-500/50 rounded-2xl p-5 sm:p-6 shadow-2xl flex flex-col md:flex-row items-center justify-between gap-4">
            <div className="flex items-center space-x-4">
              <div className="w-12 h-12 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0">
                <Wallet className="w-6 h-6" />
              </div>
              <div>
                <h2 className="text-lg font-bold text-white tracking-tight">
                  {language === 'bm' ? 'Selamat Datang ke RinggitWise! Mula dengan menetapkan gaji bulanan anda.' : 'Welcome to RinggitWise! Start by entering your take-home pay.'}
                </h2>
                <p className="text-xs text-slate-300 mt-0.5 leading-relaxed">
                  {language === 'bm'
                    ? 'Semua data bersih dan selamat disimpan di dalam pelayar anda tanpa sebarang maklumat palsu.'
                    : 'Your app starts completely empty with zero mock data. Input your monthly salary to activate the 50/30/20 buckets.'}
                </p>
              </div>
            </div>

            <button
              onClick={() => setIsSetIncomeOpen(true)}
              className="w-full md:w-auto shrink-0 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs px-5 py-3 rounded-xl shadow-lg shadow-emerald-600/30 flex items-center justify-center space-x-2 transition-all active:scale-95"
            >
              <span>{language === 'bm' ? 'Tetapkan Gaji Bersih' : 'Enter Salary (RM)'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        )}

        {/* Top Hero Banner */}
        <div className="bg-gradient-to-r from-slate-900 via-slate-900 to-emerald-950/40 border border-slate-800 rounded-2xl p-5 sm:p-6 shadow-xl flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2">
              <span className="text-xs font-bold uppercase tracking-wider text-emerald-400 bg-emerald-950 px-2 py-0.5 rounded border border-emerald-800/60">
                🇲🇾 Malaysian Personal Finance App
              </span>
              <span className="text-xs text-slate-400 hidden sm:inline">
                • Dollarwise 3-Bucket Framework
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight mt-1">
              Where Your Ringgit <span className="bg-gradient-to-r from-emerald-400 to-teal-300 bg-clip-text text-transparent">Actually Goes</span>.
            </h1>
            <p className="text-xs sm:text-sm text-slate-300 mt-1 max-w-2xl leading-relaxed">
              {language === 'bm'
                ? 'Kawal kewangan bulanan anda dalam Ringgit Malaysia (RM). Pantau sewa, bil TNB, perbelanjaan mamak, dan bayar hutang PTPTN dengan pantas.'
                : 'No more guessing. Real-time daily Safe to Spend allowance, weekly swipe reviews, DuitNow QR payments, and debt payoff for Malaysia.'}
            </p>
          </div>

          <div className="flex items-center space-x-3 shrink-0">
            <button
              onClick={() => setIsDuitNowOpen(true)}
              className="flex items-center space-x-2 bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-500 hover:to-rose-500 text-white font-bold text-xs sm:text-sm px-4 py-2.5 rounded-xl shadow-lg shadow-red-600/30 transition-all active:scale-95"
            >
              <QrCode className="w-4 h-4" />
              <span>DuitNow QR Hub</span>
            </button>
            <button
              onClick={() => setIsAddModalOpen(true)}
              className="flex items-center space-x-2 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs sm:text-sm px-4 py-2.5 rounded-xl shadow-lg shadow-emerald-600/30 transition-all active:scale-95"
            >
              <Plus className="w-4 h-4" />
              <span>{language === 'bm' ? 'Tambah Transaksi' : 'Log Expense'}</span>
            </button>
          </div>
        </div>

        {/* Safe To Spend Metric & Caleb Hammer Financial Audit Score */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          <div className="lg:col-span-7">
            <SafeToSpendGauge
              monthlyIncome={monthlyIncome}
              totalNeedsSpent={totalNeedsSpent}
              totalWantsSpent={totalWantsSpent}
              totalSavingsSpent={totalSavingsSpent}
              safeToSpendDaily={safeToSpendDaily}
              safeToSpendWeekly={safeToSpendWeekly}
              daysLeftInMonth={daysLeftInMonth}
              onOpenSetIncome={() => setIsSetIncomeOpen(true)}
              language={language}
            />
          </div>
          <div className="lg:col-span-5">
            <FinancialAuditScore
              monthlyIncome={monthlyIncome}
              totalNeedsSpent={totalNeedsSpent}
              totalWantsSpent={totalWantsSpent}
              debts={debts}
              savingsGoals={savingsGoals}
              onOpenAddModal={() => setIsAddModalOpen(true)}
              language={language}
            />
          </div>
        </div>

        {/* The Core 3-Bucket System (50/30/20) */}
        <section>
          <ThreeBucketsView
            monthlyIncome={monthlyIncome}
            totalNeedsSpent={totalNeedsSpent}
            totalWantsSpent={totalWantsSpent}
            totalSavingsSpent={totalSavingsSpent}
            onOpenSetIncome={() => setIsSetIncomeOpen(true)}
            language={language}
          />
        </section>

        {/* DuitNow Malaysian Payment Hub */}
        <section id="duitnow-section">
          <DuitNowHub
            onAddTransaction={handleAddTransaction}
            userProfile={userProfile}
            language={language}
          />
        </section>

        {/* Caleb Hammer Weekly Swipe Review & Debt Payoff Tracker */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          <div className="lg:col-span-6">
            <WeeklySwipeReview
              queue={swipeQueue}
              onResolveItem={handleResolveSwipeItem}
              onResetQueue={() => setSwipeQueue([])}
              onLoadDemoQueue={() => setSwipeQueue(DEMO_SWIPE_QUEUE)}
              language={language}
            />
          </div>
          <div className="lg:col-span-6">
            <DebtPayoffTracker
              debts={debts}
              onUpdateDebts={setDebts}
              onOpenAddDebt={() => setIsAddDebtOpen(true)}
              language={language}
            />
          </div>
        </div>

        {/* Comprehensive Transaction Ledger */}
        <section>
          <TransactionLedger
            transactions={transactions}
            onDeleteTransaction={handleDeleteTransaction}
            onExportCSV={handleExportCSV}
            onOpenAddModal={() => setIsAddModalOpen(true)}
            onOpenDuitNow={() => setIsDuitNowOpen(true)}
            language={language}
          />
        </section>

      </main>

      {/* Footer */}
      <footer className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-12 pt-6 border-t border-slate-800/80 text-center text-xs text-slate-500">
        <p>
          RinggitWise Malaysia • Built for Malaysian Lifestyle (DuitNow QR, TNG eWallet, MAE, Bank Negara Rounding)
        </p>
        <p className="mt-1">
          Reverse-engineered concept inspired by Dollarwise & Caleb Hammer's 3-Bucket Budgeting.
        </p>
      </footer>

      {/* Modals */}
      <AddTransactionModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onAddTransaction={handleAddTransaction}
        language={language}
      />

      <AddDebtModal
        isOpen={isAddDebtOpen}
        onClose={() => setIsAddDebtOpen(false)}
        onAddDebt={handleAddDebt}
        language={language}
      />

      <SetIncomeModal
        isOpen={isSetIncomeOpen}
        onClose={() => setIsSetIncomeOpen(false)}
        currentIncome={userProfile?.monthlyIncome || 0}
        currentName={userProfile?.name || ''}
        onSaveProfile={handleSaveProfile}
        language={language}
      />

      {isDuitNowOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md">
          <div className="w-full max-w-3xl max-h-[90vh] overflow-y-auto">
            <div className="relative">
              <DuitNowHub
                onAddTransaction={handleAddTransaction}
                userProfile={userProfile}
                language={language}
                onClose={() => setIsDuitNowOpen(false)}
              />
              <button
                onClick={() => setIsDuitNowOpen(false)}
                className="absolute top-4 right-4 text-slate-400 hover:text-white p-2 rounded-lg bg-slate-900 border border-slate-800"
              >
                ✕
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Floating Mobile Bottom Navigation */}
      <div className="md:hidden">
        <MobileNavigation
          activeTab={mobileTab}
          setActiveTab={(tab) => {
            setMobileTab(tab);
            if (tab === 'buckets') {
              window.scrollTo({ top: 380, behavior: 'smooth' });
            } else if (tab === 'review') {
              window.scrollTo({ top: 1200, behavior: 'smooth' });
            } else if (tab === 'debts') {
              window.scrollTo({ top: 1800, behavior: 'smooth' });
            } else {
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }
          }}
          onOpenDuitNow={() => setIsDuitNowOpen(true)}
          language={language}
        />
      </div>

    </div>
  );
}
