import enCatalog from './locales/en.json';
import bnCatalog from './locales/bn.json';

export * from './formatMoney';
export * from './formatDate';

export const dictionaries = {
  en: enCatalog,
  bn: bnCatalog,
};

export function getDictionary(locale: 'en' | 'bn') {
  return dictionaries[locale] || dictionaries.en;
}
