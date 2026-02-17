# ============================================
#  NUTRIFLOW SCHEDULER - BIRDAFALIK TEST
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
        Write-Host "[XATA] $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Cavab: $responseBody" -ForegroundColor Yellow
        }
        return $false
    }
}

# Basliq
Clear-Host
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "       NUTRIFLOW SCHEDULER TEST BASLADI                     " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green

$totalTests = 9
$passedTests = 0

# TESTLAR
if (Test-Endpoint "[1] STATUS YOXLANILIR" "$baseUrl/status" "GET") { $passedTests++ }
if (Test-Endpoint "[2] SUBSCRIPTION STATISTIKASI" "$baseUrl/subscription-count" "GET") { $passedTests++ }
if (Test-Endpoint "[3] DATABASE CLEANUP TEST" "$baseUrl/database-cleanup" "POST") { $passedTests++ }
if (Test-Endpoint "[4] REDIS STATS TEST" "$baseUrl/redis-stats" "POST") { $passedTests++ }
if (Test-Endpoint "[5] 7 GUN SONRA BITACAK SUBSCRIPTION YARADILIR" "$baseUrl/create-expiring-subscription" "POST") { $passedTests++ }
if (Test-Endpoint "[6] WARNING EMAIL GONDERILIR" "$baseUrl/test-subscription-warning" "POST") { $passedTests++ }
if (Test-Endpoint "[7] BITMIS SUBSCRIPTION YARADILIR" "$baseUrl/create-expired-subscription" "POST") { $passedTests++ }
if (Test-Endpoint "[8] SUBSCRIPTION DEACTIVATION TEST" "$baseUrl/subscription-deactivate" "POST") { $passedTests++ }
if (Test-Endpoint "[9] ADMIN REPORT EMAIL GONDERILIR" "$baseUrl/test-admin-report" "POST") { $passedTests++ }

# NATICA
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "                   TEST NATICALARI                          " -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

$failedTests = $totalTests - $passedTests
$successRate = [math]::Round(($passedTests / $totalTests) * 100, 1)

Write-Host "  [OK] Ugurlu testlar: $passedTests/$totalTests" -ForegroundColor Green
Write-Host "  [X]  Ugursuz testlar: $failedTests/$totalTests" -ForegroundColor Red
Write-Host "  [%]  Ugur nisbati: $successRate%" -ForegroundColor Yellow
Write-Host ""
Write-Host "============================================================" -ForegroundColor Yellow
Write-Host "                  EMAIL YOXLAYIN                            " -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "  Gmail hesabinizi yoxlayin (Inbox va ya Spam):" -ForegroundColor White
Write-Host "    > 7 gun xabardarliq emaili" -ForegroundColor Cyan
Write-Host "    > Abunalik bitdi emaili" -ForegroundColor Cyan
Write-Host "    > Admin haftalik report emaili" -ForegroundColor Cyan
Write-Host ""

if ($passedTests -eq $totalTests) {
    Write-Host "  [SUCCESS] TABRIKLAR! Butun testlar ugurla kecdi!" -ForegroundColor Green
} else {
    Write-Host "  [WARNING] Bazi testlar ugursuz oldu. Yuxaridaki xatalari yoxlayin." -ForegroundColor Yellow
}
Write-Host ""