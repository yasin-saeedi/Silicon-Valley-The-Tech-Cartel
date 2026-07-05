param(
    [Parameter(Mandatory = $true)]
    [string]$Message
)

Set-Location $PSScriptRoot

git pull --rebase
if ($LASTEXITCODE -ne 0) {
    Write-Host "Pull failed. Resolve conflicts before continuing."
    exit $LASTEXITCODE
}

git add .

git diff --cached --quiet
if ($LASTEXITCODE -ne 0) {
    git commit -m $Message
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
} else {
    Write-Host "No changes to commit."
}

git push
