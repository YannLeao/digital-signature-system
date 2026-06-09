import { useCallback, useState } from 'react'

import { DocumentSignError, signDocument } from '../services/documentService'
import type { SignDocumentRequest, SignedDocument } from '../types/document'
import { downloadPdf } from '../utils/downloadPdf'
import { parseApiError } from '../utils/parseApiError'

type SigningState = {
  error: string | null
  isSigning: boolean
  signedDocument: SignedDocument | null
}

export function usePdfSigning() {
  const [state, setState] = useState<SigningState>({
    error: null,
    isSigning: false,
    signedDocument: null,
  })

  const submit = useCallback(
    async (file: File, request: SignDocumentRequest) => {
      setState((current) => ({
        ...current,
        error: null,
        isSigning: true,
        signedDocument: null,
      }))

      try {
        const signedDocument = await signDocument(file, request)
        setState({
          error: null,
          isSigning: false,
          signedDocument,
        })
      } catch (error) {
        if (error instanceof DocumentSignError) {
          setState({
            error: messageForError(error.status, error.code, error.message),
            isSigning: false,
            signedDocument: null,
          })
          return
        }

        const parsedError = parseApiError(
          error,
          'Nao foi possivel assinar o documento.',
        )

        setState({
          error: messageForError(parsedError.status, parsedError.code, parsedError.message),
          isSigning: false,
          signedDocument: null,
        })
      }
    },
    [],
  )

  const download = useCallback(() => {
    if (!state.signedDocument) {
      return
    }

    downloadPdf(state.signedDocument.blob, state.signedDocument.filename)
  }, [state.signedDocument])

  const reset = useCallback(() => {
    setState({
      error: null,
      isSigning: false,
      signedDocument: null,
    })
  }, [])

  return {
    ...state,
    download,
    reset,
    submit,
  }
}

function messageForError(
  status: number | undefined,
  code: string | undefined,
  message: string,
): string {
  if (status === 401) {
    return 'Sessao expirada.'
  }
  if (status === 403) {
    return 'Requisicao bloqueada por seguranca.'
  }
  if (status === 429) {
    return 'Aguarde antes de tentar novamente.'
  }
  if (status === 500) {
    return 'Erro interno ao assinar o documento.'
  }
  if (code === 'DOC_001') {
    return message || 'Documento PDF invalido.'
  }

  return message || 'Nao foi possivel assinar o documento.'
}
