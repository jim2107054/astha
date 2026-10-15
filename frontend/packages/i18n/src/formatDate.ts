import { Locale, toBengaliDigits } from './formatMoney';

export function formatDateDhaka(
  dateOrIso: string | Date,
  formatType: 'short' | 'long' | 'timeOnly' = 'short',
  locale: Locale = 'en'
): string {
  const d = typeof dateOrIso === 'string' ? new Date(dateOrIso) : dateOrIso;

  const options: Intl.DateTimeFormatOptions = {
    timeZone: 'Asia/Dhaka',
    hour12: true,
  };

  if (formatType === 'timeOnly') {
    options.hour = '2-digit';
    options.minute = '2-digit';
    options.second = '2-digit';
  } else if (formatType === 'short') {
    options.year = 'numeric';
    options.month = 'short';
    options.day = '2-digit';
    options.hour = '2-digit';
    options.minute = '2-digit';
  } else {
    options.year = 'numeric';
    options.month = 'long';
    options.day = '2-digit';
    options.hour = '2-digit';
    options.minute = '2-digit';
    options.second = '2-digit';
  }

  const formatted = new Intl.DateTimeFormat(locale === 'bn' ? 'bn-BD' : 'en-US', options).format(d);
  return formatted;
}
