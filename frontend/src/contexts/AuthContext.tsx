import { useEffect, useMemo, useState, type ReactNode } from 'react'

import { AuthContext, type AuthContextValue } from './authContextValue'
import {
  loginUser,
  logoutUser,
  refreshSession,
  registerUser,
} from '../services/authService'
import {
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

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken,
      email,
      isAuthenticated: Boolean(accessToken),
      isTwoFactorPending,
      login: loginUser,
      logout: async () => {
        await logoutUser()
      },
      refresh: refreshSession,
      register: registerUser,
    }),
    [accessToken, email, isTwoFactorPending],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
