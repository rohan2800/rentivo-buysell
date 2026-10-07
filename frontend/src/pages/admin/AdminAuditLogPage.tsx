import { useEffect, useState } from 'react'
import { AdminLayout } from '../../components/AdminLayout'
import { Pagination } from '../../components/Pagination'
import { Spinner } from '../../components/Spinner'
import { api } from '../../lib/api'
import { formatRelative } from '../../lib/format'
import type { AuditLogEntry } from '../../lib/types'

const ACTION_LABEL: Record<string, string> = {
  LISTING_APPROVED: 'Approved listing',
  LISTING_REJECTED: 'Rejected listing',
  USER_BLOCKED: 'Blocked user',
  USER_UNBLOCKED: 'Unblocked user',
  CATEGORY_CREATED: 'Created category',
  CATEGORY_UPDATED: 'Updated category',
  CATEGORY_ACTIVATED: 'Activated category',
  CATEGORY_DEACTIVATED: 'Deactivated category',
  PLAN_CREATED: 'Created plan',
  PLAN_UPDATED: 'Updated plan',
  PLAN_ACTIVATED: 'Activated plan',
  PLAN_DEACTIVATED: 'Deactivated plan',
}

export function AdminAuditLogPage() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<{ content: AuditLogEntry[]; totalPages: number } | null>(null)

  useEffect(() => {
    setResult(null)
    api.admin.auditLog
      .list(page, 30)
      .then((res) => setResult({ content: res.content, totalPages: res.totalPages }))
      .catch(() => setResult({ content: [], totalPages: 0 }))
  }, [page])

  if (!result) return <AdminLayout><Spinner /></AdminLayout>

  return (
    <AdminLayout>
      <ul className="flex flex-col divide-y divide-line">
        {result.content.map((entry) => (
          <li key={entry.id} className="flex items-center justify-between gap-4 py-3">
            <div>
              <span className="text-sm font-medium text-ink">
                {ACTION_LABEL[entry.action] ?? entry.action}
              </span>
              {entry.targetId && (
                <span className="text-sm text-ink-soft"> · {entry.targetType} #{entry.targetId}</span>
              )}
              {entry.detail && <p className="text-sm text-ink-soft">{entry.detail}</p>}
            </div>
            <div className="flex-shrink-0 text-right text-xs text-ink-soft">
              <p>{entry.adminPhone}</p>
              <p>{formatRelative(entry.createdAt)}</p>
            </div>
          </li>
        ))}
      </ul>
      <Pagination page={page} totalPages={result.totalPages} onChange={setPage} />
    </AdminLayout>
  )
}
