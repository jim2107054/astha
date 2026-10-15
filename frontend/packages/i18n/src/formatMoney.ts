export type Locale = 'en' | 'bn';

const BENGALI_DIGITS: Record<string, string> = {
  '0': '০',
  '1': '১',
  '2': '২',
  '3': '৩',
  '4': '৪',
  '5': '৫',
  '6': '৬',
  '7': '৭',
  '8': '৮',
  '9': '৯',
};

export function toBengaliDigits(input: string | number): string {
  return String(input).replace(/[0-9]/g, (digit) => BENGALI_DIGITS[digit] || digit);
}

/**
 * Format paisa into Bangladeshi Taka with South Asian grouping (Lakh/Crore)
 * e.g. 15000000 paisa -> 1,50,000.00 BDT or ৳১,৫০,০০০.০০
 */
export function formatMoney(paisa: number | bigint, locale: Locale = 'en'): string {
  const numericPaisa = Number(paisa);
  const isNegative = numericPaisa < 0;
  const absPaisa = Math.abs(numericPaisa);

  const takaWhole = Math.floor(absPaisa / 100);
  const remainderPaisa = absPaisa % 100;
  const paisaStr = remainderPaisa.toString().padStart(2, '0');

  // South Asian Number Grouping: last 3 digits, then pairs of 2 digits
  let formattedWhole = '';
  const wholeStr = takaWhole.toString();

  if (wholeStr.length <= 3) {
    formattedWhole = wholeStr;
  } else {
    const lastThree = wholeStr.substring(wholeStr.length - 3);
    const remaining = wholeStr.substring(0, wholeStr.length - 3);
    const groups: string[] = [];

    for (let i = remaining.length; i > 0; i -= 2) {
      const start = Math.max(0, i - 2);
      groups.unshift(remaining.substring(start, i));
    }

    formattedWhole = `${groups.join(',')},${lastThree}`;
  }

  const result = `${formattedWhole}.${paisaStr}`;

  if (locale === 'bn') {
    return `${isNegative ? '-' : ''}৳${toBengaliDigits(result)}`;
  }

  return `${isNegative ? '-' : ''}BDT ${result}`;
}

export function formatPaisaShort(paisa: number, locale: Locale = 'en'): string {
  const taka = paisa / 100;
  if (taka >= 10000000) {
    const crore = (taka / 10000000).toFixed(2);
    return locale === 'bn' ? `৳${toBengaliDigits(crore)} কোটি` : `BDT ${crore} Cr`;
  }
  if (taka >= 100000) {
    const lakh = (taka / 100000).toFixed(2);
    return locale === 'bn' ? `৳${toBengaliDigits(lakh)} লাখ` : `BDT ${lakh} L`;
  }
  return formatMoney(paisa, locale);
}
