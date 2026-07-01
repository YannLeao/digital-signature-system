param(
    [Parameter(Mandatory = $true)]
    [string] $InputPath,

    [string] $OutputPath
)

$source = Resolve-Path -LiteralPath $InputPath

if (-not $OutputPath) {
    $directory = Split-Path -Parent $source.Path
    $name = [System.IO.Path]::GetFileNameWithoutExtension($source.Path)
    $OutputPath = Join-Path $directory "$name-adulterado.pdf"
}

Copy-Item -LiteralPath $source.Path -Destination $OutputPath -Force

$marker = "`r`n% demo-adulteracao: byte extra apos assinatura $(Get-Date -Format o)`r`n"
$bytes = [System.Text.Encoding]::ASCII.GetBytes($marker)
$stream = [System.IO.File]::Open($OutputPath, [System.IO.FileMode]::Append, [System.IO.FileAccess]::Write)
try {
    $stream.Write($bytes, 0, $bytes.Length)
} finally {
    $stream.Dispose()
}

Write-Host "PDF adulterado gerado em: $OutputPath"
