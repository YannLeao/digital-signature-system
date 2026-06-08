import { QRCodeSVG } from 'qrcode.react'

type TotpQrCodeProps = {
  otpauthUrl: string
}

export function TotpQrCode({ otpauthUrl }: TotpQrCodeProps) {
  return (
    <div className="inline-flex rounded-xl bg-white p-4">
      <QRCodeSVG aria-label="QR Code TOTP" size={184} value={otpauthUrl} />
    </div>
  )
}

