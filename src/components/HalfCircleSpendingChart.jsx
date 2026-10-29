import React, { useState, useMemo } from 'react';
import { Plus, QrCode, Sparkles } from 'lucide-react';
import { formatRM } from '../utils/helpers';

// Exact harmonic color palette derived directly from Image 1:
// 1. Lithuania (Cerulean / Sky Blue)
// 2. Czech Republic (Cobalt / Medium Blue)
// 3. Ireland (Slate Indigo)
// 4. Germany (Deep Violet / Purple)
// 5. Australia (Orchid Violet)
// 6. Austria (Magenta / Pink)
const EXACT_IMAGE1_COLORS = [
  '#54b3d6', // Cerulean
  '#5b8ee5', // Medium Blue
  '#5f6fe5', // Slate Indigo
  '#7f5ce6', // Purple
  '#a856ea', // Violet
  '#c856db', // Orchid Magenta
  '#10b981', // Emerald
  '#f59e0b', // Amber
  '#f43f5e', // Rose
  '#06b6d4', // Cyan
];

export default function HalfCircleSpendingChart({
  transactions = [],
  categories = [],
  language = 'en',
  onOpenAddModal,
  onOpenDuitNow,
  onLoadSampleData
}) {
  const isBM = language === 'bm';
  const [hoveredIndex, setHoveredIndex] = useState(null);
  const [period, setPeriod] = useState('month'); // 'month' | 'all'

  // Filter transactions strictly for real spending expenses (exclude income)
  const filteredExpenses = useMemo(() => {
    let list = transactions.filter(t => 
      Number(t.amount) > 0 &&
      t.type !== 'income' &&
      t.bucket !== 'income' &&
      t.category?.toLowerCase() !== 'income'
    );

    if (period === 'month') {
      const currentMonth = new Date().toISOString().slice(0, 7);
      list = list.filter(t => t.date && t.date.startsWith(currentMonth));
    }

    return list;
  }, [transactions, period]);

  // Aggregate by Category
  const { slices, totalSpent, count } = useMemo(() => {
    const total = filteredExpenses.reduce((sum, t) => sum + Number(t.amount), 0);

    if (total === 0) {
      return { slices: [], totalSpent: 0, count: 0 };
    }

    const catMap = {};
    filteredExpenses.forEach(tx => {
      const cat = tx.category || 'General';
      if (!catMap[cat]) {
        const found = categories.find(c => c.name === cat);
        catMap[cat] = {
          name: cat,
          amount: 0,
          customColor: found?.color,
        };
      }
      catMap[cat].amount += Number(tx.amount);
    });

    const sorted = Object.values(catMap).sort((a, b) => b.amount - a.amount);

    // Limit to top 6 categories to mirror Image 1's clean 6-segment arch
    let finalSlices = [];
    if (sorted.length <= 6) {
      finalSlices = sorted.map((item, idx) => ({
        id: item.name,
        name: item.name,
        amount: item.amount,
        percentage: (item.amount / total) * 100,
        color: item.customColor || EXACT_IMAGE1_COLORS[idx % EXACT_IMAGE1_COLORS.length],
      }));
    } else {
      const top5 = sorted.slice(0, 5);
      const remainingAmount = sorted.slice(5).reduce((s, it) => s + it.amount, 0);

      finalSlices = top5.map((item, idx) => ({
        id: item.name,
        name: item.name,
        amount: item.amount,
        percentage: (item.amount / total) * 100,
        color: item.customColor || EXACT_IMAGE1_COLORS[idx % EXACT_IMAGE1_COLORS.length],
      }));

      if (remainingAmount > 0) {
        finalSlices.push({
          id: 'other',
          name: isBM ? 'Lain-lain' : 'Other',
          amount: remainingAmount,
          percentage: (remainingAmount / total) * 100,
          color: EXACT_IMAGE1_COLORS[5],
        });
      }
    }

    return { slices: finalSlices, totalSpent: total, count: filteredExpenses.length };
  }, [filteredExpenses, categories, isBM]);

  // SVG Geometry Dimensions
  const svgWidth = 620;
  const svgHeight = 350;
  const cx = 310;
  const cy = 275;
  const outerR = 190;
  const innerR = 115;
  const gapDeg = slices.length > 1 ? 2.2 : 0; // Segment separation

  // Calculate arc paths and leader lines matching Image 1
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

      // Coordinates
      const xOuterStart = cx + outerR * Math.cos(startRad);
      const yOuterStart = cy - outerR * Math.sin(startRad);
      const xOuterEnd = cx + outerR * Math.cos(endRad);
      const yOuterEnd = cy - outerR * Math.sin(endRad);

      const xInnerEnd = cx + innerR * Math.cos(endRad);
      const yInnerEnd = cy - innerR * Math.sin(endRad);
      const xInnerStart = cx + innerR * Math.cos(startRad);
      const yInnerStart = cy - innerR * Math.sin(startRad);

      const pathData = [
        `M ${xOuterStart} ${yOuterStart}`,
        `A ${outerR} ${outerR} 0 0 1 ${xOuterEnd} ${yOuterEnd}`,
        `L ${xInnerEnd} ${yInnerEnd}`,
        `A ${innerR} ${innerR} 0 0 0 ${xInnerStart} ${yInnerStart}`,
        'Z',
      ].join(' ');

      // Leader Lines (Image 1 style: fine line extending from arc edge outwards)
      const leaderStartR = outerR + 4;
      const leaderElbowR = outerR + 24;
      const lx1 = cx + leaderStartR * Math.cos(midRad);
      const ly1 = cy - leaderStartR * Math.sin(midRad);
      const lx2 = cx + leaderElbowR * Math.cos(midRad);
      const ly2 = cy - leaderElbowR * Math.sin(midRad);

      const isLeft = Math.cos(midRad) < 0;
      const lx3 = isLeft ? lx2 - 18 : lx2 + 18;
      const ly3 = ly2;

      const textX = isLeft ? lx3 - 6 : lx3 + 6;
      const textY = ly3 + 3.5;
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
    <div className="bg-white dark:bg-zinc-900/70 border border-zinc-200/80 dark:border-zinc-800/80 rounded-2xl p-5 sm:p-7 shadow-2xs transition-colors">
      
      {/* Top Header */}
      <div className="flex items-center justify-between border-b border-zinc-100 dark:border-zinc-800/80 pb-4 mb-3">
        <div>
          <h2 className="text-sm font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">
            {isBM ? 'Pecahan Perbelanjaan Mengikut Kategori' : 'Spending Distribution by Category'}
          </h2>
          <p className="text-[11px] text-zinc-500 dark:text-zinc-400 mt-0.5">
            {isBM 
              ? 'Carta separuh bulatan 180° memaparkan nisbah perbelanjaan sebenar.' 
              : 'Semi-circular arch breakdown of expenses in Ringgit (RM).'}
          </p>
        </div>

        {/* Period Selector Tabs */}
        <div className="flex items-center space-x-1 bg-zinc-100 dark:bg-zinc-800/60 p-0.5 rounded-lg text-xs">
          <button
            onClick={() => setPeriod('month')}
            className={`px-2.5 py-1 rounded-md font-medium transition-all ${
              period === 'month'
                ? 'bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100 shadow-2xs'
                : 'text-zinc-500 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100'
            }`}
          >
            {isBM ? 'Bulan Ini' : 'This Month'}
          </button>
          <button
            onClick={() => setPeriod('all')}
            className={`px-2.5 py-1 rounded-md font-medium transition-all ${
              period === 'all'
                ? 'bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100 shadow-2xs'
                : 'text-zinc-500 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100'
            }`}
          >
            {isBM ? 'Semua' : 'All Time'}
          </button>
        </div>
      </div>

      {/* Main Chart Graphic or Functional Empty State */}
      {slices.length === 0 ? (
        <div className="relative flex flex-col items-center justify-center py-6">
          {/* Visual Dashed Arch Placeholder */}
          <div className="w-full max-w-[560px]">
            <svg
              viewBox={`0 0 ${svgWidth} ${svgHeight}`}
              className="w-full h-auto select-none"
            >
              {/* Dashed placeholder arch */}
              <path
                d={`M ${cx - outerR} ${cy} A ${outerR} ${outerR} 0 0 1 ${cx + outerR} ${cy} L ${cx + innerR} ${cy} A ${innerR} ${innerR} 0 0 0 ${cx - innerR} ${cy} Z`}
                fill="none"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeDasharray="6,6"
                className="text-zinc-200 dark:text-zinc-800"
              />

              {/* Center Metrics (Zero State) */}
              <g className="select-none pointer-events-none">
                <text
                  x={cx}
                  y={cy - 44}
                  textAnchor="middle"
                  className="fill-zinc-400 dark:fill-zinc-500 text-[10px] font-semibold tracking-wider uppercase"
                >
                  {isBM ? 'JUMLAH BELANJA' : 'TOTAL SPENT'}
                </text>
                <text
                  x={cx}
                  y={cy - 12}
                  textAnchor="middle"
                  className="fill-zinc-400 dark:fill-zinc-500 text-2xl font-semibold tracking-tight tabular-nums"
                >
                  RM 0.00
                </text>
                <text
                  x={cx}
                  y={cy + 12}
                  textAnchor="middle"
                  className="fill-zinc-400 dark:fill-zinc-500 text-[10.5px] font-medium"
                >
                  {isBM ? '0 rekod perbelanjaan' : '0 transactions logged'}
                </text>
              </g>
            </svg>
          </div>

          {/* Functional Call-To-Action Pills */}
          <div className="flex flex-col items-center space-y-3 mt-2">
            <span className="text-xs text-zinc-600 dark:text-zinc-400 font-medium text-center">
              {isBM 
                ? 'Belum ada perbelanjaan direkodkan untuk tempoh ini.' 
                : 'No expenses logged yet. Record your first Ringgit payment to activate the chart:'}
            </span>

            <div className="flex flex-wrap items-center justify-center gap-2">
              {onOpenAddModal && (
                <button
                  onClick={onOpenAddModal}
                  className="flex items-center space-x-1.5 px-3.5 py-1.5 bg-zinc-900 hover:bg-zinc-800 dark:bg-zinc-100 dark:hover:bg-white text-white dark:text-zinc-900 rounded-full text-xs font-semibold shadow-xs transition-colors"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>{isBM ? 'Rekod Perbelanjaan (RM)' : 'Add Expense (RM)'}</span>
                </button>
              )}

              {onOpenDuitNow && (
                <button
                  onClick={onOpenDuitNow}
                  className="flex items-center space-x-1.5 px-3.5 py-1.5 border border-zinc-200 dark:border-zinc-800 text-zinc-700 dark:text-zinc-300 hover:text-zinc-900 dark:hover:text-zinc-100 rounded-full text-xs font-medium transition-colors"
                >
                  <QrCode className="w-3.5 h-3.5 text-rose-500" />
                  <span>{isBM ? 'Imbas Resit DuitNow' : 'Scan DuitNow Slip'}</span>
                </button>
              )}
            </div>

            {onLoadSampleData && (
              <button
                onClick={onLoadSampleData}
                className="text-[11px] text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-300 underline underline-offset-4 pt-1 transition-colors"
              >
                {isBM ? 'Atau muat contoh data demonstrasi' : 'Or load sample demo data to preview'}
              </button>
            )}
          </div>
        </div>
      ) : (
        <div className="flex flex-col items-center">
          
          {/* SVG Arch Graphic (Image 1 Style) */}
          <div className="w-full max-w-[600px] overflow-visible">
            <svg
              viewBox={`0 0 ${svgWidth} ${svgHeight}`}
              className="w-full h-auto select-none overflow-visible"
            >
              {/* Subtle Base Arc Guide */}
              <path
                d={`M ${cx - outerR} ${cy} A ${outerR} ${outerR} 0 0 1 ${cx + outerR} ${cy} L ${cx + innerR} ${cy} A ${innerR} ${innerR} 0 0 0 ${cx - innerR} ${cy} Z`}
                fill="none"
                stroke="currentColor"
                strokeWidth="0.75"
                className="text-zinc-100 dark:text-zinc-800/40"
              />

              {/* Arc Segments */}
              {arcSegments.map((seg) => {
                const isHovered = hoveredIndex === seg.index;
                return (
                  <g key={seg.id} className="transition-all duration-200">
                    <path
                      d={seg.pathData}
                      fill={seg.color}
                      stroke="currentColor"
                      strokeWidth={isHovered ? '2' : '1'}
                      strokeLinejoin="round"
                      className={`cursor-pointer text-white dark:text-zinc-950 transition-all duration-150 ${
                        isHovered ? 'opacity-100 filter drop-shadow-sm' : 'opacity-90 hover:opacity-100'
                      }`}
                      style={{ transformOrigin: `${cx}px ${cy}px` }}
                      onMouseEnter={() => setHoveredIndex(seg.index)}
                      onMouseLeave={() => setHoveredIndex(null)}
                    >
                      <title>{`${seg.name}: ${formatRM(seg.amount)} (${seg.percentage.toFixed(1)}%)`}</title>
                    </path>

                    {/* Fine Hairline Leader Line (Exact Image 1 Style) */}
                    <polyline
                      points={`${seg.leader.lx1},${seg.leader.ly1} ${seg.leader.lx2},${seg.leader.ly2} ${seg.leader.lx3},${seg.leader.ly3}`}
                      fill="none"
                      stroke="currentColor"
                      strokeWidth={isHovered ? '1.25' : '0.8'}
                      className={`transition-colors duration-150 ${
                        isHovered 
                          ? 'text-zinc-900 dark:text-zinc-100' 
                          : 'text-zinc-300 dark:text-zinc-600'
                      }`}
                    />

                    {/* Anchor Dot at Arc Border */}
                    <circle
                      cx={seg.leader.lx1}
                      cy={seg.leader.ly1}
                      r={isHovered ? '2.5' : '1.5'}
                      fill={seg.color}
                    />

                    {/* Leader Label: "Category: XX.X%" (Exact Image 1 Style) */}
                    <text
                      x={seg.label.textX}
                      y={seg.label.textY}
                      textAnchor={seg.label.textAnchor}
                      className={`text-[11px] tracking-tight transition-all duration-150 select-none ${
                        isHovered
                          ? 'font-bold fill-zinc-900 dark:fill-zinc-100 text-[11.5px]'
                          : 'font-medium fill-zinc-600 dark:fill-zinc-400'
                      }`}
                    >
                      {seg.name}: {seg.percentage.toFixed(1)}%
                    </text>
                  </g>
                );
              })}

              {/* Center Hub Metrics */}
              <g className="select-none pointer-events-none">
                <text
                  x={cx}
                  y={cy - 44}
                  textAnchor="middle"
                  className="fill-zinc-400 dark:fill-zinc-500 text-[10px] font-semibold tracking-wider uppercase"
                >
                  {isBM ? 'JUMLAH BELANJA' : 'TOTAL SPENT'}
                </text>
                <text
                  x={cx}
                  y={cy - 12}
                  textAnchor="middle"
                  className="fill-zinc-900 dark:fill-zinc-100 text-2xl sm:text-[26px] font-semibold tracking-tight tabular-nums"
                >
                  {formatRM(totalSpent)}
                </text>
                <text
                  x={cx}
                  y={cy + 12}
                  textAnchor="middle"
                  className="fill-zinc-500 dark:fill-zinc-400 text-[10.5px] font-medium"
                >
                  {count} {isBM ? 'transaksi' : 'transactions'}
                </text>
              </g>
            </svg>
          </div>

          {/* Bottom Legend (Matching Image 1: rounded square swatches + name + percentage) */}
          <div className="w-full mt-4 pt-4 border-t border-zinc-100 dark:border-zinc-800/80">
            <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2 text-xs">
              {arcSegments.map((seg) => {
                const isHovered = hoveredIndex === seg.index;
                return (
                  <button
                    key={seg.id}
                    onClick={() => setHoveredIndex(isHovered ? null : seg.index)}
                    onMouseEnter={() => setHoveredIndex(seg.index)}
                    onMouseLeave={() => setHoveredIndex(null)}
                    className={`flex items-center space-x-2 py-1 px-1.5 rounded-md transition-colors ${
                      isHovered ? 'bg-zinc-100 dark:bg-zinc-800' : 'hover:bg-zinc-50 dark:hover:bg-zinc-800/50'
                    }`}
                  >
                    {/* Swatch (Image 1 rounded square) */}
                    <span
                      className="w-3.5 h-3.5 rounded-[3px] shrink-0"
                      style={{ backgroundColor: seg.color }}
                    />

                    {/* Category Name */}
                    <span className="font-normal text-zinc-700 dark:text-zinc-300">
                      {seg.name}
                    </span>

                    {/* Percentage */}
                    <span className="font-semibold text-zinc-900 dark:text-zinc-100 tabular-nums">
                      {seg.percentage.toFixed(1)}%
                    </span>

                    {/* RM Amount (Subtle) */}
                    <span className="text-[11px] text-zinc-400 dark:text-zinc-500 tabular-nums">
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
