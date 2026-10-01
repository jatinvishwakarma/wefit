$services = @{
    "Eureka" = "https://localhost:8761/eureka/apps"
    "Config Server" = "https://localhost:8888/wefit/default"
    "API Gateway" = "https://localhost:8443/actuator/health"
    "UserService" = "https://localhost:8081/actuator/health"
    "ActivityService" = "https://localhost:8082/actuator/health"
    "AiService" = "https://localhost:8083/actuator/health"
    "Wefit Web" = "http://localhost:5173"
}

$allGood = $true

foreach ($service in $services.GetEnumerator()) {
    $name = $service.Key
    $url = $service.Value
    
    try {
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
        [System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}
        $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
        Write-Host "$name is UP." -ForegroundColor Green
    } catch {
        # If it's a 401 or 404, it means the server is running and responding!
        if ($_.Exception.Response) {
            Write-Host "$name is UP (returned $($_.Exception.Response.StatusCode))." -ForegroundColor Green
        } else {
            Write-Host "$name is DOWN or not responding." -ForegroundColor Red
            $allGood = $false
        }
    }
}

if ($allGood) {
    Write-Host "All services are running fine!" -ForegroundColor Green
} else {
    Write-Host "Some services are not responding yet." -ForegroundColor Yellow
}
