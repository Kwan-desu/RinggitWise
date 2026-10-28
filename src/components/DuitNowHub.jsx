import React, { useState, useEffect, useRef } from 'react';
import QRCode from 'qrcode';
import { 
  QrCode, 
  Scan, 
  Share2, 
  Download, 
  Users, 
  Sparkles, 
  CheckCircle2, 
  ArrowRight, 
  Camera, 
  X,
  PlusCircle,
  Copy,
  Receipt
} from 'lucide-react';
import { formatRM, roundTo5Sen, playSound, fireCelebration } from '../utils/helpers';
import { POPULAR_MALAYSIAN_MERCHANTS } from '../data/initialData';

export default function DuitNowHub({
  onAddTransaction,
  userProfile,
  language,
  onClose
}) {
  const isBM = language === 'bm';
  const [activeTab, setActiveTab] = useState('scan'); // 'scan', 'generate', 'split'

  // Scanner Simulator States
  const [scanning, setScanning] = useState(false);
  const [scannedResult, setScannedResult] = useState(null);
  const [customMerchant, setCustomMerchant] = useState('');
  const [customAmount, setCustomAmount] = useState('');
  const [customBucket, setCustomBucket] = useState('needs');
  const [customCategory, setCustomCategory] = useState('Mamak & Dining');

  // Generator States
  const [genAmount, setGenAmount] = useState('25.00');
  const [genNote, setGenNote] = useState('Mamak Supper Split');
  const [genBank, setGenBank] = useState('MAE by Maybank');
  const qrCanvasRef = useRef(null);

  // Mamak Splitter States
  const [splitBillAmount, setSplitBillAmount] = useState('68.40');
  const [splitPax, setSplitPax] = useState('4');
  const [splitNote, setSplitNote] = useState('Mamak Lepak SS15');
  const [splitIncludeRounding, setSplitIncludeRounding] = useState(true);

  // Generate QR Code when tab is 'generate' or when values change
  useEffect(() => {
    if (activeTab === 'generate' && qrCanvasRef.current) {
      const payload = `DUITNOW|MALAYSIA|MYR|${genAmount}|${encodeURIComponent(userProfile?.name || 'Ahmad')}|${encodeURIComponent(genNote)}`;
      QRCode.toCanvas(qrCanvasRef.current, payload, {
        width: 220,
        margin: 2,
        color: {
          dark: '#0f172a',
          light: '#ffffff'
        }
      }, (error) => {
        if (error) console.error(error);
      });
    }
  }, [activeTab, genAmount, genNote, userProfile]);

  // Handle Quick Merchant Scan
  const handleQuickScan = (merchant) => {
    setScanning(true);
    setTimeout(() => {
      setScanning(false);
      playSound('duitnow');
      fireCelebration();

      const newTx = {
        title: merchant.name,
        category: merchant.category,
        amount: Number(merchant.amount),
        bucket: merchant.bucket,
        method: 'DuitNow QR',
        note: merchant.note,
        reviewed: true,
        shameFlag: merchant.bucket === 'wants',
      };

      onAddTransaction(newTx);

      setScannedResult({
        title: merchant.name,
        amount: merchant.amount,
        bucket: merchant.bucket,
        note: merchant.note,
        method: 'DuitNow QR'
      });
    }, 700);
  };

  // Handle Custom Scan Submission
  const handleCustomScanSubmit = (e) => {
    e.preventDefault();
    if (!customMerchant || !customAmount) return;

    setScanning(true);
    setTimeout(() => {
      setScanning(false);
      playSound('duitnow');
      fireCelebration();

      const newTx = {
        title: customMerchant,
        category: customCategory,
        amount: Number(customAmount),
        bucket: customBucket,
        method: 'DuitNow QR',
        note: `Custom DuitNow Payment to ${customMerchant}`,
        reviewed: true,
        shameFlag: customBucket === 'wants',
      };

      onAddTransaction(newTx);

      setScannedResult({
        title: customMerchant,
        amount: Number(customAmount),
        bucket: customBucket,
        note: newTx.note,
        method: 'DuitNow QR'
      });

      setCustomMerchant('');
      setCustomAmount('');
    }, 600);
  };

  // Mamak Splitter calculation
  const billNum = Number(splitBillAmount) || 0;
  const paxNum = Math.max(1, parseInt(splitPax) || 1);
  const rawPerPerson = billNum / paxNum;
  const perPersonFinal = splitIncludeRounding ? roundTo5Sen(rawPerPerson) : rawPerPerson;

  const handleApplySplitToGenerator = () => {
    setGenAmount(perPersonFinal.toFixed(2));
    setGenNote(`Bayar Bahagian Mamak (${splitNote})`);
    setActiveTab('generate');
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 sm:p-6 shadow-2xl relative">
      
      {/* Top Banner with DuitNow National Identity */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-slate-800 pb-4 mb-6">
        <div className="flex items-center space-x-3">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-tr from-red-600 to-rose-500 flex items-center justify-center shadow-lg shadow-red-500/20 text-white font-black text-xl">
            <QrCode className="w-7 h-7" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h2 className="text-xl font-black text-white tracking-tight">
                DuitNow <span className="text-red-500">QR</span> Hub Malaysia
              </h2>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-red-950 text-red-300 border border-red-800">
                Interoperable QR
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              {isBM 
                ? 'Sokongan pembayaran gaya hidup Malaysia: Imbas DuitNow, Cipta QR & Kira Bil Mamak.'
                : 'Malaysia Lifestyle payments: Scan merchant QR, Generate personal QR & Mamak Bill Splitter.'}
            </p>
          </div>
        </div>

        {/* Tab Switcher */}
        <div className="flex items-center bg-slate-950 p-1 rounded-xl border border-slate-800 text-xs">
          <button
            onClick={() => { setActiveTab('scan'); setScannedResult(null); }}
            className={`flex items-center px-3 py-1.5 rounded-lg font-semibold transition-all ${
              activeTab === 'scan'
                ? 'bg-red-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Scan className="w-3.5 h-3.5 mr-1.5" />
            {isBM ? 'Imbas QR' : 'Scan QR'}
          </button>
          <button
            onClick={() => setActiveTab('generate')}
            className={`flex items-center px-3 py-1.5 rounded-lg font-semibold transition-all ${
              activeTab === 'generate'
                ? 'bg-red-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <QrCode className="w-3.5 h-3.5 mr-1.5" />
            {isBM ? 'Cipta QR Saya' : 'My DuitNow QR'}
          </button>
          <button
            onClick={() => setActiveTab('split')}
            className={`flex items-center px-3 py-1.5 rounded-lg font-semibold transition-all ${
              activeTab === 'split'
                ? 'bg-red-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Users className="w-3.5 h-3.5 mr-1.5" />
            {isBM ? 'Kira Mamak' : 'Split Mamak'}
          </button>
        </div>
      </div>

      {/* TAB 1: SCAN DUITNOW QR SIMULATOR */}
      {activeTab === 'scan' && (
        <div>
          {/* Scanned Success Notification */}
          {scannedResult && (
            <div className="mb-6 p-4 rounded-xl border border-emerald-500/40 bg-emerald-950/40 flex items-start justify-between">
              <div className="flex items-start space-x-3">
                <div className="w-10 h-10 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <div>
                  <span className="text-xs font-bold text-emerald-400 uppercase tracking-wider block">
                    {isBM ? 'Pembayaran DuitNow Berjaya!' : 'DuitNow Payment Confirmed!'}
                  </span>
                  <div className="text-lg font-bold text-white mt-0.5">
                    {scannedResult.title} — {formatRM(scannedResult.amount)}
                  </div>
                  <p className="text-xs text-slate-300 mt-1">
                    {scannedResult.note} • Ditugaskan ke: <span className="font-semibold text-emerald-400 uppercase">{scannedResult.bucket}</span>
                  </p>
                </div>
              </div>
              <button
                onClick={() => setScannedResult(null)}
                className="text-slate-400 hover:text-white p-1"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
            
            {/* Viewfinder Camera Simulation */}
            <div className="lg:col-span-5 bg-slate-950 border border-slate-800 rounded-2xl p-5 text-center relative overflow-hidden">
              <div className="relative w-full aspect-square max-w-[260px] mx-auto bg-slate-900 rounded-xl border-2 border-dashed border-red-500/60 p-4 flex flex-col items-center justify-center">
                
                {/* Laser animation */}
                <div className="absolute inset-x-4 top-4 h-0.5 bg-gradient-to-r from-transparent via-red-500 to-transparent shadow-[0_0_12px_#ED1C24] animate-bounce"></div>

                <div className="w-20 h-20 rounded-2xl bg-red-950/40 border border-red-800/40 flex items-center justify-center text-red-400 mb-3">
                  <Camera className="w-10 h-10 animate-pulse" />
                </div>

                <span className="text-xs font-semibold text-slate-300">
                  {scanning ? (isBM ? 'Memproses DuitNow...' : 'Processing DuitNow...') : (isBM ? 'Halakan ke mana-mana QR DuitNow' : 'Point camera at DuitNow QR')}
                </span>
                <span className="text-[10px] text-slate-500 mt-1">
                  {isBM ? 'Menyokong MAE, TNG, Boost, GrabPay, Bank Islam' : 'Supports all Malaysian banks & eWallets'}
                </span>

                {/* Corner Frame Accents */}
                <div className="absolute top-2 left-2 w-4 h-4 border-t-2 border-l-2 border-red-500"></div>
                <div className="absolute top-2 right-2 w-4 h-4 border-t-2 border-r-2 border-red-500"></div>
                <div className="absolute bottom-2 left-2 w-4 h-4 border-b-2 border-l-2 border-red-500"></div>
                <div className="absolute bottom-2 right-2 w-4 h-4 border-b-2 border-r-2 border-red-500"></div>
              </div>

              {/* Manual Custom Amount Entry */}
              <form onSubmit={handleCustomScanSubmit} className="mt-4 pt-4 border-t border-slate-800/80 text-left">
                <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block mb-2">
                  {isBM ? 'Atau Masukkan QR Tersuai:' : 'Or Enter Custom Merchant QR:'}
                </span>
                <div className="grid grid-cols-2 gap-2 mb-2">
                  <input
                    type="text"
                    value={customMerchant}
                    onChange={(e) => setCustomMerchant(e.target.value)}
                    placeholder={isBM ? 'cth. Roti Canai Transfer' : 'e.g. Mamak Stall SS2'}
                    className="bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-1.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-red-500"
                  />
                  <div className="relative">
                    <span className="absolute left-2.5 top-1.5 text-xs text-slate-400">RM</span>
                    <input
                      type="number"
                      step="0.05"
                      value={customAmount}
                      onChange={(e) => setCustomAmount(e.target.value)}
                      placeholder="15.00"
                      className="w-full bg-slate-900 border border-slate-700 rounded-lg pl-8 pr-2.5 py-1.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-red-500"
                    />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-2 mb-2">
                  <select
                    value={customBucket}
                    onChange={(e) => setCustomBucket(e.target.value)}
                    className="bg-slate-900 border border-slate-700 rounded-lg px-2 py-1.5 text-xs text-white focus:outline-none focus:border-red-500"
                  >
                    <option value="needs">Keperluan (Needs)</option>
                    <option value="wants">Kehendak (Wants)</option>
                  </select>
                  <select
                    value={customCategory}
                    onChange={(e) => setCustomCategory(e.target.value)}
                    className="bg-slate-900 border border-slate-700 rounded-lg px-2 py-1.5 text-xs text-white focus:outline-none focus:border-red-500"
                  >
                    <option value="Mamak & Dining">Mamak & Dining</option>
                    <option value="Groceries">Groceries</option>
                    <option value="Fuel & Transport">Fuel & Transport</option>
                    <option value="Drinks & Cafe">Drinks & Cafe</option>
                    <option value="Utilities">Utilities</option>
                  </select>
                </div>
                <button
                  type="submit"
                  disabled={!customMerchant || !customAmount}
                  className="w-full bg-red-600 hover:bg-red-500 disabled:opacity-50 text-white font-bold text-xs py-2 rounded-lg transition-all shadow-md shadow-red-600/20"
                >
                  {isBM ? 'Imbas & Bayar Sekarang' : 'Scan & Pay RM Now'}
                </button>
              </form>
            </div>

            {/* Popular Malaysian Merchant 1-Tap Quick Pay Grid */}
            <div className="lg:col-span-7">
              <div className="flex items-center justify-between mb-3">
                <span className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center">
                  <Sparkles className="w-3.5 h-3.5 mr-1.5 text-red-400" />
                  {isBM ? 'Peniaga Pilihan Rakyat Malaysia (1-Tap Imbas):' : 'Popular Malaysian Lifestyle Merchants (1-Tap):'}
                </span>
                <span className="text-[11px] text-slate-500">
                  {isBM ? 'Klik mana-mana untuk simulasi' : 'Click to test live scan'}
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {POPULAR_MALAYSIAN_MERCHANTS.map((m, idx) => (
                  <button
                    key={idx}
                    onClick={() => handleQuickScan(m)}
                    className="flex items-center justify-between p-3.5 rounded-xl border border-slate-800 bg-slate-950/70 hover:border-red-500/60 hover:bg-slate-900 text-left transition-all group"
                  >
                    <div className="flex items-center space-x-3">
                      <div className="w-10 h-10 rounded-lg bg-red-950/60 border border-red-800/40 text-red-400 flex items-center justify-center font-black text-xs group-hover:scale-105 transition-transform">
                        {m.logoText}
                      </div>
                      <div>
                        <span className="text-xs font-bold text-white group-hover:text-red-400 transition-colors block">
                          {m.name}
                        </span>
                        <span className="text-[10px] text-slate-400 block line-clamp-1">
                          {m.note}
                        </span>
                        <span className={`text-[9px] font-semibold uppercase px-1.5 py-0.2 rounded inline-block mt-0.5 ${
                          m.bucket === 'needs' ? 'text-emerald-400 bg-emerald-950/60' : 'text-amber-400 bg-amber-950/60'
                        }`}>
                          {m.bucket === 'needs' ? 'Keperluan' : 'Kehendak'}
                        </span>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className="text-sm font-extrabold text-white block">
                        {formatRM(m.amount)}
                      </span>
                      <span className="text-[10px] text-red-400 font-semibold flex items-center justify-end mt-1">
                        Imbas <ArrowRight className="w-2.5 h-2.5 ml-0.5 group-hover:translate-x-0.5 transition-transform" />
                      </span>
                    </div>
                  </button>
                ))}
              </div>
            </div>

          </div>
        </div>
      )}

      {/* TAB 2: MY DUITNOW QR GENERATOR */}
      {activeTab === 'generate' && (
        <div className="max-w-xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-center">
            
            {/* The Authentic DuitNow QR Card Template */}
            <div className="md:col-span-6 flex justify-center">
              <div className="w-[260px] bg-white rounded-2xl p-4 shadow-2xl border-4 border-red-600 text-slate-900 text-center">
                {/* Official DuitNow Header */}
                <div className="flex items-center justify-between pb-2 mb-2 border-b border-slate-200">
                  <div className="flex items-center space-x-1.5">
                    <div className="w-6 h-6 rounded bg-red-600 text-white font-black text-xs flex items-center justify-center">
                      DN
                    </div>
                    <span className="font-black text-sm tracking-tight text-red-600">
                      DuitNow
                    </span>
                  </div>
                  <span className="text-[9px] font-bold text-slate-500 uppercase">
                    QR Pay
                  </span>
                </div>

                {/* Recipient info */}
                <div className="my-1">
                  <div className="font-bold text-xs text-slate-800 line-clamp-1">
                    {userProfile?.name || 'Ahmad / Kwan'}
                  </div>
                  <span className="text-[10px] text-slate-500 block">
                    {genBank}
                  </span>
                </div>

                {/* The QR Canvas */}
                <div className="p-2 bg-slate-50 rounded-xl my-2 flex justify-center">
                  <canvas ref={qrCanvasRef} className="rounded-lg shadow-sm" />
                </div>

                {/* Amount to receive */}
                <div className="mt-2 pt-2 border-t border-slate-200">
                  <span className="text-[10px] text-slate-500 uppercase font-semibold block">
                    {isBM ? 'Jumlah Perlu Dibayar' : 'Amount to Receive'}
                  </span>
                  <div className="text-xl font-black text-red-600">
                    {formatRM(genAmount)}
                  </div>
                  <span className="text-[10px] text-slate-600 italic block mt-0.5 line-clamp-1">
                    "{genNote}"
                  </span>
                </div>

                {/* Supported Apps Footer */}
                <div className="mt-2 pt-1.5 border-t border-slate-100 text-[8px] text-slate-400">
                  MAE • TNG eWallet • CIMB • Boost • GrabPay
                </div>
              </div>
            </div>

            {/* Generator Controls */}
            <div className="md:col-span-6 bg-slate-950 p-5 rounded-2xl border border-slate-800">
              <h3 className="text-sm font-bold text-white mb-3 flex items-center">
                <Sparkles className="w-4 h-4 mr-1.5 text-red-400" />
                {isBM ? 'Ubah Suai DuitNow QR Anda' : 'Customize Your DuitNow QR'}
              </h3>

              <div className="space-y-3 text-xs">
                <div>
                  <label className="text-slate-400 block mb-1 font-medium">
                    {isBM ? 'Jumlah (RM)' : 'Amount (RM)'}
                  </label>
                  <div className="relative">
                    <span className="absolute left-3 top-2 text-slate-400 font-bold">RM</span>
                    <input
                      type="number"
                      step="0.05"
                      value={genAmount}
                      onChange={(e) => setGenAmount(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-700 rounded-lg pl-10 pr-3 py-2 text-white font-bold text-sm focus:outline-none focus:border-red-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="text-slate-400 block mb-1 font-medium">
                    {isBM ? 'Tujuan / Nota Bayaran' : 'Payment Note'}
                  </label>
                  <input
                    type="text"
                    value={genNote}
                    onChange={(e) => setGenNote(e.target.value)}
                    placeholder="cth. Bahagian Makan Tengah Hari"
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-red-500"
                  />
                </div>

                <div>
                  <label className="text-slate-400 block mb-1 font-medium">
                    {isBM ? 'Akaun Penerima (Bank / eWallet)' : 'Receiving Account'}
                  </label>
                  <select
                    value={genBank}
                    onChange={(e) => setGenBank(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-red-500"
                  >
                    <option value="MAE by Maybank">MAE by Maybank</option>
                    <option value="Touch 'n Go eWallet">Touch 'n Go eWallet</option>
                    <option value="CIMB Octo">CIMB Octo</option>
                    <option value="Bank Islam">Bank Islam</option>
                    <option value="GrabPay">GrabPay</option>
                    <option value="RHB Mobile">RHB Mobile</option>
                  </select>
                </div>

                <div className="pt-2 flex space-x-2">
                  <button
                    onClick={() => {
                      if (qrCanvasRef.current) {
                        const link = document.createElement('a');
                        link.download = `DuitNow-QR-${genAmount}.png`;
                        link.href = qrCanvasRef.current.toDataURL();
                        link.click();
                      }
                    }}
                    className="flex-1 bg-red-600 hover:bg-red-500 text-white font-bold py-2 rounded-lg flex items-center justify-center space-x-1.5 transition-all shadow-md shadow-red-600/20"
                  >
                    <Download className="w-4 h-4" />
                    <span>{isBM ? 'Muat Turun PNG' : 'Download PNG'}</span>
                  </button>
                  <button
                    onClick={() => {
                      navigator.clipboard.writeText(`DuitNow RM${genAmount} to ${userProfile?.name || 'Me'} for ${genNote}`);
                      alert(isBM ? 'Disalin ke papan keratan!' : 'Copied payment request link!');
                    }}
                    className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg border border-slate-700 transition-colors"
                    title="Copy details"
                  >
                    <Copy className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>

          </div>
        </div>
      )}

      {/* TAB 3: KIRA MAMAK (BILL SPLITTER) */}
      {activeTab === 'split' && (
        <div className="max-w-2xl mx-auto">
          <div className="bg-slate-950 border border-slate-800 rounded-2xl p-6">
            <div className="flex items-center space-x-2 mb-4">
              <Receipt className="w-5 h-5 text-red-400" />
              <h3 className="text-base font-bold text-white">
                {isBM ? 'Kira-Kira Bil Mamak (Pembahagi Bil Pintar)' : 'Kira Mamak — Malaysian Bill Splitter'}
              </h3>
            </div>
            <p className="text-xs text-slate-400 mb-6 leading-relaxed">
              {isBM 
                ? 'Lepak mamak bersama kawan-kawan? Bahagikan bil mengikut dasar pembundaran 5 sen Bank Negara Malaysia dan cipta DuitNow QR serta-merta!'
                : 'Mamak supper with friends? Automatically split the bill with Bank Negara 5-sen rounding and generate an instant DuitNow QR for everyone to pay!'}
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-5">
              <div>
                <label className="text-xs text-slate-400 block mb-1 font-medium">
                  {isBM ? 'Jumlah Bil (RM)' : 'Total Bill (RM)'}
                </label>
                <div className="relative">
                  <span className="absolute left-3 top-2 text-xs text-slate-400 font-bold">RM</span>
                  <input
                    type="number"
                    step="0.05"
                    value={splitBillAmount}
                    onChange={(e) => setSplitBillAmount(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 rounded-lg pl-10 pr-3 py-2 text-white font-bold text-sm focus:outline-none focus:border-red-500"
                  />
                </div>
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1 font-medium">
                  {isBM ? 'Bilangan Orang (Pax)' : 'Number of People'}
                </label>
                <input
                  type="number"
                  min="1"
                  max="50"
                  value={splitPax}
                  onChange={(e) => setSplitPax(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-white font-bold text-sm focus:outline-none focus:border-red-500"
                />
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1 font-medium">
                  {isBM ? 'Nama Sesi Lepak' : 'Hangout Occasion'}
                </label>
                <input
                  type="text"
                  value={splitNote}
                  onChange={(e) => setSplitNote(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-red-500"
                />
              </div>
            </div>

            {/* Bank Negara 5 sen rounding toggle */}
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-900 border border-slate-800 text-xs mb-6">
              <span className="text-slate-300">
                {isBM ? 'Guna Pembundaran 5 Sen Bank Negara Malaysia' : 'Apply Bank Negara 5-sen Rounding'}
              </span>
              <input
                type="checkbox"
                checked={splitIncludeRounding}
                onChange={(e) => setSplitIncludeRounding(e.target.checked)}
                className="w-4 h-4 accent-red-600 rounded cursor-pointer"
              />
            </div>

            {/* Split Result Card */}
            <div className="bg-gradient-to-r from-red-950/40 via-slate-900 to-slate-900 border border-red-500/40 rounded-xl p-5 flex flex-col sm:flex-row items-center justify-between gap-4">
              <div>
                <span className="text-xs text-slate-400 font-medium">
                  {isBM ? 'Setiap Orang Perlu Bayar:' : 'Each Person Pays:'}
                </span>
                <div className="text-3xl font-black text-red-400 tracking-tight mt-0.5">
                  {formatRM(perPersonFinal)}
                </div>
                <span className="text-[10px] text-slate-500 mt-1 block">
                  {isBM ? `Jumlah RM ${billNum.toFixed(2)} dibahagi ${paxNum} orang` : `Total RM ${billNum.toFixed(2)} divided by ${paxNum} friends`}
                </span>
              </div>

              <button
                onClick={handleApplySplitToGenerator}
                className="w-full sm:w-auto bg-red-600 hover:bg-red-500 text-white font-bold text-xs px-4 py-2.5 rounded-xl shadow-lg shadow-red-600/30 flex items-center justify-center space-x-1.5 transition-all"
              >
                <QrCode className="w-4 h-4" />
                <span>{isBM ? 'Jana DuitNow QR Bahagian Ini' : 'Generate DuitNow QR For This'}</span>
              </button>
            </div>

          </div>
        </div>
      )}

    </div>
  );
}
