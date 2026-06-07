import { useEffect, useMemo, useState, type ReactNode } from 'react'

import { AuthContext, type AuthContextValue } from './authContextValue'
import {
  loginUser,
  logoutUser,
  refreshSession,
  registerUser,
} from '../services/authService'
import {
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

  useEffect(() => subscribeToAccessToken(setAccessTokenState), [])

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken,
      isAuthenticated: Boolean(accessToken),
      login: loginUser,
      logout: async () => {
        await logoutUser()
      },
      refresh: refreshSession,
      register: registerUser,
    }),
    [accessToken],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
