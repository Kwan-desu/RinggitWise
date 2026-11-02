import React, { useState } from 'react';
import { Search, Download, Trash2, Plus, Calendar } from 'lucide-react';
import { formatRM, formatMonthYear } from '../utils/helpers';

export default function TransactionLedger({
  transactions = [],
  categories = [],
  selectedMonth = '2026-10',
  onSelectMonth,
  availableMonths = [],
  onDeleteTransaction,
  onExportCSV,
  onOpenAddModal,
  language = 'en'
}) {
  const isBM = language === 'bm';
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');

  const filtered = transactions.filter(tx => {
    const matchesSearch = 
      tx.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      tx.category.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (tx.note && tx.note.toLowerCase().includes(searchTerm.toLowerCase()));
    
    const matchesCat = selectedCategory === 'all' || tx.category === selectedCategory;
    const matchesMonth = !selectedMonth || selectedMonth === 'all' || (tx.date && tx.date.startsWith(selectedMonth));
    return matchesSearch && matchesCat && matchesMonth;
  });

  return (
    <div className="p-5 sm:p-7 rounded-3xl transition-all" style={{
      backgroundColor: 'var(--md-sys-color-surface-container-high)',
      color: 'var(--md-sys-color-on-surface)'
    }}>
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b pb-4 mb-4" style={{ borderColor: 'var(--md-sys-color-outline-variant)' }}>
        <div>
          <h2 className="text-base font-bold tracking-tight" style={{ color: 'var(--md-sys-color-on-surface)' }}>
            {isBM ? 'Lejar Rekod Transaksi' : 'Transaction History'}
          </h2>
          <p className="text-xs mt-0.5" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
            {filtered.length} {isBM ? 'rekod dijumpai' : 'records logged in Malaysian Ringgit'}
          </p>
        </div>

        <div className="flex items-center space-x-2">
          {transactions.length > 0 && (
            <button
              onClick={onExportCSV}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-full text-xs font-semibold transition-all hover:opacity-90 active:scale-95"
              style={{
                backgroundColor: 'var(--md-sys-color-surface-container)',
                color: 'var(--md-sys-color-on-surface)',
                border: '1px solid var(--md-sys-color-outline-variant)'
              }}
            >
              <Download className="w-3.5 h-3.5" />
              <span>CSV</span>
            </button>
          )}

          <button
            onClick={onOpenAddModal}
            className="flex items-center space-x-1.5 px-4 py-1.5 rounded-full text-xs font-bold transition-all shadow-xs active:scale-95"
            style={{
              backgroundColor: 'var(--md-sys-color-primary)',
              color: 'var(--md-sys-color-on-primary)'
            }}
          >
            <Plus className="w-3.5 h-3.5" />
            <span>{isBM ? 'Rekod RM' : 'Add Expense'}</span>
          </button>
        </div>
      </div>

      {transactions.length > 0 ? (
        <>
          {/* Controls: Search and Filter (M3 Chips) */}
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 mb-4">
            <div className="relative flex-1 max-w-sm">
              <Search className="w-4 h-4 absolute left-3.5 top-2.5" style={{ color: 'var(--md-sys-color-on-surface-variant)' }} />
              <input
                type="text"
                placeholder={isBM ? 'Cari perbelanjaan...' : 'Filter transactions...'}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full rounded-full pl-10 pr-4 py-2 text-xs focus:outline-none transition-colors"
                style={{
                  backgroundColor: 'var(--md-sys-color-surface)',
                  color: 'var(--md-sys-color-on-surface)',
                  border: '1px solid var(--md-sys-color-outline-variant)'
                }}
              />
            </div>

            <div className="flex items-center space-x-2 overflow-x-auto pb-1 sm:pb-0">
              {/* Month Selector Filter (M3 Assist Chip) */}
              <div className="flex items-center space-x-1.5 rounded-full px-3 py-1.5 shrink-0" style={{
                backgroundColor: 'var(--md-sys-color-surface)',
                border: '1px solid var(--md-sys-color-outline-variant)'
              }}>
                <Calendar className="w-3.5 h-3.5" style={{ color: 'var(--md-sys-color-primary)' }} />
                <select
                  value={selectedMonth || 'all'}
                  onChange={(e) => onSelectMonth && onSelectMonth(e.target.value)}
                  className="bg-transparent border-none text-xs font-semibold focus:outline-none cursor-pointer"
                  style={{ color: 'var(--md-sys-color-on-surface)' }}
                >
                  {availableMonths.map((mStr) => (
                    <option key={mStr} value={mStr} style={{ backgroundColor: 'var(--md-sys-color-surface-container)' }}>
                      {formatMonthYear(mStr, isBM)}
                    </option>
                  ))}
                  <option value="all" style={{ backgroundColor: 'var(--md-sys-color-surface-container)' }}>
                    {isBM ? 'Semua Bulan' : 'All Months'}
                  </option>
                </select>
              </div>

              {/* Category Filter */}
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="rounded-full px-3 py-1.5 text-xs font-semibold focus:outline-none shrink-0 cursor-pointer"
                style={{
                  backgroundColor: 'var(--md-sys-color-surface)',
                  color: 'var(--md-sys-color-on-surface)',
                  border: '1px solid var(--md-sys-color-outline-variant)'
                }}
              >
                <option value="all">{isBM ? 'Semua Kategori' : 'All Categories'}</option>
                {categories.map((c) => (
                  <option key={c.id || c.name} value={c.name} style={{ backgroundColor: 'var(--md-sys-color-surface-container)' }}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* M3 List Layout */}
          <div className="space-y-2">
            {filtered.map((tx) => {
              const catData = categories.find(c => c.name === tx.category);
              const dotColor = catData?.color || '#006C51';

              return (
                <div
                  key={tx.id}
                  className="flex items-center justify-between p-3 sm:p-3.5 rounded-2xl transition-all hover:scale-[1.005] group"
                  style={{
                    backgroundColor: 'var(--md-sys-color-surface)',
                    border: '1px solid var(--md-sys-color-outline-variant)'
                  }}
                >
                  {/* Left: Avatar Icon + Description & Details */}
                  <div className="flex items-center space-x-3 min-w-0 flex-1 pr-3">
                    <div 
                      className="w-10 h-10 rounded-full flex items-center justify-center shrink-0 font-bold text-xs text-white shadow-2xs"
                      style={{ backgroundColor: dotColor }}
                    >
                      {(tx.title || tx.category || 'RM').slice(0, 1).toUpperCase()}
                    </div>

                    <div className="min-w-0 flex-1">
                      <div className="flex items-center space-x-2">
                        <span className="font-bold text-sm truncate block" style={{ color: 'var(--md-sys-color-on-surface)' }}>
                          {tx.title || tx.category || (isBM ? 'Perbelanjaan' : 'Expense')}
                        </span>
                        <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full shrink-0" style={{
                          backgroundColor: 'var(--md-sys-color-surface-container)',
                          color: 'var(--md-sys-color-on-surface-variant)'
                        }}>
                          {tx.category}
                        </span>
                      </div>

                      <div className="flex items-center space-x-2 text-[11px] mt-0.5 truncate" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
                        <span className="tabular-nums">{tx.date}</span>
                        <span>•</span>
                        <span>{tx.method}</span>
                        {tx.note && (
                          <>
                            <span>•</span>
                            <span className="truncate italic">{tx.note}</span>
                          </>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Right: Amount & Delete Button */}
                  <div className="flex items-center space-x-2 sm:space-x-3 shrink-0">
                    <span className="text-sm sm:text-base font-extrabold tabular-nums" style={{ color: 'var(--md-sys-color-on-surface)' }}>
                      {formatRM(tx.amount)}
                    </span>

                    <button
                      onClick={() => onDeleteTransaction(tx.id)}
                      className="p-1.5 rounded-full transition-colors opacity-70 group-hover:opacity-100 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-950/40"
                      style={{ color: 'var(--md-sys-color-on-surface-variant)' }}
                      title="Delete"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </>
      ) : (
        <div className="py-8 text-center text-xs" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
          {isBM ? 'Tiada rekod transaksi.' : 'No transactions recorded yet.'}
        </div>
      )}

    </div>
  );
}
