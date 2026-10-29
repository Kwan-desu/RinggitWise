import React, { useState, useEffect, useMemo } from 'react';
import Navbar from './components/Navbar';
import HalfCircleSpendingChart from './components/HalfCircleSpendingChart';
import BudgetSummaryCards from './components/BudgetSummaryCards';
import TransactionLedger from './components/TransactionLedger';
import DuitNowHub from './components/DuitNowHub';
import AddTransactionModal from './components/AddTransactionModal';
import CategoryManagerModal from './components/CategoryManagerModal';
import SetBudgetModal from './components/SetBudgetModal';
import PastSpendingModal from './components/PastSpendingModal';
import { 
  DEFAULT_CATEGORIES, 
  INITIAL_USER_PROFILE 
} from './data/initialData';
import { getPreviousMonthStr, getNextMonthStr } from './utils/helpers';

export default function App() {
  // Theme State
  const [theme, setTheme] = useState(() => {
    const saved = localStorage.getItem('rw_v2_theme');
    if (saved) return saved;
    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  });

  useEffect(() => {
    localStorage.setItem('rw_v2_theme', theme);
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
    return localStorage.getItem('rw_v2_language') || 'en';
  });

  useEffect(() => {
    localStorage.setItem('rw_v2_language', language);
  }, [language]);

  // Categories State (Clean defaults, extensible, automatically includes Parking & Tolls)
  const [categories, setCategories] = useState(() => {
    const saved = localStorage.getItem('rw_v2_categories');
    if (!saved) return DEFAULT_CATEGORIES;
    try {
      const parsed = JSON.parse(saved);
      // Ensure essential categories like 'Parking & Tolls' are included even for returning users!
      const existingNames = new Set(parsed.map(c => c.name.toLowerCase()));
      const missing = DEFAULT_CATEGORIES.filter(d => !existingNames.has(d.name.toLowerCase()));
      return [...parsed, ...missing];
    } catch (e) {
      return DEFAULT_CATEGORIES;
    }
  });

  useEffect(() => {
    localStorage.setItem('rw_v2_categories', JSON.stringify(categories));
  }, [categories]);

  // User Profile & Budget State
  const [userProfile, setUserProfile] = useState(() => {
    const saved = localStorage.getItem('rw_v2_profile');
    return saved ? JSON.parse(saved) : INITIAL_USER_PROFILE;
  });

  useEffect(() => {
    localStorage.setItem('rw_v2_profile', JSON.stringify(userProfile));
  }, [userProfile]);

  // Transactions State: 100% EMPTY by default for real user usage!
  const [transactions, setTransactions] = useState(() => {
    // Purge any legacy polluted keys from old app versions
    localStorage.removeItem('rw_transactions');
    localStorage.removeItem('rw_debts');
    localStorage.removeItem('rw_savings');
    localStorage.removeItem('rw_swipe_queue');

    const saved = localStorage.getItem('rw_v2_transactions');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        // Ensure no corrupt old income or non-expense data polluted the array
        return parsed.filter(t => t.category?.toLowerCase() !== 'income' && t.bucket !== 'income');
      } catch (e) {
        return [];
      }
    }
    // Start with a clean slate (zero mock transactions)
    return [];
  });

  useEffect(() => {
    localStorage.setItem('rw_v2_transactions', JSON.stringify(transactions));
  }, [transactions]);

  // Modal States
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [isBudgetModalOpen, setIsBudgetModalOpen] = useState(false);
  const [isDuitNowOpen, setIsDuitNowOpen] = useState(false);
  const [isPastSpendingOpen, setIsPastSpendingOpen] = useState(false);

  // Active Month Navigation State (defaults to current month YYYY-MM)
  const [selectedMonth, setSelectedMonth] = useState(() => {
    return new Date().toISOString().slice(0, 7);
  });

  // Financial Figures (strictly real expenses only, exclude any income or negative amounts)
  const expenseTransactions = transactions.filter(t => 
    Number(t.amount) > 0 && 
    t.type !== 'income' && 
    t.bucket !== 'income' && 
    t.category?.toLowerCase() !== 'income'
  );

  // Available distinct months found in transactions + current month
  const availableMonths = useMemo(() => {
    const set = new Set();
    set.add(new Date().toISOString().slice(0, 7));
    expenseTransactions.forEach(t => {
      if (t.date && t.date.length >= 7) {
        set.add(t.date.slice(0, 7));
      }
    });
    return Array.from(set).sort((a, b) => b.localeCompare(a));
  }, [expenseTransactions]);

  // Expenses for the currently selected month
  const activeMonthExpenses = useMemo(() => {
    if (!selectedMonth || selectedMonth === 'all') return expenseTransactions;
    return expenseTransactions.filter(t => t.date && t.date.startsWith(selectedMonth));
  }, [expenseTransactions, selectedMonth]);

  const activeMonthSpent = activeMonthExpenses.reduce((acc, t) => acc + Number(t.amount || 0), 0);
  const totalLifetimeSpent = expenseTransactions.reduce((acc, t) => acc + Number(t.amount || 0), 0);
  const monthlyBudget = userProfile?.monthlyBudget || 3500;

  // Month Navigation Handlers
  const handlePrevMonth = () => {
    setSelectedMonth(prev => getPreviousMonthStr(prev));
  };

  const handleNextMonth = () => {
    setSelectedMonth(prev => getNextMonthStr(prev));
  };

  const handleSelectMonth = (monthStr) => {
    setSelectedMonth(monthStr);
  };

  // Handlers
  const handleAddTransaction = (newTx) => {
    const tx = {
      id: `tx-${Date.now()}`,
      date: newTx.date || new Date().toISOString().split('T')[0],
      ...newTx,
    };
    setTransactions(prev => [tx, ...prev]);
  };

  const handleAddMultipleTransactions = (newTxs) => {
    if (!Array.isArray(newTxs) || newTxs.length === 0) return;
    const formatted = newTxs.map((t, idx) => ({
      id: `tx-${Date.now()}-${idx}-${Math.random().toString(36).substr(2, 4)}`,
      date: t.date || new Date().toISOString().split('T')[0],
      ...t,
    }));
    setTransactions(prev => [...formatted, ...prev]);
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
    if (window.confirm(isBM ? 'Kosongkan semua data perbelanjaan dan mula dari sifar?' : 'Reset and clear all transactions to start completely fresh?')) {
      setTransactions([]);
      localStorage.removeItem('rw_v2_transactions');
    }
  };

  const handleExportCSV = () => {
    const headers = ['ID', 'Date', 'Description', 'Amount (RM)', 'Category', 'Method', 'Note'];
    const rows = expenseTransactions.map(t => [
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
        selectedMonth={selectedMonth}
        onPrevMonth={handlePrevMonth}
        onNextMonth={handleNextMonth}
        onSelectMonth={handleSelectMonth}
        onOpenPastSpending={() => setIsPastSpendingOpen(true)}
        availableMonths={availableMonths}
        onOpenAddModal={() => setIsAddModalOpen(true)}
        onOpenCategoryManager={() => setIsCategoryModalOpen(true)}
        onOpenBudgetModal={() => setIsBudgetModalOpen(true)}
        onClearAllData={handleClearAllData}
        onOpenDuitNow={() => setIsDuitNowOpen(true)}
      />

      {/* Main Content Area */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-7 space-y-6">
        
        {/* Executive Summary Metrics */}
        <BudgetSummaryCards
          monthlyBudget={monthlyBudget}
          onUpdateBudget={handleUpdateBudget}
          totalSpent={activeMonthSpent}
          transactionCount={activeMonthExpenses.length}
          selectedMonth={selectedMonth}
          language={language}
        />

        {/* Asymmetrical Bento Grid: Half-Circle Chart (Image 1) & DuitNow Quick Scanner */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          
          {/* Main Half-Circle Arch Chart (7 columns) */}
          <div className="lg:col-span-7">
            <HalfCircleSpendingChart
              transactions={expenseTransactions}
              categories={categories}
              selectedMonth={selectedMonth}
              language={language}
              onOpenAddModal={() => setIsAddModalOpen(true)}
              onOpenDuitNow={() => setIsDuitNowOpen(true)}
            />
          </div>

          {/* DuitNow Hub & Receipt OCR (5 columns) */}
          <div className="lg:col-span-5">
            <DuitNowHub
              onAddTransaction={handleAddTransaction}
              onAddMultipleTransactions={handleAddMultipleTransactions}
              categories={categories}
              onQuickAddCategory={handleQuickAddCategory}
              userProfile={userProfile}
              language={language}
            />
          </div>

        </div>

        {/* High-Precision Transaction Ledger */}
        <section>
          <TransactionLedger
            transactions={expenseTransactions}
            categories={categories}
            selectedMonth={selectedMonth}
            onSelectMonth={handleSelectMonth}
            availableMonths={availableMonths}
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

      {/* Past Spending Historical Timeline Modal */}
      <PastSpendingModal
        isOpen={isPastSpendingOpen}
        onClose={() => setIsPastSpendingOpen(false)}
        transactions={expenseTransactions}
        categories={categories}
        monthlyBudget={monthlyBudget}
        selectedMonth={selectedMonth}
        onSelectMonth={handleSelectMonth}
        language={language}
      />

      {/* Standalone DuitNow Modal if opened from Navbar */}
      {isDuitNowOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
          <div className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <DuitNowHub
              onAddTransaction={handleAddTransaction}
              onAddMultipleTransactions={handleAddMultipleTransactions}
              categories={categories}
              onQuickAddCategory={handleQuickAddCategory}
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
