$userPath = [Environment]::GetEnvironmentVariable("PATH", "User")
$machinePath = [Environment]::GetEnvironmentVariable("PATH", "Machine")

function Clean-PathString {
    param([string]$pathStr)
    $elements = $pathStr -split ';'
    $cleanElements = @()
    foreach ($element in $elements) {
        $element = $element.Trim()
        if ($element -ne "" -and $element -ne "%PATH%" -and $element -ne "%USERPROFILE%\AppData\Local\Microsoft\WindowsApps") {
            if (-not ($cleanElements -contains $element)) {
                $cleanElements += $element
            }
        }
    }
    return ($cleanElements -join ';')
}

$cleanUserPath = Clean-PathString -pathStr $userPath
$cleanMachinePath = Clean-PathString -pathStr $machinePath

Write-Output "Cleaned User PATH: $cleanUserPath"
[Environment]::SetEnvironmentVariable("PATH", $cleanUserPath, "User")

try {
    [Environment]::SetEnvironmentVariable("PATH", $cleanMachinePath, "Machine")
    Write-Output "Cleaned Machine PATH successfully."
} catch {
    Write-Output "Failed to clean Machine PATH (requires admin). Adding essential Windows paths to User PATH just in case."
    # Ensure Windows paths are in User PATH
    $essentialPaths = @("C:\Windows\system32", "C:\Windows", "C:\Windows\System32\Wbem", "C:\Windows\System32\WindowsPowerShell\v1.0\")
    $userElements = $cleanUserPath -split ';'
    $newElements = @()
    foreach ($ep in $essentialPaths) {
        if (-not ($userElements -contains $ep) -and -not ($cleanMachinePath -match [regex]::Escape($ep))) {
            $newElements += $ep
        }
    }
    if ($newElements.Length -gt 0) {
        $cleanUserPath = ($newElements -join ';') + ';' + $cleanUserPath
        [Environment]::SetEnvironmentVariable("PATH", $cleanUserPath, "User")
        Write-Output "Appended essential Windows paths to User PATH."
    }
}
Write-Output "PATH environment variables cleaned up."
