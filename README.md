# 🇲🇾 RinggitWise — Malaysian Personal Finance & DuitNow QR App

> **Reverse-engineered Malaysian adaptation of Dollarwise ([dollarwise.com](https://dollarwise.com/)) & Caleb Hammer's 3-Bucket Financial Audit Framework.**  
> Built 100% in Malaysian Ringgit (RM) with native support for the Malaysian lifestyle: DuitNow QR payments, eWallets, Mamak bill splitting, PTPTN loan tracking, and dual PC/Mobile UI.

---

## 🌟 Key Features

### 1. 💰 All in Ringgit Malaysia (RM) & 3-Bucket Framework (50 / 30 / 20)
- **Keperluan (Needs - 50%)**: Rent/Mortgage, TNB electricity, Syabas / Air Selangor water, RON95 petrol (Petronas/Shell), Touch 'n Go RFID toll, groceries (99 Speedmart, Lotus's, Jaya Grocer), and medical costs.
- **Kehendak (Wants - 30%)**: Boba tea (Tealive/Chagee), cafe hunting, Shopee/TikTok shop 11.11 checkouts, Steam games, cinema tickets (GSC/TGV), and leisure dining.
- **Simpanan & Hutang (Savings & Debt - 20%)**: PTPTN loan repayment, Perodua/Proton car loan payoff, EPF (KWSP) voluntary contribution, ASB, and emergency fund tabung.

### 2. ⚡ DuitNow QR Malaysian Lifestyle Payment Hub
- **DuitNow QR Scanner Simulator**:
  - Live animated viewfinder with laser scanning effect.
  - **1-Tap Quick Pay for Popular Malaysian Merchants**:
    - *Nasi Kandar Pelita* (RM 13.80)
    - *99 Speedmart* (RM 34.60)
    - *Petronas Mesra RON95* (RM 50.00)
    - *Tealive Boba* (RM 12.50)
    - *FamilyMart Oden* (RM 17.90)
    - *Village Grocer* (RM 78.40)
    - *RapidKL LRT Commute* (RM 5.40)
    - *Mamak Roti Canai + Teh Tarik* (RM 9.50)
  - Custom merchant and amount scanning with authentic DuitNow chime sound (`Ting! Duit Masuk`) and celebration effects.
- **Personal DuitNow QR Generator**:
  - Enter recipient name, receiving bank/eWallet (MAE by Maybank, TNG eWallet, CIMB Octo, Boost, Bank Islam), amount in RM, and payment note.
  - Generates authentic official-style pink/red framed DuitNow QR code downloadable as PNG.
- **Kira-Kira Mamak (Bill Splitter)**:
  - Split bills with friends during lepak sessions.
  - Automatically incorporates **Bank Negara Malaysia's 5-sen rounding mechanism**.
  - Generates an instant DuitNow QR for everyone at the table to scan and pay back their exact share!

### 3. 🎯 "Safe to Spend" Metric (Dollarwise Signature)
- Real-time gauge calculating:
  - **Safe to Spend Today (RM)**
  - **Safe to Spend This Week (RM)**
- Guarantees you never spend money that belongs to next week's rent, TNB bill, or PTPTN repayment.

### 4. 🔥 Caleb Hammer's Weekly Swipe Review (Malaysia Edition)
- Tinder-style swipe queue for weekly transactions:
  - **Swipe Left / Red Hammer**: *"HAMMER SHAME / Pembaziran!"* (Plays shame buzzer, flags wasteful spend, displays spicy audit roasts: *"Aiyo! RM28 for iced latte when your SPayLater is overdue?!"*).
  - **Swipe Right / Green Check**: *"APPROVED / Keperluan"* (Essential expense approved).

### 5. 🏆 Malaysian Financial Audit Score (0 - 100)
- Caleb Hammer style scorecard evaluating:
  - Emergency Fund reserves (e.g. 3-6 months liquid cash in ASB / Versa).
  - Debt-to-Income (DTI) ratio.
  - Discretionary wants discipline.
  - Letter grade (A to D) with humorous yet hard-hitting financial advice.

### 6. 🚗 Malaysian Debt Payoff Planner (Snowball vs. Avalanche)
- Tracks Malaysian debts: PTPTN loan (1% ujrah), Myvi / Axia Hire Purchase car loan, Maybank/CIMB Credit Cards (15%), and Shopee SPayLater / GrabPayLater (18%).
- Switch between **Avalanche** (save highest interest) and **Snowball** (quick psychological momentum).
- Displays exact debt-free projection date and total interest saved in RM.

### 7. 📱 Dual PC UI & Mobile UI (Switchable Simulator)
- **PC Desktop UI**: Expansive multi-column dashboard with side-by-side analytics, budget breakdown, and complete transaction ledger with CSV export.
- **Mobile UI**: Native Malaysian fintech aesthetic with bottom navigation bar, floating action DuitNow button, touch cards, and swipe gestures.
- **One-Click View Mode Switcher**: Easily test and preview between "Auto Responsive", "PC Dashboard", and "Mobile Phone Simulator Frame" (complete with iPhone bezel and status bar).

---

## 🛠️ Tech Stack

- **Framework**: React 19 + Vite 8
- **Styling**: Tailwind CSS v4 + Plus Jakarta Sans Typography
- **Icons**: Lucide React
- **QR Code Engine**: `qrcode` (Vector canvas generation)
- **Audio Synthesis**: Native Web Audio API (Offline, zero external audio asset latency)
- **Effects**: `canvas-confetti`
- **Persistence**: `localStorage` (Zero-backend, privacy-first, offline-ready)

---

## 🚀 Quick Start & Development

```bash
# Clone the repository
git clone https://github.com/Kwan-desu/RinggitWise.git
cd RinggitWise

# Install dependencies
npm install

# Start local development server
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## 📦 Deployment to GitHub Pages

### Automatic Deployment (GitHub Actions)
A preconfigured workflow is included at `.github/workflows/deploy.yml`. When you push to `main` or `master`, GitHub Actions builds and publishes the site to GitHub Pages automatically.

To enable GitHub Pages in your repo:
1. Go to your repository on GitHub: `https://github.com/Kwan-desu/RinggitWise/settings/pages`
2. Under **Build and deployment > Source**, select **GitHub Actions**.

### Manual Deployment
```bash
npm run deploy
```

---

## 🇲🇾 Proudly Designed for Malaysia
Made with ❤️ for Malaysians seeking financial freedom, smarter budgeting, and zero spreadsheet headaches.
