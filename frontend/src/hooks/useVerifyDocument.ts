import { useCallback, useState } from 'react'

import { verifyPdfFileSchema } from '../schemas/verifyDocumentSchemas'
import { verifyDocument } from '../services/verifyService'
import type { VerifyDocumentResponse } from '../types/verify'
import { parseApiError } from '../utils/parseApiError'

type VerifyDocumentState = {
  error: string | null
  file: File | null
  isVerifying: boolean
  result: VerifyDocumentResponse | null
}

export function useVerifyDocument() {
  const [state, setState] = useState<VerifyDocumentState>({
    error: null,
    file: null,
    isVerifying: false,
    result: null,
  })

  const selectFile = useCallback((file: File | null) => {
    if (!file) {
      setState((current) => ({
        ...current,
        error: null,
        file: null,
        result: null,
      }))
      return
    }

    const parsedFile = verifyPdfFileSchema.safeParse(file)

    if (!parsedFile.success) {
      setState((current) => ({
        ...current,
        error: parsedFile.error.issues[0]?.message ?? 'PDF invalido.',
        file: null,
        result: null,
      }))
      return
    }

    setState((current) => ({
      ...current,
      error: null,
      file: parsedFile.data,
      result: null,
    }))
  }, [])

  const submit = useCallback(async () => {
    if (!state.file || state.isVerifying) {
      return
    }

    setState((current) => ({
      ...current,
      error: null,
      isVerifying: true,
      result: null,
    }))

    try {
      const result = await verifyDocument(state.file)
      setState((current) => ({
        ...current,
        error: null,
        isVerifying: false,
        result,
      }))
    } catch (error) {
      const parsedError = parseApiError(
        error,
        'Nao foi possivel verificar o documento.',
      )

      setState((current) => ({
        ...current,
        error: messageForError(
          parsedError.status,
          parsedError.code,
          parsedError.message,
        ),
        isVerifying: false,
        result: null,
      }))
    }
  }, [state.file, state.isVerifying])

  const reset = useCallback(() => {
    setState({
      error: null,
      file: null,
      isVerifying: false,
      result: null,
    })
  }, [])

  return {
    ...state,
    reset,
    selectFile,
    submit,
  }
}

function messageForError(
  status: number | undefined,
  code: string | undefined,
  message: string,
): string {
  if (status === 400 || code?.startsWith('DOC_') || code?.startsWith('VAL_')) {
    return message || 'O arquivo enviado nao e um PDF valido.'
  }

  if (status === 429) {
    return 'Muitas tentativas de verificacao. Aguarde um pouco antes de tentar novamente.'
  }

  if (status === 500) {
    return 'Erro interno ao verificar o documento. Tente novamente em instantes.'
  }

  return message || 'Nao foi possivel verificar o documento.'
}
