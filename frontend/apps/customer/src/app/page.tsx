'use client';

import React, { useState } from 'react';
import { formatMoney, formatDateDhaka, Locale } from '@astha/i18n';

interface Transaction {
  id: string;
  counterparty: string;
  type: string;
  amountPaisa: number;
  date: string;
  status: 'SUCCESS' | 'FAILED_DEBITED' | 'PENDING';
}

interface CustomerDispute {
  id: string;
  txnId: string;
  reason: string;
  amountPaisa: number;
  status: 'SUBMITTED' | 'UNDER_INVESTIGATION' | 'RESOLVED_REFUNDED';
  createdAt: string;
  deadlineDhaka: string;
  autoRefundEligible: boolean;
  notes: string;
}

const INITIAL_TRANSACTIONS: Transaction[] = [
  {
    id: 'TXN-88210',
    counterparty: 'গুলশান ব্রাঞ্চ এটিএম (ATM-04)',
    type: 'ক্যাশ আউট (Cash Out)',
    amountPaisa: 500000, // 5,000 Taka
    date: '2026-10-15T09:30:00Z',
    status: 'FAILED_DEBITED',
  },
  {
    id: 'TXN-88209',
    counterparty: 'স্বপ্ন সুপারশপ (স্বপ্না গ্রোসারি)',
    type: 'মার্চেন্ট পেমেন্ট (Merchant QR)',
    amountPaisa: 185000, // 1,850 Taka
    date: '2026-10-14T18:15:00Z',
    status: 'SUCCESS',
  },
  {
    id: 'TXN-88208',
    counterparty: '০১৭৮৮৯৯০০১১ (Send Money)',
    type: 'সেন্ড মানি (P2P)',
    amountPaisa: 250000, // 2,500 Taka
    date: '2026-10-14T11:45:00Z',
    status: 'SUCCESS',
  },
];

export default function CustomerPortalPage() {
  const [locale, setLocale] = useState<Locale>('bn');
  const [phone, setPhone] = useState('+8801711223344');
  const [otp, setOtp] = useState('');
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [otpSent, setOtpSent] = useState(false);
  const [activeView, setActiveView] = useState<'home' | 'dispute_wizard' | 'tracker' | 'merchant'>('home');

  // Dispute Wizard State (4-Tap flow)
  const [selectedTxn, setSelectedTxn] = useState<Transaction | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [note, setNote] = useState('');
  const [photoAttached, setPhotoAttached] = useState(false);
  const [disputes, setDisputes] = useState<CustomerDispute[]>([]);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 4000);
  };

  const handleSendOtp = (e: React.FormEvent) => {
    e.preventDefault();
    if (phone.length < 11) return;
    setOtpSent(true);
    setOtp('123456'); // Pre-fill mock OTP for seamless testing
    showToast(locale === 'bn' ? 'ওটিপি কোড পাঠানো হয়েছে: 123456' : 'OTP Sent: 123456');
  };

  const handleVerifyOtp = (e: React.FormEvent) => {
    e.preventDefault();
    if (otp === '123456') {
      setIsLoggedIn(true);
      showToast(locale === 'bn' ? 'লগইন সফল হয়েছে' : 'Login successful');
    } else {
      showToast(locale === 'bn' ? 'ভুল ওটিপি কোড' : 'Invalid OTP');
    }
  };

  // 4-Tap Wizard Submit
  const handleConfirmDispute = () => {
    if (!selectedTxn) return;

    const isAutoRefund = selectedTxn.status === 'FAILED_DEBITED';
    const newDispute: CustomerDispute = {
      id: `DSP-CUS-${Date.now().toString().slice(-4)}`,
      txnId: selectedTxn.id,
      reason: selectedCategory,
      amountPaisa: selectedTxn.amountPaisa,
      status: isAutoRefund ? 'RESOLVED_REFUNDED' : 'SUBMITTED',
      createdAt: new Date().toISOString(),
      deadlineDhaka: '১৮ অক্টোবর ২০২৬, বিকাল ৬:০০ (৩ কার্যদিবস)',
      autoRefundEligible: isAutoRefund,
      notes: note || 'গ্রাহক দ্বারা রিপোর্ট করা সমস্যা',
    };

    setDisputes([newDispute, ...disputes]);
    setActiveView('tracker');
    setSelectedTxn(null);
    setSelectedCategory('');
    setNote('');
    showToast(
      isAutoRefund
        ? (locale === 'bn' ? 'অটো-রিফান্ড অনুমোদিত! অর্থ স্বয়ংক্রিয়ভাবে ফেরত দেওয়া হয়েছে।' : 'Auto-refund approved and processed!')
        : (locale === 'bn' ? 'অভিযোগ সফলভাবে দাখিল হয়েছে।' : 'Dispute successfully submitted.')
    );
  };

  return (
    <div className="max-w-md mx-auto min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans border-x border-slate-900 shadow-2xl">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 bg-teal-900 border border-teal-500 text-white text-xs px-4 py-2 rounded-full shadow-lg">
          🔔 {toastMessage}
        </div>
      )}

      {/* Header */}
      <header className="p-4 bg-slate-900 border-b border-slate-800 flex items-center justify-between sticky top-0 z-30">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-teal-600 flex items-center justify-center font-bold text-white shadow-md shadow-teal-900/50">
            আ
          </div>
          <div>
            <h1 className="font-bold text-sm tracking-tight text-white">আস্থা (Astha)</h1>
            <span className="text-[10px] text-teal-400">গ্রাহক সেবা ও বিরোধ পোর্টাল</span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setLocale(locale === 'bn' ? 'en' : 'bn')}
            className="px-2 py-1 rounded bg-slate-800 text-xs text-slate-300 font-semibold border border-slate-700"
          >
            {locale === 'bn' ? 'English' : 'বাংলা'}
          </button>
          {isLoggedIn && (
            <button
              onClick={() => {
                setIsLoggedIn(false);
                setOtpSent(false);
              }}
              className="text-xs text-rose-400 hover:underline"
            >
              লগআউট
            </button>
          )}
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 p-4 pb-20 space-y-4">
        {!isLoggedIn ? (
          /* Phone OTP Sign-in */
          <div className="p-6 rounded-2xl bg-slate-900/80 border border-slate-800 space-y-5 my-auto mt-12">
            <div className="text-center space-y-1">
              <h2 className="text-lg font-bold text-white">
                {locale === 'bn' ? 'এমএফএস বিরোধ নিষ্পত্তি' : 'MFS Dispute Resolution'}
              </h2>
              <p className="text-xs text-slate-400">
                {locale === 'bn'
                  ? 'লেনদেনে কোনো সমস্যা হলে সরাসরি অভিযোগ দাখিল করুন'
                  : 'Report disputed transactions with regulated bank SLA'}
              </p>
            </div>

            {!otpSent ? (
              <form onSubmit={handleSendOtp} className="space-y-4">
                <div>
                  <label className="text-xs text-slate-300 block mb-1">
                    {locale === 'bn' ? 'মোবাইল নম্বর' : 'Mobile Number'}
                  </label>
                  <input
                    type="tel"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="+88017xxxxxxxx"
                    className="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm font-mono text-white focus:outline-none focus:border-teal-500"
                    required
                  />
                </div>
                <button
                  type="submit"
                  className="w-full py-3 rounded-xl bg-teal-600 hover:bg-teal-500 text-white font-semibold text-sm transition-colors shadow-lg shadow-teal-900/40"
                >
                  {locale === 'bn' ? 'ওটিপি কোড পাঠান' : 'Send OTP'}
                </button>
              </form>
            ) : (
              <form onSubmit={handleVerifyOtp} className="space-y-4">
                <div>
                  <label className="text-xs text-slate-300 block mb-1">
                    {locale === 'bn' ? '৬-সংখ্যার ওটিপি কোড' : '6-Digit OTP Code'}
                  </label>
                  <input
                    type="text"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)}
                    maxLength={6}
                    placeholder="123456"
                    className="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-center text-lg font-mono tracking-widest text-teal-400 focus:outline-none focus:border-teal-500"
                    required
                  />
                </div>
                <button
                  type="submit"
                  className="w-full py-3 rounded-xl bg-teal-600 hover:bg-teal-500 text-white font-semibold text-sm transition-colors shadow-lg shadow-teal-900/40"
                >
                  {locale === 'bn' ? 'যাচাই করুন ও প্রবেশ করুন' : 'Verify & Enter'}
                </button>
              </form>
            )}
          </div>
        ) : activeView === 'home' ? (
          /* Logged In Home View */
          <div className="space-y-4">
            {/* Account Card */}
            <div className="p-4 rounded-2xl bg-gradient-to-br from-teal-950 to-slate-900 border border-teal-800/40 space-y-2">
              <span className="text-[11px] text-teal-300 font-medium">নিবন্ধিত ওয়ালেট নম্বর</span>
              <div className="flex items-center justify-between">
                <span className="font-mono text-lg font-bold text-white tracking-wider">{phone}</span>
                <span className="px-2 py-0.5 rounded text-[10px] bg-teal-500/20 text-teal-300 border border-teal-500/40 font-bold">
                  সক্রিয় (Active)
                </span>
              </div>
            </div>

            {/* Quick Action Button */}
            <button
              onClick={() => setActiveView('dispute_wizard')}
              className="w-full py-3.5 px-4 rounded-2xl bg-teal-600 hover:bg-teal-500 text-white font-bold text-sm shadow-xl shadow-teal-900/40 flex items-center justify-center gap-2 transition-transform active:scale-[0.98]"
            >
              <span>⚠️</span>
              <span>{locale === 'bn' ? 'লেনদেনের সমস্যা রিপোর্ট করুন' : 'Report a Problem'}</span>
            </button>

            {/* Recent Transactions List */}
            <div className="space-y-2 pt-2">
              <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                {locale === 'bn' ? 'সাম্প্রতিক লেনদেনসমূহ' : 'Recent Transactions'}
              </h3>
              <div className="space-y-2">
                {INITIAL_TRANSACTIONS.map((txn) => (
                  <div
                    key={txn.id}
                    onClick={() => {
                      setSelectedTxn(txn);
                      setActiveView('dispute_wizard');
                    }}
                    className="p-3.5 rounded-xl bg-slate-900/90 border border-slate-800 hover:border-teal-500/50 cursor-pointer transition-colors space-y-2"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-sm text-white">{txn.counterparty}</span>
                      <span className="font-mono font-bold text-sm text-teal-300">
                        {formatMoney(txn.amountPaisa, locale)}
                      </span>
                    </div>
                    <div className="flex items-center justify-between text-xs text-slate-400">
                      <span>{txn.type}</span>
                      {txn.status === 'FAILED_DEBITED' ? (
                        <span className="px-2 py-0.5 rounded text-[10px] bg-rose-500/20 text-rose-300 font-bold border border-rose-500/30">
                          টাকা কেটেছে কিন্তু ক্যাশ পাননি
                        </span>
                      ) : (
                        <span className="text-[10px] text-emerald-400 font-medium">সফল</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ) : activeView === 'dispute_wizard' ? (
          /* 4-Tap Dispute Wizard */
          <div className="space-y-5">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <button onClick={() => setActiveView('home')} className="text-xs text-slate-400 hover:text-white">
                ← {locale === 'bn' ? 'বাতিল' : 'Back'}
              </button>
              <h2 className="text-sm font-bold text-white">
                {locale === 'bn' ? 'বিরোধ দাখিল উইজার্ড' : 'Dispute Filing Wizard'}
              </h2>
              <span className="text-[10px] font-mono text-teal-400 font-bold">৪ ধাপের সহজ সমাধান</span>
            </div>

            {/* Tap 1: Transaction Confirmation */}
            <div className="p-3 rounded-xl bg-slate-900 border border-slate-800 space-y-1">
              <span className="text-[10px] text-slate-400 uppercase">ধাপ ১: নির্বাচিত লেনদেন</span>
              <div className="flex justify-between items-center">
                <span className="text-sm font-bold text-white">
                  {selectedTxn ? selectedTxn.counterparty : 'TXN-88210 (গুলশান এটিএম)'}
                </span>
                <span className="font-mono text-sm font-bold text-teal-300">
                  {formatMoney(selectedTxn ? selectedTxn.amountPaisa : 500000, locale)}
                </span>
              </div>
            </div>

            {/* Tap 2: Select Typology */}
            <div className="space-y-2">
              <span className="text-xs font-semibold text-slate-300 block">
                ধাপ ২: কী সমস্যা হয়েছে? (একটি বেছে নিন)
              </span>
              {[
                ['FAILED_DEBIT', 'টাকা কর্তন হয়েছে কিন্তু ক্যাশ পাইনি (Auto-Refund eligible)'],
                ['WRONG_NUMBER', 'ভুল মোবাইল নম্বরে টাকা চলে গেছে'],
                ['UNAUTHORIZED', 'আমি এই লেনদেনটি অনুমোদন করিনি (অ্যাকাউন্ট হ্যাক/চুরি)'],
                ['MERCHANT_ISSUE', 'মার্চেন্ট পণ্য সরবরাহ করেনি'],
              ].map(([key, text]) => (
                <button
                  key={key}
                  onClick={() => setSelectedCategory(key)}
                  className={`w-full p-3 text-left rounded-xl border text-xs font-medium transition-colors ${
                    selectedCategory === key
                      ? 'bg-teal-950/60 border-teal-500 text-teal-200 font-bold'
                      : 'bg-slate-900/60 border-slate-800 text-slate-300 hover:bg-slate-800'
                  }`}
                >
                  {text}
                </button>
              ))}
            </div>

            {/* Tap 3: Short Note & Photo Proof */}
            <div className="space-y-2">
              <span className="text-xs font-semibold text-slate-300 block">
                ধাপ ৩: অতিরিক্ত বিবরণ ও প্রমাণক
              </span>
              <textarea
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="যেমন: এটিএম স্লিপ অনুযায়ী ত্রুটি কোড ই-৭৭..."
                className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-xs text-slate-200 focus:outline-none focus:border-teal-500"
                rows={2}
              />
              <button
                type="button"
                onClick={() => {
                  setPhotoAttached(!photoAttached);
                  showToast(photoAttached ? 'ছবি সরানো হয়েছে' : 'ক্যামেরা ছবি সংযুক্ত হয়েছে');
                }}
                className={`w-full py-2 rounded-xl border text-xs font-semibold flex items-center justify-center gap-2 ${
                  photoAttached
                    ? 'bg-emerald-950/40 border-emerald-500 text-emerald-300'
                    : 'bg-slate-900 border-slate-700 text-slate-300'
                }`}
              >
                📷 {photoAttached ? '✓ এটিএম স্লিপের ছবি সংযুক্ত' : 'ক্যামেরা / গ্যালারি থেকে ছবি সংযুক্ত করুন'}
              </button>
            </div>

            {/* Auto-refund notification banner if applicable */}
            {selectedTxn?.status === 'FAILED_DEBITED' && (
              <div className="p-3 rounded-xl bg-teal-950/80 border border-teal-500/50 text-xs text-teal-200 space-y-1">
                <span className="font-bold flex items-center gap-1">⚡ দ্রুত রিফান্ড বিজ্ঞপ্তি</span>
                <p className="text-[11px] text-teal-300">
                  সিস্টেম টেলিমেট্রি নিশ্চিত করেছে আপনার লেনদেনটি ব্যর্থ হয়েছে। অটো-রিফান্ড ব্যবস্থার মাধ্যমে আপনার ওয়ালেটে সরাসরি রিফান্ড সমন্বয় করা হচ্ছে।
                </p>
              </div>
            )}

            {/* Tap 4: Confirm & Submit */}
            <button
              onClick={handleConfirmDispute}
              disabled={!selectedCategory}
              className={`w-full py-3.5 rounded-xl text-white font-bold text-sm shadow-xl transition-colors ${
                selectedCategory
                  ? 'bg-teal-600 hover:bg-teal-500 shadow-teal-900/40'
                  : 'bg-slate-800 text-slate-500 cursor-not-allowed'
              }`}
            >
              ধাপ ৪: নিশ্চিত করুন ও অভিযোগ জমা দিন
            </button>
          </div>
        ) : (
          /* Live Dispute Status Tracker */
          <div className="space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h2 className="text-sm font-bold text-white">আমার অভিযোগ ট্র্যাকার (Dispute Tracker)</h2>
              <button onClick={() => setActiveView('home')} className="text-xs text-teal-400">
                + নতুন অভিযোগ
              </button>
            </div>

            {disputes.length === 0 ? (
              <div className="text-center py-12 text-slate-500 text-xs">
                কোনো সক্রিয় বিরোধ বা অভিযোগ নেই।
              </div>
            ) : (
              <div className="space-y-4">
                {disputes.map((d) => (
                  <div key={d.id} className="p-4 rounded-2xl bg-slate-900/90 border border-slate-800 space-y-3">
                    <div className="flex items-center justify-between">
                      <span className="font-mono text-xs font-bold text-teal-400">{d.id}</span>
                      <span
                        className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                          d.status === 'RESOLVED_REFUNDED'
                            ? 'bg-emerald-500/20 text-emerald-300'
                            : 'bg-amber-500/20 text-amber-300'
                        }`}
                      >
                        {d.status === 'RESOLVED_REFUNDED' ? 'রিফান্ড সম্পন্ন (Refunded)' : 'তদন্তাধীন (Under Review)'}
                      </span>
                    </div>

                    <div className="flex justify-between items-center text-xs">
                      <span className="text-slate-300">{d.reason}</span>
                      <span className="font-mono font-bold text-white">{formatMoney(d.amountPaisa, locale)}</span>
                    </div>

                    {/* Timeline visualization */}
                    <div className="p-3 rounded-xl bg-slate-950/80 border border-slate-800/80 space-y-2 text-xs">
                      <div className="flex items-center gap-2 text-emerald-400 text-[11px]">
                        <span>✓</span> <span>অভিযোগ গৃহীত ও যাচাই হয়েছে</span>
                      </div>
                      <div className="flex items-center gap-2 text-teal-300 text-[11px]">
                        <span>✓</span> <span>এমএফএস কোর সিস্টেমে অনুসন্ধান চলছে</span>
                      </div>
                      <div className="text-[10px] text-slate-400 pt-1 border-t border-slate-800">
                        বাংলাদেশ ব্যাংক বিধি: শুক্রবার ও শনিবার ব্যতীত ৩ কার্যদিবসের মধ্যে সমাধান বাধ্যবাধকতা।
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>

      {/* Bottom Mobile Navigation */}
      {isLoggedIn && (
        <nav className="fixed bottom-0 max-w-md w-full bg-slate-900/95 border-t border-slate-800 px-6 py-2.5 flex justify-around text-xs backdrop-blur z-30">
          <button
            onClick={() => setActiveView('home')}
            className={`flex flex-col items-center gap-1 ${activeView === 'home' ? 'text-teal-400 font-bold' : 'text-slate-400'}`}
          >
            <span>🏠</span>
            <span className="text-[10px]">হোম</span>
          </button>
          <button
            onClick={() => setActiveView('dispute_wizard')}
            className={`flex flex-col items-center gap-1 ${activeView === 'dispute_wizard' ? 'text-teal-400 font-bold' : 'text-slate-400'}`}
          >
            <span>⚡</span>
            <span className="text-[10px]">অভিযোগ করুন</span>
          </button>
          <button
            onClick={() => setActiveView('tracker')}
            className={`flex flex-col items-center gap-1 ${activeView === 'tracker' ? 'text-teal-400 font-bold' : 'text-slate-400'}`}
          >
            <span>📋</span>
            <span className="text-[10px]">ট্র্যাকার ({disputes.length})</span>
          </button>
        </nav>
      )}
    </div>
  );
}
