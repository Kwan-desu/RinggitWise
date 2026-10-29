import React, { useState } from 'react';
import { X, Plus } from 'lucide-react';
import { playSound, fireCelebration } from '../utils/helpers';

export default function AddTransactionModal({
  isOpen,
  onClose,
  onAddTransaction,
  categories = [],
  onQuickAddCategory,
  language = 'en'
}) {
  const isBM = language === 'bm';

  const [title, setTitle] = useState('');
  const [amount, setAmount] = useState('');
  const [category, setCategory] = useState(categories[0]?.name || 'Mamak & Dining');
  const [method, setMethod] = useState('DuitNow QR');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [note, setNote] = useState('');

  // Quick In-line Category Creator
  const [isCreatingCat, setIsCreatingCat] = useState(false);
  const [newCatName, setNewCatName] = useState('');

  if (!isOpen) return null;

  const handleCreateCategoryInline = (e) => {
    e.preventDefault();
    if (!newCatName.trim()) return;

    if (onQuickAddCategory) {
      const created = onQuickAddCategory({
        name: newCatName.trim(),
        color: '#54b3d6',
      });
      if (created) {
        setCategory(created.name);
      }
    }
    setNewCatName('');
    setIsCreatingCat(false);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!title || !amount) return;

    const newTx = {
      title,
      amount: Number(amount),
      category,
      method,
      note,
      date: date || new Date().toISOString().split('T')[0],
    };

    onAddTransaction(newTx);
    playSound('approve');
    fireCelebration();

    setTitle('');
    setAmount('');
    setNote('');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-sm bg-white dark:bg-zinc-900 border border-zinc-200/80 dark:border-zinc-800/80 rounded-2xl shadow-xl p-5 relative transition-colors">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-zinc-100 dark:border-zinc-800/80 pb-3 mb-4">
          <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
            {isBM ? 'Rekod Perbelanjaan' : 'Record Expense'}
          </h3>
          <button
            onClick={onClose}
            className="text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 p-1"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
          
          {/* Amount in RM */}
          <div>
            <label className="text-[11px] font-medium text-zinc-500 block mb-1 uppercase tracking-wider">
              {isBM ? 'Jumlah (RM)' : 'Amount'}
            </label>
            <div className="relative">
              <span className="absolute left-3 top-2 text-zinc-400 font-semibold">RM</span>
              <input
                type="number"
                step="0.05"
                required
                placeholder="0.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-lg pl-10 pr-3 py-2 text-zinc-900 dark:text-zinc-100 font-semibold text-base tabular-nums focus:outline-none focus:border-zinc-900 dark:focus:border-zinc-100"
                autoFocus
              />
            </div>
          </div>

          {/* Title */}
          <div>
            <label className="text-[11px] font-medium text-zinc-500 block mb-1 uppercase tracking-wider">
              {isBM ? 'Penerima / Keterangan' : 'Description'}
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Village Grocer, ZUS Coffee"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-lg px-3 py-2 text-zinc-900 dark:text-zinc-100 focus:outline-none"
            />
          </div>

          {/* Category Picker with Inline + Button */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="text-[11px] font-medium text-zinc-500 uppercase tracking-wider">
                {isBM ? 'Kategori' : 'Category'}
              </label>
              <button
                type="button"
                onClick={() => setIsCreatingCat(!isCreatingCat)}
                className="text-[10px] text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 font-medium"
              >
                + Quick Category
              </button>
            </div>

            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-lg px-2.5 py-1.5 text-zinc-900 dark:text-zinc-100 focus:outline-none"
            >
              {categories.map((c) => (
                <option key={c.id || c.name} value={c.name}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          {/* Quick Inline Category Creator */}
          {isCreatingCat && (
            <div className="p-2.5 bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-lg space-y-2">
              <div className="flex items-center space-x-2">
                <input
                  type="text"
                  placeholder="New category name..."
                  value={newCatName}
                  onChange={(e) => setNewCatName(e.target.value)}
                  className="w-full bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-700 rounded-md px-2 py-1 text-xs text-zinc-900 dark:text-zinc-100 focus:outline-none"
                />
                <button
                  type="button"
                  onClick={handleCreateCategoryInline}
                  className="px-2.5 py-1 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 font-semibold rounded-md text-[11px]"
                >
                  Save
                </button>
              </div>
            </div>
          )}

          {/* Method & Date */}
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="text-[11px] font-medium text-zinc-500 block mb-1 uppercase tracking-wider">
                {isBM ? 'Kaedah' : 'Method'}
              </label>
              <select
                value={method}
                onChange={(e) => setMethod(e.target.value)}
                className="w-full bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-lg px-2 py-1.5 text-zinc-900 dark:text-zinc-100 focus:outline-none"
              >
                <option value="DuitNow QR">DuitNow QR</option>
                <option value="Touch 'n Go eWallet">TNG eWallet</option>
                <option value="MAE by Maybank">MAE Maybank</option>
                <option value="CIMB OCTO">CIMB OCTO</option>
                <option value="GrabPay">GrabPay</option>
                <option value="Credit Card">Credit Card</option>
                <option value="Cash">Cash</option>
              </select>
            </div>

            <div>
              <label className="text-[11px] font-medium text-zinc-500 block mb-1 uppercase tracking-wider">
                {isBM ? 'Tarikh' : 'Date'}
              </label>
              <input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="w-full bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-lg px-2 py-1.5 text-zinc-900 dark:text-zinc-100 focus:outline-none"
              />
            </div>
          </div>

          {/* Note */}
          <div>
            <label className="text-[11px] font-medium text-zinc-500 block mb-1 uppercase tracking-wider">
              {isBM ? 'Nota' : 'Note (Optional)'}
            </label>
            <input
              type="text"
              placeholder="e.g. Lunch with team"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              className="w-full bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-lg px-3 py-1.5 text-zinc-900 dark:text-zinc-100 focus:outline-none"
            />
          </div>

          <div className="pt-2">
            <button
              type="submit"
              className="w-full bg-zinc-900 hover:bg-zinc-800 dark:bg-zinc-100 dark:hover:bg-white text-white dark:text-zinc-900 font-semibold py-2 rounded-lg transition-colors"
            >
              {isBM ? 'Simpan Rekod RM' : 'Save Expense (RM)'}
            </button>
          </div>

        </form>

      </div>
    </div>
  );
}
