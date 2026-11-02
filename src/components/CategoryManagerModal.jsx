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
      <div 
        className="w-full max-w-md rounded-[28px] shadow-2xl overflow-hidden flex flex-col max-h-[85vh] transition-all"
        style={{
          backgroundColor: 'var(--md-sys-color-surface-container-high)',
          color: 'var(--md-sys-color-on-surface)'
        }}
      >
        
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b" style={{ borderColor: 'var(--md-sys-color-outline-variant)' }}>
          <div>
            <h3 className="text-base font-bold" style={{ color: 'var(--md-sys-color-on-surface)' }}>
              {isBM ? 'Pengurus Kategori' : 'Manage Categories'}
            </h3>
            <p className="text-xs" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
              {categories.length} {isBM ? 'kategori aktif' : 'active spending categories'}
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:opacity-80 transition-colors"
            style={{
              backgroundColor: 'var(--md-sys-color-surface-container)',
              color: 'var(--md-sys-color-on-surface-variant)'
            }}
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-4">
          
          {/* Quick Add Form */}
          <form onSubmit={handleAddCategory} className="rounded-2xl p-4 space-y-3" style={{
            backgroundColor: 'var(--md-sys-color-surface)',
            border: '1px solid var(--md-sys-color-outline-variant)'
          }}>
            <div>
              <label className="block text-[11px] font-bold mb-1 uppercase tracking-wider" style={{ color: 'var(--md-sys-color-primary)' }}>
                {isBM ? 'Nama Kategori Baharu' : 'New Category Name'}
              </label>
              <input
                type="text"
                required
                placeholder={isBM ? 'cth. Servis Kereta, Gym' : 'e.g. Gym, Car Maintenance'}
                value={newCatName}
                onChange={(e) => setNewCatName(e.target.value)}
                className="w-full rounded-xl px-3 py-2 text-xs focus:outline-none"
                style={{
                  backgroundColor: 'var(--md-sys-color-surface-container)',
                  border: '1px solid var(--md-sys-color-outline-variant)',
                  color: 'var(--md-sys-color-on-surface)'
                }}
              />
            </div>

            {/* Color Swatch Picker */}
            <div>
              <label className="block text-[11px] font-semibold mb-1.5" style={{ color: 'var(--md-sys-color-on-surface-variant)' }}>
                {isBM ? 'Warna Carta' : 'Arc Chart Color'}
              </label>
              <div className="flex flex-wrap gap-2 items-center">
                {COLOR_PRESETS.map((color) => (
                  <button
                    key={color}
                    type="button"
                    onClick={() => setNewCatColor(color)}
                    className={`w-6 h-6 rounded-full transition-transform ${
                      newCatColor === color ? 'scale-125 ring-2 ring-emerald-500' : 'hover:scale-110'
                    }`}
                    style={{ backgroundColor: color }}
                  />
                ))}
              </div>
            </div>

            <button
              type="submit"
              className="w-full font-bold py-2 rounded-full text-xs transition-all shadow-xs flex items-center justify-center space-x-1"
              style={{
                backgroundColor: 'var(--md-sys-color-primary)',
                color: 'var(--md-sys-color-on-primary)'
              }}
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
                className="flex items-center justify-between p-2.5 rounded-xl transition-colors"
                style={{
                  backgroundColor: 'var(--md-sys-color-surface)',
                  border: '1px solid var(--md-sys-color-outline-variant)'
                }}
              >
                <div className="flex items-center space-x-2.5">
                  <span
                    className="w-3.5 h-3.5 rounded-full shrink-0"
                    style={{ backgroundColor: cat.color }}
                  />
                  <span className="text-xs font-semibold" style={{ color: 'var(--md-sys-color-on-surface)' }}>
                    {cat.name}
                  </span>
                </div>

                <button
                  onClick={() => handleDeleteCategory(cat.id)}
                  className="p-1 rounded-full text-zinc-400 hover:text-rose-500 transition-colors"
                  title="Delete Category"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}
          </div>

        </div>

        {/* Footer */}
        <div className="px-6 py-3.5 border-t flex items-center justify-between" style={{
          backgroundColor: 'var(--md-sys-color-surface-container)',
          borderColor: 'var(--md-sys-color-outline-variant)'
        }}>
          <button
            onClick={handleResetDefaults}
            className="flex items-center space-x-1.5 text-xs font-semibold hover:opacity-80 transition-colors"
            style={{ color: 'var(--md-sys-color-on-surface-variant)' }}
          >
            <RotateCcw className="w-3 h-3" />
            <span>{isBM ? 'Pulihkan Asal' : 'Reset Defaults'}</span>
          </button>

          <button
            onClick={onClose}
            className="px-4 py-1.5 rounded-full text-xs font-bold shadow-xs"
            style={{
              backgroundColor: 'var(--md-sys-color-primary)',
              color: 'var(--md-sys-color-on-primary)'
            }}
          >
            {isBM ? 'Selesai' : 'Done'}
          </button>
        </div>

      </div>
    </div>
  );
}
