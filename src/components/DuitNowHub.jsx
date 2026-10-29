import React, { useState, useEffect, useRef, useMemo } from 'react';
import QRCode from 'qrcode';
import { 
  QrCode, 
  Camera, 
  X,
  Receipt,
  Upload,
  FileText,
  Loader2,
  Check,
  Users,
  FileSpreadsheet,
  ClipboardPaste,
  Sparkles,
  CheckSquare,
  Square,
  ArrowUpDown,
  Filter
} from 'lucide-react';
import { formatRM, roundTo5Sen, playSound, fireCelebration } from '../utils/helpers';
import { POPULAR_MALAYSIAN_MERCHANTS } from '../data/initialData';
import { processFileForTransactions, parseFinancialText } from '../utils/statementParser';

export default function DuitNowHub({
  onAddTransaction,
  onAddMultipleTransactions,
  categories = [],
  onQuickAddCategory,
  userProfile,
  language = 'en',
  onClose
}) {
  const isBM = language === 'bm';
  const [activeTab, setActiveTab] = useState('ocr'); // 'ocr', 'camera_qr', 'generate', 'split'

  // ==========================================
  // 1. OCR & STATEMENT SCANNER STATES
  // ==========================================
  const [ocrLoading, setOcrLoading] = useState(false);
  const [ocrStatusText, setOcrStatusText] = useState('');
  const [ocrResult, setOcrResult] = useState(null);
  const [inputMode, setInputMode] = useState('upload'); // 'upload' | 'paste'
  const [pastedText, setPastedText] = useState('');
  const [statementSortBy, setStatementSortBy] = useState('category'); // 'category' | 'date' | 'amount'
  const [statementFilterCat, setStatementFilterCat] = useState('all');
  const fileInputRef = useRef(null);

  // File upload handler (PDF or Image)
  const handleFileUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setOcrLoading(true);
    setOcrStatusText(isBM ? 'Menganalisis dokumen...' : 'Analyzing document...');

    try {
      const result = await processFileForTransactions(file, (msg) => {
        setOcrStatusText(msg);
      });
      setOcrResult(result);
      if (result.items?.length > 0) {
        playSound('duitnow');
      }
    } catch (err) {
      console.error('File processing error:', err);
      setOcrResult({
        type: 'error',
        items: [],
        rawText: err?.message || 'Error parsing document',
      });
    } finally {
      setOcrLoading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  // Text paste parser handler
  const handleParsePastedText = () => {
    if (!pastedText.trim()) return;
    setOcrLoading(true);
    setOcrStatusText(isBM ? 'Menghuraikan teks penyata...' : 'Parsing statement text...');

    setTimeout(() => {
      try {
        const result = parseFinancialText(pastedText);
        setOcrResult(result);
        if (result.items?.length > 0) {
          playSound('duitnow');
        }
      } catch (err) {
        console.error('Text parsing error:', err);
      } finally {
        setOcrLoading(false);
      }
    }, 150);
  };

  // Single transaction confirm handler
  const handleConfirmSingleTransaction = (item) => {
    const target = item || ocrResult?.items?.[0];
    if (!target) return;

    // Ensure category exists
    const catName = target.category || 'Mamak & Dining';
    if (!categories.some(c => c.name.toLowerCase() === catName.toLowerCase()) && onQuickAddCategory) {
      onQuickAddCategory({ name: catName });
    }

    const newTx = {
      title: target.title || 'Scanned Expense',
      amount: Number(target.amount || 0),
      category: catName,
      method: target.method || 'DuitNow / Card',
      note: target.note || 'Scanned',
      date: target.date || new Date().toISOString().split('T')[0],
    };

    onAddTransaction(newTx);
    playSound('approve');
    fireCelebration();
    setOcrResult(null);
    if (onClose) onClose();
  };

  // Multi-transaction statement import handler
  const handleImportSelectedTransactions = () => {
    if (!ocrResult || !Array.isArray(ocrResult.items)) return;

    const selectedItems = ocrResult.items.filter(item => item.selected && item.amount > 0 && item.type !== 'credit');
    if (selectedItems.length === 0) {
      alert(isBM ? 'Sila pilih sekurang-kurangnya satu perbelanjaan untuk diimport.' : 'Please select at least one expense to import.');
      return;
    }

    // Auto-register any missing categories into category list
    const existingNames = new Set(categories.map(c => c.name.toLowerCase()));
    selectedItems.forEach(item => {
      if (item.category && !existingNames.has(item.category.toLowerCase())) {
        existingNames.add(item.category.toLowerCase());
        if (onQuickAddCategory) {
          onQuickAddCategory({ name: item.category });
        }
      }
    });

    if (onAddMultipleTransactions) {
      onAddMultipleTransactions(selectedItems);
    } else {
      selectedItems.forEach(item => onAddTransaction(item));
    }

    playSound('approve');
    fireCelebration();
    setOcrResult(null);
    if (onClose) onClose();
  };

  // Toggle select in statement list
  const toggleItemSelection = (id) => {
    if (!ocrResult || !ocrResult.items) return;
    setOcrResult({
      ...ocrResult,
      items: ocrResult.items.map(item => 
        item.id === id ? { ...item, selected: !item.selected } : item
      )
    });
  };

  const toggleSelectAll = (select) => {
    if (!ocrResult || !ocrResult.items) return;
    setOcrResult({
      ...ocrResult,
      items: ocrResult.items.map(item => {
        // If filtering by specific category, only toggle matching items
        if (statementFilterCat !== 'all' && item.category !== statementFilterCat) {
          return item;
        }
        return { ...item, selected: select && item.type !== 'credit' };
      })
    });
  };

  // Update item field in statement list
  const updateStatementItem = (id, field, value) => {
    if (!ocrResult || !ocrResult.items) return;
    setOcrResult({
      ...ocrResult,
      items: ocrResult.items.map(item => 
        item.id === id ? { ...item, [field]: value } : item
      )
    });
  };

  // Compute Category Counts for Filter Chips
  const categoryCounts = useMemo(() => {
    if (!ocrResult || !ocrResult.items) return {};
    const counts = {};
    ocrResult.items.forEach(it => {
      if (it.type !== 'credit' && it.amount > 0) {
        const cat = it.category || 'Mamak & Dining';
        counts[cat] = (counts[cat] || 0) + 1;
      }
    });
    return counts;
  }, [ocrResult]);

  // Compute Sorted and Filtered Statement Items
  const displayedStatementItems = useMemo(() => {
    if (!ocrResult || !ocrResult.items) return [];
    let list = [...ocrResult.items];

    // Filter
    if (statementFilterCat !== 'all') {
      list = list.filter(it => it.category === statementFilterCat);
    }

    // Sort
    if (statementSortBy === 'category') {
      list.sort((a, b) => a.category.localeCompare(b.category) || a.date.localeCompare(b.date));
    } else if (statementSortBy === 'date') {
      list.sort((a, b) => a.date.localeCompare(b.date));
    } else if (statementSortBy === 'amount') {
      list.sort((a, b) => b.amount - a.amount);
    }

    return list;
  }, [ocrResult, statementFilterCat, statementSortBy]);

  // All combined available categories (including any detected in statement)
  const allAvailableCategories = useMemo(() => {
    const list = [...categories];
    const existing = new Set(list.map(c => c.name.toLowerCase()));
    
    // Always guarantee Parking & Tolls exists
    if (!existing.has('parking & tolls')) {
      list.unshift({ id: 'cat-parking', name: 'Parking & Tolls', color: '#54b3d6' });
      existing.add('parking & tolls');
    }

    // Add any category from statement if not already present
    if (ocrResult?.items) {
      ocrResult.items.forEach(it => {
        if (it.category && !existing.has(it.category.toLowerCase())) {
          list.push({ id: `cat-detected-${it.category}`, name: it.category });
          existing.add(it.category.toLowerCase());
        }
      });
    }

    return list;
  }, [categories, ocrResult]);

  // ==========================================
  // 2. LIVE CAMERA / QR CODE SCANNER STATES
  // ==========================================
  const [cameraActive, setCameraActive] = useState(false);
  const videoRef = useRef(null);

  const startCamera = async () => {
    setCameraActive(true);
    try {
      if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
        if (videoRef.current) {
          videoRef.current.srcObject = stream;
        }
      }
    } catch (err) {
      console.warn('Camera feed simulated in browser sandbox.', err);
    }
  };

  const stopCamera = () => {
    if (videoRef.current && videoRef.current.srcObject) {
      const tracks = videoRef.current.srcObject.getTracks();
      tracks.forEach(track => track.stop());
    }
    setCameraActive(false);
  };

  useEffect(() => {
    return () => {
      stopCamera();
    };
  }, []);

  // ==========================================
  // 3. GENERATOR STATES
  // ==========================================
  const [genAmount, setGenAmount] = useState('25.00');
  const [genNote, setGenNote] = useState('Mamak Supper Split');
  const qrCanvasRef = useRef(null);

  useEffect(() => {
    if (activeTab === 'generate' && qrCanvasRef.current) {
      const payload = `DUITNOW|MYR|${genAmount}|${encodeURIComponent(userProfile?.name || 'User')}|${encodeURIComponent(genNote)}`;
      QRCode.toCanvas(qrCanvasRef.current, payload, {
        width: 200,
        margin: 2,
        color: {
          dark: '#09090b',
          light: '#ffffff'
        }
      });
    }
  }, [activeTab, genAmount, genNote, userProfile]);

  // ==========================================
  // 4. MAMAK BILL SPLITTER STATES
  // ==========================================
  const [splitTotal, setSplitTotal] = useState('48.60');
  const [splitPpl, setSplitPpl] = useState(3);
  const [splitCustomTip, setSplitCustomTip] = useState(0);

  const rawPerPerson = (parseFloat(splitTotal || 0) + parseFloat(splitCustomTip || 0)) / (parseInt(splitPpl || 1) || 1);
  const roundedPerPerson = roundTo5Sen(rawPerPerson);

  return (
    <div className="bg-white dark:bg-zinc-900 border border-zinc-200/80 dark:border-zinc-800/80 rounded-2xl p-5 sm:p-6 shadow-xs transition-colors">
      
      {/* Component Title & Quick Close */}
      <div className="flex items-center justify-between pb-3.5 border-b border-zinc-100 dark:border-zinc-800/80 mb-4">
        <div className="flex items-center space-x-2.5">
          <div className="w-8 h-8 rounded-lg bg-rose-500/10 dark:bg-rose-500/20 flex items-center justify-center text-rose-600 dark:text-rose-400 font-bold text-xs">
            RM
          </div>
          <div>
            <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100 flex items-center space-x-2">
              <span>{isBM ? 'Pusat DuitNow & Penyata OCR' : 'DuitNow & Statement OCR'}</span>
              <span className="text-[10px] bg-rose-100 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 px-2 py-0.5 rounded-full font-medium">
                PDF + Image
              </span>
            </h3>
            <p className="text-[11px] text-zinc-500 dark:text-zinc-400">
              {isBM 
                ? 'Imbas penyata APSpace PDF, resit MAE/TNG, atau susun mengikut tempat letak kereta & tol' 
                : 'Parse APSpace PDF statements, auto-sort Parking & Tolls, or scan QR'}
            </p>
          </div>
        </div>

        {onClose && (
          <button
            onClick={() => { stopCamera(); onClose(); }}
            className="p-1 rounded-lg text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Segmented Navigation Tabs */}
      <div className="flex space-x-1 p-0.5 bg-zinc-100 dark:bg-zinc-800/60 rounded-xl mb-5 text-xs">
        <button
          onClick={() => { setActiveTab('ocr'); stopCamera(); }}
          className={`flex-1 py-1.5 rounded-lg font-medium transition-all ${
            activeTab === 'ocr'
              ? 'bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100 shadow-2xs'
              : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100'
          }`}
        >
          {isBM ? 'Imbas Penyata / Resit' : 'Statement & Slip OCR'}
        </button>

        <button
          onClick={() => { setActiveTab('camera_qr'); startCamera(); }}
          className={`flex-1 py-1.5 rounded-lg font-medium transition-all ${
            activeTab === 'camera_qr'
              ? 'bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100 shadow-2xs'
              : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100'
          }`}
        >
          {isBM ? 'Kamera QR' : 'Scan QR'}
        </button>

        <button
          onClick={() => { setActiveTab('generate'); stopCamera(); }}
          className={`flex-1 py-1.5 rounded-lg font-medium transition-all ${
            activeTab === 'generate'
              ? 'bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100 shadow-2xs'
              : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100'
          }`}
        >
          {isBM ? 'Jana QR' : 'Generate QR'}
        </button>

        <button
          onClick={() => { setActiveTab('split'); stopCamera(); }}
          className={`flex-1 py-1.5 rounded-lg font-medium transition-all ${
            activeTab === 'split'
              ? 'bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100 shadow-2xs'
              : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100'
          }`}
        >
          {isBM ? 'Bahagi Bil' : 'Split Bill'}
        </button>
      </div>

      {/* ================================================================= */}
      {/* TAB 1: RECEIPT & STATEMENT OCR SCANNER                            */}
      {/* ================================================================= */}
      {activeTab === 'ocr' && (
        <div className="space-y-4">
          
          {/* Submode Switcher: File Upload vs Direct Text Paste */}
          <div className="flex items-center justify-between text-xs pb-2 border-b border-zinc-100 dark:border-zinc-800/60">
            <div className="flex space-x-2">
              <button
                onClick={() => setInputMode('upload')}
                className={`px-2.5 py-1 rounded-md font-medium transition-colors ${
                  inputMode === 'upload'
                    ? 'bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900'
                    : 'text-zinc-500 hover:text-zinc-800 dark:hover:text-zinc-200'
                }`}
              >
                {isBM ? 'Muat Naik Fail (PDF / Imej)' : 'Upload File (PDF / Image)'}
              </button>
              <button
                onClick={() => setInputMode('paste')}
                className={`px-2.5 py-1 rounded-md font-medium transition-colors ${
                  inputMode === 'paste'
                    ? 'bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900'
                    : 'text-zinc-500 hover:text-zinc-800 dark:hover:text-zinc-200'
                }`}
              >
                {isBM ? 'Tampal Teks Penyata' : 'Paste Statement Text'}
              </button>
            </div>

            {ocrResult && (
              <button
                onClick={() => setOcrResult(null)}
                className="text-[11px] text-zinc-400 hover:text-rose-500 transition-colors"
              >
                {isBM ? 'Kosongkan' : 'Clear result'}
              </button>
            )}
          </div>

          {/* Mode 1: File Upload Dropzone */}
          {inputMode === 'upload' && !ocrResult && (
            <div>
              <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileUpload}
                accept="image/*,application/pdf,.pdf"
                className="hidden"
              />

              <div
                onClick={() => fileInputRef.current?.click()}
                className="border-2 border-dashed border-zinc-300 dark:border-zinc-700 hover:border-zinc-500 dark:hover:border-zinc-400 rounded-xl p-6 flex flex-col items-center justify-center text-center cursor-pointer transition-colors bg-zinc-50/60 dark:bg-zinc-950/40"
              >
                <Upload className="w-6 h-6 text-zinc-400 mb-2 animate-pulse" />
                <span className="text-xs font-semibold text-zinc-800 dark:text-zinc-200">
                  {isBM ? 'Klik untuk muat naik Penyata PDF atau Imej Resit' : 'Click to upload PDF Statement or Receipt Image'}
                </span>
                <span className="text-[11px] text-zinc-400 mt-1 max-w-sm">
                  {isBM 
                    ? 'Sokong penyata APSpace PDF, e-penyata Maybank/CIMB/TNG, dan tangkap layar resit'
                    : 'Supports APSpace PDF reports, university cards, Maybank/TNG e-statements, and cropped slips'}
                </span>
              </div>
            </div>
          )}

          {/* Mode 2: Paste Statement Text Directly */}
          {inputMode === 'paste' && !ocrResult && (
            <div className="space-y-2">
              <textarea
                value={pastedText}
                onChange={(e) => setPastedText(e.target.value)}
                placeholder={isBM 
                  ? "Tampal baris dari laporan APSpace atau penyata bank di sini...\nContoh:\n18 2026-10-16 12:48:04 Parking Fee 3.94\n4 2026-10-05 12:46:22 Creamy Mushroom Katsu Chicken Rice 9.5"
                  : "Paste lines from your APSpace report or bank statement here...\nExample:\n18 2026-10-16 12:48:04 Parking Fee 3.94\n4 2026-10-05 12:46:22 Creamy Mushroom Katsu Chicken Rice 9.5"}
                rows={5}
                className="w-full bg-zinc-50/60 dark:bg-zinc-950/40 border border-zinc-300 dark:border-zinc-700 rounded-xl p-3 text-xs font-mono text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-400 focus:outline-none focus:ring-1 focus:ring-zinc-500"
              />

              <div className="flex justify-end">
                <button
                  type="button"
                  onClick={handleParsePastedText}
                  disabled={!pastedText.trim()}
                  className="px-4 py-1.5 bg-zinc-900 hover:bg-zinc-800 dark:bg-zinc-100 dark:hover:bg-white text-white dark:text-zinc-900 rounded-lg text-xs font-semibold disabled:opacity-40 transition-colors"
                >
                  {isBM ? 'Ekstrak & Huraikan Penyata' : 'Parse Statement Text'}
                </button>
              </div>
            </div>
          )}

          {/* Loading Indicator */}
          {ocrLoading && (
            <div className="border border-zinc-200/80 dark:border-zinc-800/80 rounded-xl p-6 flex flex-col items-center justify-center text-center space-y-2 bg-zinc-50/40 dark:bg-zinc-900/40">
              <Loader2 className="w-6 h-6 text-zinc-600 dark:text-zinc-400 animate-spin" />
              <span className="text-xs font-medium text-zinc-700 dark:text-zinc-200">
                {ocrStatusText}
              </span>
              <span className="text-[10.5px] text-zinc-400">
                {isBM ? 'Mengekstrak baris urus niaga dan memadankan Tempat Letak Kereta & Makanan...' : 'Sorting transactions into Parking & Tolls, Dining, Drinks & Snacks...'}
              </span>
            </div>
          )}

          {/* Result View A: MULTI-TRANSACTION STATEMENT (e.g. APCard 30+ items) */}
          {ocrResult && ocrResult.type === 'statement' && (
            <div className="border border-zinc-200 dark:border-zinc-800 rounded-xl p-4 bg-zinc-50/50 dark:bg-zinc-950/40 space-y-3">
              
              {/* Header Bar */}
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 border-b border-zinc-200 dark:border-zinc-800/80 pb-3">
                <div>
                  <div className="flex items-center space-x-2">
                    <span className="text-xs font-bold text-zinc-900 dark:text-zinc-100">
                      {isBM ? 'Penyata Dikenal Pasti' : 'Statement Detected'}
                    </span>
                    <span className="text-[10px] bg-emerald-100 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-400 px-2 py-0.5 rounded-full font-semibold">
                      {ocrResult.items.filter(i => i.selected).length} / {ocrResult.items.length} {isBM ? 'dipilih' : 'selected'}
                    </span>
                  </div>
                  <p className="text-[11px] text-zinc-500 dark:text-zinc-400 mt-0.5">
                    {isBM ? 'Jumlah belanja perbelanjaan: ' : 'Total debit expenses: '}
                    <span className="font-semibold text-zinc-900 dark:text-zinc-100 tabular-nums">
                      {formatRM(ocrResult.items.filter(i => i.selected).reduce((acc, t) => acc + (t.amount || 0), 0))}
                    </span>
                  </p>
                </div>

                <div className="flex items-center space-x-2 text-[11px]">
                  <button
                    onClick={() => toggleSelectAll(true)}
                    className="text-zinc-600 dark:text-zinc-300 hover:underline"
                  >
                    {isBM ? 'Pilih Semua' : 'Select All'}
                  </button>
                  <span className="text-zinc-300 dark:text-zinc-700">|</span>
                  <button
                    onClick={() => toggleSelectAll(false)}
                    className="text-zinc-600 dark:text-zinc-300 hover:underline"
                  >
                    {isBM ? 'Nyahpilih' : 'Deselect All'}
                  </button>
                </div>
              </div>

              {/* Category Filter Chips & Sort Controls */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pt-1 pb-2 border-b border-zinc-100 dark:border-zinc-800/60 text-[11px]">
                
                {/* Filter Chips */}
                <div className="flex flex-wrap items-center gap-1.5">
                  <button
                    type="button"
                    onClick={() => setStatementFilterCat('all')}
                    className={`px-2.5 py-0.5 rounded-full font-medium transition-colors ${
                      statementFilterCat === 'all'
                        ? 'bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 font-semibold'
                        : 'bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-200'
                    }`}
                  >
                    {isBM ? 'Semua' : 'All'} ({ocrResult.items.filter(i => i.type !== 'credit').length})
                  </button>

                  {Object.entries(categoryCounts).map(([catName, count]) => (
                    <button
                      key={catName}
                      type="button"
                      onClick={() => setStatementFilterCat(catName)}
                      className={`px-2.5 py-0.5 rounded-full font-medium transition-colors flex items-center space-x-1 ${
                        statementFilterCat === catName
                          ? 'bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 font-semibold'
                          : 'bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-200'
                      }`}
                    >
                      <span>{catName}</span>
                      <span className="opacity-70 font-mono">({count})</span>
                    </button>
                  ))}
                </div>

                {/* Sort dropdown */}
                <div className="flex items-center space-x-1.5 shrink-0 text-zinc-500 self-end sm:self-center">
                  <ArrowUpDown className="w-3 h-3 text-zinc-400" />
                  <span className="text-[10px] uppercase font-semibold">{isBM ? 'Susun:' : 'Sort:'}</span>
                  <select
                    value={statementSortBy}
                    onChange={(e) => setStatementSortBy(e.target.value)}
                    className="bg-zinc-100 dark:bg-zinc-800 border-none rounded px-2 py-0.5 text-[11px] font-medium text-zinc-800 dark:text-zinc-200 focus:outline-none cursor-pointer"
                  >
                    <option value="category">{isBM ? 'Kategori (Tempat Letak Kereta & Makanan)' : 'Category (Parking & Meals)'}</option>
                    <option value="date">{isBM ? 'Tarikh (Kronologi)' : 'Date (Chronological)'}</option>
                    <option value="amount">{isBM ? 'Jumlah RM (Tertinggi)' : 'Amount RM (Highest)'}</option>
                  </select>
                </div>
              </div>

              {/* Scrollable Items Table */}
              <div className="max-h-64 overflow-y-auto divide-y divide-zinc-100 dark:divide-zinc-800/60 pr-1 text-xs">
                {displayedStatementItems.map((item) => (
                  <div
                    key={item.id}
                    className={`flex items-center space-x-3 py-2 px-1 rounded-lg transition-colors ${
                      item.selected ? 'bg-white dark:bg-zinc-900' : 'opacity-40 hover:opacity-70'
                    }`}
                  >
                    <button
                      type="button"
                      onClick={() => toggleItemSelection(item.id)}
                      className="text-zinc-700 dark:text-zinc-300"
                    >
                      {item.selected ? (
                        <CheckSquare className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                      ) : (
                        <Square className="w-4 h-4 text-zinc-400" />
                      )}
                    </button>

                    <div className="w-20 text-[10.5px] text-zinc-400 tabular-nums shrink-0">
                      {item.date}
                    </div>

                    <div className="flex-1 min-w-0">
                      <input
                        type="text"
                        value={item.title}
                        onChange={(e) => updateStatementItem(item.id, 'title', e.target.value)}
                        className="w-full bg-transparent font-medium text-zinc-900 dark:text-zinc-100 truncate focus:outline-none"
                      />
                    </div>

                    <div className="w-36 shrink-0">
                      <select
                        value={item.category}
                        onChange={(e) => updateStatementItem(item.id, 'category', e.target.value)}
                        className="w-full text-[11px] bg-zinc-100 dark:bg-zinc-800 rounded px-2 py-1 text-zinc-800 dark:text-zinc-200 border-none focus:outline-none font-medium cursor-pointer"
                      >
                        {allAvailableCategories.map((c) => (
                          <option key={c.id || c.name} value={c.name}>
                            {c.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="text-right tabular-nums font-semibold text-zinc-900 dark:text-zinc-100 w-20 shrink-0">
                      {formatRM(item.amount)}
                    </div>
                  </div>
                ))}
              </div>

              {/* Action Buttons */}
              <div className="pt-2 flex items-center justify-between gap-3 border-t border-zinc-200 dark:border-zinc-800/80">
                <button
                  type="button"
                  onClick={() => setOcrResult(null)}
                  className="px-3 py-1.5 text-xs text-zinc-500 hover:text-zinc-800 dark:hover:text-zinc-200"
                >
                  {isBM ? 'Batal' : 'Cancel'}
                </button>

                <button
                  type="button"
                  onClick={handleImportSelectedTransactions}
                  className="px-4 py-1.5 bg-zinc-900 hover:bg-zinc-800 dark:bg-zinc-100 dark:hover:bg-white text-white dark:text-zinc-900 text-xs font-semibold rounded-lg shadow-xs transition-colors"
                >
                  {isBM 
                    ? `Import ${ocrResult.items.filter(i => i.selected).length} Perbelanjaan (${formatRM(ocrResult.items.filter(i => i.selected).reduce((acc, t) => acc + (t.amount || 0), 0))})`
                    : `Import ${ocrResult.items.filter(i => i.selected).length} Expenses (${formatRM(ocrResult.items.filter(i => i.selected).reduce((acc, t) => acc + (t.amount || 0), 0))})`}
                </button>
              </div>

            </div>
          )}

          {/* Result View B: SINGLE TRANSACTION CARD (e.g. 1 cropped row or 1 receipt slip) */}
          {ocrResult && ocrResult.type === 'single' && (
            <div className="border border-emerald-500/40 rounded-xl p-4 bg-emerald-50/20 dark:bg-emerald-950/20 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-semibold text-emerald-700 dark:text-emerald-400 uppercase tracking-wide">
                  {ocrResult.items[0]?.method || 'DuitNow / Statement Row'}
                </span>
                <span className="text-[10px] text-zinc-400">
                  {ocrResult.items[0]?.date || 'Today'}
                </span>
              </div>

              <div>
                <label className="text-[10px] text-zinc-400 uppercase font-semibold block mb-0.5">
                  {isBM ? 'Jumlah (RM)' : 'Amount (RM)'}
                </label>
                <div className="flex items-center space-x-1">
                  <span className="text-sm font-semibold text-zinc-500">RM</span>
                  <input
                    type="number"
                    step="0.01"
                    value={ocrResult.items[0]?.amount || ''}
                    onChange={(e) => updateStatementItem(ocrResult.items[0].id, 'amount', parseFloat(e.target.value) || 0)}
                    className="w-full text-2xl font-semibold text-zinc-900 dark:text-zinc-100 tracking-tight tabular-nums bg-transparent focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="text-[10px] text-zinc-400 uppercase font-semibold block mb-0.5">
                  {isBM ? 'Penerima / Perkara' : 'Description / Merchant'}
                </label>
                <input
                  type="text"
                  value={ocrResult.items[0]?.title || ''}
                  onChange={(e) => updateStatementItem(ocrResult.items[0].id, 'title', e.target.value)}
                  className="w-full bg-white dark:bg-zinc-900 border border-zinc-300 dark:border-zinc-700 rounded-md px-2.5 py-1 text-xs text-zinc-900 dark:text-zinc-100 font-medium focus:outline-none"
                />
              </div>

              <div>
                <label className="text-[10px] text-zinc-400 uppercase font-semibold block mb-0.5">
                  {isBM ? 'Kategori' : 'Category'}
                </label>
                <select
                  value={ocrResult.items[0]?.category || 'Parking & Tolls'}
                  onChange={(e) => updateStatementItem(ocrResult.items[0].id, 'category', e.target.value)}
                  className="w-full bg-white dark:bg-zinc-900 border border-zinc-300 dark:border-zinc-700 rounded-md px-2.5 py-1 text-xs text-zinc-800 dark:text-zinc-200 focus:outline-none font-medium cursor-pointer"
                >
                  {allAvailableCategories.map((c) => (
                    <option key={c.id || c.name} value={c.name}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="pt-2 flex items-center space-x-2">
                <button
                  type="button"
                  onClick={() => setOcrResult(null)}
                  className="flex-1 py-1.5 border border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 rounded-lg text-xs font-medium transition-colors"
                >
                  {isBM ? 'Batal' : 'Cancel'}
                </button>
                <button
                  type="button"
                  onClick={() => handleConfirmSingleTransaction(ocrResult.items[0])}
                  className="flex-1 bg-zinc-900 hover:bg-zinc-800 dark:bg-zinc-100 dark:hover:bg-white text-white dark:text-zinc-950 font-semibold py-1.5 rounded-lg text-xs transition-colors"
                >
                  {isBM ? 'Sahkan & Catat' : 'Confirm & Log Expense'}
                </button>
              </div>
            </div>
          )}

          {/* Result View C: Unparsed / Empty */}
          {ocrResult && (ocrResult.type === 'empty' || ocrResult.items.length === 0) && (
            <div className="border border-amber-200 dark:border-amber-900/50 rounded-xl p-4 bg-amber-50/20 dark:bg-amber-950/20 text-center space-y-2">
              <span className="text-xs font-semibold text-amber-800 dark:text-amber-300 block">
                {isBM ? 'Tiada data urus niaga dapat dikesan' : 'No transactions clearly identified'}
              </span>
              <p className="text-[11px] text-zinc-500 max-w-sm mx-auto">
                {isBM 
                  ? 'Cuba gunakan mod "Tampal Teks Penyata" untuk menampal terus teks dari dokumen anda.' 
                  : 'Try switching to the "Paste Statement Text" tab to paste the lines directly.'}
              </p>
              <button
                type="button"
                onClick={() => { setInputMode('paste'); setOcrResult(null); }}
                className="px-3 py-1 bg-amber-600 hover:bg-amber-700 text-white rounded-md text-xs font-medium transition-colors"
              >
                {isBM ? 'Tukar ke Tampal Teks' : 'Switch to Paste Text'}
              </button>
            </div>
          )}

        </div>
      )}

      {/* ================================================================= */}
      {/* TAB 2: LIVE CAMERA QR SCANNER                                     */}
      {/* ================================================================= */}
      {activeTab === 'camera_qr' && (
        <div className="space-y-4">
          <div className="relative rounded-xl overflow-hidden bg-zinc-950 aspect-video flex items-center justify-center border border-zinc-800">
            {cameraActive ? (
              <video
                ref={videoRef}
                autoPlay
                playsInline
                muted
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="flex flex-col items-center justify-center text-zinc-500 space-y-2">
                <Camera className="w-8 h-8 text-zinc-600 animate-pulse" />
                <span className="text-xs">
                  {isBM ? 'Kamera tidak aktif' : 'Camera inactive'}
                </span>
              </div>
            )}

            {/* Target Reticle Overlay */}
            <div className="absolute inset-0 pointer-events-none flex items-center justify-center">
              <div className="w-48 h-48 border-2 border-rose-500/80 rounded-2xl relative shadow-lg">
                <div className="absolute top-0 left-0 w-4 h-4 border-t-2 border-l-2 border-rose-400 -mt-0.5 -ml-0.5" />
                <div className="absolute top-0 right-0 w-4 h-4 border-t-2 border-r-2 border-rose-400 -mt-0.5 -mr-0.5" />
                <div className="absolute bottom-0 left-0 w-4 h-4 border-b-2 border-l-2 border-rose-400 -mb-0.5 -ml-0.5" />
                <div className="absolute bottom-0 right-0 w-4 h-4 border-b-2 border-r-2 border-rose-400 -mb-0.5 -mr-0.5" />
              </div>
            </div>
          </div>

          <div className="flex items-center justify-between">
            <span className="text-[11px] text-zinc-500">
              {isBM ? 'Arahkan kamera ke mana-mana kod DuitNow QR' : 'Point camera at any DuitNow QR code'}
            </span>
            <button
              onClick={cameraActive ? stopCamera : startCamera}
              className="px-3 py-1 bg-zinc-100 hover:bg-zinc-200 dark:bg-zinc-800 dark:hover:bg-zinc-700 text-zinc-800 dark:text-zinc-200 rounded-lg text-xs font-medium transition-colors"
            >
              {cameraActive ? (isBM ? 'Henti Kamera' : 'Stop Camera') : (isBM ? 'Mula Kamera' : 'Start Camera')}
            </button>
          </div>
        </div>
      )}

      {/* ================================================================= */}
      {/* TAB 3: GENERATE DUITNOW QR                                        */}
      {/* ================================================================= */}
      {activeTab === 'generate' && (
        <div className="flex flex-col sm:flex-row items-center gap-6">
          <div className="bg-white p-3 rounded-2xl shadow-sm border border-zinc-200 flex flex-col items-center">
            <canvas ref={qrCanvasRef} className="rounded-lg" />
            <span className="text-[10px] font-bold text-rose-600 mt-1 uppercase tracking-wider">
              DuitNow Interoperable QR
            </span>
          </div>

          <div className="flex-1 space-y-3 w-full">
            <div>
              <label className="text-[10px] font-semibold text-zinc-400 uppercase tracking-wider block mb-1">
                {isBM ? 'Jumlah Permintaan (RM)' : 'Requested Amount (RM)'}
              </label>
              <input
                type="number"
                step="0.05"
                value={genAmount}
                onChange={(e) => setGenAmount(e.target.value)}
                className="w-full bg-zinc-50 dark:bg-zinc-800/60 border border-zinc-200 dark:border-zinc-700/80 rounded-lg px-3 py-1.5 text-sm font-semibold tabular-nums text-zinc-900 dark:text-zinc-100 focus:outline-none"
              />
            </div>

            <div>
              <label className="text-[10px] font-semibold text-zinc-400 uppercase tracking-wider block mb-1">
                {isBM ? 'Catatan / Tujuan' : 'Payment Purpose / Note'}
              </label>
              <input
                type="text"
                value={genNote}
                onChange={(e) => setGenNote(e.target.value)}
                className="w-full bg-zinc-50 dark:bg-zinc-800/60 border border-zinc-200 dark:border-zinc-700/80 rounded-lg px-3 py-1.5 text-xs text-zinc-900 dark:text-zinc-100 focus:outline-none"
              />
            </div>

            <div className="pt-1 text-[11px] text-zinc-400">
              {isBM 
                ? 'Rakan anda boleh mengimbas kod ini menggunakan Maybank, TNG, CIMB, atau mana-mana aplikasi bank di Malaysia.' 
                : 'Friends can scan this QR using Maybank MAE, Touch n Go eWallet, CIMB OCTO, or any Malaysian banking app.'}
            </div>
          </div>
        </div>
      )}

      {/* ================================================================= */}
      {/* TAB 4: MAMAK BILL SPLITTER                                        */}
      {/* ================================================================= */}
      {activeTab === 'split' && (
        <div className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            
            <div className="space-y-3">
              <div>
                <label className="text-[10px] font-semibold text-zinc-400 uppercase tracking-wider block mb-1">
                  {isBM ? 'Jumlah Bil Kedai (RM)' : 'Total Bill Amount (RM)'}
                </label>
                <input
                  type="number"
                  step="0.05"
                  value={splitTotal}
                  onChange={(e) => setSplitTotal(e.target.value)}
                  className="w-full bg-zinc-50 dark:bg-zinc-800/60 border border-zinc-200 dark:border-zinc-700/80 rounded-lg px-3 py-1.5 text-sm font-semibold tabular-nums text-zinc-900 dark:text-zinc-100 focus:outline-none"
                />
              </div>

              <div>
                <label className="text-[10px] font-semibold text-zinc-400 uppercase tracking-wider block mb-1">
                  {isBM ? 'Bilangan Orang (Pax)' : 'Number of People (Pax)'}
                </label>
                <input
                  type="number"
                  min="1"
                  max="50"
                  value={splitPpl}
                  onChange={(e) => setSplitPpl(parseInt(e.target.value) || 1)}
                  className="w-full bg-zinc-50 dark:bg-zinc-800/60 border border-zinc-200 dark:border-zinc-700/80 rounded-lg px-3 py-1.5 text-sm font-semibold tabular-nums text-zinc-900 dark:text-zinc-100 focus:outline-none"
                />
              </div>
            </div>

            {/* Split Calculation Card */}
            <div className="bg-zinc-50 dark:bg-zinc-800/40 border border-zinc-200/80 dark:border-zinc-700/80 rounded-xl p-4 flex flex-col justify-between">
              <div>
                <span className="text-[10px] font-semibold text-zinc-400 uppercase tracking-wider block mb-1">
                  {isBM ? 'SETIAP ORANG PERLU BAYAR' : 'EACH PERSON PAYS'}
                </span>
                <span className="text-2xl font-bold text-zinc-900 dark:text-zinc-100 tracking-tight tabular-nums block">
                  {formatRM(roundedPerPerson)}
                </span>
                <span className="text-[10.5px] text-zinc-400 mt-1 block">
                  {isBM 
                    ? `(Dibundarkan mengikut mekanisme 5 sen BNM Malaysia)`
                    : `(Rounded to nearest 5-sen per Bank Negara Malaysia)`}
                </span>
              </div>

              <button
                onClick={() => {
                  setGenAmount(roundedPerPerson.toFixed(2));
                  setGenNote(`Split Bill (${splitPpl} pax)`);
                  setActiveTab('generate');
                }}
                className="mt-3 w-full py-1.5 bg-zinc-900 hover:bg-zinc-800 dark:bg-zinc-100 dark:hover:bg-white text-white dark:text-zinc-900 rounded-lg text-xs font-semibold transition-colors"
              >
                {isBM ? 'Jana DuitNow QR Bahagian Ini' : 'Generate QR for this Share'}
              </button>
            </div>

          </div>
        </div>
      )}

    </div>
  );
}
