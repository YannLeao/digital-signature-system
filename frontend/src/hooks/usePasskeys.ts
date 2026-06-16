import { useCallback, useEffect, useState } from 'react'

import {
  finishPasskeyRegistration,
  listPasskeys,
  revokePasskey,
  startPasskeyRegistration,
} from '../services/passkeyService'
import type { PasskeyDevice } from '../types/passkey'
import { parseApiError } from '../utils/parseApiError'
import {
  getWebAuthnErrorMessage,
  isUserCancellation,
  isWebAuthnSupported,
  serializeRegistrationCredential,
  toCredentialCreationOptions,
} from '../utils/webauthn'

type UsePasskeysResult = {
  devices: PasskeyDevice[]
  error: string | null
  isAdding: boolean
  isLoading: boolean
  loadPasskeys: () => Promise<void>
  registerPasskey: (deviceName: string) => Promise<void>
  revokeDevice: (id: number) => Promise<void>
}

export function usePasskeys(email: string | null): UsePasskeysResult {
  const [devices, setDevices] = useState<PasskeyDevice[]>([])
  const [error, setError] = useState<string | null>(null)
  const [isAdding, setIsAdding] = useState(false)
  const [isLoading, setIsLoading] = useState(false)

  const loadPasskeys = useCallback(async () => {
    if (!email) {
      setDevices([])
      return
    }

    setIsLoading(true)
    setError(null)

    try {
      setDevices(await listPasskeys(email))
    } catch (requestError) {
      setError(
        parseApiError(
          requestError,
          'Não foi possível carregar suas passkeys.',
        ).message,
      )
    } finally {
      setIsLoading(false)
    }
  }, [email])

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void loadPasskeys()
    }, 0)

    return () => window.clearTimeout(timeoutId)
  }, [loadPasskeys])

  const registerPasskey = useCallback(
    async (deviceName: string) => {
      if (!email) {
        setError('Sessão sem e-mail disponível. Entre novamente.')
        return
      }

      if (!isWebAuthnSupported()) {
        setError('Este navegador não oferece suporte a passkeys.')
        return
      }

      setIsAdding(true)
      setError(null)

      try {
        const options = await startPasskeyRegistration({ email })
        const credential = await navigator.credentials.create(
          toCredentialCreationOptions(options),
        )

        if (!(credential instanceof PublicKeyCredential)) {
          setError('O autenticador não retornou uma credencial válida.')
          return
        }

        await finishPasskeyRegistration({
          credential: serializeRegistrationCredential(credential),
          deviceName,
          email,
        })
        await loadPasskeys()
      } catch (registrationError) {
        if (isUserCancellation(registrationError)) {
          setError('Criação da passkey cancelada.')
          return
        }

        const webAuthnErrorMessage = getWebAuthnErrorMessage(registrationError)
        if (webAuthnErrorMessage) {
          setError(webAuthnErrorMessage)
          return
        }

        setError(
          parseApiError(
            registrationError,
            'Não foi possível adicionar a passkey.',
          ).message,
        )
      } finally {
        setIsAdding(false)
      }
    },
    [email, loadPasskeys],
  )

  const revokeDevice = useCallback(
    async (id: number) => {
      if (!email) {
        setError('Sessão sem e-mail disponível. Entre novamente.')
        return
      }

      setError(null)

      try {
        await revokePasskey(id, email)
        await loadPasskeys()
      } catch (requestError) {
        setError(
          parseApiError(requestError, 'Não foi possível revogar a passkey.')
            .message,
        )
      }
    },
    [email, loadPasskeys],
  )

  return {
    devices,
    error,
    isAdding,
    isLoading,
    loadPasskeys,
    registerPasskey,
    revokeDevice,
  }
}
