import { describe, it, expect } from 'vitest';
import { mockCases, mockApprovals } from '@astha/mocks';

describe('Console State & Mock Integrity', () => {
  it('loads valid initial mock cases with priority scores', () => {
    expect(mockCases.length).toBeGreaterThan(0);
    const firstCase = mockCases[0];
    expect(firstCase.id).toBe('CAS-2026-001');
    expect(firstCase.priority).toBeGreaterThanOrEqual(90);
    expect(firstCase.reasonCodes.length).toBeGreaterThan(0);
  });

  it('contains pending four-eyes approvals requiring secondary review', () => {
    expect(mockApprovals.length).toBeGreaterThan(0);
    const approval = mockApprovals[0];
    expect(approval.status).toBe('PENDING');
    expect(approval.requesterId).toBeTruthy();
  });
});
