// Clean initial data for Ringgit Finance WebUI App

export const DEFAULT_CATEGORIES = [
  { id: "cat-1", name: "Mamak & Dining", color: "#38bdf8", icon: "🍔" },
  { id: "cat-2", name: "Groceries", color: "#3b82f6", icon: "🛒" },
  { id: "cat-3", name: "Fuel & Transport", color: "#6366f1", icon: "⛽" },
  { id: "cat-4", name: "Utilities & Bills", color: "#8b5cf6", icon: "💡" },
  { id: "cat-5", name: "Drinks & Cafe", color: "#a855f7", icon: "🧋" },
  { id: "cat-6", name: "Shopping & Online", color: "#d946ef", icon: "🛍️" },
  { id: "cat-7", name: "Entertainment", color: "#ec4899", icon: "🎬" },
  { id: "cat-8", name: "Healthcare & Pharmacy", color: "#10b981", icon: "🏥" },
  { id: "cat-9", name: "Education & Books", color: "#06b6d4", icon: "📚" },
  { id: "cat-10", name: "Savings & Investments", color: "#f59e0b", icon: "📈" },
];

export const INITIAL_USER_PROFILE = {
  name: "User",
  monthlyBudget: 3500,
  currency: "RM",
};

// Malaysian merchant templates for quick DuitNow QR simulation
export const POPULAR_MALAYSIAN_MERCHANTS = [
  {
    name: "Nasi Kandar Pelita",
    category: "Mamak & Dining",
    amount: 14.80,
    logoText: "Pelita",
    note: "Ayam Bawang + Nasi Kosong + Sirap Limau",
  },
  {
    name: "99 Speedmart",
    category: "Groceries",
    amount: 38.60,
    logoText: "99",
    note: "Maggi Kari, Telur Gred A, Dutch Lady Milk",
  },
  {
    name: "Petronas Mesra",
    category: "Fuel & Transport",
    amount: 50.00,
    logoText: "Petronas",
    note: "Primax 95 Fuel Full Tank",
  },
  {
    name: "Tealive",
    category: "Drinks & Cafe",
    amount: 12.50,
    logoText: "Tealive",
    note: "Signature Brown Sugar Pearl Milk Tea",
  },
  {
    name: "FamilyMart Malaysia",
    category: "Mamak & Dining",
    amount: 17.90,
    logoText: "FM",
    note: "Tom Yum Oden Bowl + Sofuto Cone",
  },
  {
    name: "Village Grocer",
    category: "Groceries",
    amount: 82.40,
    logoText: "VG",
    note: "Fresh vegetables, chicken breast & sourdough",
  },
  {
    name: "ZUS Coffee",
    category: "Drinks & Cafe",
    amount: 13.90,
    logoText: "ZUS",
    note: "Spanish Latte (Iced)",
  },
  {
    name: "Tenaga Nasional Berhad (TNB)",
    category: "Utilities & Bills",
    amount: 114.50,
    logoText: "TNB",
    note: "Monthly electricity bill",
  },
];

// Sample preview transactions matching Image 1 distribution
export const SAMPLE_TRANSACTIONS = [
  {
    id: "tx-1",
    date: "2026-10-28",
    title: "Nasi Kandar Pelita Bangsar",
    category: "Mamak & Dining",
    amount: 320.50,
    method: "DuitNow QR",
    note: "Mamak dinners & lunches",
  },
  {
    id: "tx-2",
    date: "2026-10-27",
    title: "Village Grocer & 99 Speedmart",
    category: "Groceries",
    amount: 240.00,
    method: "Touch 'n Go eWallet",
    note: "Weekly household groceries",
  },
  {
    id: "tx-3",
    date: "2026-10-26",
    title: "TNB Electricity & Syabas Water",
    category: "Utilities & Bills",
    amount: 160.00,
    method: "MAE by Maybank",
    note: "Monthly utilities payment",
  },
  {
    id: "tx-4",
    date: "2026-10-25",
    title: "Petronas RON95 & RFID Toll",
    category: "Fuel & Transport",
    amount: 132.00,
    method: "DuitNow QR",
    note: "Commute to KL sentral",
  },
  {
    id: "tx-5",
    date: "2026-10-24",
    title: "Shopee & Uniqlo",
    category: "Shopping & Online",
    amount: 111.00,
    method: "DuitNow QR",
    note: "Work attire and cables",
  },
  {
    id: "tx-6",
    date: "2026-10-23",
    title: "ZUS Coffee & Tealive",
    category: "Drinks & Cafe",
    amount: 102.50,
    method: "Touch 'n Go eWallet",
    note: "Afternoon coffee treats",
  },
];
