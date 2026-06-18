import {QRCodeSVG} from 'qrcode.react'

type TotpQrCodeProps = {
    otpauthUrl: string
}

export function TotpQrCode({ otpauthUrl }: TotpQrCodeProps) {
    return (
        <div className="inline-flex items-center justify-center rounded-xl border border-[#06B6D4]/20 bg-white p-4 shadow-xl shadow-black/20 self-center lg:self-start">
            <QRCodeSVG aria-label="QR Code TOTP" size={170} value={otpauthUrl} />
        </div>
    )
}
