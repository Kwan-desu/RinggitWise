import React, { useState, useMemo } from 'react';
import { PieChart, Filter, Calendar, Sparkles, Tag } from 'lucide-react';
import { formatRM } from '../utils/helpers';

// Harmonic, vibrant color palette inspired directly by Image 1:
// Sky Blue (Lithuania) -> Medium Blue (Czech) -> Slate Blue (Ireland) -> Indigo (Germany) -> Purple (Australia) -> Orchid / Violet (Austria)
const ARC_PALETTE = [
  '#38bdf8', // 1. Sky Blue (Lithuania in img 1)
  '#60a5fa', // 2. Medium Blue (Czech in img 1)
  '#6366f1', // 3. Slate Blue / Indigo (Ireland in img 1)
  '#818cf8', // 4. Indigo / Lavender (Germany in img 1)
  '#a855f7', // 5. Purple (Australia in img 1)
  '#c084fc', // 6. Orchid / Magenta (Austria in img 1)
  '#f43f5e', // 7. Rose
  '#10b981', // 8. Emerald
  '#f59e0b', // 9. Amber
  '#06b6d4', // 10. Cyan
  '#14b8a6', // 11. Teal
  '#ec4899', // 12. Pink
];

export default function HalfCircleSpendingChart({
  transactions = [],
  categories = [],
  language = 'en',
  onOpenAddModal,
  onLoadSampleData
}) {
  const isBM = language === 'bm';
  const [hoveredIndex, setHoveredIndex] = useState(null);
  const [timeFilter, setTimeFilter] = useState('all'); // 'this_month' | 'all'

  // Filter transactions
  const filteredTransactions = useMemo(() => {
    if (timeFilter === 'this_month') {
      const currentMonth = new Date().toISOString().slice(0, 7);
      return transactions.filter(t => t.date && t.date.startsWith(currentMonth));
    }
    return transactions;
  }, [transactions, timeFilter]);

  // Calculate Category Breakdown & Total Spent
  const { slices, totalSpent, count } = useMemo(() => {
    const expenses = filteredTransactions.filter(t => Number(t.amount) > 0);
    const total = expenses.reduce((sum, t) => sum + Number(t.amount), 0);

    if (total === 0) {
      return { slices: [], totalSpent: 0, count: 0 };
    }

    const catMap = {};
    expenses.forEach(tx => {
      const cat = tx.category || 'General';
      if (!catMap[cat]) {
        const found = categories.find(c => c.name === cat);
        catMap[cat] = {
          name: cat,
          amount: 0,
          customColor: found?.color,
          icon: found?.icon || '🏷️',
        };
      }
      catMap[cat].amount += Number(tx.amount);
    });

    const sorted = Object.values(catMap).sort((a, b) => b.amount - a.amount);

    // Limit to top 6 categories + "Other" if necessary (to mirror Image 1's clean 6-slice arch)
    let finalSlices = [];
    if (sorted.length <= 6) {
      finalSlices = sorted.map((item, idx) => ({
        id: item.name,
        name: item.name,
        amount: item.amount,
        percentage: (item.amount / total) * 100,
        color: item.customColor || ARC_PALETTE[idx % ARC_PALETTE.length],
        icon: item.icon,
      }));
    } else {
      const top5 = sorted.slice(0, 5);
      const remainingAmount = sorted.slice(5).reduce((s, it) => s + it.amount, 0);

      finalSlices = top5.map((item, idx) => ({
        id: item.name,
        name: item.name,
        amount: item.amount,
        percentage: (item.amount / total) * 100,
        color: item.customColor || ARC_PALETTE[idx % ARC_PALETTE.length],
        icon: item.icon,
      }));

      if (remainingAmount > 0) {
        finalSlices.push({
          id: 'other',
          name: isBM ? 'Lain-lain' : 'Other Categories',
          amount: remainingAmount,
          percentage: (remainingAmount / total) * 100,
          color: ARC_PALETTE[5],
          icon: '📦',
        });
      }
    }

    return { slices: finalSlices, totalSpent: total, count: expenses.length };
  }, [filteredTransactions, categories, isBM]);

  // SVG Geometry Settings (Half-Circle Arch from 180° on Left to 0° on Right)
  const svgWidth = 600;
  const svgHeight = 350;
  const cx = 300;
  const cy = 270;
  const outerR = 195;
  const innerR = 118;
  const gapDeg = slices.length > 1 ? 2.5 : 0; // Space between arc segments

  // Calculate arc segments and leader lines
  const arcSegments = useMemo(() => {
    if (slices.length === 0) return [];

    let currentAngle = 180;
    const totalGaps = (slices.length - 1) * gapDeg;
    const availableSpan = 180 - totalGaps;

    return slices.map((slice, index) => {
      const spanDeg = Math.max(1, (slice.percentage / 100) * availableSpan);
      const startAngle = currentAngle;
      const endAngle = currentAngle - spanDeg;
      currentAngle = endAngle - gapDeg;

      const toRad = (deg) => (deg * Math.PI) / 180;
      const startRad = toRad(startAngle);
      const endRad = toRad(endAngle);
      const midRad = toRad((startAngle + endAngle) / 2);

      // Coordinates for outer and inner arcs
      const xOuterStart = cx + outerR * Math.cos(startRad);
      const yOuterStart = cy - outerR * Math.sin(startRad);
      const xOuterEnd = cx + outerR * Math.cos(endRad);
      const yOuterEnd = cy - outerR * Math.sin(endRad);

      const xInnerEnd = cx + innerR * Math.cos(endRad);
      const yInnerEnd = cy - innerR * Math.sin(endRad);
      const xInnerStart = cx + innerR * Math.cos(startRad);
      const yInnerStart = cy - innerR * Math.sin(startRad);

      // SVG path definition
      const pathData = [
        `M ${xOuterStart} ${yOuterStart}`,
        `A ${outerR} ${outerR} 0 0 1 ${xOuterEnd} ${yOuterEnd}`,
        `L ${xInnerEnd} ${yInnerEnd}`,
        `A ${innerR} ${innerR} 0 0 0 ${xInnerStart} ${yInnerStart}`,
        'Z',
      ].join(' ');

      // Leader line coordinates (Image 1 style)
      const leaderStartR = outerR + 5;
      const leaderElbowR = outerR + 25;
      const lx1 = cx + leaderStartR * Math.cos(midRad);
      const ly1 = cy - leaderStartR * Math.sin(midRad);
      const lx2 = cx + leaderElbowR * Math.cos(midRad);
      const ly2 = cy - leaderElbowR * Math.sin(midRad);

      const isLeft = Math.cos(midRad) < 0;
      const lx3 = isLeft ? lx2 - 16 : lx2 + 16;
      const ly3 = ly2;

      const textX = isLeft ? lx3 - 6 : lx3 + 6;
      const textY = ly3 + 4;
      const textAnchor = isLeft ? 'end' : 'start';

      return {
        ...slice,
        index,
        pathData,
        leader: { lx1, ly1, lx2, ly2, lx3, ly3 },
        label: { textX, textY, textAnchor },
      };
    });
  }, [slices, gapDeg, cx, cy, outerR, innerR]);

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 sm:p-6 shadow-xl transition-colors">
      
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-slate-100 dark:border-slate-800 pb-4 mb-4">
        <div>
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <PieChart className="w-4 h-4" />
            </div>
            <h2 className="text-lg font-bold text-slate-900 dark:text-white tracking-tight">
              {isBM ? 'Ringkasan Perbelanjaan Mengikut Kategori' : 'Spending Summary Breakdown'}
            </h2>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            {isBM 
              ? 'Carta separuh bulatan interaktif memaparkan peratusan dan jumlah Ringgit bagi setiap kategori.'
              : 'Interactive half-circle spending arc displaying category proportions in Ringgit (RM).'}
          </p>
        </div>

        {/* Time Period Filter */}
        <div className="flex items-center space-x-1.5 self-start sm:self-auto bg-slate-100 dark:bg-slate-950 p-1 rounded-xl border border-slate-200 dark:border-slate-800 text-xs">
          <button
            onClick={() => setTimeFilter('all')}
            className={`px-3 py-1.5 rounded-lg font-medium transition-all ${
              timeFilter === 'all'
                ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 font-bold shadow-xs'
                : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            {isBM ? 'Semua Rekod' : 'All Time'}
          </button>
          <button
            onClick={() => setTimeFilter('this_month')}
            className={`px-3 py-1.5 rounded-lg font-medium transition-all ${
              timeFilter === 'this_month'
                ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 font-bold shadow-xs'
                : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
            }`}
          >
            {isBM ? 'Bulan Ini' : 'This Month'}
          </button>
        </div>
      </div>

      {/* Main Chart Graphic or Empty State */}
      {slices.length === 0 ? (
        <div className="py-12 flex flex-col items-center justify-center text-center space-y-3">
          <div className="w-16 h-16 rounded-2xl bg-slate-100 dark:bg-slate-800 text-slate-400 dark:text-slate-500 flex items-center justify-center">
            <PieChart className="w-8 h-8 opacity-60" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-800 dark:text-slate-200">
              {isBM ? 'Tiada Perbelanjaan Direkodkan Lagi' : 'No Spending Logged Yet'}
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 max-w-sm">
              {isBM 
                ? 'Tambah transaksi perbelanjaan atau muat naik resit DuitNow QR untuk melihat visual carta separuh bulatan ini.'
                : 'Log an expense or scan a DuitNow payment slip to render your live spending arc chart.'}
            </p>
          </div>

          <div className="flex items-center space-x-2 pt-2">
            {onOpenAddModal && (
              <button
                onClick={onOpenAddModal}
                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold shadow-md shadow-emerald-600/20 transition-all"
              >
                + {isBM ? 'Tambah Transaksi RM' : 'Log Expense (RM)'}
              </button>
            )}
            {onLoadSampleData && (
              <button
                onClick={onLoadSampleData}
                className="px-4 py-2 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 rounded-xl text-xs font-semibold transition-all"
              >
                {isBM ? 'Muat Contoh Data' : 'Load Sample Data'}
              </button>
            )}
          </div>
        </div>
      ) : (
        <div className="relative flex flex-col items-center">
          
          {/* SVG Arc Half Circle (Matching Image 1) */}
          <div className="w-full max-w-[580px] overflow-visible">
            <svg
              viewBox={`0 0 ${svgWidth} ${svgHeight}`}
              className="w-full h-auto select-none drop-shadow-xs"
            >
              {/* Subtle background guide */}
              <path
                d={`M ${cx - outerR} ${cy} A ${outerR} ${outerR} 0 0 1 ${cx + outerR} ${cy} L ${cx + innerR} ${cy} A ${innerR} ${innerR} 0 0 0 ${cx - innerR} ${cy} Z`}
                fill="none"
                stroke="currentColor"
                strokeWidth="1"
                className="text-slate-100 dark:text-slate-800/40"
              />

              {/* Segment Slices */}
              {arcSegments.map((seg) => {
                const isHovered = hoveredIndex === seg.index;
                return (
                  <g key={seg.id} className="transition-all duration-200">
                    <path
                      d={seg.pathData}
                      fill={seg.color}
                      stroke="currentColor"
                      strokeWidth={isHovered ? '2.5' : '1.5'}
                      strokeLinejoin="round"
                      className={`cursor-pointer text-white dark:text-slate-900 transition-all duration-200 ${
                        isHovered ? 'opacity-100 scale-[1.015]' : 'opacity-95 hover:opacity-100'
                      }`}
                      style={{ transformOrigin: `${cx}px ${cy}px` }}
                      onMouseEnter={() => setHoveredIndex(seg.index)}
                      onMouseLeave={() => setHoveredIndex(null)}
                    >
                      <title>{`${seg.name}: ${formatRM(seg.amount)} (${seg.percentage.toFixed(1)}%)`}</title>
                    </path>

                    {/* Leader Line (Image 1 Style) */}
                    <polyline
                      points={`${seg.leader.lx1},${seg.leader.ly1} ${seg.leader.lx2},${seg.leader.ly2} ${seg.leader.lx3},${seg.leader.ly3}`}
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="1"
                      strokeDasharray={isHovered ? 'none' : '2,2'}
                      className={`transition-colors duration-200 ${
                        isHovered 
                          ? 'text-emerald-500 dark:text-emerald-400 stroke-2' 
                          : 'text-slate-300 dark:text-slate-600'
                      }`}
                    />

                    {/* Dot at Slice Anchor */}
                    <circle
                      cx={seg.leader.lx1}
                      cy={seg.leader.ly1}
                      r={isHovered ? '3.5' : '2'}
                      fill={seg.color}
                      className="transition-all"
                    />

                    {/* Leader Text Label: "Category: XX.X%" (Exact Image 1 Style) */}
                    <text
                      x={seg.label.textX}
                      y={seg.label.textY}
                      textAnchor={seg.label.textAnchor}
                      className={`text-[11.5px] font-semibold tracking-tight transition-all duration-200 select-none ${
                        isHovered
                          ? 'fill-emerald-600 dark:fill-emerald-400 font-bold text-[12.5px]'
                          : 'fill-slate-700 dark:fill-slate-300'
                      }`}
                    >
                      {seg.name}: {seg.percentage.toFixed(1)}%
                    </text>
                  </g>
                );
              })}

              {/* Center Hub: Total Spend in RM */}
              <g className="select-none pointer-events-none">
                <text
                  x={cx}
                  y={cy - 48}
                  textAnchor="middle"
                  className="fill-slate-500 dark:fill-slate-400 text-xs font-semibold uppercase tracking-wider"
                >
                  {isBM ? 'Jumlah Belanja' : 'Total Spent'}
                </text>
                <text
                  x={cx}
                  y={cy - 14}
                  textAnchor="middle"
                  className="fill-slate-900 dark:fill-white text-2xl sm:text-3xl font-black tracking-tight"
                >
                  {formatRM(totalSpent)}
                </text>
                <text
                  x={cx}
                  y={cy + 10}
                  textAnchor="middle"
                  className="fill-emerald-600 dark:fill-emerald-400 text-[11px] font-bold"
                >
                  {count} {isBM ? 'transaksi' : 'transactions'}
                </text>
              </g>
            </svg>
          </div>

          {/* Bottom Legend (Matching Image 1 with color swatch, name, percentage & RM) */}
          <div className="w-full mt-4 pt-4 border-t border-slate-100 dark:border-slate-800">
            <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2.5">
              {arcSegments.map((seg) => {
                const isHovered = hoveredIndex === seg.index;
                return (
                  <button
                    key={seg.id}
                    onClick={() => setHoveredIndex(isHovered ? null : seg.index)}
                    onMouseEnter={() => setHoveredIndex(seg.index)}
                    onMouseLeave={() => setHoveredIndex(null)}
                    className={`flex items-center space-x-2 px-2.5 py-1.5 rounded-xl border text-xs transition-all ${
                      isHovered
                        ? 'bg-slate-100 dark:bg-slate-800 border-emerald-500 shadow-xs scale-105'
                        : 'bg-transparent border-transparent hover:bg-slate-50 dark:hover:bg-slate-800/50'
                    }`}
                  >
                    {/* Swatch (Image 1 style rounded rectangle) */}
                    <span
                      className="w-3.5 h-3.5 rounded-[4px] shrink-0"
                      style={{ backgroundColor: seg.color }}
                    ></span>

                    {/* Category Name */}
                    <span className="font-medium text-slate-800 dark:text-slate-200">
                      {seg.name}
                    </span>

                    {/* Percentage */}
                    <span className="font-bold text-slate-900 dark:text-white">
                      {seg.percentage.toFixed(1)}%
                    </span>

                    {/* RM Amount */}
                    <span className="text-[11px] text-slate-500 dark:text-slate-400 hidden sm:inline">
                      ({formatRM(seg.amount)})
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

        </div>
      )}

    </div>
  );
}
