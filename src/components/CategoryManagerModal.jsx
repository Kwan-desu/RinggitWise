import React, { useState } from 'react';
import { 
  X, 
  Plus, 
  Trash2, 
  Tag, 
  Sparkles, 
  FolderPlus,
  RotateCcw
} from 'lucide-react';
import { DEFAULT_CATEGORIES } from '../data/initialData';

const COLOR_PRESETS = [
  '#38bdf8', // 1. Sky Blue (Lithuania in img 1)
  '#60a5fa', // 2. Blue (Czech Republic)
  '#6366f1', // 3. Slate Blue (Ireland)
  '#818cf8', // 4. Indigo (Germany)
  '#a855f7', // 5. Purple (Australia)
  '#c084fc', // 6. Orchid / Magenta (Austria)
  '#f43f5e', // 7. Rose
  '#10b981', // 8. Emerald
  '#f59e0b', // 9. Amber
  '#06b6d4', // 10. Cyan
  '#14b8a6', // 11. Teal
  '#ea580c', // 12. Orange
  '#ec4899', // 13. Pink
  '#84cc16', // 14. Lime
];

const EMOJI_PRESETS = [
  '🍔', '🛒', '⛽', '💡', '🧋', '🛍️', '🎬', '🏥', '📚', '📈',
  '🚗', '🏠', '✈️', '🎮', '☕', '🍜', '📱', '🏋️', '💳', '💰'
];

export default function CategoryManagerModal({
  isOpen,
  onClose,
  categories = [],
  onUpdateCategories,
  language = 'en'
}) {
  const isBM = language === 'bm';
  const [newCatName, setNewCatName] = useState('');
  const [newCatColor, setNewCatColor] = useState(COLOR_PRESETS[0]);
  const [newCatEmoji, setNewCatEmoji] = useState('🛍️');

  if (!isOpen) return null;

  const handleAddCategory = (e) => {
    e.preventDefault();
    if (!newCatName.trim()) return;

    // Check duplicate
    if (categories.some(c => c.name.toLowerCase() === newCatName.trim().toLowerCase())) {
      alert(isBM ? 'Kategori ini sudah wujud!' : 'This category already exists!');
      return;
    }

    const newCategory = {
      id: `cat-${Date.now()}`,
      name: newCatName.trim(),
      color: newCatColor,
      icon: newCatEmoji,
      isCustom: true,
    };

    onUpdateCategories([...categories, newCategory]);
    setNewCatName('');
  };

  const handleDeleteCategory = (catId) => {
    if (categories.length <= 1) {
      alert(isBM ? 'Anda mesti mengekalkan sekurang-kurangnya satu kategori.' : 'You must keep at least one category.');
      return;
    }
    onUpdateCategories(categories.filter(c => c.id !== catId));
  };

  const handleResetDefaults = () => {
    if (window.confirm(isBM ? 'Tetapkan semula kategori kepada pilihan asal Malaysia?' : 'Reset categories to Malaysian defaults?')) {
      onUpdateCategories(DEFAULT_CATEGORIES);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh] transition-colors">
        
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center space-x-2.5">
            <div className="w-8 h-8 rounded-lg bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <FolderPlus className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900 dark:text-white">
                {isBM ? 'Pengurus Kategori Perbelanjaan' : 'Spending Category Manager'}
              </h3>
              <p className="text-[11px] text-slate-500 dark:text-slate-400">
                {isBM ? 'Tambah, sunting warna dan emoji kategori perbelanjaan anda' : 'Create custom categories, choose colors and icons for the half-circle chart'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-700 dark:hover:text-white p-1 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 overflow-y-auto space-y-6">
          
          {/* Quick Add Form */}
          <form onSubmit={handleAddCategory} className="bg-slate-50 dark:bg-slate-950/70 border border-slate-200 dark:border-slate-800 rounded-xl p-4 space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-800 dark:text-slate-200 flex items-center space-x-1.5">
                <Plus className="w-3.5 h-3.5 text-emerald-500" />
                <span>{isBM ? 'Cipta Kategori Baharu' : 'Quick Create Category'}</span>
              </span>
              <span className="text-[11px] text-slate-500 dark:text-slate-400">
                {isBM ? 'Dipaparkan dalam carta separuh bulatan' : 'Appears in half-circle chart'}
              </span>
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">
                {isBM ? 'Nama Kategori' : 'Category Name'}
              </label>
              <input
                type="text"
                required
                placeholder={isBM ? 'cth. Durian & Buah, Gym, Servis Kereta' : 'e.g. Durian Fest, Gym Membership, Car Service'}
                value={newCatName}
                onChange={(e) => setNewCatName(e.target.value)}
                className="w-full bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:border-emerald-500"
              />
            </div>

            {/* Color Palette Selector */}
            <div>
              <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1.5">
                {isBM ? 'Pilih Warna Carta (Image 1 Style)' : 'Chart Arc Color'}
              </label>
              <div className="flex flex-wrap gap-2 items-center">
                {COLOR_PRESETS.map((color) => (
                  <button
                    key={color}
                    type="button"
                    onClick={() => setNewCatColor(color)}
                    className={`w-6 h-6 rounded-lg transition-transform ${
                      newCatColor === color ? 'scale-125 ring-2 ring-emerald-500 ring-offset-2 dark:ring-offset-slate-900' : 'hover:scale-110'
                    }`}
                    style={{ backgroundColor: color }}
                  />
                ))}
              </div>
            </div>

            {/* Emoji Selector */}
            <div>
              <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1.5">
                {isBM ? 'Ikon / Emoji' : 'Icon / Emoji'}
              </label>
              <div className="flex flex-wrap gap-1.5">
                {EMOJI_PRESETS.map((em) => (
                  <button
                    key={em}
                    type="button"
                    onClick={() => setNewCatEmoji(em)}
                    className={`w-7 h-7 rounded-lg text-sm flex items-center justify-center transition-all ${
                      newCatEmoji === em 
                        ? 'bg-emerald-500/20 border-2 border-emerald-500 scale-110' 
                        : 'bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:bg-slate-100 dark:hover:bg-slate-800'
                    }`}
                  >
                    {em}
                  </button>
                ))}
              </div>
            </div>

            <button
              type="submit"
              className="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-2 rounded-xl text-xs flex items-center justify-center space-x-1.5 shadow-md shadow-emerald-600/20 transition-all active:scale-95"
            >
              <Plus className="w-4 h-4" />
              <span>{isBM ? 'Simpan Kategori' : 'Add Category'}</span>
            </button>
          </form>

          {/* Existing Categories List */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-slate-800 dark:text-slate-200">
                {isBM ? 'Senarai Kategori Sedia Ada' : 'Active Categories'} ({categories.length})
              </span>
            </div>

            <div className="space-y-2 max-h-60 overflow-y-auto pr-1">
              {categories.map((cat) => (
                <div
                  key={cat.id}
                  className="flex items-center justify-between p-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900/90 hover:border-slate-300 dark:hover:border-slate-700 transition-colors"
                >
                  <div className="flex items-center space-x-3">
                    <span
                      className="w-4 h-4 rounded-[4px] shrink-0"
                      style={{ backgroundColor: cat.color }}
                    ></span>
                    <span className="text-sm">{cat.icon || '🏷️'}</span>
                    <span className="text-xs font-semibold text-slate-800 dark:text-slate-200">
                      {cat.name}
                    </span>
                  </div>

                  <button
                    onClick={() => handleDeleteCategory(cat.id)}
                    className="text-slate-400 hover:text-rose-500 p-1 rounded-md transition-colors"
                    title={isBM ? 'Padam Kategori' : 'Delete Category'}
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
          </div>

        </div>

        {/* Footer */}
        <div className="px-6 py-3.5 bg-slate-50 dark:bg-slate-950/80 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
          <button
            onClick={handleResetDefaults}
            className="flex items-center space-x-1 text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 text-xs transition-colors"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>{isBM ? 'Kembalikan Asal' : 'Reset to Defaults'}</span>
          </button>

          <button
            onClick={onClose}
            className="px-4 py-1.5 rounded-xl bg-slate-800 dark:bg-slate-700 text-white font-bold text-xs hover:bg-slate-700 dark:hover:bg-slate-600 transition-colors"
          >
            {isBM ? 'Selesai' : 'Done'}
          </button>
        </div>

      </div>
    </div>
  );
}
