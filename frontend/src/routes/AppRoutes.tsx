import { Navigate, Route, Routes } from 'react-router-dom'

import { BaseLayout } from '../components/BaseLayout'
import { LoginPage } from '../pages/auth/LoginPage'
import { RegisterPage } from '../pages/auth/RegisterPage'
import { HomePage } from '../pages/HomePage'
import { ProtectedPage } from '../pages/ProtectedPage'
import { SignDocumentPage } from '../pages/documents/SignDocumentPage'
import { VerifyDocumentPage } from '../pages/public/VerifyDocumentPage'
import { PasskeysPage } from '../pages/security/PasskeysPage'
import { SecurityActivityPage } from '../pages/security/SecurityActivityPage'
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
        <Route element={<VerifyDocumentPage />} path="/verificar" />
        <Route element={<VerifyDocumentPage />} path="/verify" />
        <Route element={<PrivateRoute />}>
          <Route element={<ProtectedPage />} path="/app" />
          <Route element={<SignDocumentPage />} path="/documents/sign" />
          <Route element={<SecurityActivityPage />} path="/security" />
          <Route element={<PasskeysPage />} path="/security/passkeys" />
          <Route element={<SecurityActivityPage />} path="/settings/security" />
          <Route element={<TwoFactorPage />} path="/settings/2fa" />
        </Route>
        <Route element={<Navigate replace to="/" />} path="*" />
      </Route>
    </Routes>
  )
}
