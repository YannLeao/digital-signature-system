import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { getAccessToken } from '../utils/authToken'

export function PrivateRoute() {
  const location = useLocation()
  const accessToken = getAccessToken()

  if (!accessToken) {
    return <Navigate replace state={{ from: location }} to="/login" />
  }

  return <Outlet />
}
