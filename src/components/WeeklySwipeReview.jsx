import React, { useState } from 'react';
import { 
  Flame, 
  ThumbsUp, 
  ThumbsDown, 
  AlertOctagon, 
  CheckCircle2, 
  Sparkles, 
  ArrowLeft, 
  ArrowRight,
  RotateCcw,
  QrCode,
  Inbox
} from 'lucide-react';
import { formatRM, playSound, fireCelebration } from '../utils/helpers';

export default function WeeklySwipeReview({
  queue = [],
  onResolveItem,
  onResetQueue,
  onLoadDemoQueue,
  language
}) {
  const isBM = language === 'bm';
  const [currentIndex, setCurrentIndex] = useState(0);
  const [lastAction, setLastAction] = useState(null);

  const currentItem = queue && queue.length > 0 && currentIndex < queue.length 
    ? queue[currentIndex] 
    : null;

  const handleSwipe = (action) => {
    if (!currentItem) return;

    if (action === 'shame') {
      playSound('hammer-shame');
      setLastAction({
        type: 'shame',
        message: isBM ? '🔨 HAMMER SHAME! Ditandakan sebagai pembaziran!' : '🔨 HAMMER SHAME! Flagged as wasteful spending!',
        comment: currentItem.hammerComment
      });
      onResolveItem(currentItem.id, 'wants', true);
    } else {
      playSound('approve');
      setLastAction({
        type: 'approved',
        message: isBM ? '✅ DILULUSKAN! Keperluan sah.' : '✅ APPROVED! Legitimate essential need.',
        comment: currentItem.hammerComment
      });
      onResolveItem(currentItem.id, 'needs', false);
    }

    if (currentIndex + 1 >= queue.length) {
      fireCelebration();
    }
    setCurrentIndex(prev => prev + 1);
  };

  const handleReset = () => {
    setCurrentIndex(0);
    setLastAction(null);
    onResetQueue();
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 sm:p-6 shadow-xl relative overflow-hidden">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-slate-800/80 pb-4 mb-5">
        <div>
          <div className="flex items-center space-x-2">
            <div className="p-1.5 rounded-lg bg-red-500/10 border border-red-500/20 text-red-400">
              <Flame className="w-5 h-5" />
            </div>
            <h2 className="text-xl font-bold text-white tracking-tight">
              {isBM ? 'Ulasan Leret Mingguan (Weekly Swipe Review)' : 'The Weekly Swipe Review'}
            </h2>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            {isBM 
              ? 'Gaya Caleb Hammer: Leret ke kiri untuk "Shame!", leret ke kanan untuk "Keperluan Sah".'
              : 'Sort every pending Ringgit swipe-by-swipe. Confront your real spending habits.'}
          </p>
        </div>

        {queue.length > 0 && (
          <div className="flex items-center space-x-3">
            <span className="text-xs font-semibold text-slate-400">
              {`${Math.min(currentIndex, queue.length)} / ${queue.length} ${isBM ? 'Disemak' : 'Reviewed'}`}
            </span>
            <button
              onClick={handleReset}
              className="flex items-center text-xs text-slate-400 hover:text-slate-200 bg-slate-950 px-2.5 py-1.5 rounded-lg border border-slate-800 transition-colors"
            >
              <RotateCcw className="w-3.5 h-3.5 mr-1" />
              {isBM ? 'Ulang' : 'Reset'}
            </button>
          </div>
        )}
      </div>

      {/* When Queue has items */}
      {currentItem ? (
        <div className="max-w-md mx-auto my-4">
          
          {/* Active Card */}
          <div className="bg-gradient-to-b from-slate-800/90 to-slate-950 border-2 border-slate-700/80 rounded-2xl p-6 shadow-2xl relative transition-all">
            
            <div className="flex items-center justify-between text-xs text-slate-400 border-b border-slate-800 pb-3">
              <span className="flex items-center font-medium">
                <QrCode className="w-3.5 h-3.5 mr-1 text-red-400" />
                {currentItem.method}
              </span>
              <span className="text-slate-500">{currentItem.date}</span>
            </div>

            <div className="text-center my-6">
              <span className="text-xs uppercase font-bold tracking-wider text-slate-400 px-2.5 py-1 rounded-full bg-slate-800/60 inline-block mb-3">
                {currentItem.category}
              </span>
              <h3 className="text-xl sm:text-2xl font-black text-white tracking-tight">
                {currentItem.title}
              </h3>
              <div className="text-3xl sm:text-4xl font-extrabold text-emerald-400 mt-2">
                {formatRM(currentItem.amount)}
              </div>
            </div>

            <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-3.5 text-xs text-slate-300 relative">
              <div className="flex items-center space-x-1.5 text-amber-400 font-semibold mb-1">
                <AlertOctagon className="w-4 h-4" />
                <span>{isBM ? 'Ulasan Caleb Hammer:' : 'Caleb Hammer Roast Check:'}</span>
              </div>
              <p className="italic text-slate-400 leading-relaxed">
                "{currentItem.hammerComment}"
              </p>
            </div>

            {/* Swipe Action Buttons */}
            <div className="grid grid-cols-2 gap-4 mt-6">
              
              <button
                onClick={() => handleSwipe('shame')}
                className="group flex flex-col items-center justify-center p-4 rounded-xl border border-rose-500/40 bg-gradient-to-b from-rose-950/40 to-slate-950 text-rose-300 hover:border-rose-500 hover:text-white hover:bg-rose-900/50 transition-all active:scale-95 shadow-lg shadow-rose-950/30"
              >
                <div className="w-10 h-10 rounded-full bg-rose-500/20 flex items-center justify-center mb-1.5 group-hover:scale-110 transition-transform">
                  <ThumbsDown className="w-5 h-5 text-rose-400" />
                </div>
                <span className="text-xs font-bold uppercase tracking-wider">
                  {isBM ? 'Pembaziran!' : 'Hammer Shame!'}
                </span>
                <span className="text-[10px] text-rose-400/80">
                  {isBM ? 'Kehendak / Wants' : 'Flag as Want'}
                </span>
              </button>

              <button
                onClick={() => handleSwipe('approved')}
                className="group flex flex-col items-center justify-center p-4 rounded-xl border border-emerald-500/40 bg-gradient-to-b from-emerald-950/40 to-slate-950 text-emerald-300 hover:border-emerald-500 hover:text-white hover:bg-emerald-900/50 transition-all active:scale-95 shadow-lg shadow-emerald-950/30"
              >
                <div className="w-10 h-10 rounded-full bg-emerald-500/20 flex items-center justify-center mb-1.5 group-hover:scale-110 transition-transform">
                  <ThumbsUp className="w-5 h-5 text-emerald-400" />
                </div>
                <span className="text-xs font-bold uppercase tracking-wider">
                  {isBM ? 'Keperluan Asas' : 'Essential Need'}
                </span>
                <span className="text-[10px] text-emerald-400/80">
                  {isBM ? 'Luluskan / Needs' : 'Approve as Need'}
                </span>
              </button>

            </div>

          </div>

        </div>
      ) : (
        /* Empty Queue State */
        <div className="max-w-md mx-auto text-center py-10 px-4">
          <div className="w-16 h-16 rounded-2xl bg-slate-800/60 border border-slate-700/60 text-slate-400 flex items-center justify-center mx-auto mb-4">
            <Inbox className="w-8 h-8 text-emerald-400" />
          </div>
          <h3 className="text-lg font-bold text-white">
            {isBM ? 'Tiada Transaksi Tertunggak' : 'Inbox Zero • All Caught Up!'}
          </h3>
          <p className="text-xs text-slate-400 mt-2 max-w-sm mx-auto leading-relaxed">
            {isBM 
              ? 'Hebat! Tiada transaksi menunggu untuk disemak. Setiap kali anda merekod perbelanjaan baru, ia akan dipaparkan di sini untuk ulasan leret mingguan.' 
              : 'You have no unreviewed expenses pending. Any new DuitNow QR or manual payment can be reviewed here.'}
          </p>

          {onLoadDemoQueue && (
            <button
              onClick={onLoadDemoQueue}
              className="mt-5 inline-flex items-center px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white text-xs font-semibold border border-slate-700 transition-all"
            >
              <Sparkles className="w-3.5 h-3.5 mr-1.5 text-amber-400" />
              {isBM ? 'Muat Contoh Kad Leret (Demo)' : 'Load Sample Review Cards'}
            </button>
          )}
        </div>
      )}

      {/* Last Action Notification Toast */}
      {lastAction && (
        <div className={`mt-4 p-3 rounded-xl border flex items-center space-x-2.5 text-xs transition-all ${
          lastAction.type === 'shame' 
            ? 'bg-rose-950/40 border-rose-800/60 text-rose-300' 
            : 'bg-emerald-950/40 border-emerald-800/60 text-emerald-300'
        }`}>
          {lastAction.type === 'shame' ? (
            <AlertOctagon className="w-4 h-4 text-rose-400 shrink-0" />
          ) : (
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          )}
          <div>
            <span className="font-bold">{lastAction.message}</span>
            <span className="text-slate-400 block text-[11px] mt-0.5">"{lastAction.comment}"</span>
          </div>
        </div>
      )}

    </div>
  );
}
