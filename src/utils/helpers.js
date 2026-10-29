import confetti from 'canvas-confetti';

// Format currency as Malaysian Ringgit (RM)
export const formatRM = (val) => {
  const num = Number(val) || 0;
  return `RM ${num.toLocaleString('en-MY', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
};

// Bank Negara Malaysia 5-sen rounding rule
// 1, 2 sen -> round down to 0
// 3, 4, 6, 7 sen -> round to 5
// 8, 9 sen -> round up to 10
export const roundTo5Sen = (amount) => {
  const num = Number(amount) || 0;
  return Math.round(num * 20) / 20;
};

// Web Audio API Synthesizers (no external asset downloads needed!)
export const playSound = (type = 'success') => {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    const ctx = new AudioContext();

    if (type === 'duitnow') {
      // DuitNow signature dual chime (crisp high chime)
      const osc1 = ctx.createOscillator();
      const osc2 = ctx.createOscillator();
      const gain = ctx.createGain();

      osc1.type = 'sine';
      osc2.type = 'triangle';
      osc1.frequency.setValueAtTime(880, ctx.currentTime); // A5
      osc1.frequency.exponentialRampToValueAtTime(1760, ctx.currentTime + 0.15); // A6
      
      osc2.frequency.setValueAtTime(1320, ctx.currentTime);
      osc2.frequency.exponentialRampToValueAtTime(2200, ctx.currentTime + 0.2);

      gain.gain.setValueAtTime(0.2, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.45);

      osc1.connect(gain);
      osc2.connect(gain);
      gain.connect(ctx.destination);

      osc1.start();
      osc2.start();
      osc1.stop(ctx.currentTime + 0.45);
      osc2.stop(ctx.currentTime + 0.45);
    } else if (type === 'hammer-shame') {
      // Caleb Hammer buzzer / dramatic minor triad
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(140, ctx.currentTime);
      osc.frequency.linearRampToValueAtTime(95, ctx.currentTime + 0.35);

      gain.gain.setValueAtTime(0.25, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.35);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start();
      osc.stop(ctx.currentTime + 0.35);
    } else if (type === 'approve') {
      // Cheerful approval ding
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(523.25, ctx.currentTime); // C5
      osc.frequency.setValueAtTime(659.25, ctx.currentTime + 0.1); // E5
      osc.frequency.setValueAtTime(783.99, ctx.currentTime + 0.2); // G5

      gain.gain.setValueAtTime(0.18, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.4);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start();
      osc.stop(ctx.currentTime + 0.4);
    }
  } catch (e) {
    console.warn('Audio feedback failed to play', e);
  }
};

// Fire celebration confetti
export const fireCelebration = () => {
  try {
    confetti({
      particleCount: 80,
      spread: 70,
      origin: { y: 0.65 },
      colors: ['#059669', '#10B981', '#34D399', '#ED1C24', '#F59E0B'],
    });
  } catch (e) {
    // ignore
  }
};

// Format YYYY-MM to human readable (e.g. '2026-10' -> 'October 2026', '2026' -> 'Year 2026')
export const formatMonthYear = (yyyyMm, isBM = false) => {
  if (!yyyyMm || yyyyMm === 'all') return isBM ? 'Semua Masa' : 'All Time';
  if (/^\d{4}$/.test(yyyyMm)) {
    return isBM ? `Tahun ${yyyyMm}` : `Year ${yyyyMm}`;
  }
  const parts = yyyyMm.split('-');
  if (parts.length < 2) return yyyyMm;
  const year = parts[0];
  const monthIdx = parseInt(parts[1], 10) - 1;

  const enMonths = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];
  const bmMonths = [
    'Januari', 'Februari', 'Mac', 'April', 'Mei', 'Jun',
    'Julai', 'Ogos', 'September', 'Oktober', 'November', 'Disember'
  ];

  const mName = isBM ? bmMonths[monthIdx] : enMonths[monthIdx];
  return `${mName || ''} ${year}`.trim();
};

// Calculate previous YYYY-MM or YYYY
export const getPreviousMonthStr = (yyyyMm) => {
  if (!yyyyMm || yyyyMm === 'all') {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  }
  if (/^\d{4}$/.test(yyyyMm)) {
    return String(parseInt(yyyyMm, 10) - 1);
  }
  const [yearStr, monthStr] = yyyyMm.split('-');
  let y = parseInt(yearStr, 10);
  let m = parseInt(monthStr, 10) - 1;
  if (m < 1) {
    m = 12;
    y -= 1;
  }
  return `${y}-${String(m).padStart(2, '0')}`;
};

// Calculate next YYYY-MM or YYYY
export const getNextMonthStr = (yyyyMm) => {
  if (!yyyyMm || yyyyMm === 'all') {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  }
  if (/^\d{4}$/.test(yyyyMm)) {
    return String(parseInt(yyyyMm, 10) + 1);
  }
  const [yearStr, monthStr] = yyyyMm.split('-');
  let y = parseInt(yearStr, 10);
  let m = parseInt(monthStr, 10) + 1;
  if (m > 12) {
    m = 1;
    y += 1;
  }
  return `${y}-${String(m).padStart(2, '0')}`;
};

// Group transactions by month descending
export const groupTransactionsByMonth = (transactions) => {
  const map = {};
  transactions.forEach(tx => {
    const mStr = (tx.date && tx.date.length >= 7) ? tx.date.slice(0, 7) : 'Unknown';
    if (!map[mStr]) map[mStr] = [];
    map[mStr].push(tx);
  });
  return Object.keys(map)
    .sort((a, b) => b.localeCompare(a))
    .map(month => ({
      month,
      transactions: map[month],
      totalSpent: map[month].reduce((sum, t) => sum + Number(t.amount || 0), 0),
      count: map[month].length
    }));
};
