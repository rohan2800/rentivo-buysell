import { useState, type ReactNode } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../lib/auth'
import { ConnectivityBanner } from './ConnectivityBanner'
import { NotificationBell } from './NotificationBell'

const NAV_LINKS = [
  { to: '/browse', label: 'Browse', authOnly: false },
  { to: '/listings/new', label: 'Post listing', authOnly: true },
  { to: '/my-listings', label: 'My listings', authOnly: true },
  { to: '/favorites', label: 'Favorites', authOnly: true },
  { to: '/plans', label: 'Plans', authOnly: true },
]

export function Layout({ children }: { children: ReactNode }) {
  const { isAuthenticated, user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  const links = NAV_LINKS.filter((l) => !l.authOnly || isAuthenticated)

  return (
    <div className="min-h-screen bg-paper text-ink">
      <ConnectivityBanner />
      <header className="border-b border-line">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-6 px-4 py-4">
          <Link to="/" className="font-display text-xl font-bold tracking-tight text-ink">
            Rentivo
          </Link>

          <nav className="hidden flex-1 items-center gap-6 md:flex">
            {links.map((l) => (
              <Link key={l.to} to={l.to} className="text-sm font-medium text-steel">
                {l.label}
              </Link>
            ))}
            {isAuthenticated && <NotificationBell />}
          </nav>

          <div className="hidden items-center gap-3 md:flex">
            {isAuthenticated ? (
              <>
                <span className="text-sm text-ink-soft">{user?.name}</span>
                <button
                  type="button"
                  onClick={() => {
                    logout()
                    navigate('/')
                  }}
                  className="text-sm font-medium text-steel"
                >
                  Log out
                </button>
              </>
            ) : (
              <Link
                to="/login"
                className="bg-ochre px-4 py-1.5 text-sm font-semibold text-ink hover:bg-ochre-dark"
              >
                Log in
              </Link>
            )}
          </div>

          <button
            type="button"
            onClick={() => setMenuOpen((v) => !v)}
            className="flex h-9 w-9 flex-col items-center justify-center gap-1 md:hidden"
            aria-label="Toggle menu"
          >
            <span className="block h-0.5 w-5 bg-ink" />
            <span className="block h-0.5 w-5 bg-ink" />
            <span className="block h-0.5 w-5 bg-ink" />
          </button>
        </div>

        {menuOpen && (
          <nav className="flex flex-col gap-1 border-t border-line px-4 py-3 md:hidden">
            {links.map((l) => (
              <Link
                key={l.to}
                to={l.to}
                onClick={() => setMenuOpen(false)}
                className="py-2 text-sm font-medium text-steel"
              >
                {l.label}
              </Link>
            ))}
            {isAuthenticated && (
              <Link
                to="/notifications"
                onClick={() => setMenuOpen(false)}
                className="py-2 text-sm font-medium text-steel"
              >
                Notifications
              </Link>
            )}
            {isAuthenticated ? (
              <button
                type="button"
                onClick={() => {
                  logout()
                  setMenuOpen(false)
                  navigate('/')
                }}
                className="py-2 text-left text-sm font-medium text-steel"
              >
                Log out ({user?.name})
              </button>
            ) : (
              <Link
                to="/login"
                onClick={() => setMenuOpen(false)}
                className="py-2 text-sm font-semibold text-ochre-dark"
              >
                Log in
              </Link>
            )}
          </nav>
        )}
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8">{children}</main>
      <footer className="mt-16 border-t border-line py-8">
        <div className="mx-auto max-w-6xl px-4 text-sm text-ink-soft">
          Rentivo — post for free, browse what's nearby.
        </div>
      </footer>
    </div>
  )
}
