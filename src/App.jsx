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

export default function App() {
  // Theme State
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

  // Language State
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

  // User Profile
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

  // Modal States
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [isBudgetModalOpen, setIsBudgetModalOpen] = useState(false);
  const [isDuitNowOpen, setIsDuitNowOpen] = useState(false);

  // Financial Figures
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
    if (window.confirm(isBM ? 'Kosongkan semua data perbelanjaan?' : 'Clear all transaction data?')) {
      setTransactions([]);
    }
  };

  const handleLoadSampleData = () => {
    setTransactions(SAMPLE_TRANSACTIONS);
    setCategories(DEFAULT_CATEGORIES);
  };

  const handleExportCSV = () => {
    const headers = ['ID', 'Date', 'Description', 'Amount (RM)', 'Category', 'Method', 'Note'];
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
    <div className="min-h-screen bg-zinc-50/50 dark:bg-zinc-950 text-zinc-900 dark:text-zinc-100 pb-20 selection:bg-zinc-900 selection:text-white dark:selection:bg-zinc-100 dark:selection:text-zinc-900 transition-colors duration-200">
      
      {/* Precision Header */}
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

      {/* Main Content Area */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-7 space-y-6">
        
        {/* Executive Summary Metrics */}
        <BudgetSummaryCards
          monthlyBudget={monthlyBudget}
          onUpdateBudget={handleUpdateBudget}
          totalSpent={totalSpent}
          transactionCount={transactions.length}
          language={language}
        />

        {/* Asymmetrical Bento Grid: Half-Circle Chart (Image 1) & DuitNow Quick Scanner */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          
          {/* Main Half-Circle Arch Chart (7 columns) */}
          <div className="lg:col-span-7">
            <HalfCircleSpendingChart
              transactions={transactions}
              categories={categories}
              language={language}
              onOpenAddModal={() => setIsAddModalOpen(true)}
              onLoadSampleData={handleLoadSampleData}
            />
          </div>

          {/* DuitNow Hub & Receipt OCR (5 columns) */}
          <div className="lg:col-span-5">
            <DuitNowHub
              onAddTransaction={handleAddTransaction}
              categories={categories}
              userProfile={userProfile}
              language={language}
            />
          </div>

        </div>

        {/* High-Precision Transaction Ledger */}
        <section>
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

      {/* Standalone DuitNow Modal if opened from Navbar */}
      {isDuitNowOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <DuitNowHub
              onAddTransaction={handleAddTransaction}
              categories={categories}
              userProfile={userProfile}
              language={language}
              onClose={() => setIsDuitNowOpen(false)}
            />
          </div>
        </div>
      )}

    </div>
  );
}
