import React, { useState } from 'react';
import { Search, Download, Trash2, Plus } from 'lucide-react';
import { formatRM } from '../utils/helpers';

export default function TransactionLedger({
  transactions = [],
  categories = [],
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
    return matchesSearch && matchesCat;
  });

  return (
    <div className="bg-white dark:bg-zinc-900/70 border border-zinc-200/80 dark:border-zinc-800/80 rounded-2xl p-5 sm:p-7 shadow-2xs transition-colors">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-zinc-100 dark:border-zinc-800/80 pb-4 mb-4">
        <div>
          <h2 className="text-sm font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">
            {isBM ? 'Lejar Rekod Transaksi' : 'Transaction History'}
          </h2>
          <p className="text-[11px] text-zinc-500 dark:text-zinc-400 mt-0.5">
            {filtered.length} {isBM ? 'rekod dijumpai' : 'records logged in Malaysian Ringgit'}
          </p>
        </div>

        <div className="flex items-center space-x-2">
          {transactions.length > 0 && (
            <button
              onClick={onExportCSV}
              className="flex items-center space-x-1 px-2.5 py-1.5 rounded-lg border border-zinc-200 dark:border-zinc-800 text-xs text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 transition-colors"
            >
              <Download className="w-3.5 h-3.5" />
              <span>CSV</span>
            </button>
          )}

          <button
            onClick={onOpenAddModal}
            className="flex items-center space-x-1 px-3 py-1.5 rounded-lg bg-zinc-900 hover:bg-zinc-800 dark:bg-zinc-100 dark:hover:bg-white text-white dark:text-zinc-900 text-xs font-semibold transition-all active:scale-95"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>{isBM ? 'Rekod RM' : 'Add Expense'}</span>
          </button>
        </div>
      </div>

      {transactions.length > 0 ? (
        <>
          {/* Controls: Search and Filter */}
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 mb-4">
            <div className="relative flex-1 max-w-sm">
              <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-zinc-400" />
              <input
                type="text"
                placeholder={isBM ? 'Cari perbelanjaan...' : 'Filter transactions...'}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-lg pl-8 pr-3 py-1.5 text-xs text-zinc-900 dark:text-zinc-100 placeholder-zinc-400 focus:outline-none focus:border-zinc-400 dark:focus:border-zinc-600"
              />
            </div>

            <div className="flex items-center space-x-2 overflow-x-auto pb-1 sm:pb-0">
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 rounded-lg px-2.5 py-1.5 text-xs text-zinc-800 dark:text-zinc-200 focus:outline-none"
              >
                <option value="all">{isBM ? 'Semua Kategori' : 'All Categories'}</option>
                {categories.map((c) => (
                  <option key={c.id || c.name} value={c.name}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Clean Data Table */}
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-zinc-100 dark:border-zinc-800 text-[11px] font-medium text-zinc-400 uppercase tracking-wider">
                  <th className="pb-2.5 px-3">{isBM ? 'Tarikh' : 'Date'}</th>
                  <th className="pb-2.5 px-3">{isBM ? 'Penerima / Keterangan' : 'Description'}</th>
                  <th className="pb-2.5 px-3">{isBM ? 'Kategori' : 'Category'}</th>
                  <th className="pb-2.5 px-3">{isBM ? 'Kaedah' : 'Method'}</th>
                  <th className="pb-2.5 px-3 text-right">{isBM ? 'Jumlah (RM)' : 'Amount (RM)'}</th>
                  <th className="pb-2.5 px-2 text-right"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800/60">
                {filtered.map((tx) => {
                  const catData = categories.find(c => c.name === tx.category);
                  return (
                    <tr key={tx.id} className="hover:bg-zinc-50/80 dark:hover:bg-zinc-800/30 transition-colors group">
                      <td className="py-3 px-3 text-zinc-500 tabular-nums whitespace-nowrap">
                        {tx.date}
                      </td>

                      <td className="py-3 px-3">
                        <span className="font-medium text-zinc-900 dark:text-zinc-100 block">
                          {tx.title}
                        </span>
                        {tx.note && (
                          <span className="text-[11px] text-zinc-400 block truncate max-w-xs">
                            {tx.note}
                          </span>
                        )}
                      </td>

                      <td className="py-3 px-3 whitespace-nowrap">
                        <span className="inline-flex items-center space-x-1.5 text-[11px] px-2 py-0.5 rounded-md bg-zinc-100 dark:bg-zinc-800/80 text-zinc-700 dark:text-zinc-300">
                          {catData?.color && (
                            <span
                              className="w-2 h-2 rounded-full shrink-0"
                              style={{ backgroundColor: catData.color }}
                            />
                          )}
                          <span>{tx.category}</span>
                        </span>
                      </td>

                      <td className="py-3 px-3 text-zinc-500 whitespace-nowrap">
                        {tx.method}
                      </td>

                      <td className="py-3 px-3 text-right font-semibold tabular-nums text-zinc-900 dark:text-zinc-100">
                        {formatRM(tx.amount)}
                      </td>

                      <td className="py-3 px-2 text-right">
                        <button
                          onClick={() => onDeleteTransaction(tx.id)}
                          className="opacity-0 group-hover:opacity-100 text-zinc-400 hover:text-rose-500 transition-opacity p-1"
                          title="Delete"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </>
      ) : (
        <div className="py-8 text-center text-xs text-zinc-400">
          {isBM ? 'Tiada rekod transaksi.' : 'No transactions recorded yet.'}
        </div>
      )}

    </div>
  );
}
