import {useState} from 'react'
import {AlertCircle, Fingerprint, ShieldAlert} from 'lucide-react'

import {AddPasskeyButton} from '../../components/passkeys/AddPasskeyButton'
import {PasskeyList} from '../../components/passkeys/PasskeyList'
import {RevokePasskeyDialog} from '../../components/passkeys/RevokePasskeyDialog'
import {useAuth} from '../../hooks/useAuth'
import {usePasskeys} from '../../hooks/usePasskeys'
import type {PasskeyDevice} from '../../types/passkey'
import {isWebAuthnSupported} from '../../utils/webauthn'

export function PasskeysPage() {
    const { email } = useAuth()
    const {
        devices,
        error,
        isAdding,
        isLoading,
        registerPasskey,
        revokeDevice,
    } = usePasskeys(email)
    const [deviceToRevoke, setDeviceToRevoke] = useState<PasskeyDevice | null>(
        null,
    )
    const browserSupported = isWebAuthnSupported()

    async function handleConfirmRevoke(device: PasskeyDevice) {
        await revokeDevice(device.id)
        setDeviceToRevoke(null)
    }

    return (
        <section className="max-w-4xl space-y-6 animate-fade-in">
            <div>
                <div className="flex items-center gap-2 text-sm font-medium uppercase tracking-wider text-[#06B6D4]">
                    <Fingerprint className="h-4 w-4" />
                    <span>Segurança da conta</span>
                </div>
                <h1 className="mt-1 text-3xl font-bold text-[#F9FAFB] tracking-tight">
                    Gerenciar passkeys
                </h1>
                <p className="mt-2 text-base text-[#9CA3AF]">
                    Adicione autenticadores biométricos e chaves de segurança de hardware para entrar na sua conta com total segurança, dispensando senhas tradicionais[cite: 27].
                </p>
            </div>

            <div className="rounded-xl border border-[#374151] bg-[#111827] p-5 space-y-5 shadow-2xl shadow-black/30">
                {!browserSupported ? (
                    <div className="flex items-center gap-2.5 rounded-lg border border-amber-500/40 bg-amber-500/10 px-4 py-3 text-sm text-amber-200 font-medium">
                        <AlertCircle className="h-5 w-5 text-amber-500 shrink-0" />
                        <span>Este navegador não oferece suporte nativo a chaves passkeys[cite: 27].</span>
                    </div>
                ) : null}

                {!email ? (
                    <div className="flex items-center gap-2.5 rounded-lg border border-red-500/40 bg-red-500/10 px-4 py-3 text-sm text-red-300 font-medium">
                        <ShieldAlert className="h-5 w-5 text-red-500 shrink-0" />
                        <span>Sessão expirada. Por favor, realize o login novamente para gerenciar suas chaves[cite: 27].</span>
                    </div>
                ) : null}

                {error ? (
                    <div className="flex items-center gap-2.5 rounded-lg border border-red-500/40 bg-red-500/10 px-4 py-3 text-sm text-red-300 font-medium">
                        <AlertCircle className="h-5 w-5 text-red-500 shrink-0" />
                        <span>{error}</span>
                    </div>
                ) : null}

                <PasskeyList
                    devices={devices}
                    isLoading={isLoading}
                    onRevoke={setDeviceToRevoke}
                />

                <AddPasskeyButton
                    disabled={!email || !browserSupported || isAdding}
                    onAdd={registerPasskey}
                />
            </div>

            <RevokePasskeyDialog
                device={deviceToRevoke}
                onCancel={() => setDeviceToRevoke(null)}
                onConfirm={handleConfirmRevoke}
            />
        </section>
    )
}
