import React, { useState } from 'react';
import { 
  Search, 
  Filter, 
  Trash2, 
  QrCode, 
  Download, 
  Calendar, 
  AlertTriangle, 
  CheckCircle2,
  DollarSign
} from 'lucide-react';
import { formatRM } from '../utils/helpers';

export default function TransactionLedger({
  transactions,
  onDeleteTransaction,
  onExportCSV,
  language
}) {
  const isBM = language === 'bm';
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedBucket, setSelectedBucket] = useState('all');
  const [selectedMethod, setSelectedMethod] = useState('all');

  const filtered = transactions.filter(tx => {
    const matchesSearch = 
      tx.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      tx.category.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (tx.note && tx.note.toLowerCase().includes(searchTerm.toLowerCase()));
    
    const matchesBucket = selectedBucket === 'all' || tx.bucket === selectedBucket;
    const matchesMethod = selectedMethod === 'all' || tx.method === selectedMethod;

    return matchesSearch && matchesBucket && matchesMethod;
  });

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 sm:p-6 shadow-xl">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-slate-800 pb-4 mb-5">
        <div>
          <h2 className="text-xl font-bold text-white tracking-tight">
            {isBM ? 'Lejar Transaksi Ringgit (Transactions History)' : 'Ringgit Transaction Ledger'}
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            {isBM 
              ? 'Semua rekod perbelanjaan dan pembayaran DuitNow QR, TNG eWallet dan pindahan bank.'
              : 'Every Ringgit tracked across DuitNow QR, eWallets, cards and bank transfers.'}
          </p>
        </div>

        <button
          onClick={onExportCSV}
          className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-slate-950 border border-slate-800 text-xs text-slate-300 hover:text-white hover:border-slate-700 transition-colors self-start sm:self-auto"
        >
          <Download className="w-3.5 h-3.5 text-emerald-400" />
          <span>{isBM ? 'Eksport CSV' : 'Export CSV'}</span>
        </button>
      </div>

      {/* Filters & Search */}
      <div className="grid grid-cols-1 sm:grid-cols-12 gap-3 mb-5">
        
        {/* Search */}
        <div className="sm:col-span-5 relative">
          <Search className="w-4 h-4 absolute left-3 top-2.5 text-slate-500" />
          <input
            type="text"
            placeholder={isBM ? 'Cari peniaga, kedai mamak, bil...' : 'Search merchant, mamak, bills...'}
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-3 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
          />
        </div>

        {/* Bucket Filter */}
        <div className="sm:col-span-4">
          <select
            value={selectedBucket}
            onChange={(e) => setSelectedBucket(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-emerald-500"
          >
            <option value="all">{isBM ? 'Semua Balang' : 'All 3 Buckets'}</option>
            <option value="needs">Keperluan (Needs 50%)</option>
            <option value="wants">Kehendak (Wants 30%)</option>
            <option value="savings">Simpanan & Hutang (Savings/Debt 20%)</option>
            <option value="income">Pendapatan (Income)</option>
          </select>
        </div>

        {/* Payment Method Filter */}
        <div className="sm:col-span-3">
          <select
            value={selectedMethod}
            onChange={(e) => setSelectedMethod(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-emerald-500"
          >
            <option value="all">{isBM ? 'Semua Kaedah' : 'All Payment Methods'}</option>
            <option value="DuitNow QR">DuitNow QR</option>
            <option value="Touch 'n Go eWallet">Touch 'n Go eWallet</option>
            <option value="MAE by Maybank">MAE by Maybank</option>
            <option value="GrabPay">GrabPay</option>
            <option value="SPayLater">SPayLater</option>
            <option value="Credit Card">Credit Card</option>
          </select>
        </div>

      </div>

      {/* Transactions Table / List */}
      <div className="divide-y divide-slate-800/80 max-h-[480px] overflow-y-auto pr-1">
        {filtered.length > 0 ? (
          filtered.map((tx) => {
            const isIncome = tx.bucket === 'income';

            return (
              <div 
                key={tx.id}
                className="py-3 flex items-center justify-between hover:bg-slate-950/40 px-2 rounded-lg transition-colors group"
              >
                <div className="flex items-center space-x-3">
                  <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${
                    isIncome 
                      ? 'bg-emerald-950 border border-emerald-800 text-emerald-400' 
                      : tx.method.includes('DuitNow')
                      ? 'bg-red-950 border border-red-800/60 text-red-400'
                      : 'bg-slate-800 border border-slate-700 text-slate-300'
                  }`}>
                    {tx.method.includes('DuitNow') ? (
                      <QrCode className="w-5 h-5" />
                    ) : (
                      <DollarSign className="w-5 h-5" />
                    )}
                  </div>

                  <div>
                    <div className="flex items-center space-x-2">
                      <span className="text-xs sm:text-sm font-bold text-white">
                        {tx.title}
                      </span>
                      {tx.shameFlag && (
                        <span className="text-[9px] font-extrabold px-1.5 py-0.2 rounded bg-rose-950 text-rose-400 border border-rose-800">
                          Shame!
                        </span>
                      )}
                    </div>

                    <div className="flex items-center space-x-2 text-[11px] text-slate-400 mt-0.5">
                      <span className="text-slate-300 font-medium">{tx.category}</span>
                      <span>•</span>
                      <span>{tx.method}</span>
                      <span>•</span>
                      <span className="text-slate-500">{tx.date}</span>
                    </div>

                    {tx.note && (
                      <p className="text-[10px] text-slate-500 mt-0.5 italic">
                        "{tx.note}"
                      </p>
                    )}
                  </div>
                </div>

                <div className="flex items-center space-x-3">
                  <div className="text-right">
                    <span className={`text-sm sm:text-base font-extrabold block ${
                      isIncome ? 'text-emerald-400' : 'text-slate-100'
                    }`}>
                      {isIncome ? '+' : '-'}{formatRM(tx.amount)}
                    </span>
                    <span className={`text-[10px] uppercase font-bold px-1.5 py-0.2 rounded inline-block ${
                      tx.bucket === 'needs' 
                        ? 'text-emerald-400 bg-emerald-950/60' 
                        : tx.bucket === 'wants' 
                        ? 'text-amber-400 bg-amber-950/60' 
                        : tx.bucket === 'savings' 
                        ? 'text-blue-400 bg-blue-950/60'
                        : 'text-purple-400 bg-purple-950/60'
                    }`}>
                      {tx.bucket}
                    </span>
                  </div>

                  <button
                    onClick={() => onDeleteTransaction(tx.id)}
                    className="opacity-0 group-hover:opacity-100 p-1.5 text-slate-500 hover:text-rose-400 hover:bg-slate-800 rounded transition-all"
                    title="Delete record"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            );
          })
        ) : (
          <div className="text-center py-10 text-xs text-slate-500">
            {isBM ? 'Tiada transaksi dijumpai padanan penapis anda.' : 'No transactions found matching your filters.'}
          </div>
        )}
      </div>

    </div>
  );
}
