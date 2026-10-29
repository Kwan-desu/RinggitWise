import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import HalfCircleSpendingChart from './components/HalfCircleSpendingChart';
import BudgetSummaryCards from './components/BudgetSummaryCards';
import TransactionLedger from './components/TransactionLedger';
import DuitNowHub from './components/DuitNowHub';
import AddTransactionModal from './components/AddTransactionModal';
import CategoryManagerModal from './components/CategoryManagerModal';
import SetBudgetModal from './components/SetBudgetModal';
import { 
  DEFAULT_CATEGORIES, 
  INITIAL_USER_PROFILE, 
  SAMPLE_TRANSACTIONS 
} from './data/initialData';
import { formatRM } from './utils/helpers';
import { QrCode, Plus, Sparkles, FolderPlus, Wallet, ShieldCheck, ArrowRight } from 'lucide-react';

export default function App() {
  // Theme State (Dark / Light mode)
  const [theme, setTheme] = useState(() => {
    const saved = localStorage.getItem('rw_theme');
    if (saved) return saved;
    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  });

  useEffect(() => {
    localStorage.setItem('rw_theme', theme);
    const root = document.documentElement;
    if (theme === 'dark') {
      root.classList.add('dark');
      root.classList.remove('light');
    } else {
      root.classList.remove('dark');
      root.classList.add('light');
    }
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prev => (prev === 'dark' ? 'light' : 'dark'));
  };

  // Language State (en / bm)
  const [language, setLanguage] = useState(() => {
    return localStorage.getItem('rw_language') || 'en';
  });

  useEffect(() => {
    localStorage.setItem('rw_language', language);
  }, [language]);

  // Categories State
  const [categories, setCategories] = useState(() => {
    const saved = localStorage.getItem('rw_categories');
    return saved ? JSON.parse(saved) : DEFAULT_CATEGORIES;
  });

  useEffect(() => {
    localStorage.setItem('rw_categories', JSON.stringify(categories));
  }, [categories]);

  // User Profile & Budget State
  const [userProfile, setUserProfile] = useState(() => {
    const saved = localStorage.getItem('rw_profile');
    return saved ? JSON.parse(saved) : INITIAL_USER_PROFILE;
  });

  useEffect(() => {
    localStorage.setItem('rw_profile', JSON.stringify(userProfile));
  }, [userProfile]);

  // Transactions State
  const [transactions, setTransactions] = useState(() => {
    const saved = localStorage.getItem('rw_transactions');
    return saved ? JSON.parse(saved) : SAMPLE_TRANSACTIONS;
  });

  useEffect(() => {
    localStorage.setItem('rw_transactions', JSON.stringify(transactions));
  }, [transactions]);

  // Modals
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [isBudgetModalOpen, setIsBudgetModalOpen] = useState(false);
  const [isDuitNowOpen, setIsDuitNowOpen] = useState(false);

  // Financial Calculations
  const totalSpent = transactions.reduce((acc, t) => acc + Number(t.amount || 0), 0);
  const monthlyBudget = userProfile?.monthlyBudget || 3500;

  // Handlers
  const handleAddTransaction = (newTx) => {
    const tx = {
      id: `tx-${Date.now()}`,
      date: newTx.date || new Date().toISOString().split('T')[0],
      ...newTx,
    };
    setTransactions(prev => [tx, ...prev]);
  };

  const handleDeleteTransaction = (id) => {
    setTransactions(prev => prev.filter(t => t.id !== id));
  };

  const handleQuickAddCategory = (newCat) => {
    const fullCat = {
      id: `cat-${Date.now()}`,
      ...newCat,
    };
    setCategories(prev => [...prev, fullCat]);
    return fullCat;
  };

  const handleUpdateBudget = (newBudget) => {
    setUserProfile(prev => ({ ...prev, monthlyBudget: newBudget }));
  };

  const handleClearAllData = () => {
    const isBM = language === 'bm';
    if (window.confirm(isBM ? 'Kosongkan semua rekod perbelanjaan dan mula dari awal?' : 'Clear all transactions and start fresh?')) {
      setTransactions([]);
    }
  };

  const handleLoadSampleData = () => {
    setTransactions(SAMPLE_TRANSACTIONS);
    setCategories(DEFAULT_CATEGORIES);
  };

  const handleExportCSV = () => {
    const headers = ['ID', 'Date', 'Merchant / Title', 'Amount (RM)', 'Category', 'Method', 'Note'];
    const rows = transactions.map(t => [
      t.id,
      t.date,
      `"${t.title.replace(/"/g, '""')}"`,
      t.amount,
      `"${t.category}"`,
      `"${t.method}"`,
      `"${(t.note || '').replace(/"/g, '""')}"`
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(e => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `RinggitWise_Expenses_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 pb-20 selection:bg-emerald-500 selection:text-white transition-colors duration-200">
      
      {/* Top Navbar */}
      <Navbar
        userProfile={userProfile}
        theme={theme}
        onToggleTheme={toggleTheme}
        language={language}
        setLanguage={setLanguage}
        onOpenAddModal={() => setIsAddModalOpen(true)}
        onOpenCategoryManager={() => setIsCategoryModalOpen(true)}
        onOpenBudgetModal={() => setIsBudgetModalOpen(true)}
        onClearAllData={handleClearAllData}
        onLoadSampleData={handleLoadSampleData}
        onOpenDuitNow={() => setIsDuitNowOpen(true)}
      />

      {/* Main Container */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
        
        {/* Hero Banner with Quick Actions */}
        <div className="bg-gradient-to-r from-white via-white to-emerald-50/50 dark:from-slate-900 dark:via-slate-900 dark:to-emerald-950/30 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 sm:p-6 shadow-sm flex flex-col md:flex-row md:items-center md:justify-between gap-4 transition-colors">
          <div>
            <div className="flex items-center space-x-2">
              <span className="text-xs font-bold uppercase tracking-wider text-emerald-700 dark:text-emerald-400 bg-emerald-100 dark:bg-emerald-950 px-2 py-0.5 rounded border border-emerald-300 dark:border-emerald-800/60">
                🇲🇾 Malaysian Ringgit (RM)
              </span>
              <span className="text-xs text-slate-500 dark:text-slate-400">
                • DuitNow QR & AI OCR
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-slate-900 dark:text-white tracking-tight mt-1.5">
              Personal Finance & <span className="bg-gradient-to-r from-emerald-500 to-teal-500 bg-clip-text text-transparent">Spending Insights</span>
            </h1>
            <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-400 mt-1 max-w-2xl leading-relaxed">
              {language === 'bm'
                ? 'Pantau perbelanjaan harian dalam Ringgit Malaysia (RM), imbas resit DuitNow secara pintar dengan OCR, dan lihat pecahan perbelanjaan anda dalam carta separuh bulatan.'
                : 'Track your daily Ringgit expenses, scan DuitNow receipts with client-side AI OCR, and visualize your spending breakdown with an interactive half-circle arch chart.'}
            </p>
          </div>

          <div className="flex items-center space-x-2.5 shrink-0">
            <button
              onClick={() => setIsDuitNowOpen(true)}
              className="flex items-center space-x-2 bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-500 hover:to-rose-500 text-white font-bold text-xs sm:text-sm px-4 py-2.5 rounded-xl shadow-md shadow-red-600/25 transition-all active:scale-95"
            >
              <QrCode className="w-4 h-4" />
              <span>DuitNow OCR Hub</span>
            </button>
            <button
              onClick={() => setIsAddModalOpen(true)}
              className="flex items-center space-x-2 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs sm:text-sm px-4 py-2.5 rounded-xl shadow-md shadow-emerald-600/25 transition-all active:scale-95"
            >
              <Plus className="w-4 h-4" />
              <span>{language === 'bm' ? 'Rekod RM' : 'Log Expense (RM)'}</span>
            </button>
          </div>
        </div>

        {/* Budget Summary Cards */}
        <BudgetSummaryCards
          monthlyBudget={monthlyBudget}
          onUpdateBudget={handleUpdateBudget}
          totalSpent={totalSpent}
          transactionCount={transactions.length}
          language={language}
        />

        {/* Half-Circle Spending Summary Chart (Image 1 Centerpiece) */}
        <section id="spending-chart-section">
          <HalfCircleSpendingChart
            transactions={transactions}
            categories={categories}
            language={language}
            onOpenAddModal={() => setIsAddModalOpen(true)}
            onLoadSampleData={handleLoadSampleData}
          />
        </section>

        {/* DuitNow QR & Receipt OCR Hub */}
        <section id="duitnow-hub-section">
          <DuitNowHub
            onAddTransaction={handleAddTransaction}
            categories={categories}
            userProfile={userProfile}
            language={language}
          />
        </section>

        {/* Transactions Ledger */}
        <section id="ledger-section">
          <TransactionLedger
            transactions={transactions}
            categories={categories}
            onDeleteTransaction={handleDeleteTransaction}
            onExportCSV={handleExportCSV}
            onOpenAddModal={() => setIsAddModalOpen(true)}
            language={language}
          />
        </section>

      </main>

      {/* Modals */}
      <AddTransactionModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onAddTransaction={handleAddTransaction}
        categories={categories}
        onQuickAddCategory={handleQuickAddCategory}
        language={language}
      />

      <CategoryManagerModal
        isOpen={isCategoryModalOpen}
        onClose={() => setIsCategoryModalOpen(false)}
        categories={categories}
        onUpdateCategories={setCategories}
        language={language}
      />

      <SetBudgetModal
        isOpen={isBudgetModalOpen}
        onClose={() => setIsBudgetModalOpen(false)}
        currentBudget={monthlyBudget}
        currentName={userProfile?.name || 'User'}
        onSaveBudget={(data) => {
          setUserProfile(prev => ({ ...prev, ...data }));
        }}
        language={language}
      />

      {isDuitNowOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-3xl max-h-[92vh] overflow-y-auto">
            <div className="relative">
              <DuitNowHub
                onAddTransaction={handleAddTransaction}
                categories={categories}
                userProfile={userProfile}
                language={language}
                onClose={() => setIsDuitNowOpen(false)}
              />
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
