import React, { useState } from 'react';
import { X, PlusCircle, Plus, Tag, Check } from 'lucide-react';
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

  // Quick In-line Category Creator State
  const [isCreatingCat, setIsCreatingCat] = useState(false);
  const [newCatName, setNewCatName] = useState('');

  if (!isOpen) return null;

  const handleCreateCategoryInline = (e) => {
    e.preventDefault();
    if (!newCatName.trim()) return;

    if (onQuickAddCategory) {
      const created = onQuickAddCategory({
        name: newCatName.trim(),
        color: '#38bdf8',
        icon: '🏷️',
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

    // Reset
    setTitle('');
    setAmount('');
    setNote('');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-md bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-2xl p-6 relative transition-colors">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-3 mb-4">
          <div className="flex items-center space-x-2">
            <PlusCircle className="w-5 h-5 text-emerald-500" />
            <h3 className="text-base font-bold text-slate-900 dark:text-white">
              {isBM ? 'Rekod Perbelanjaan Ringgit (RM)' : 'Log Ringgit Expense'}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-700 dark:hover:text-white p-1 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
          
          {/* Title */}
          <div>
            <label className="text-slate-700 dark:text-slate-300 font-semibold block mb-1">
              {isBM ? 'Nama Peniaga / Pembayaran' : 'Merchant / Description'}
            </label>
            <input
              type="text"
              required
              placeholder={isBM ? 'cth. Nasi Kandar Pelita, 99 Speedmart, Petronas' : 'e.g. Village Grocer, ZUS Coffee, Shell Petrol'}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-xl px-3 py-2 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:border-emerald-500"
            />
          </div>

          {/* Amount in RM */}
          <div>
            <label className="text-slate-700 dark:text-slate-300 font-semibold block mb-1">
              {isBM ? 'Jumlah (RM)' : 'Amount (Ringgit Malaysia)'}
            </label>
            <div className="relative">
              <span className="absolute left-3 top-2 text-slate-500 dark:text-slate-400 font-bold">RM</span>
              <input
                type="number"
                step="0.05"
                required
                placeholder="25.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-xl pl-11 pr-3 py-2 text-slate-900 dark:text-white font-bold text-sm focus:outline-none focus:border-emerald-500"
              />
            </div>
          </div>

          {/* Category with In-line Quick Add button */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="text-slate-700 dark:text-slate-300 font-semibold block">
                {isBM ? 'Kategori Perbelanjaan' : 'Expense Category'}
              </label>
              <button
                type="button"
                onClick={() => setIsCreatingCat(!isCreatingCat)}
                className="text-[11px] text-emerald-600 dark:text-emerald-400 font-bold hover:underline flex items-center"
              >
                <Plus className="w-3 h-3 mr-0.5" />
                {isBM ? 'Cipta Kategori' : '+ Quick Category'}
              </button>
            </div>

            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-xl px-2.5 py-2 text-slate-900 dark:text-white focus:outline-none focus:border-emerald-500"
            >
              {categories.map((c) => (
                <option key={c.id || c.name} value={c.name}>
                  {c.icon ? `${c.icon} ` : ''}{c.name}
                </option>
              ))}
            </select>
          </div>

          {/* In-line Quick Category Creator */}
          {isCreatingCat && (
            <div className="p-3 bg-emerald-50/70 dark:bg-slate-950 border border-emerald-300 dark:border-emerald-800 rounded-xl space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-bold text-emerald-800 dark:text-emerald-300">
                  {isBM ? 'Tambah Kategori Segera' : 'Quick Add Category'}
                </span>
                <button
                  type="button"
                  onClick={() => setIsCreatingCat(false)}
                  className="text-slate-400 hover:text-slate-600 dark:hover:text-white"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>

              <div className="flex items-center space-x-2">
                <input
                  type="text"
                  placeholder={isBM ? 'cth. Durian & Buah...' : 'e.g. Durian Fest, Gym...'}
                  value={newCatName}
                  onChange={(e) => setNewCatName(e.target.value)}
                  className="w-full bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg px-2.5 py-1.5 text-xs text-slate-900 dark:text-white focus:outline-none"
                />
                <button
                  type="button"
                  onClick={handleCreateCategoryInline}
                  className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-lg text-xs shrink-0 transition-colors"
                >
                  {isBM ? 'Cipta' : 'Add'}
                </button>
              </div>
            </div>
          )}

          {/* Payment Method & Date */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-slate-700 dark:text-slate-300 font-semibold block mb-1">
                {isBM ? 'Kaedah Bayaran' : 'Payment Method'}
              </label>
              <select
                value={method}
                onChange={(e) => setMethod(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-xl px-2.5 py-2 text-slate-900 dark:text-white focus:outline-none focus:border-emerald-500"
              >
                <option value="DuitNow QR">DuitNow QR 🇲🇾</option>
                <option value="Touch 'n Go eWallet">Touch 'n Go eWallet</option>
                <option value="MAE by Maybank">MAE by Maybank</option>
                <option value="CIMB OCTO">CIMB OCTO</option>
                <option value="GrabPay">GrabPay</option>
                <option value="Credit/Debit Card">Credit/Debit Card</option>
                <option value="Cash">Cash (Tunai)</option>
              </select>
            </div>

            <div>
              <label className="text-slate-700 dark:text-slate-300 font-semibold block mb-1">
                {isBM ? 'Tarikh' : 'Date'}
              </label>
              <input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-xl px-2.5 py-1.5 text-slate-900 dark:text-white focus:outline-none focus:border-emerald-500"
              />
            </div>
          </div>

          {/* Note */}
          <div>
            <label className="text-slate-700 dark:text-slate-300 font-semibold block mb-1">
              {isBM ? 'Catatan / Nota' : 'Personal Note'}
            </label>
            <input
              type="text"
              placeholder={isBM ? 'cth. Roti canai + teh o ais limau' : 'e.g. Lunch with team, monthly reload'}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-xl px-3 py-2 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div className="pt-2">
            <button
              type="submit"
              className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-2.5 rounded-xl transition-all shadow-lg shadow-emerald-600/30"
            >
              {isBM ? 'Simpan Transaksi RM' : 'Save Transaction (RM)'}
            </button>
          </div>

        </form>

      </div>
    </div>
  );
}
