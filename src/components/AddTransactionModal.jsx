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
    if (!amount || isNaN(Number(amount)) || Number(amount) <= 0) return;

    const trimmedTitle = title.trim();
    const finalTitle = trimmedTitle || category || (isBM ? 'Perbelanjaan' : 'Expense');

    const newTx = {
      title: finalTitle,
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
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div 
        className="w-full sm:max-w-md p-6 relative transition-all rounded-t-[28px] sm:rounded-[28px] shadow-2xl max-h-[92vh] overflow-y-auto"
        style={{
          backgroundColor: 'var(--md-sys-color-surface-container-high)',
          color: 'var(--md-sys-color-on-surface)'
        }}
      >
        {/* Drag handle for mobile bottom sheet */}
        <div className="w-10 h-1.5 rounded-full mx-auto mb-4 sm:hidden" style={{ backgroundColor: 'var(--md-sys-color-outline-variant)' }} />

        {/* Header */}
        <div className="flex items-center justify-between border-b pb-3.5 mb-4" style={{ borderColor: 'var(--md-sys-color-outline-variant)' }}>
          <div>
            <h3 className="text-base font-bold" style={{ color: 'var(--md-sys-color-on-surface)' }}>
              {isBM ? 'Rekod Perbelanjaan' : 'Record Expense'}
            </h3>
            <p className="text-xs" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
              {isBM ? 'Masukkan jumlah perbelanjaan Ringgit' : 'Log Malaysian Ringgit expenditure'}
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:opacity-80 transition-colors"
            style={{
              backgroundColor: 'var(--md-sys-color-surface-container)',
              color: 'var(--md-sys-color-on-surface-variant)'
            }}
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          
          {/* Amount in RM (Large M3 Outlined Field) */}
          <div>
            <label className="text-[11px] font-bold block mb-1 uppercase tracking-wider" style={{ color: 'var(--md-sys-color-primary)' }}>
              {isBM ? 'Jumlah (RM)' : 'Amount (RM)'}
            </label>
            <div className="relative">
              <span className="absolute left-3.5 top-2.5 font-black text-lg" style={{ color: 'var(--md-sys-color-primary)' }}>RM</span>
              <input
                type="number"
                step="0.05"
                required
                placeholder="0.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full rounded-2xl pl-12 pr-4 py-2.5 font-black text-2xl tabular-nums focus:outline-none transition-all"
                style={{
                  backgroundColor: 'var(--md-sys-color-surface)',
                  border: '2px solid var(--md-sys-color-primary)',
                  color: 'var(--md-sys-color-on-surface)'
                }}
                autoFocus
              />
            </div>
          </div>

          {/* Title / Description (Optional) */}
          <div>
            <label className="text-[11px] font-semibold block mb-1 uppercase tracking-wider" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
              {isBM ? 'Penerima / Keterangan (Pilihan)' : 'Description (Optional)'}
            </label>
            <input
              type="text"
              placeholder={isBM ? `Pilihan (cth. ${category})` : `Optional (e.g. ${category})`}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full rounded-2xl px-3.5 py-2.5 text-xs focus:outline-none transition-colors"
              style={{
                backgroundColor: 'var(--md-sys-color-surface)',
                border: '1px solid var(--md-sys-color-outline-variant)',
                color: 'var(--md-sys-color-on-surface)'
              }}
            />
          </div>

          {/* Category Picker with Inline + Button */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="text-[11px] font-semibold uppercase tracking-wider" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
                {isBM ? 'Kategori' : 'Category'}
              </label>
              <button
                type="button"
                onClick={() => setIsCreatingCat(!isCreatingCat)}
                className="text-[11px] font-bold"
                style={{ color: 'var(--md-sys-color-primary)' }}
              >
                + Quick Category
              </button>
            </div>

            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full rounded-2xl px-3.5 py-2.5 text-xs font-semibold focus:outline-none cursor-pointer"
              style={{
                backgroundColor: 'var(--md-sys-color-surface)',
                border: '1px solid var(--md-sys-color-outline-variant)',
                color: 'var(--md-sys-color-on-surface)'
              }}
            >
              {categories.map((c) => (
                <option key={c.id || c.name} value={c.name} style={{ backgroundColor: 'var(--md-sys-color-surface-container)' }}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          {/* Quick Inline Category Creator */}
          {isCreatingCat && (
            <div className="p-3 rounded-2xl space-y-2" style={{
              backgroundColor: 'var(--md-sys-color-surface-container)',
              border: '1px solid var(--md-sys-color-outline-variant)'
            }}>
              <div className="flex items-center space-x-2">
                <input
                  type="text"
                  placeholder="New category name..."
                  value={newCatName}
                  onChange={(e) => setNewCatName(e.target.value)}
                  className="w-full rounded-xl px-3 py-1.5 text-xs focus:outline-none"
                  style={{
                    backgroundColor: 'var(--md-sys-color-surface)',
                    border: '1px solid var(--md-sys-color-outline-variant)',
                    color: 'var(--md-sys-color-on-surface)'
                  }}
                />
                <button
                  type="button"
                  onClick={handleCreateCategoryInline}
                  className="px-3 py-1.5 rounded-xl font-bold text-xs"
                  style={{
                    backgroundColor: 'var(--md-sys-color-primary)',
                    color: 'var(--md-sys-color-on-primary)'
                  }}
                >
                  Save
                </button>
              </div>
            </div>
          )}

          {/* Method & Date */}
          <div className="grid grid-cols-2 gap-2.5">
            <div>
              <label className="text-[11px] font-semibold block mb-1 uppercase tracking-wider" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
                {isBM ? 'Kaedah' : 'Method'}
              </label>
              <select
                value={method}
                onChange={(e) => setMethod(e.target.value)}
                className="w-full rounded-2xl px-3 py-2 text-xs font-semibold focus:outline-none cursor-pointer"
                style={{
                  backgroundColor: 'var(--md-sys-color-surface)',
                  border: '1px solid var(--md-sys-color-outline-variant)',
                  color: 'var(--md-sys-color-on-surface)'
                }}
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
              <label className="text-[11px] font-semibold block mb-1 uppercase tracking-wider" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
                {isBM ? 'Tarikh' : 'Date'}
              </label>
              <input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="w-full rounded-2xl px-3 py-2 text-xs font-semibold focus:outline-none"
                style={{
                  backgroundColor: 'var(--md-sys-color-surface)',
                  border: '1px solid var(--md-sys-color-outline-variant)',
                  color: 'var(--md-sys-color-on-surface)'
                }}
              />
            </div>
          </div>

          {/* Note */}
          <div>
            <label className="text-[11px] font-semibold block mb-1 uppercase tracking-wider" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
              {isBM ? 'Nota' : 'Note (Optional)'}
            </label>
            <input
              type="text"
              placeholder="e.g. Lunch with team"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              className="w-full rounded-2xl px-3.5 py-2 text-xs focus:outline-none"
              style={{
                backgroundColor: 'var(--md-sys-color-surface)',
                border: '1px solid var(--md-sys-color-outline-variant)',
                color: 'var(--md-sys-color-on-surface)'
              }}
            />
          </div>

          <div className="pt-2">
            <button
              type="submit"
              className="w-full font-bold py-3 rounded-full transition-all active:scale-[0.98] shadow-md text-sm"
              style={{
                backgroundColor: 'var(--md-sys-color-primary)',
                color: 'var(--md-sys-color-on-primary)'
              }}
            >
              {isBM ? 'Simpan Rekod RM' : 'Save Expense (RM)'}
            </button>
          </div>

        </form>

      </div>
    </div>
  );
}
