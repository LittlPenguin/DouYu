param(
    [string]$ComposeFile = "docker-compose.yml",
    [string]$Service = "postgres"
)

$ErrorActionPreference = "Stop"

$query = @"
select 'posts post_seed%' as bucket, count(*) as rows from posts where id like 'post_seed%'
union all
select 'products prod_%' as bucket, count(*) as rows from products where id like 'prod_%'
union all
select 'topics' as bucket, count(*) as rows from topics
union all
select 'sticker_packs' as bucket, count(*) as rows from sticker_packs;
"@

Write-Host "Read-only dev seed/demo residue diagnostic. This script does not delete or update data."
docker compose -f $ComposeFile exec -T $Service psql -U douyu -d douyu -v ON_ERROR_STOP=1 -c $query
