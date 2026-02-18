# ============================================
#  NUTRIFLOW SCHEDULER - ONE-TIME TEST SCRIPT
# ============================================

$baseUrl = "http://localhost:8080/api/admin/scheduler-test"

# Helper function
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET"
    )

    Write-Host ""
    Write-Host $Name -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor DarkGray

    try {
        $response = Invoke-WebRequest -Uri $Url -Method $Method -ErrorAction Stop
        Write-Host $response.Content -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "[ERROR] $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response: $responseBody" -ForegroundColor Yellow
        }
        return $false
    }
}

# Header
Clear-Host
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "          NUTRIFLOW SCHEDULER TEST STARTED                  " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green

$totalTests = 9
$passedTests = 0

# TESTS
if (Test-Endpoint "[1] CHECKING STATUS" "$baseUrl/status" "GET") { $passedTests++ }
if (Test-Endpoint "[2] SUBSCRIPTION STATISTICS" "$baseUrl/subscription-count" "GET") { $passedTests++ }
if (Test-Endpoint "[3] DATABASE CLEANUP TEST" "$baseUrl/database-cleanup" "POST") { $passedTests++ }
if (Test-Endpoint "[4] REDIS STATS TEST" "$baseUrl/redis-stats" "POST") { $passedTests++ }
if (Test-Endpoint "[5] CREATING SUBSCRIPTION EXPIRING IN 7 DAYS" "$baseUrl/create-expiring-subscription" "POST") { $passedTests++ }
if (Test-Endpoint "[6] SENDING WARNING EMAIL" "$baseUrl/test-subscription-warning" "POST") { $passedTests++ }
if (Test-Endpoint "[7] CREATING EXPIRED SUBSCRIPTION" "$baseUrl/create-expired-subscription" "POST") { $passedTests++ }
if (Test-Endpoint "[8] SUBSCRIPTION DEACTIVATION TEST" "$baseUrl/subscription-deactivate" "POST") { $passedTests++ }
if (Test-Endpoint "[9] SENDING ADMIN REPORT EMAIL" "$baseUrl/test-admin-report" "POST") { $passedTests++ }

# RESULTS
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "                     TEST RESULTS                           " -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

$failedTests = $totalTests - $passedTests
$successRate = [math]::Round(($passedTests / $totalTests) * 100, 1)

Write-Host "  [OK] Passed tests : $passedTests/$totalTests" -ForegroundColor Green
Write-Host "  [X]  Failed tests : $failedTests/$totalTests" -ForegroundColor Red
Write-Host "  [%]  Success rate : $successRate%" -ForegroundColor Yellow
Write-Host ""
Write-Host "============================================================" -ForegroundColor Yellow
Write-Host "                   CHECK YOUR EMAIL                         " -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "  Please check your Gmail account (Inbox or Spam):" -ForegroundColor White
Write-Host "    > 7-day expiration warning email" -ForegroundColor Cyan
Write-Host "    > Subscription expired email" -ForegroundColor Cyan
Write-Host "    > Admin weekly report email" -ForegroundColor Cyan
Write-Host ""

if ($passedTests -eq $totalTests) {
    Write-Host "  [SUCCESS] CONGRATULATIONS! All tests passed successfully!" -ForegroundColor Green
} else {
    Write-Host "  [WARNING] Some tests failed. Please check the errors above." -ForegroundColor Yellow
}
Write-Host ""