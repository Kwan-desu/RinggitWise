import * as pdfjsLib from 'pdfjs-dist';
import { createWorker } from 'tesseract.js';

// Configure pdfjs worker for browser
if (typeof window !== 'undefined' && pdfjsLib.GlobalWorkerOptions) {
  // Use unpkg CDN matching installed pdfjs-dist version
  pdfjsLib.GlobalWorkerOptions.workerSrc = `https://unpkg.com/pdfjs-dist@${pdfjsLib.version || '4.0.379'}/build/pdf.worker.min.mjs`;
}

/**
 * Preprocess an image file or DataURL onto an HTML5 Canvas:
 * - Upscales small images (e.g. cropped table rows with height < 200px)
 * - Flattens RGBA onto a pure white background
 * - Enhances contrast for crisp character edges
 */
export async function preprocessImage(imageSrc) {
  return new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');

      // Calculate scale factor: boost low-res / cropped images
      let scale = 1;
      if (img.height < 120) scale = 3.5;
      else if (img.height < 300) scale = 2.5;
      else if (img.height < 600) scale = 1.8;

      canvas.width = Math.round(img.width * scale);
      canvas.height = Math.round(img.height * scale);

      // 1. Fill solid white background (eliminates transparent PNG issues)
      ctx.fillStyle = '#FFFFFF';
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      // 2. High quality scaling
      ctx.imageSmoothingEnabled = true;
      ctx.imageSmoothingQuality = 'high';
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

      // 3. Mild contrast enhancement for OCR
      try {
        const imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        const data = imgData.data;
        for (let i = 0; i < data.length; i += 4) {
          // Grayscale luminosity
          const avg = 0.299 * data[i] + 0.587 * data[i + 1] + 0.114 * data[i + 2];
          // Simple contrast stretch: threshold dark text to darker, light to pure white
          const adjusted = avg > 190 ? 255 : (avg < 90 ? 0 : avg);
          data[i] = adjusted;
          data[i + 1] = adjusted;
          data[i + 2] = adjusted;
        }
        ctx.putImageData(imgData, 0, 0);
      } catch (e) {
        // In case of CORS canvas security restrictions, continue with scaled canvas
      }

      resolve(canvas.toDataURL('image/png'));
    };
    img.onerror = () => resolve(imageSrc);
    img.src = imageSrc;
  });
}

/**
 * Extract clean line-by-line text from a digital PDF file using pdfjs-dist.
 */
export async function extractTextFromPdf(arrayBuffer) {
  const loadingTask = pdfjsLib.getDocument({ data: arrayBuffer });
  const pdf = await loadingTask.promise;
  const lines = [];

  for (let pageNum = 1; pageNum <= pdf.numPages; pageNum++) {
    const page = await pdf.getPage(pageNum);
    const content = await page.getTextContent();

    // Group text items by vertical position Y
    const rowMap = new Map();
    for (const item of content.items) {
      if (!item.str || !item.str.trim()) continue;
      const y = Math.round(item.transform[5] / 3) * 3; // quantize Y by 3px
      if (!rowMap.has(y)) rowMap.set(y, []);
      rowMap.get(y).push(item);
    }

    // Sort rows descending (top to bottom of page)
    const sortedY = Array.from(rowMap.keys()).sort((a, b) => b - a);

    for (const y of sortedY) {
      const itemsInRow = rowMap.get(y);
      // Sort left to right
      itemsInRow.sort((a, b) => a.transform[4] - b.transform[4]);
      const rowText = itemsInRow.map(it => it.str.trim()).join(' ').trim();
      if (rowText) lines.push(rowText);
    }
  }

  return lines.join('\n');
}

/**
 * Intelligent categorization based on Malaysian spending terminology
 */
export function autoCategorize(title, rawLine = '') {
  const lower = `${title} ${rawLine}`.toLowerCase();

  // 1. Parking & Tolls (Explicit priority for parking & toll gates)
  if (/\b(parking|parkir|valet|autopay|toll|plazatoll|rfid|smarttag)\b|touch\s*['’]?n\s*go|tng\s*parking|plus\s*toll/i.test(lower)) {
    return 'Parking & Tolls';
  }

  // 2. Main Meals, Rice, Noodles & Dining (Checked before drinks to avoid 'rice' matching 'ice')
  if (/\b(rice|katsu|curry|chicken|mee|nasi|kandar|roti|burger|lunch|dinner|breakfast|food|dining|makan|restaurant|bistro|cafeteria|ayam|daging|ikan|soup|noodle|pasta|spaghetti|pizza)\b/i.test(lower)) {
    return 'Mamak & Dining';
  }

  // 3. Drinks, Coffee & Beverages (using word boundaries so 'rice' won't match 'ice')
  if (/\b(americano|latte|cappuccino|coffee|kopi|teh|tea|tealive|zus|starbucks|chagee|boba|ice|drink|drinks|beverage|mocha|espresso)\b/i.test(lower)) {
    return 'Drinks & Cafe';
  }

  // 4. Snacks & Desserts
  if (/\b(waffle|cake|dessert|donut|doughnut|croissant|snack|cookie|bakery|aiskrim|pastry|pie)\b|ice\s*cream/i.test(lower)) {
    return 'Snacks & Desserts';
  }

  // 5. Fuel & Public Transport
  if (/\b(petronas|shell|petron|caltex|fuel|petrol|diesel|rapid|mrt|lrt|bus|bas|ktm|monorail)\b|grab\s*ride/i.test(lower)) {
    return 'Fuel & Transport';
  }

  // 6. Groceries & Supermarkets
  if (/\b(grocer|groceries|speedmart|lotus|jaya|village|supermarket|pasar|mydin|econsave)\b|family\s*mart|7-eleven|cu\s*mart/i.test(lower)) {
    return 'Groceries';
  }

  // 7. Shopping & Retail
  if (/\b(shopee|lazada|tiktok|uniqlo|padini|h&m|zara|boutique)\b|mr\s*diy/i.test(lower)) {
    return 'Shopping & Online';
  }

  // 8. Utilities & Monthly Bills
  if (/\b(unifi|maxis|celcom|digi|tnb|astro|bill|bills|utility)\b|air\s*selangor|indah\s*water/i.test(lower)) {
    return 'Utilities & Bills';
  }

  // 9. Healthcare & Pharmacy
  if (/\b(clinic|klinik|hospital|pharmacy|farmasi|guardian|watsons|caring)\b/i.test(lower)) {
    return 'Healthcare & Pharmacy';
  }

  // 10. Top Up
  if (/\b(top\s*up|reload|deposit|credit)\b/i.test(lower)) {
    return 'Top Up (Credit)';
  }

  return 'Mamak & Dining';
}

/**
 * Core Parser: Parses both Statements (multi-transaction) and single Slips/Receipts
 */
export function parseFinancialText(rawText) {
  if (!rawText || !rawText.trim()) return { type: 'empty', items: [], rawText: '' };

  const cleanText = rawText.replace(/\r/g, '');
  const lines = cleanText.split('\n').map(l => l.trim()).filter(Boolean);
  const foundTransactions = [];

  // =========================================================================
  // PATTERN 1: Tabular Statement (APSpace / APCard / University / Bank table)
  // Format examples:
  // "18 2026-10-16 12:48:04 Parking Fee 3.94"
  // "4 2026-10-05 12:46:22 Creamy Mushroom Katsu Chicken Rice 9.5"
  // "18 | 2026-10-16 | 12:48:04 | Parking Fee | 3.94"
  // =========================================================================
  const statementRowRegex = /^(?:#?\s*)?(\d+)?\s*\|?\s*(\d{4}-\d{2}-\d{2})\s*\|?\s*(\d{2}:\d{2}:\d{2})?\s*\|?\s*(.+?)\s*\|?\s*([0-9]+(?:\.[0-9]{1,2})?)$/;

  for (const line of lines) {
    const match = line.match(statementRowRegex);
    if (match) {
      const [_, num, date, time, rawTitle, valStr] = match;
      const amount = parseFloat(valStr);
      let title = rawTitle.replace(/[|#]/g, '').trim();

      // Clean up title like "- Sauce (Japanese Curry)" or "RM10.00"
      if (/^RM\s*[0-9]+(?:\.[0-9]{2})?$/i.test(title)) {
        title = `Cafeteria Meal (${title})`;
      }

      const isTopUp = /top\s*up/i.test(title);
      const cat = autoCategorize(title, line);

      foundTransactions.push({
        id: `tx_${num || Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
        date: date || new Date().toISOString().split('T')[0],
        time: time || '',
        title,
        amount: Math.round(amount * 100) / 100,
        type: isTopUp ? 'credit' : 'debit',
        category: cat,
        method: 'Campus Card / APCard',
        note: time ? `Time: ${time}` : '',
        selected: !isTopUp && amount > 0, // Debits pre-selected for expense import
      });
    }
  }

  // =========================================================================
  // PATTERN 2: Single Cropped Row / Pipe Line
  // e.g. "18 | 2026-10-16 | 12:48:04 | Parking Fee | 3.94"
  // or "Parking Fee 3.94"
  // =========================================================================
  if (foundTransactions.length === 0) {
    const pipeRegex = /(\d{4}-\d{2}-\d{2})?\s*\|?\s*(\d{2}:\d{2}:\d{2})?\s*\|?\s*([^|0-9]+?)\s*\|?\s*([0-9]+(?:\.[0-9]{1,2}))$/;
    for (const line of lines) {
      const match = line.match(pipeRegex);
      if (match) {
        const [_, date, time, rawTitle, valStr] = match;
        const amount = parseFloat(valStr);
        const title = rawTitle.replace(/[|#]/g, '').trim();
        const cat = autoCategorize(title, line);

        foundTransactions.push({
          id: `tx_crop_${Date.now()}`,
          date: date || new Date().toISOString().split('T')[0],
          time: time || '',
          title: title || 'Scanned Payment',
          amount: Math.round(amount * 100) / 100,
          type: 'debit',
          category: cat,
          method: 'Card / Slip',
          note: time ? `Time: ${time}` : '',
          selected: true,
        });
        break;
      }
    }
  }

  // =========================================================================
  // PATTERN 3: Single DuitNow / Bank Transfer Slip (MAE, TNG, CIMB, GrabPay)
  // =========================================================================
  if (foundTransactions.length === 0) {
    // 1. Amount Extraction
    let amount = 0;
    const amountRegexes = [
      /(?:RM|MYR)\s*([0-9]{1,4}(?:,[0-9]{3})*\.[0-9]{2})/i,
      /(?:Amount|Total|Jumlah)\s*[:]?\s*(?:RM|MYR)?\s*([0-9]{1,4}(?:,[0-9]{3})*\.[0-9]{2})/i,
      /([0-9]{1,4}\.[0-9]{2})\s*(?:MYR|RM)/i,
      /([0-9]{1,4}\.[0-9]{2})/ // Standalone decimal amount
    ];

    for (const reg of amountRegexes) {
      const m = cleanText.match(reg);
      if (m && m[1]) {
        amount = parseFloat(m[1].replace(/,/g, ''));
        if (amount > 0) break;
      }
    }

    // 2. Bank / Provider Detection
    let method = 'DuitNow QR';
    if (/touch\s*['’]?n\s*go/i.test(cleanText) || /tng/i.test(cleanText)) {
      method = "Touch 'n Go eWallet";
    } else if (/mae/i.test(cleanText) || /maybank/i.test(cleanText)) {
      method = 'MAE by Maybank';
    } else if (/cimb/i.test(cleanText) || /octo/i.test(cleanText)) {
      method = 'CIMB OCTO';
    } else if (/grabpay/i.test(cleanText) || /grab/i.test(cleanText)) {
      method = 'GrabPay';
    } else if (/public\s*bank/i.test(cleanText)) {
      method = 'Public Bank';
    }

    // 3. Merchant / Recipient
    let title = '';
    const merchRegexes = [
      /(?:Paid To|Recipient|Beneficiary|Transfer To|Merchant)\s*[:]?\s*([^\n\r]{3,40})/i,
      /(?:To|Kepada)\s*[:]?\s*([^\n\r]{3,40})/i,
    ];

    for (const reg of merchRegexes) {
      const m = cleanText.match(reg);
      if (m && m[1]) {
        title = m[1].trim().replace(/[^a-zA-Z0-9\s&.-]/g, '');
        break;
      }
    }

    if (!title) {
      // Find prominent line
      const candidateLines = lines.filter(l => 
        !/successful|berjaya|completed|ref|reference|date|tarikh|duitnow|qr/i.test(l) &&
        l.length >= 3 && l.length <= 40
      );
      if (candidateLines.length > 0) title = candidateLines[0];
    }

    if (!title) title = 'DuitNow Payment';

    // 4. Reference Number & Date
    let refNo = '';
    const refMatch = cleanText.match(/(?:Ref|Reference|No\s*Rujukan)\s*(?:No|Number)?\s*[:]?\s*([A-Za-z0-9]{6,24})/i);
    if (refMatch && refMatch[1]) refNo = refMatch[1];

    let date = new Date().toISOString().split('T')[0];
    const dateMatch = cleanText.match(/(\d{4}-\d{2}-\d{2}|\d{2}\s+[A-Za-z]{3}\s+\d{4}|\d{2}\/\d{2}\/\d{4})/);
    if (dateMatch && dateMatch[1]) {
      const d = new Date(dateMatch[1]);
      if (!isNaN(d.getTime())) date = d.toISOString().split('T')[0];
    }

    if (amount > 0 || title) {
      foundTransactions.push({
        id: `tx_slip_${Date.now()}`,
        date,
        time: '',
        title,
        amount: amount || 0,
        type: 'debit',
        category: autoCategorize(title, cleanText),
        method,
        note: refNo ? `Ref: ${refNo}` : '',
        selected: true,
      });
    }
  }

  // Determine result nature
  const isStatement = foundTransactions.length > 1;

  return {
    type: isStatement ? 'statement' : (foundTransactions.length === 1 ? 'single' : 'unrecognized'),
    items: foundTransactions,
    rawText: cleanText,
    totalDebit: foundTransactions.filter(t => t.type === 'debit').reduce((acc, t) => acc + t.amount, 0),
    totalCredit: foundTransactions.filter(t => t.type === 'credit').reduce((acc, t) => acc + t.amount, 0),
  };
}

/**
 * Unified Process Function for File (PDF or Image)
 */
export async function processFileForTransactions(file, onProgress) {
  const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');

  if (isPdf) {
    if (onProgress) onProgress('Reading and extracting PDF text...');
    const buffer = await file.arrayBuffer();
    const pdfText = await extractTextFromPdf(buffer);

    if (pdfText && pdfText.trim().length > 10) {
      return parseFinancialText(pdfText);
    }

    // If PDF text was completely empty (e.g. scanned image inside PDF), notify
    return {
      type: 'empty_pdf',
      items: [],
      rawText: 'Scanned image PDF without digital text layer. Please use an image screenshot.',
    };
  }

  // Otherwise, handle as Image with pre-processing and Tesseract OCR
  if (onProgress) onProgress('Enhancing image resolution & contrast...');
  const reader = new FileReader();
  const rawDataUrl = await new Promise((res) => {
    reader.onload = (e) => res(e.target.result);
    reader.readAsDataURL(file);
  });

  const preprocessedUrl = await preprocessImage(rawDataUrl);

  if (onProgress) onProgress('Scanning text via Tesseract OCR engine...');
  const worker = await createWorker('eng');
  
  // Try PSM 6 (uniform block/table row)
  await worker.setParameters({
    tessedit_pageseg_mode: '6',
  });

  let ret = await worker.recognize(preprocessedUrl);
  let text = ret.data.text;

  // If text is very short or blank, try PSM 11 (sparse text for table cells)
  if (!text || text.trim().length < 5) {
    await worker.setParameters({
      tessedit_pageseg_mode: '11',
    });
    ret = await worker.recognize(preprocessedUrl);
    text = ret.data.text;
  }

  await worker.terminate();
  return parseFinancialText(text);
}
