param(
    [string]$BaseUrl = "http://127.0.0.1:8082"
)

$ErrorActionPreference = "Stop"

$checks = @(
    @{ Name = "feed"; Path = "/api/v1/posts/feed?page=1&size=20" },
    @{ Name = "products"; Path = "/api/v1/products?page=1&size=20" },
    @{ Name = "topics"; Path = "/api/v1/topics?page=1&size=20" },
    @{ Name = "sticker-packs"; Path = "/api/v1/sticker-packs" }
)

foreach ($check in $checks) {
    $uri = "$BaseUrl$($check.Path)"
    $response = Invoke-RestMethod -Method Get -Uri $uri
    $items = $response.data.items
    $count = if ($null -eq $items) { -1 } else { @($items).Count }
    $total = $response.data.total

    if ($response.code -ne "OK" -or $count -ne 0 -or $total -ne 0) {
        throw "qa-empty check failed for $($check.Name): code=$($response.code), itemCount=$count, total=$total"
    }

    Write-Host "OK $($check.Name): 200 empty list"
}

Write-Host "qa-empty HTTP verification passed for $BaseUrl"
