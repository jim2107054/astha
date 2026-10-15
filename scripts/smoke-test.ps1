# Smoke test for Astha platform services
param (
    [string]$BaseUrl = "http://localhost:8083"
)

Write-Host "Running Astha smoke tests against $BaseUrl..." -ForegroundColor Cyan

# 1. Check health actuator
try {
    $health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method Get -TimeoutSec 5
    Write-Host "✓ Health check passed: status = $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "✗ Health check failed: $_" -ForegroundColor Red
}

# 2. Check rules listing
try {
    $rules = Invoke-RestMethod -Uri "$BaseUrl/v1/risk/rules" -Method Get -TimeoutSec 5
    Write-Host "✓ Rules listing passed: $($rules.Count) rules active" -ForegroundColor Green
} catch {
    Write-Host "✗ Rules check failed: $_" -ForegroundColor Red
}

# 3. Test risk score endpoint
try {
    $body = @{
        eventId = [System.Guid]::NewGuid().ToString()
        accountId = "acc-01712345678"
        amountPaisa = 1500000
        eventType = "P2P"
    } | ConvertTo-Json

    $headers = @{
        "Content-Type" = "application/json"
        "Idempotency-Key" = "smoke-" + [System.Guid]::NewGuid().ToString()
    }

    $score = Invoke-RestMethod -Uri "$BaseUrl/v1/risk/score" -Method Post -Headers $headers -Body $body -TimeoutSec 5
    Write-Host "✓ Risk scoring passed: decision = $($score.decision), score = $($score.score), latency = $($score.latencyMs)ms" -ForegroundColor Green
} catch {
    Write-Host "✗ Risk score check failed: $_" -ForegroundColor Red
}
