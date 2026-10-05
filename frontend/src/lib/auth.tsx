import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, setAuthToken } from './api'
import type { Role } from './types'

interface StoredUser {
  token: string
  userId: number
  name: string
  phone: string
  role: Role
}

interface AuthContextValue {
  user: StoredUser | null
  isAuthenticated: boolean
  sendOtp: (phone: string) => Promise<string | null>
  verifyOtp: (phone: string, code: string, name?: string) => Promise<void>
  logout: () => void
}

const STORAGE_KEY = 'rentivo.auth'
const AuthContext = createContext<AuthContextValue | null>(null)

function load(): StoredUser | null {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as StoredUser
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<StoredUser | null>(() => load())

  useEffect(() => {
    setAuthToken(user?.token ?? null)
  }, [user])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      sendOtp: async (phone: string) => {
        const res = await api.auth.sendOtp(phone)
        return res.developmentOtp
      },
      verifyOtp: async (phone: string, code: string, name?: string) => {
        const res = await api.auth.verifyOtp(phone, code, name)
        const stored: StoredUser = {
          token: res.token,
          userId: res.userId,
          name: res.name,
          phone: res.phone,
          role: res.role,
        }
        localStorage.setItem(STORAGE_KEY, JSON.stringify(stored))
        setUser(stored)
      },
      logout: () => {
        localStorage.removeItem(STORAGE_KEY)
        setUser(null)
      },
    }),
    [user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
