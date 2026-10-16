import { test, expect } from '@playwright/test';

test.describe('Analyst Console Workflows', () => {
  test('Analyst works a mule-ring case end-to-end and requests freeze', async ({ page }) => {
    await page.goto('/');

    // 1. Verify queue loaded with cases
    await expect(page.locator('h1')).toContainText('Fraud Alerts & Triage Queue');
    await expect(page.locator('text=CAS-2026-001')).toBeVisible();

    // 2. Open Case CAS-2026-001
    await page.click('text=CAS-2026-001');
    await expect(page.locator('h1')).toContainText('CAS-2026-001');

    // 3. Verify PII is masked by default
    await expect(page.locator('text=+880****3344')).toBeVisible();

    // 4. Reveal PII with mandatory justification
    await page.click('button:has-text("Reveal")');
    await expect(page.locator('text=Audited PII Reveal Request')).toBeVisible();
    await page.fill('input[placeholder*="Investigating"]', 'CID inquiry #8821');
    await page.click('button:has-text("Confirm & Audit")');
    await expect(page.locator('text=Temporary (30s)')).toBeVisible();

    // 5. Inspect reason codes and graph canvas
    await expect(page.locator('text=R01_FAST_PASS_THROUGH')).toBeVisible();
    await expect(page.locator('text=Associated Mule Ring')).toBeVisible();

    // 6. Recommend Freeze with typed reason
    await page.click('button:has-text("Recommend Freeze")');
    await expect(page.locator('text=Confirm Irreversible Account Freeze Recommendation')).toBeVisible();
    await page.fill('textarea[placeholder*="regulatory"]', 'Rapid pass-through cashout pattern verified with CID reference.');
    await page.click('button:has-text("Submit for Four-Eyes Review")');

    // 7. Check pending approvals tab
    await page.click('button:has-text("Four-Eyes Approvals")');
    await expect(page.locator('text=FREEZE_ACCOUNT').first()).toBeVisible();

    // 8. Self-approval violation check: analyst cannot approve their own action
    await expect(page.locator('text=Self-approval prohibited').first()).toBeVisible();

    // 9. Switch role to APPROVER (manager-tamim)
    await page.selectOption('header select', 'APPROVER');
    await expect(page.locator('button:has-text("Approve Action")').first()).toBeEnabled();

    // 10. Approve action as distinct approver
    await page.locator('button:has-text("Approve Action")').first().click();
    await expect(page.locator('text=successfully approved').first()).toBeVisible();
  });

  test('Language toggle switches between English and Bengali', async ({ page }) => {
    await page.goto('/');
    await page.click('button:has-text("বাংলা")');
    await expect(page.locator('h1')).toContainText('জালিয়াতি সতর্কতা');

    // Numbers display in Bengali format
    await page.click('button:has-text("EN")');
    await expect(page.locator('h1')).toContainText('Fraud Alerts & Triage Queue');
  });

  test('Dispute Desk: SLA tracking, automated refund simulation, and refund resolution', async ({ page }) => {
    await page.goto('/');

    // 1. Navigate to Dispute Desk tab
    await page.click('button:has-text("Dispute Desk")');
    await expect(page.locator('h1')).toContainText('Bangladesh MFS Dispute Operations Desk');
    await expect(page.locator('text=DSP-2026-101').first()).toBeVisible();

    // 2. Verify SLA working day clock and warning badges
    await expect(page.locator('text=WARNING_50')).toBeVisible();
    await expect(page.locator('text=Bangladesh Bank SLA Clock')).toBeVisible();

    // 3. Simulate an automated debited-but-failed refund event
    await page.click('button:has-text("Simulate Auto-Refund Event")');
    await expect(page.locator('text=AUTO-REFUND').first()).toBeVisible();

    // 4. Resolve dispute DSP-2026-101 with Full Refund
    await page.locator('tr:has-text("DSP-2026-101") button:has-text("Inspect")').click();
    await page.click('button:has-text("Authorize Full Refund")');
    await expect(page.locator('text=Confirm Dispute Resolution: Full Refund')).toBeVisible();
    await page.fill('textarea[placeholder*="regulatory"]', 'Confirmed ATM dispenser error from hardware log.');
    await page.click('button:has-text("Execute Refund Resolution")');
    await expect(page.locator('text=resolved with Full Refund').first()).toBeVisible();
  });
});
