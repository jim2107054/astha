import { describe, it, expect } from 'vitest';
import { formatDateDhaka } from './formatDate';

describe('formatDateDhaka', () => {
  it('formats UTC ISO timestamp in Asia/Dhaka (+06:00)', () => {
    // 2026-10-15T06:30:00Z -> 12:30 PM Dhaka
    const formatted = formatDateDhaka('2026-10-15T06:30:00Z', 'short', 'en');
    expect(formatted).toContain('2026');
    expect(formatted).toContain('12:30');
  });

  it('formats Bengali locale with Bengali numerals', () => {
    const formatted = formatDateDhaka('2026-10-15T06:30:00Z', 'short', 'bn');
    expect(formatted).toContain('২০২৬');
  });
});
