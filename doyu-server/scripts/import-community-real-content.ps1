param(
    [string]$BaseUrl = "http://127.0.0.1:8081",
    [string]$ManifestPath = ".\scripts\community-real-content-manifest.json",
    [string]$OutputPath = ".\community-real-content-import-report.json",
    [string]$Phone = "13900006007",
    [string]$Nickname = ""
)

$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($Nickname)) {
    $Nickname = -join ([char[]](0x8C46, 0x5C7F, 0x771F, 0x5B9E, 0x4F5C, 0x54C1))
}

function Read-DotEnv {
    param([string]$Path)
    if (-not (Test-Path $Path)) {
        return
    }
    Get-Content -Path $Path | ForEach-Object {
        $line = $_.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith("#") -or -not $line.Contains("=")) {
            return
        }
        $parts = $line -split "=", 2
        $name = $parts[0].Trim()
        $value = $parts[1]
        if ($name.Length -gt 0 -and -not [Environment]::GetEnvironmentVariable($name, "Process")) {
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
}

function Invoke-Json {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body,
        [string]$Token
    )
    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    $params = @{
        Method = $Method
        Uri = "$BaseUrl$Path"
        TimeoutSec = 30
        Headers = $headers
    }
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 8 -Compress
        $params["ContentType"] = "application/json; charset=utf-8"
        $params["Body"] = [System.Text.Encoding]::UTF8.GetBytes($json)
    }
    Invoke-RestMethod @params
}

function Get-ImageSize {
    param([string]$Path)
    Add-Type -AssemblyName System.Drawing
    $image = [System.Drawing.Image]::FromFile((Resolve-Path $Path).Path)
    try {
        [pscustomobject]@{
            Width = $image.Width
            Height = $image.Height
        }
    } finally {
        $image.Dispose()
    }
}

function Get-MimeType {
    param([string]$Path)
    $extension = [System.IO.Path]::GetExtension($Path).ToLowerInvariant()
    switch ($extension) {
        ".png" { "image/png" }
        ".webp" { "image/webp" }
        ".gif" { "image/gif" }
        default { "image/jpeg" }
    }
}

function Download-Image {
    param(
        [string]$Url,
        [string]$OutFile
    )
    if ((Test-Path $OutFile) -and (Get-Item $OutFile).Length -gt 0) {
        try {
            $existingSize = Get-ImageSize -Path $OutFile
            if ($existingSize.Width -gt 0 -and $existingSize.Height -gt 0) {
                return
            }
        } catch {
            Remove-Item -LiteralPath $OutFile -Force -ErrorAction SilentlyContinue
        }
    }

    $userAgent = "SpellBeanDev/1.0 (community-real-content; reusable-media-import)"
    $curl = Get-Command curl.exe -ErrorAction SilentlyContinue
    for ($attempt = 1; $attempt -le 4; $attempt++) {
        Remove-Item -LiteralPath $OutFile -Force -ErrorAction SilentlyContinue
        if ($curl) {
            & curl.exe -L --fail --retry 1 --retry-delay 2 --connect-timeout 10 --max-time 60 --speed-time 20 --speed-limit 1024 `
                -A $userAgent -o $OutFile $Url
            if ($LASTEXITCODE -eq 0 -and (Test-Path $OutFile) -and (Get-Item $OutFile).Length -gt 0) {
                return
            }
        } else {
            try {
                Invoke-WebRequest -Uri $Url -OutFile $OutFile -TimeoutSec 120 -Headers @{ "User-Agent" = $userAgent }
                if ((Test-Path $OutFile) -and (Get-Item $OutFile).Length -gt 0) {
                    return
                }
            } catch {
                if ($attempt -eq 4) {
                    throw
                }
            }
        }
        Start-Sleep -Seconds (5 * $attempt)
    }
    throw "Failed to download reusable image: $Url"
}

function Upload-File {
    param(
        [string]$Url,
        [string]$Path,
        [hashtable]$Headers
    )
    $curl = Get-Command curl.exe -ErrorAction SilentlyContinue
    if ($curl) {
        $args = @("-sS", "-L", "--fail", "--connect-timeout", "20", "--max-time", "180", "-X", "PUT")
        foreach ($key in $Headers.Keys) {
            $args += @("-H", "$key`: $($Headers[$key])")
        }
        $args += @("--upload-file", $Path, $Url)
        & curl.exe @args
        if ($LASTEXITCODE -ne 0) {
            throw "PUT upload failed with curl exit code $LASTEXITCODE"
        }
        return
    }
    Invoke-WebRequest -Method Put -Uri $Url -InFile $Path -Headers $Headers -TimeoutSec 120 | Out-Null
}

Read-DotEnv -Path (Join-Path (Resolve-Path ".\..").Path ".env")
if (-not $env:DOUYU_OSS_PROVIDER -and
        $env:DOUYU_ALIYUN_OSS_ENDPOINT -and
        $env:DOUYU_ALIYUN_OSS_REGION -and
        $env:DOUYU_ALIYUN_OSS_BUCKET -and
        $env:DOUYU_ALIYUN_OSS_ACCESS_KEY_ID -and
        $env:DOUYU_ALIYUN_OSS_ACCESS_KEY_SECRET -and
        $env:DOUYU_ALIYUN_OSS_PUBLIC_BASE_URL) {
    $env:DOUYU_OSS_PROVIDER = "aliyun"
}

$manifest = Get-Content -Path $ManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
if ($manifest.Count -lt 10) {
    throw "Manifest must contain at least 10 reusable image records."
}

$health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -TimeoutSec 10
if ($health.status -ne "UP") {
    throw "Backend health is not UP."
}

$downloadsDir = Join-Path (Resolve-Path ".").Path ".community-real-content"
if (-not (Test-Path $downloadsDir)) {
    New-Item -ItemType Directory -Path $downloadsDir | Out-Null
}

Invoke-Json -Method "POST" -Path "/api/v1/auth/sms-code" -Body @{ phone = $Phone } | Out-Null
$login = Invoke-Json -Method "POST" -Path "/api/v1/auth/login/sms" -Body @{
    phone = $Phone
    code = "123456"
    ageGroup = "AGE_18_PLUS"
    nickname = $Nickname
}
$userToken = $login.data.accessToken

$adminLogin = Invoke-Json -Method "POST" -Path "/api/v1/admin/auth/login" -Body @{
    username = "admin"
    password = "admin123"
}
$adminToken = $adminLogin.data.accessToken

$topicGroups = $manifest | Group-Object -Property topicId
foreach ($group in $topicGroups) {
    $first = $group.Group[0]
    Invoke-Json -Method "PUT" -Path "/api/v1/dev/community/topics" -Token $userToken -Body @{
        topicId = $first.topicId
        name = $first.topicName
        description = $first.topicDescription
        postCount = $group.Count
    } | Out-Null
}

$reportItems = @()
$index = 0
foreach ($item in $manifest) {
    $index++
    $sourceExtension = [System.IO.Path]::GetExtension(([Uri]$item.imageUrl).AbsolutePath)
    if ([string]::IsNullOrWhiteSpace($sourceExtension)) {
        $sourceExtension = ".jpg"
    }
    $localFile = Join-Path $downloadsDir ("{0:00}-{1}{2}" -f $index, $item.id, $sourceExtension)
    Download-Image -Url $item.imageUrl -OutFile $localFile
    Start-Sleep -Milliseconds 750
    $size = Get-ImageSize -Path $localFile
    $mimeType = Get-MimeType -Path $localFile
    $bytes = (Get-Item $localFile).Length

    $presign = Invoke-Json -Method "POST" -Path "/api/v1/uploads/presign" -Token $userToken -Body @{
        usage = "POST_IMAGE"
        mimeType = $mimeType
        sizeBytes = $bytes
        fileName = [System.IO.Path]::GetFileName($localFile)
    }
    $headers = @{}
    if ($presign.data.headers) {
        foreach ($property in $presign.data.headers.PSObject.Properties) {
            $headers[$property.Name] = [string]$property.Value
        }
    }
    if (-not $headers.ContainsKey("Content-Type")) {
        $headers["Content-Type"] = $mimeType
    }
    Upload-File -Url $presign.data.uploadUrl -Path $localFile -Headers $headers

    $confirmed = Invoke-Json -Method "POST" -Path "/api/v1/uploads/confirm" -Token $userToken -Body @{
        fileKey = $presign.data.fileKey
        usage = "POST_IMAGE"
        mimeType = $mimeType
        sizeBytes = $bytes
        width = $size.Width
        height = $size.Height
    }

    $created = Invoke-Json -Method "POST" -Path "/api/v1/posts" -Token $userToken -Body @{
        title = $item.title
        content = $item.content
        mediaFileIds = @($confirmed.data.fileId)
        topicIds = @($item.topicId)
    }
    $postId = $created.data.postId
    $audit = Invoke-Json -Method "POST" -Path "/api/v1/admin/posts/$postId/audit" -Token $adminToken -Body @{
        approved = $true
        reason = "community real content import"
    }

    $reportItems += [pscustomobject]@{
        id = $item.id
        postId = $postId
        title = $item.title
        topicId = $item.topicId
        topicName = $item.topicName
        sourceUrl = $item.sourceUrl
        author = $item.author
        license = $item.license
        localFile = $localFile
        width = $size.Width
        height = $size.Height
        fileId = $confirmed.data.fileId
        ossUrl = $confirmed.data.publicUrl
        status = $audit.data.status
    }
    Write-Host ("Imported {0}: {1} ({2}x{3})" -f $postId, $item.title, $size.Width, $size.Height)
}

$report = [pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    baseUrl = $BaseUrl
    importedCount = $reportItems.Count
    items = $reportItems
}
$report | ConvertTo-Json -Depth 8 | Set-Content -Path $OutputPath -Encoding UTF8
Write-Host "Community real content import complete: $OutputPath"
