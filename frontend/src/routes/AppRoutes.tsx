import { Navigate, Route, Routes } from 'react-router-dom'

import { BaseLayout } from '../components/BaseLayout'
import { HomePage } from '../pages/HomePage'
import { LoginPage } from '../pages/LoginPage'
import { ProtectedPage } from '../pages/ProtectedPage'
import { PrivateRoute } from './PrivateRoute'

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<BaseLayout />}>
        <Route index element={<HomePage />} />
        <Route element={<LoginPage />} path="/login" />
        <Route element={<PrivateRoute />}>
          <Route element={<ProtectedPage />} path="/app" />
        </Route>
        <Route element={<Navigate replace to="/" />} path="*" />
      </Route>
    </Routes>
  )
}
