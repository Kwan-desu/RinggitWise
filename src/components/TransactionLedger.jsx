import React, { useState } from 'react';
import { 
  Search, 
  Trash2, 
  Download, 
  Plus, 
  Receipt,
  Calendar,
  Tag
} from 'lucide-react';
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
  const [selectedMethod, setSelectedMethod] = useState('all');

  const filtered = transactions.filter(tx => {
    const matchesSearch = 
      tx.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      tx.category.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (tx.note && tx.note.toLowerCase().includes(searchTerm.toLowerCase()));
    
    const matchesCat = selectedCategory === 'all' || tx.category === selectedCategory;
    const matchesMethod = selectedMethod === 'all' || tx.method === selectedMethod;

    return matchesSearch && matchesCat && matchesMethod;
  });

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 sm:p-6 shadow-xl transition-colors">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-slate-100 dark:border-slate-800 pb-4 mb-5">
        <div>
          <h2 className="text-lg font-bold text-slate-900 dark:text-white tracking-tight">
            {isBM ? 'Lejar Transaksi Ringgit' : 'Ringgit Transaction History'}
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            {isBM 
              ? 'Rekod perbelanjaan merentasi DuitNow QR, e-dompet, dan pemindahan tunai.'
              : 'Every Ringgit expense tracked with real-time filters and CSV export.'}
          </p>
        </div>

        <div className="flex items-center space-x-2">
          {transactions.length > 0 && (
            <button
              onClick={onExportCSV}
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-xs text-slate-700 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white hover:border-slate-300 dark:hover:border-slate-700 transition-colors"
            >
              <Download className="w-3.5 h-3.5 text-emerald-500" />
              <span>{isBM ? 'Eksport CSV' : 'Export CSV'}</span>
            </button>
          )}

          <button
            onClick={onOpenAddModal}
            className="flex items-center space-x-1.5 px-3 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold shadow-md shadow-emerald-600/20 transition-all active:scale-95"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>{isBM ? 'Tambah Rekod RM' : 'Log Expense'}</span>
          </button>
        </div>
      </div>

      {transactions.length > 0 ? (
        <>
          {/* Filters & Search */}
          <div className="grid grid-cols-1 sm:grid-cols-12 gap-3 mb-5">
            {/* Search Input */}
            <div className="sm:col-span-6 relative">
              <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-400" />
              <input
                type="text"
                placeholder={isBM ? 'Cari peniaga, kategori, nota...' : 'Search merchant, category, notes...'}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl pl-9 pr-3 py-2 text-xs text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:border-emerald-500 transition-colors"
              />
            </div>

            {/* Category Filter */}
            <div className="sm:col-span-3">
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl px-2.5 py-2 text-xs text-slate-900 dark:text-white focus:outline-none focus:border-emerald-500 transition-colors"
              >
                <option value="all">{isBM ? 'Semua Kategori' : 'All Categories'}</option>
                {categories.map((c) => (
                  <option key={c.id || c.name} value={c.name}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Payment Method Filter */}
            <div className="sm:col-span-3">
              <select
                value={selectedMethod}
                onChange={(e) => setSelectedMethod(e.target.value)}
                className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl px-2.5 py-2 text-xs text-slate-900 dark:text-white focus:outline-none focus:border-emerald-500 transition-colors"
              >
                <option value="all">{isBM ? 'Semua Kaedah' : 'All Methods'}</option>
                <option value="DuitNow QR">DuitNow QR</option>
                <option value="Touch 'n Go eWallet">Touch 'n Go eWallet</option>
                <option value="MAE by Maybank">MAE by Maybank</option>
                <option value="CIMB OCTO">CIMB OCTO</option>
                <option value="GrabPay">GrabPay</option>
                <option value="Credit/Debit Card">Credit/Debit Card</option>
                <option value="Cash">Cash</option>
              </select>
            </div>
          </div>

          {/* Transaction Table */}
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-slate-100 dark:border-slate-800 text-slate-400 font-semibold">
                  <th className="pb-3 px-2">{isBM ? 'Tarikh' : 'Date'}</th>
                  <th className="pb-3 px-2">{isBM ? 'Peniaga / Pembayaran' : 'Merchant / Description'}</th>
                  <th className="pb-3 px-2">{isBM ? 'Kategori' : 'Category'}</th>
                  <th className="pb-3 px-2">{isBM ? 'Kaedah' : 'Method'}</th>
                  <th className="pb-3 px-2 text-right">{isBM ? 'Jumlah (RM)' : 'Amount (RM)'}</th>
                  <th className="pb-3 px-2 text-right">{isBM ? 'Tindakan' : 'Action'}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60">
                {filtered.map((tx) => {
                  const catData = categories.find(c => c.name === tx.category);
                  return (
                    <tr key={tx.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors">
                      {/* Date */}
                      <td className="py-3 px-2 text-slate-500 dark:text-slate-400 whitespace-nowrap">
                        {tx.date}
                      </td>

                      {/* Merchant & Note */}
                      <td className="py-3 px-2">
                        <div className="font-bold text-slate-900 dark:text-white">
                          {tx.title}
                        </div>
                        {tx.note && (
                          <div className="text-[11px] text-slate-400 truncate max-w-xs">
                            {tx.note}
                          </div>
                        )}
                      </td>

                      {/* Category Badge */}
                      <td className="py-3 px-2">
                        <span className="inline-flex items-center space-x-1.5 text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-200 border border-slate-200 dark:border-slate-700">
                          {catData?.color && (
                            <span
                              className="w-2.5 h-2.5 rounded-full shrink-0"
                              style={{ backgroundColor: catData.color }}
                            ></span>
                          )}
                          <span>{tx.category}</span>
                        </span>
                      </td>

                      {/* Payment Method */}
                      <td className="py-3 px-2 text-slate-600 dark:text-slate-300 font-medium">
                        {tx.method}
                      </td>

                      {/* Ringgit Amount */}
                      <td className="py-3 px-2 text-right">
                        <span className="font-black text-sm text-slate-900 dark:text-white">
                          {formatRM(tx.amount)}
                        </span>
                      </td>

                      {/* Delete Action */}
                      <td className="py-3 px-2 text-right">
                        <button
                          onClick={() => onDeleteTransaction(tx.id)}
                          className="text-slate-400 hover:text-rose-500 p-1 rounded-lg transition-colors"
                          title={isBM ? 'Padam Rekod' : 'Delete Record'}
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
        <div className="py-10 flex flex-col items-center justify-center text-center space-y-2">
          <div className="w-12 h-12 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-400 dark:text-slate-500 flex items-center justify-center">
            <Receipt className="w-6 h-6" />
          </div>
          <h4 className="text-sm font-bold text-slate-800 dark:text-slate-200">
            {isBM ? 'Tiada Rekod Transaksi' : 'Zero Transactions in Ledger'}
          </h4>
          <p className="text-xs text-slate-500 dark:text-slate-400 max-w-sm">
            {isBM 
              ? 'Mula rekod perbelanjaan harian anda secara manual atau imbas kod DuitNow QR.'
              : 'Add your first Ringgit transaction above or scan a DuitNow receipt with the OCR scanner.'}
          </p>
        </div>
      )}

    </div>
  );
}
