import React, { useState } from 'react';
import { X, PlusCircle, QrCode } from 'lucide-react';
import { playSound, fireCelebration } from '../utils/helpers';

export default function AddTransactionModal({
  isOpen,
  onClose,
  onAddTransaction,
  language
}) {
  const isBM = language === 'bm';

  const [title, setTitle] = useState('');
  const [amount, setAmount] = useState('');
  const [bucket, setBucket] = useState('needs');
  const [category, setCategory] = useState('Mamak & Dining');
  const [method, setMethod] = useState('DuitNow QR');
  const [note, setNote] = useState('');

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!title || !amount) return;

    const newTx = {
      title,
      amount: Number(amount),
      bucket,
      category,
      method,
      note,
      date: new Date().toISOString().split('T')[0],
      reviewed: true,
      shameFlag: bucket === 'wants' && Number(amount) > 50,
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
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6 relative">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-800 pb-3 mb-4">
          <div className="flex items-center space-x-2">
            <PlusCircle className="w-5 h-5 text-emerald-400" />
            <h3 className="text-base font-bold text-white">
              {isBM ? 'Rekod Transaksi Ringgit (RM)' : 'Log Ringgit Transaction'}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
          
          {/* Title */}
          <div>
            <label className="text-slate-300 font-semibold block mb-1">
              {isBM ? 'Nama Peniaga / Penerima' : 'Merchant / Recipient Name'}
            </label>
            <input
              type="text"
              required
              placeholder={isBM ? 'cth. Nasi Lemak Wanjo, TNB Bill' : 'e.g. Village Grocer, Shell Petrol'}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
            />
          </div>

          {/* Amount in RM */}
          <div>
            <label className="text-slate-300 font-semibold block mb-1">
              {isBM ? 'Jumlah (RM)' : 'Amount (RM)'}
            </label>
            <div className="relative">
              <span className="absolute left-3 top-2 text-slate-400 font-bold">RM</span>
              <input
                type="number"
                step="0.05"
                required
                placeholder="25.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-11 pr-3 py-2 text-white font-bold text-sm focus:outline-none focus:border-emerald-500"
              />
            </div>
          </div>

          {/* 3-Bucket Classification */}
          <div>
            <label className="text-slate-300 font-semibold block mb-1">
              {isBM ? 'Klasifikasi 3 Balang (Dollarwise Bucket)' : 'Dollarwise 3-Bucket Partition'}
            </label>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => setBucket('needs')}
                className={`py-2 px-1 rounded-xl text-center border font-bold transition-all ${
                  bucket === 'needs'
                    ? 'bg-emerald-950 border-emerald-500 text-emerald-400'
                    : 'bg-slate-950 border-slate-800 text-slate-400 hover:text-white'
                }`}
              >
                Keperluan (50%)
              </button>
              <button
                type="button"
                onClick={() => setBucket('wants')}
                className={`py-2 px-1 rounded-xl text-center border font-bold transition-all ${
                  bucket === 'wants'
                    ? 'bg-amber-950 border-amber-500 text-amber-400'
                    : 'bg-slate-950 border-slate-800 text-slate-400 hover:text-white'
                }`}
              >
                Kehendak (30%)
              </button>
              <button
                type="button"
                onClick={() => setBucket('savings')}
                className={`py-2 px-1 rounded-xl text-center border font-bold transition-all ${
                  bucket === 'savings'
                    ? 'bg-blue-950 border-blue-500 text-blue-400'
                    : 'bg-slate-950 border-slate-800 text-slate-400 hover:text-white'
                }`}
              >
                Hutang/Simpan (20%)
              </button>
            </div>
          </div>

          {/* Malaysian Category & Method */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-slate-300 font-semibold block mb-1">
                {isBM ? 'Kategori' : 'Category'}
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-2.5 py-2 text-white focus:outline-none focus:border-emerald-500"
              >
                <option value="Mamak & Dining">Mamak & Dining</option>
                <option value="Groceries">Groceries</option>
                <option value="Fuel & Transport">Fuel & Transport</option>
                <option value="Utilities">Utilities (TNB/Water)</option>
                <option value="Drinks & Cafe">Drinks & Cafe</option>
                <option value="Shopping">Shopping & Fashion</option>
                <option value="Healthcare">Healthcare</option>
                <option value="Debt Payoff">Debt Payoff</option>
              </select>
            </div>

            <div>
              <label className="text-slate-300 font-semibold block mb-1">
                {isBM ? 'Kaedah Bayaran' : 'Payment Method'}
              </label>
              <select
                value={method}
                onChange={(e) => setMethod(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-2.5 py-2 text-white focus:outline-none focus:border-emerald-500"
              >
                <option value="DuitNow QR">DuitNow QR</option>
                <option value="Touch 'n Go eWallet">Touch 'n Go eWallet</option>
                <option value="MAE by Maybank">MAE by Maybank</option>
                <option value="GrabPay">GrabPay</option>
                <option value="SPayLater">SPayLater (BNPL)</option>
                <option value="Credit Card">Credit Card</option>
                <option value="Cash">Cash (Tunai)</option>
              </select>
            </div>
          </div>

          {/* Note */}
          <div>
            <label className="text-slate-300 font-semibold block mb-1">
              {isBM ? 'Catatan / Nota' : 'Personal Note'}
            </label>
            <input
              type="text"
              placeholder={isBM ? 'cth. Roti canai garing + kopi o' : 'e.g. Weekly family groceries'}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div className="pt-2">
            <button
              type="submit"
              className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-2.5 rounded-xl transition-all shadow-lg shadow-emerald-600/30"
            >
              {isBM ? 'Simpan Transaksi RM' : 'Save Transaction RM'}
            </button>
          </div>

        </form>

      </div>
    </div>
  );
}
