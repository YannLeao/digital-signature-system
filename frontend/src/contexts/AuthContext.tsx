import { useEffect, useMemo, useState, type ReactNode } from 'react'

import { AuthContext, type AuthContextValue } from './authContextValue'
import {
  loginUser,
  logoutUser,
  refreshSession,
  registerUser,
} from '../services/authService'
import {
  clearAccessToken,
  getAuthenticatedEmail,
  getAccessToken,
  subscribeToAccessToken,
} from '../utils/authTokenStore'

type AuthProviderProps = {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [accessToken, setAccessTokenState] = useState<string | null>(() =>
    getAccessToken(),
  )
  const [email, setEmail] = useState<string | null>(() => getAuthenticatedEmail())
  const [isInitializing, setIsInitializing] = useState(() => !getAccessToken())
  const [isTwoFactorPending, setIsTwoFactorPending] = useState(false)

  useEffect(
    () =>
      subscribeToAccessToken((session) => {
        setAccessTokenState(session.accessToken)
        setEmail(session.email)
        setIsTwoFactorPending(Boolean(session.twoFactorToken))
      }),
    [],
  )

  useEffect(() => {
    if (getAccessToken()) {
      return
    }

    let isMounted = true

    refreshSession()
      .catch(() => {
        clearAccessToken()
      })
      .finally(() => {
        if (isMounted) {
          setIsInitializing(false)
        }
      })

    return () => {
      isMounted = false
    }
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken,
      email,
      isAuthenticated: Boolean(accessToken),
      isInitializing,
      isTwoFactorPending,
      login: loginUser,
      logout: async () => {
        await logoutUser()
      },
      refresh: refreshSession,
      register: registerUser,
    }),
    [accessToken, email, isInitializing, isTwoFactorPending],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
