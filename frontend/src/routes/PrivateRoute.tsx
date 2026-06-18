import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { useAuth } from '../hooks/useAuth'

export function PrivateRoute() {
  const location = useLocation()
  const { isAuthenticated, isInitializing } = useAuth()

  if (isInitializing) {
    return (
      <div className="flex min-h-80 items-center justify-center">
        <div className="flex items-center gap-3 rounded-lg border border-[#374151] bg-[#111827] px-4 py-3 text-sm text-[#D1D5DB]">
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-[#06B6D4] border-t-transparent" />
          Restaurando sessão...
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate replace state={{ from: location }} to="/login" />
  }

  return <Outlet />
}
