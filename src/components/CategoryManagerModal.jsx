import React, { useState } from 'react';
import { X, Plus, Trash2, RotateCcw } from 'lucide-react';
import { DEFAULT_CATEGORIES } from '../data/initialData';

const COLOR_PRESETS = [
  '#54b3d6', // Sky Cerulean
  '#5b8ee5', // Medium Blue
  '#5f6fe5', // Slate Indigo
  '#7f5ce6', // Purple
  '#a856ea', // Violet
  '#c856db', // Orchid Magenta
  '#10b981', // Emerald
  '#f59e0b', // Amber
  '#f43f5e', // Rose
  '#06b6d4', // Cyan
  '#64748b', // Slate
  '#d946ef', // Fuchsia
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

  if (!isOpen) return null;

  const handleAddCategory = (e) => {
    e.preventDefault();
    if (!newCatName.trim()) return;

    if (categories.some(c => c.name.toLowerCase() === newCatName.trim().toLowerCase())) {
      alert(isBM ? 'Kategori ini telah wujud!' : 'This category already exists!');
      return;
    }

    const newCategory = {
      id: `cat-${Date.now()}`,
      name: newCatName.trim(),
      color: newCatColor,
    };

    onUpdateCategories([...categories, newCategory]);
    setNewCatName('');
  };

  const handleDeleteCategory = (catId) => {
    if (categories.length <= 1) {
      alert(isBM ? 'Perlu sekurang-kurangnya 1 kategori.' : 'You must keep at least 1 category.');
      return;
    }
    onUpdateCategories(categories.filter(c => c.id !== catId));
  };

  const handleResetDefaults = () => {
    if (window.confirm(isBM ? 'Tetapkan semula kategori asal?' : 'Reset to default categories?')) {
      onUpdateCategories(DEFAULT_CATEGORIES);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-md bg-white dark:bg-zinc-900 border border-zinc-200/80 dark:border-zinc-800/80 rounded-2xl shadow-xl overflow-hidden flex flex-col max-h-[85vh] transition-colors">
        
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-zinc-100 dark:border-zinc-800/80">
          <div>
            <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
              {isBM ? 'Pengurus Kategori' : 'Manage Categories'}
            </h3>
            <p className="text-[11px] text-zinc-500">
              {categories.length} {isBM ? 'kategori aktif' : 'active spending categories'}
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 p-1"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 overflow-y-auto space-y-5">
          
          {/* Quick Add Form */}
          <form onSubmit={handleAddCategory} className="bg-zinc-50 dark:bg-zinc-950/60 border border-zinc-200/80 dark:border-zinc-800/80 rounded-xl p-3.5 space-y-3">
            <div>
              <label className="block text-[11px] font-medium text-zinc-600 dark:text-zinc-400 mb-1">
                {isBM ? 'Nama Kategori Baharu' : 'New Category Name'}
              </label>
              <input
                type="text"
                required
                placeholder={isBM ? 'cth. Servis Kereta, Gym' : 'e.g. Gym, Car Maintenance'}
                value={newCatName}
                onChange={(e) => setNewCatName(e.target.value)}
                className="w-full bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-700 rounded-lg px-2.5 py-1.5 text-xs text-zinc-900 dark:text-zinc-100 focus:outline-none"
              />
            </div>

            {/* Color Swatch Picker */}
            <div>
              <label className="block text-[11px] font-medium text-zinc-600 dark:text-zinc-400 mb-1.5">
                {isBM ? 'Warna Carta' : 'Arc Chart Color'}
              </label>
              <div className="flex flex-wrap gap-2 items-center">
                {COLOR_PRESETS.map((color) => (
                  <button
                    key={color}
                    type="button"
                    onClick={() => setNewCatColor(color)}
                    className={`w-5 h-5 rounded-md transition-transform ${
                      newCatColor === color ? 'scale-125 ring-2 ring-zinc-900 dark:ring-zinc-100' : 'hover:scale-110'
                    }`}
                    style={{ backgroundColor: color }}
                  />
                ))}
              </div>
            </div>

            <button
              type="submit"
              className="w-full bg-zinc-900 hover:bg-zinc-800 dark:bg-zinc-100 dark:hover:bg-white text-white dark:text-zinc-950 font-semibold py-1.5 rounded-lg text-xs transition-colors flex items-center justify-center space-x-1"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>{isBM ? 'Simpan Kategori' : 'Add Category'}</span>
            </button>
          </form>

          {/* Categories List */}
          <div className="space-y-1.5 max-h-56 overflow-y-auto pr-1">
            {categories.map((cat) => (
              <div
                key={cat.id}
                className="flex items-center justify-between p-2 rounded-lg border border-zinc-200/80 dark:border-zinc-800/80 bg-white dark:bg-zinc-900/60"
              >
                <div className="flex items-center space-x-2.5">
                  <span
                    className="w-3 h-3 rounded-[3px] shrink-0"
                    style={{ backgroundColor: cat.color }}
                  />
                  <span className="text-xs font-medium text-zinc-800 dark:text-zinc-200">
                    {cat.name}
                  </span>
                </div>

                <button
                  onClick={() => handleDeleteCategory(cat.id)}
                  className="text-zinc-400 hover:text-rose-500 p-1 transition-colors"
                  title="Delete Category"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}
          </div>

        </div>

        {/* Footer */}
        <div className="px-5 py-3 bg-zinc-50/80 dark:bg-zinc-950/80 border-t border-zinc-100 dark:border-zinc-800/80 flex items-center justify-between">
          <button
            onClick={handleResetDefaults}
            className="flex items-center space-x-1 text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 text-xs transition-colors"
          >
            <RotateCcw className="w-3 h-3" />
            <span>{isBM ? 'Pulihkan Asal' : 'Reset Defaults'}</span>
          </button>

          <button
            onClick={onClose}
            className="px-3 py-1 rounded-md bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 text-xs font-semibold"
          >
            {isBM ? 'Tutup' : 'Done'}
          </button>
        </div>

      </div>
    </div>
  );
}
