import { test, expect } from '@playwright/test';

test.describe('Customer Portal Experience (Phase F3)', () => {
  test('Customer logs in via OTP, files dispute in 4 taps, and tracks SLA status', async ({ page }) => {
    await page.goto('/');

    // 1. Phone + OTP login
    await expect(page.locator('h2')).toContainText('এমএফএস বিরোধ নিষ্পত্তি');
    await page.fill('input[type="tel"]', '+8801711223344');
    await page.click('button:has-text("ওটিপি কোড পাঠান")');

    await expect(page.locator('text=৬-সংখ্যার ওটিপি কোড')).toBeVisible();
    await page.click('button:has-text("যাচাই করুন ও প্রবেশ করুন")');

    // 2. Home view: Recent transactions
    await expect(page.locator('text=নিবন্ধিত ওয়ালেট নম্বর')).toBeVisible();
    await expect(page.locator('text=গুলশান ব্রাঞ্চ এটিএম')).toBeVisible();

    // 3. 4-Tap Dispute Wizard
    // Tap 1: Click transaction
    await page.click('text=গুলশান ব্রাঞ্চ এটিএম');
    await expect(page.locator('h2')).toContainText('বিরোধ দাখিল উইজার্ড');

    // Tap 2: Select typology
    await page.click('text=টাকা কর্তন হয়েছে কিন্তু ক্যাশ পাইনি');

    // Tap 3: Attach note and photo
    await page.fill('textarea', 'ATM journal code E-77 recorded.');
    await page.click('button:has-text("ক্যামেরা")');
    await expect(page.locator('text=ছবি সংযুক্ত').first()).toBeVisible();

    // Tap 4: Confirm and submit
    await page.click('button:has-text("ধাপ ৪: নিশ্চিত করুন")');

    // 4. Live Tracker & SLA notice
    await expect(page.locator('h2')).toContainText('আমার অভিযোগ ট্র্যাকার');
    await expect(page.locator('text=রিফান্ড সম্পন্ন (Refunded)').first()).toBeVisible();
    await expect(page.locator('text=৩ কার্যদিবস').first()).toBeVisible();
  });
});
