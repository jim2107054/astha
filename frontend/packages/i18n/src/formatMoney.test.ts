import { describe, it, expect } from 'vitest';
import { formatMoney, formatPaisaShort, toBengaliDigits } from './formatMoney';

describe('formatMoney - South Asian Lakh/Crore Formatting', () => {
  it('formats small amounts correctly in BDT and Bengali', () => {
    expect(formatMoney(50000, 'en')).toBe('BDT 500.00');
    expect(formatMoney(50000, 'bn')).toBe('৳৫০০.০০');
  });

  it('formats 1.5 Lakh correctly (1,50,000.00)', () => {
    // 1,50,000 Taka = 15,000,000 paisa
    expect(formatMoney(15000000, 'en')).toBe('BDT 1,50,000.00');
    expect(formatMoney(15000000, 'bn')).toBe('৳১,৫০,০০০.০০');
  });

  it('formats 2.5 Crore correctly (2,50,00,000.00)', () => {
    // 2,50,00,000 Taka = 2,500,000,000 paisa
    expect(formatMoney(2500000000, 'en')).toBe('BDT 2,50,00,000.00');
    expect(formatMoney(2500000000, 'bn')).toBe('৳২,৫০,০০,০০০.০০');
  });
});
