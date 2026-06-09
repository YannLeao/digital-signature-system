import { Navigate, Route, Routes } from 'react-router-dom'

import { BaseLayout } from '../components/BaseLayout'
import { LoginPage } from '../pages/auth/LoginPage'
import { RegisterPage } from '../pages/auth/RegisterPage'
import { HomePage } from '../pages/HomePage'
import { ProtectedPage } from '../pages/ProtectedPage'
import { SignDocumentPage } from '../pages/documents/SignDocumentPage'
import { PasskeysPage } from '../pages/security/PasskeysPage'
import { TotpVerifyPage } from '../pages/security/TotpVerifyPage'
import { TwoFactorPage } from '../pages/security/TwoFactorPage'
import { PrivateRoute } from './PrivateRoute'

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<BaseLayout />}>
        <Route index element={<HomePage />} />
        <Route element={<LoginPage />} path="/login" />
        <Route element={<RegisterPage />} path="/register" />
        <Route element={<TotpVerifyPage />} path="/two-factor" />
        <Route element={<PrivateRoute />}>
          <Route element={<ProtectedPage />} path="/app" />
          <Route element={<SignDocumentPage />} path="/documents/sign" />
          <Route element={<PasskeysPage />} path="/security/passkeys" />
          <Route element={<TwoFactorPage />} path="/settings/2fa" />
        </Route>
        <Route element={<Navigate replace to="/" />} path="*" />
      </Route>
    </Routes>
  )
}
