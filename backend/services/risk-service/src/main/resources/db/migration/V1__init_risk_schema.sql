-- Astha Risk Service Initial Schema

CREATE TABLE IF NOT EXISTS rules (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    typology VARCHAR(64) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    weight INT NOT NULL,
    active_version_id VARCHAR(64),
    description_en TEXT NOT NULL,
    description_bn TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS rule_versions (
    id VARCHAR(64) PRIMARY KEY,
    rule_id VARCHAR(64) NOT NULL REFERENCES rules(id),
    version_number INT NOT NULL,
    status VARCHAR(32) NOT NULL, -- DRAFT, PENDING_APPROVAL, ACTIVE, REJECTED, ARCHIVED
    parameters_json TEXT NOT NULL,
    author_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    approved_by VARCHAR(64),
    approved_at TIMESTAMP WITH TIME ZONE,
    approval_notes TEXT
);

CREATE TABLE IF NOT EXISTS rule_approvals (
    id UUID PRIMARY KEY,
    version_id VARCHAR(64) NOT NULL REFERENCES rule_versions(id),
    requested_by VARCHAR(64) NOT NULL,
    decided_by VARCHAR(64) NOT NULL,
    decision VARCHAR(32) NOT NULL,
    notes TEXT,
    decided_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS lists (
    id UUID PRIMARY KEY,
    list_type VARCHAR(16) NOT NULL, -- ALLOW, BLOCK
    entity_type VARCHAR(32) NOT NULL, -- ACCOUNT, DEVICE, PHONE, NID, AGENT, MERCHANT
    entity_value VARCHAR(128) NOT NULL,
    reason TEXT NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS decisions (
    decision_id UUID PRIMARY KEY,
    event_id VARCHAR(128) NOT NULL UNIQUE,
    account_id VARCHAR(64) NOT NULL,
    score INT NOT NULL,
    decision VARCHAR(32) NOT NULL, -- ALLOW, REVIEW, BLOCK_RECOMMENDED, ALLOW_WITH_FLAG
    features_snapshot_json TEXT NOT NULL,
    rule_versions_json TEXT NOT NULL,
    latency_ms INT NOT NULL,
    degraded BOOLEAN NOT NULL DEFAULT FALSE,
    decided_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS decision_reasons (
    id BIGSERIAL PRIMARY KEY,
    decision_id UUID NOT NULL REFERENCES decisions(decision_id),
    rule_id VARCHAR(64) NOT NULL,
    reason_code VARCHAR(64) NOT NULL,
    weight INT NOT NULL,
    text_en TEXT NOT NULL,
    text_bn TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS shadow_results (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(128) NOT NULL,
    candidate_version_id VARCHAR(64) NOT NULL,
    rule_id VARCHAR(64) NOT NULL,
    candidate_score INT NOT NULL,
    candidate_hit BOOLEAN NOT NULL,
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS processed_events (
    event_id VARCHAR(128) PRIMARY KEY,
    consumer_group VARCHAR(64) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(128) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    topic VARCHAR(128) NOT NULL,
    payload_json TEXT NOT NULL,
    trace_id VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    retry_count INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS idempotency_keys (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    request_hash VARCHAR(64) NOT NULL,
    response_status INT NOT NULL,
    response_body TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Seed initial default rules R01 to R10
INSERT INTO rules (id, name, typology, severity, weight, active_version_id, description_en, description_bn, created_at, updated_at) VALUES
('R01_PASS_THROUGH_MULE', 'Pass-through Mule', 'Mule account', 'HIGH', 35, 'R01_v1', 'Many unrelated senders in a short window followed by rapid cash-out or transfer.', 'স্বল্প সময়ে একাধিক প্রেরকের কাছ থেকে টাকা পেয়ে দ্রুত ক্যাশ আউট বা স্থানান্তর।', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('R02_YOUNG_ACCOUNT_BURST', 'Young Account Burst', 'Mule or fake account', 'HIGH', 30, 'R02_v1', 'Account under 7 days old receives unusually high inflow.', 'সাত দিনের কম বয়সী নতুন একাউন্টে অস্বাভাবিক বেশি অর্থ জমা।', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('R03_QR_CASH_SUBSTITUTE', 'QR Cash Substitute', 'Bangla QR cash-out abuse', 'CRITICAL', 45, 'R03_v1', 'Merchant QR receives large round-amount payments from repeat payers near daily limit with rapid bank settlement.', 'মার্চেন্ট কিউআরের মাধ্যমে ক্যাশ আউটের উদ্দেশ্যে বড় অঙ্কের রাউন্ড পেমেন্ট গ্রহণ।', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('R04_ACCOUNT_TAKEOVER', 'Account Takeover', 'OTP phishing or SIM swap', 'CRITICAL', 50, 'R04_v1', 'New device or SIM change followed within 24h by high-value drain to new beneficiary.', 'ডিভাইস বা সিম পরিবর্তনের ২৪ ঘণ্টার মধ্যে নতুন অ্যাকাউন্টে বড় অঙ্কের টাকা ড্রেন।', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('R05_STRUCTURING', 'Structuring', 'Limit evasion', 'MEDIUM', 25, 'R05_v1', 'Multiple transactions just under regulatory thresholds in a short window.', 'লেনদেনের নির্দিষ্ট সীমা এড়াতে কাছাকাছি অঙ্কে একাধিক বার লেনদেন।', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('R06_AGENT_ANOMALY', 'Agent Anomaly', 'Agent misuse', 'HIGH', 30, 'R06_v1', 'Agent cash-out velocity far above peer baseline or many accounts on one device.', 'এজেন্টের ক্যাশ-আউট গতি অস্বাভাবিক বেশি অথবা এক ডিভাইসে অনেক অ্যাকাউন্ট।', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('R07_DORMANT_WAKE_UP', 'Dormant Wake-up', 'Takeover or mule', 'MEDIUM', 25, 'R07_v1', 'Long-idle account suddenly active with high-value transactions.', 'দীর্ঘদিন নিষ্ক্রিয় অ্যাকাউন্ট হঠাৎ বড় অঙ্কের লেনদেনে সক্রিয় হওয়া।', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('R08_SHARED_DEVICE_LISTED', 'Shared Device / Listed Entity', 'Network link', 'CRITICAL', 60, 'R08_v1', 'Device or phone linked to a blocklisted or known ring entity.', 'কালোতালিকাভুক্ত বা প্রতারক চক্রের সাথে ডিভাইস বা নম্বরের সংযোগ।', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('R09_RING_SCORE', 'Graph Ring Score', 'Network', 'HIGH', 40, 'R09_v1', 'Graph service detects high mule ring topology score.', 'গ্রাফ সেবায় শনাক্তকৃত প্রতারক রিং স্কোর সীমা অতিক্রম করেছে।', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('R10_FAILED_DEBIT_WATCH', 'Failed-debit Watch', 'Operational', 'LOW', 15, 'R10_v1', 'Debited but failed transaction creating potential dispute candidate.', 'টাকা কেটে নেওয়া হলেও লেনদেন ব্যর্থ হওয়া পরিচালনাগত ত্রুটি।', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed versions for R01 to R10
INSERT INTO rule_versions (id, rule_id, version_number, status, parameters_json, author_id, created_at, approved_by, approved_at, approval_notes) VALUES
('R01_v1', 'R01_PASS_THROUGH_MULE', 1, 'ACTIVE', '{"minDistinctSenders1h": 3, "minInflow1hPaisa": 1000000, "minOutflowRatio30m": 0.80, "maxAccountAgeDays": 30}', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM_ADMIN', CURRENT_TIMESTAMP, 'Initial baseline deployment'),
('R02_v1', 'R02_YOUNG_ACCOUNT_BURST', 1, 'ACTIVE', '{"maxAccountAgeDays": 7, "minInflow24hPaisa": 2500000}', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM_ADMIN', CURRENT_TIMESTAMP, 'Initial baseline deployment'),
('R03_v1', 'R03_QR_CASH_SUBSTITUTE', 1, 'ACTIVE', '{"minRoundAmountRatio": 0.70, "minPayerRepeatRate": 0.30, "minNearLimitRatio": 0.50, "minDailyInflowPaisa": 3000000, "minSettlementRatio": 0.80}', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM_ADMIN', CURRENT_TIMESTAMP, 'Initial baseline deployment'),
('R04_v1', 'R04_ACCOUNT_TAKEOVER', 1, 'ACTIVE', '{"hoursSinceSimOrDeviceChange": 24, "minBalanceDrainRatio": 0.75, "minDrainAmountPaisa": 1500000, "requireNewBeneficiary": true}', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM_ADMIN', CURRENT_TIMESTAMP, 'Initial baseline deployment'),
('R05_v1', 'R05_STRUCTURING', 1, 'ACTIVE', '{"nearLimitCount24h": 3, "nearLimitThresholdPaisa": 2400000, "singleLimitPaisa": 2500000}', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM_ADMIN', CURRENT_TIMESTAMP, 'Initial baseline deployment'),
('R06_v1', 'R06_AGENT_ANOMALY', 1, 'ACTIVE', '{"zscoreCashoutThreshold": 3.0, "maxCustomersPerDevice": 5, "minRoundRatio": 0.80}', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM_ADMIN', CURRENT_TIMESTAMP, 'Initial baseline deployment'),
('R07_v1', 'R07_DORMANT_WAKE_UP', 1, 'ACTIVE', '{"minIdleDays": 90, "minAmountPaisa": 2000000, "requireNewDevice": true}', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM_ADMIN', CURRENT_TIMESTAMP, 'Initial baseline deployment'),
('R08_v1', 'R08_SHARED_DEVICE_LISTED', 1, 'ACTIVE', '{"checkBlocklist": true, "checkRingMembership": true}', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM_ADMIN', CURRENT_TIMESTAMP, 'Initial baseline deployment'),
('R09_v1', 'R09_RING_SCORE', 1, 'ACTIVE', '{"minRingScore": 75, "minRingSize": 3}', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM_ADMIN', CURRENT_TIMESTAMP, 'Initial baseline deployment'),
('R10_v1', 'R10_FAILED_DEBIT_WATCH', 1, 'ACTIVE', '{"minAgeMinutes": 10, "autoDisputeCandidate": true}', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM_ADMIN', CURRENT_TIMESTAMP, 'Initial baseline deployment');
