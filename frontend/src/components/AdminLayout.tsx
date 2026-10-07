import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'

const TABS = [
  { to: '/admin', label: 'Dashboard', end: true },
  { to: '/admin/listings', label: 'Listings' },
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/categories', label: 'Categories' },
  { to: '/admin/plans', label: 'Plans' },
  { to: '/admin/audit-log', label: 'Audit log' },
]

export function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex flex-col gap-6">
      <h1 className="font-display text-2xl font-bold text-ink">Admin</h1>
      <nav className="flex flex-wrap gap-1 border-b border-line">
        {TABS.map((t) => (
          <NavLink
            key={t.to}
            to={t.to}
            end={t.end}
            className={({ isActive }) =>
              `px-3 py-2 text-sm font-medium ${
                isActive ? 'border-b-2 border-ochre-dark text-ink' : 'text-ink-soft'
              }`
            }
          >
            {t.label}
          </NavLink>
        ))}
      </nav>
      {children}
    </div>
  )
}
