// Clean empty default states - Users input their own financial data

export const EMPTY_USER_PROFILE = {
  name: "",
  monthlyIncome: 0,
  targetEmergencyFundMonths: 6,
  currency: "RM",
  language: "en",
};

export const EMPTY_TRANSACTIONS = [];
export const EMPTY_DEBTS = [];
export const EMPTY_SAVINGS_GOALS = [];
export const EMPTY_SWIPE_QUEUE = [];

export const INITIAL_BUCKETS = {
  needs: {
    name: "Keperluan (Needs)",
    targetPercent: 50,
    color: "emerald",
    description: "Rent, bills (TNB, Syabas), groceries (99 Speedmart), petrol (RON95), toll (RFID), medical & basic food.",
  },
  wants: {
    name: "Kehendak (Wants)",
    targetPercent: 30,
    color: "amber",
    description: "Boba tea, cafe hopping, Shopee/TikTok shop, gaming, weekend travel, movie tickets, dining out.",
  },
  savings: {
    name: "Simpanan & Hutang (Savings & Debt)",
    targetPercent: 20,
    color: "blue",
    description: "PTPTN repayment, Car loan extra, EPF (KWSP) voluntary, ASB, Emergency fund tabung.",
  },
};

// Malaysian merchant templates for quick DuitNow QR simulation
export const POPULAR_MALAYSIAN_MERCHANTS = [
  {
    name: "Nasi Kandar Pelita",
    category: "Mamak & Dining",
    amount: 13.80,
    bucket: "needs",
    logoText: "Pelita",
    note: "Ayam Bawang + Nasi Kosong + Sirap Limau",
  },
  {
    name: "99 Speedmart",
    category: "Groceries",
    amount: 34.60,
    bucket: "needs",
    logoText: "99",
    note: "Maggi Kari, Telur Gred A, Dutch Lady Milk",
  },
  {
    name: "Petronas Mesra",
    category: "Fuel & Transport",
    amount: 50.00,
    bucket: "needs",
    logoText: "Petronas",
    note: "Primax 95 with Pro-Drive",
  },
  {
    name: "Tealive",
    category: "Drinks & Cafe",
    amount: 12.50,
    bucket: "wants",
    logoText: "Tealive",
    note: "Signature Brown Sugar Pearl Milk Tea",
  },
  {
    name: "FamilyMart Malaysia",
    category: "Convenience & Snacks",
    amount: 17.90,
    bucket: "wants",
    logoText: "FM",
    note: "Tom Yum Oden Bowl + Sofuto Cone",
  },
  {
    name: "Village Grocer",
    category: "Groceries",
    amount: 78.40,
    bucket: "needs",
    logoText: "VG",
    note: "Fresh vegetables, chicken breast & sourdough",
  },
  {
    name: "RapidKL LRT Pass",
    category: "Public Transport",
    amount: 5.40,
    bucket: "needs",
    logoText: "Rapid",
    note: "Kelana Jaya Line commute token",
  },
  {
    name: "Mamak Corner SS2",
    category: "Mamak & Dining",
    amount: 9.50,
    bucket: "needs",
    logoText: "Mamak",
    note: "Roti Canai x2 + Teh Tarik Kurang Manis",
  },
];

// Optional Demo Dataset (Only loaded if user explicitly clicks "Load Demo Data")
export const DEMO_USER_PROFILE = {
  name: "Kwan / Ahmad",
  monthlyIncome: 4800,
  targetEmergencyFundMonths: 6,
  currency: "RM",
  language: "en",
};

export const DEMO_DEBTS = [
  {
    id: "debt-1",
    name: "PTPTN Student Loan",
    category: "Education",
    totalAmount: 18500,
    remainingAmount: 12400,
    interestRate: 1.0,
    minimumMonthly: 150,
    currentPayment: 200,
  },
  {
    id: "debt-2",
    name: "Perodua Myvi 1.5 H Car Loan",
    category: "Vehicle",
    totalAmount: 48000,
    remainingAmount: 31200,
    interestRate: 3.2,
    minimumMonthly: 590,
    currentPayment: 590,
  },
  {
    id: "debt-3",
    name: "Maybank 2 Gold Credit Card",
    category: "Credit Card",
    totalAmount: 4200,
    remainingAmount: 1850,
    interestRate: 15.0,
    minimumMonthly: 100,
    currentPayment: 350,
  },
  {
    id: "debt-4",
    name: "Shopee SPayLater",
    category: "BNPL",
    totalAmount: 1200,
    remainingAmount: 420,
    interestRate: 18.0,
    minimumMonthly: 140,
    currentPayment: 140,
  },
];

export const DEMO_SAVINGS_GOALS = [
  {
    id: "sav-1",
    name: "Dana Kecemasan (Emergency Fund 6 Bulan)",
    target: 14400,
    current: 9200,
    institution: "ASB / Versa Cash / KDI",
    icon: "ShieldAlert",
  },
  {
    id: "sav-2",
    name: "KWSP / EPF Simpanan Sukarela",
    target: 5000,
    current: 2400,
    institution: "KWSP i-Akaun",
    icon: "PiggyBank",
  },
];

export const DEMO_TRANSACTIONS = [
  {
    id: "tx-1",
    date: "2026-10-28",
    title: "Nasi Kandar Pelita Bangsar",
    category: "Mamak & Dining",
    amount: 14.50,
    bucket: "needs",
    method: "DuitNow QR",
    note: "Nasi kandar + teh tarik panas",
    reviewed: true,
    shameFlag: false,
  },
  {
    id: "tx-2",
    date: "2026-10-27",
    title: "99 Speedmart SS15",
    category: "Groceries",
    amount: 48.90,
    bucket: "needs",
    method: "Touch 'n Go eWallet",
    note: "Beras, telur, Milo, roti",
    reviewed: true,
    shameFlag: false,
  },
  {
    id: "tx-3",
    date: "2026-10-27",
    title: "Petronas Mesra Subang Jaya",
    category: "Fuel & Transport",
    amount: 50.00,
    bucket: "needs",
    method: "DuitNow QR",
    note: "RON95 full tank Myvi",
    reviewed: true,
    shameFlag: false,
  },
  {
    id: "tx-4",
    date: "2026-10-26",
    title: "Tenaga Nasional Berhad (TNB)",
    category: "Utilities",
    amount: 138.45,
    bucket: "needs",
    method: "MAE by Maybank",
    note: "Monthly electricity bill",
    reviewed: true,
    shameFlag: false,
  },
  {
    id: "tx-5",
    date: "2026-10-25",
    title: "Chagee Milk Tea Mid Valley",
    category: "Drinks & Cafe",
    amount: 18.90,
    bucket: "wants",
    method: "DuitNow QR",
    note: "White Peach Oolong Latte",
    reviewed: true,
    shameFlag: true,
  },
];

export const DEMO_SWIPE_QUEUE = [
  {
    id: "sq-1",
    title: "Starbucks Reserve Bukit Bintang",
    category: "Cafe & Drinks",
    amount: 28.50,
    suggestedBucket: "wants",
    method: "DuitNow QR",
    date: "Today, 4:15 PM",
    hammerComment: "Aiyo! RM28.50 for iced caramel macchiato when you have SPayLater overdue?! That's 5 plates of economy rice bro!",
    isEmergency: false,
  },
  {
    id: "sq-2",
    title: "Air Selangor Water Bill",
    category: "Utilities",
    amount: 16.80,
    suggestedBucket: "needs",
    method: "MAE by Maybank",
    date: "Yesterday",
    hammerComment: "Legitimate essential. Clean water is a basic human necessity, pay your bill on time!",
    isEmergency: false,
  },
  {
    id: "sq-3",
    title: "TikTok Shop Midnight Checkout",
    category: "Shopping",
    amount: 74.00,
    suggestedBucket: "wants",
    method: "Touch 'n Go eWallet",
    date: "Oct 27, 2:30 AM",
    hammerComment: "3:00 AM doom-scrolling shopping spree?! Do you really need another RGB mousepad while living paycheck to paycheck?",
    isEmergency: false,
  },
];
