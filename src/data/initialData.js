// Precision dataset matching Image 1 color palette & Malaysian lifestyle

export const DEFAULT_CATEGORIES = [
  { id: "cat-1", name: "Mamak & Dining", color: "#54b3d6" },
  { id: "cat-2", name: "Groceries", color: "#5b8ee5" },
  { id: "cat-3", name: "Utilities & Bills", color: "#5f6fe5" },
  { id: "cat-4", name: "Fuel & Transport", color: "#7f5ce6" },
  { id: "cat-5", name: "Shopping & Online", color: "#a856ea" },
  { id: "cat-6", name: "Drinks & Cafe", color: "#c856db" },
  { id: "cat-7", name: "Healthcare & Pharmacy", color: "#10b981" },
  { id: "cat-8", name: "Education & Books", color: "#06b6d4" },
  { id: "cat-9", name: "Savings & Investments", color: "#f59e0b" },
];

export const INITIAL_USER_PROFILE = {
  name: "User",
  monthlyBudget: 3500,
  currency: "RM",
};

// Malaysian merchants for quick testing
export const POPULAR_MALAYSIAN_MERCHANTS = [
  {
    name: "Nasi Kandar Pelita",
    category: "Mamak & Dining",
    amount: 14.80,
    note: "Ayam Bawang + Nasi Kosong + Sirap Limau",
  },
  {
    name: "99 Speedmart",
    category: "Groceries",
    amount: 38.60,
    note: "Maggi Kari, Telur Gred A, Dutch Lady Milk",
  },
  {
    name: "Petronas Mesra",
    category: "Fuel & Transport",
    amount: 50.00,
    note: "Primax 95 Fuel Full Tank",
  },
  {
    name: "Tealive",
    category: "Drinks & Cafe",
    amount: 12.50,
    note: "Signature Brown Sugar Pearl Milk Tea",
  },
  {
    name: "FamilyMart Malaysia",
    category: "Mamak & Dining",
    amount: 17.90,
    note: "Tom Yum Oden Bowl + Sofuto Cone",
  },
  {
    name: "Village Grocer",
    category: "Groceries",
    amount: 82.40,
    note: "Fresh vegetables, chicken breast & sourdough",
  },
  {
    name: "ZUS Coffee",
    category: "Drinks & Cafe",
    amount: 13.90,
    note: "Spanish Latte (Iced)",
  },
  {
    name: "Tenaga Nasional Berhad (TNB)",
    category: "Utilities & Bills",
    amount: 114.50,
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
    note: "Dinner with friends",
  },
  {
    id: "tx-2",
    date: "2026-10-27",
    title: "Village Grocer & 99 Speedmart",
    category: "Groceries",
    amount: 240.00,
    method: "Touch 'n Go eWallet",
    note: "Weekly groceries",
  },
  {
    id: "tx-3",
    date: "2026-10-26",
    title: "TNB Electricity & Water Bill",
    category: "Utilities & Bills",
    amount: 160.00,
    method: "MAE by Maybank",
    note: "October utilities",
  },
  {
    id: "tx-4",
    date: "2026-10-25",
    title: "Petronas RON95 & RFID Toll",
    category: "Fuel & Transport",
    amount: 132.00,
    method: "DuitNow QR",
    note: "Weekly commute",
  },
  {
    id: "tx-5",
    date: "2026-10-24",
    title: "Shopee & Uniqlo",
    category: "Shopping & Online",
    amount: 111.00,
    method: "DuitNow QR",
    note: "Basics & cables",
  },
  {
    id: "tx-6",
    date: "2026-10-23",
    title: "ZUS Coffee & Tealive",
    category: "Drinks & Cafe",
    amount: 102.50,
    method: "Touch 'n Go eWallet",
    note: "Coffee run",
  },
];
