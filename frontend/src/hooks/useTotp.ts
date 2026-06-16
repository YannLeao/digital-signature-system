import { useCallback, useEffect, useState } from 'react'

import {
  getTotpStatus,
  startTotpSetup,
  verifyTotpSetup,
} from '../services/totpService'
import type { TotpSetupResponse } from '../types/totp'
import { parseApiError } from '../utils/parseApiError'
import { extractTotpSecret, formatTotpSecret } from '../utils/totp'

type UseTotpResult = {
  backupCodes: string[]
  clearBackupCodes: () => void
  code: string
  error: string | null
  isConfirming: boolean
  isEnabled: boolean
  isStatusLoading: boolean
  isStarting: boolean
  manualSecret: string
  otpauthUrl?: string
  setCode: (code: string) => void
  startSetup: () => Promise<void>
  verifySetup: (code: string) => Promise<void>
}

export function useTotp(): UseTotpResult {
  const [setupResponse, setSetupResponse] = useState<TotpSetupResponse | null>(
    null,
  )
  const [backupCodes, setBackupCodes] = useState<string[]>([])
  const [code, setCode] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isEnabled, setIsEnabled] = useState(false)
  const [isStarting, setIsStarting] = useState(false)
  const [isStatusLoading, setIsStatusLoading] = useState(true)
  const [isConfirming, setIsConfirming] = useState(false)
  const rawSecret = setupResponse
    ? extractTotpSecret(setupResponse.otpauthUrl)
    : null
  const manualSecret = rawSecret ? formatTotpSecret(rawSecret) : ''

  useEffect(
    () => {
      let isMounted = true

      void getTotpStatus()
        .then((status) => {
          if (isMounted) {
            setIsEnabled(status.enabled)
          }
        })
        .catch(() => {
          if (isMounted) {
            setError('Nao foi possivel carregar o status do 2FA.')
          }
        })
        .finally(() => {
          if (isMounted) {
            setIsStatusLoading(false)
          }
        })

      return () => {
        isMounted = false
        setSetupResponse(null)
        setBackupCodes([])
        setCode('')
      }
    },
    [],
  )

  const startSetup = useCallback(async () => {
    setIsStarting(true)
    setError(null)
    setBackupCodes([])

    try {
      setSetupResponse(await startTotpSetup())
    } catch (requestError) {
      setError(
        parseApiError(
          requestError,
          'Nao foi possivel iniciar a configuracao do 2FA.',
        ).message,
      )
    } finally {
      setIsStarting(false)
    }
  }, [])

  const verifySetup = useCallback(
    async (nextCode: string) => {
      const normalizedCode = nextCode.replace(/\D/g, '').slice(0, 6)

      if (
        isConfirming ||
        !setupResponse ||
        !rawSecret ||
        normalizedCode.length !== 6
      ) {
        return
      }

      setIsConfirming(true)
      setError(null)

      try {
        const response = await verifyTotpSetup({ code: normalizedCode })
        setBackupCodes(response.backupCodes)
        setIsEnabled(true)
        setCode('')
      } catch (verificationError) {
        setError(
          parseApiError(
            verificationError,
            'Nao foi possivel confirmar a ativacao.',
          ).message,
        )
      } finally {
        setIsConfirming(false)
      }
    },
    [isConfirming, rawSecret, setupResponse],
  )

  const clearBackupCodes = useCallback(() => {
    setBackupCodes([])
  }, [])

  return {
    backupCodes,
    clearBackupCodes,
    code,
    error,
    isConfirming,
    isEnabled,
    isStatusLoading,
    isStarting,
    manualSecret,
    otpauthUrl: setupResponse?.otpauthUrl,
    setCode,
    startSetup,
    verifySetup,
  }
}
