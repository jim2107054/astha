# Astha 3-Minute Live Demonstration Script

**Platform:** Astha Real-Time MFS Fraud & Dispute Platform  
**Target Audience:** Risk Executives, Operations Leads, Bangladesh Bank Auditors  
**Duration:** ~3 Minutes  

---

## Step 1: Initialize Synthetic Population (00:00 - 00:30)

Seed deterministic synthetic population representing Bangladeshi financial topology:
```bash
# Seed 30 Customers, 8 Agents, 6 Merchants with authentic Bangladeshi names and MSISDNs
curl -X POST http://localhost:8090/v1/simulator/reset \
  -H "Content-Type: application/json" \
  -d '{"seed": 42}'
```
*Expected Output:* HTTP 200 with 44 entities initialized.

---

## Step 2: Ingest High-Risk Mule Pass-Through Transaction (00:30 - 01:15)

Simulate a rapid cash-in immediately followed by a high-velocity pass-through transfer:
```bash
# Deposit Tk 50,000 cash at agent
curl -X POST http://localhost:8090/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "EVT-DEMO-01",
    "eventType": "CASH_IN",
    "senderMsisdn": "+8801820000001",
    "receiverMsisdn": "+8801710000001",
    "amountPaisa": 5000000,
    "timestamp": "2026-10-16T10:00:00Z"
  }'

# Send Tk 49,500 within 90 seconds (Triggers R01_PASS_THROUGH_MULE)
curl -X POST http://localhost:8090/v1/events/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "EVT-DEMO-02",
    "eventType": "SEND_MONEY",
    "senderMsisdn": "+8801710000001",
    "receiverMsisdn": "+8801710000005",
    "amountPaisa": 4950000,
    "timestamp": "2026-10-16T10:01:30Z"
  }'
```
*Expected Result:*
- Risk Engine flags transaction in < 25ms (`R01_PASS_THROUGH_MULE`).
- Alert grouped into Case Dossier `CASE-CUST-1` with priority score 85+.

---

## Step 3: Analyst Console Triage & Audited PII Reveal (01:15 - 02:00)

1. Open `http://localhost:3000` (Analyst Console).
2. View high-priority triage queue: `CUST-1` is at top with `PASS_THROUGH_MULE` label.
3. Open Case Dossier: observe masked phone number `+88017****0001`.
4. Click **"Reveal PII"** (Audited Unmasking):
   - Confirmation prompt logs supervisor reason into hash-chained `audit_log`.
   - Full MSISDN reveals in real-time.
5. Click **"Propose Mule Freeze"**:
   - Maker action submitted; transitions case to `PENDING_APPROVAL`.

---

## Step 4: Four-Eyes Maker-Checker Enforcement (02:00 - 02:30)

Attempt self-approval (prohibited by maker-checker rules):
```bash
# Attempt to approve with same maker ID (Fails with 403 Forbidden)
curl -X POST http://localhost:8090/v1/cases/CASE-CUST-1/approve \
  -H "Content-Type: application/json" \
  -H "X-Actor-Id: analyst_1" \
  -d '{"action": "FREEZE"}'
```
*Expected Output:* HTTP 403 Forbidden (`FourEyesBypassAttemptException`).

Approve with senior supervisor (`supervisor_1`):
```bash
curl -X POST http://localhost:8090/v1/cases/CASE-CUST-1/approve \
  -H "Content-Type: application/json" \
  -H "X-Actor-Id: supervisor_1" \
  -d '{"action": "FREEZE"}'
```
*Expected Result:* Account frozen idempotently across provider mock; SMS notification dispatched.

---

## Step 5: Customer Dispute Wizard & Automated Refund (02:30 - 03:00)

1. Open `http://localhost:3001` (Customer Complaint Portal PWA).
2. Select Bengali Language (`বাংলা`).
3. 4-tap dispute wizard: Select **"টাকা কেটেছে কিন্তু পায়নি"** (Failed debit, debited but not received).
4. Auto-refund worker detects failed provider status:
   - Instantly issues idempotent reversal via `provider-mock`.
   - Dispute state marks `AUTO_REFUNDED`.
   - Customer receives bilingual SMS: `"আপনার BDT 2,500 সফলভাবে রিফান্ড করা হয়েছে"`.
