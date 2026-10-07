import { useEffect, useState } from 'react'
import { AdminLayout } from '../../components/AdminLayout'
import { Pagination } from '../../components/Pagination'
import { Spinner } from '../../components/Spinner'
import { ApiError, api } from '../../lib/api'
import { useAuth } from '../../lib/auth'
import { formatDate } from '../../lib/format'
import { useToast } from '../../lib/toast'
import type { AdminUser } from '../../lib/types'

export function AdminUsersPage() {
  const toast = useToast()
  const { user: currentUser } = useAuth()
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<{ content: AdminUser[]; totalPages: number } | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)

  function reload() {
    api.admin.users
      .list(page, 20)
      .then((res) => setResult({ content: res.content, totalPages: res.totalPages }))
      .catch(() => setResult({ content: [], totalPages: 0 }))
  }

  useEffect(reload, [page])

  async function toggle(u: AdminUser) {
    setBusyId(u.id)
    try {
      await api.admin.users.setActive(u.id, !u.active)
      toast.show(u.active ? 'User blocked' : 'User unblocked')
      reload()
    } catch (err) {
      toast.show(err instanceof ApiError ? err.message : 'Could not update user', 'error')
    } finally {
      setBusyId(null)
    }
  }

  if (!result) return <AdminLayout><Spinner /></AdminLayout>

  return (
    <AdminLayout>
      <table className="w-full text-left text-sm">
        <thead>
          <tr className="border-b border-line text-ink-soft">
            <th className="py-2 font-medium">Name</th>
            <th className="font-medium">Phone</th>
            <th className="font-medium">Role</th>
            <th className="font-medium">Joined</th>
            <th className="font-medium">Status</th>
            <th />
          </tr>
        </thead>
        <tbody className="divide-y divide-line">
          {result.content.map((u) => (
            <tr key={u.id}>
              <td className="py-2 text-ink">{u.name}</td>
              <td className="text-ink-soft">{u.phone}</td>
              <td className="text-ink-soft">{u.role}</td>
              <td className="text-ink-soft">{formatDate(u.createdAt)}</td>
              <td className={u.active ? 'text-ochre-dark' : 'text-brick'}>
                {u.active ? 'Active' : 'Blocked'}
              </td>
              <td className="text-right">
                {u.role !== 'ADMIN' && (
                  <button
                    type="button"
                    onClick={() => toggle(u)}
                    disabled={busyId === u.id || u.id === currentUser?.userId}
                    className="border border-line px-3 py-1 text-xs font-medium text-steel disabled:opacity-40"
                  >
                    {u.active ? 'Block' : 'Unblock'}
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pagination page={page} totalPages={result.totalPages} onChange={setPage} />
    </AdminLayout>
  )
}
