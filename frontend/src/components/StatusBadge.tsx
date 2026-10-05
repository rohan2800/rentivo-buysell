import type { ListingStatus } from '../lib/types'

const STYLES: Record<ListingStatus, string> = {
  PENDING: 'bg-steel-soft text-steel',
  APPROVED: 'bg-ochre/15 text-ochre-dark',
  REJECTED: 'bg-brick-soft text-brick',
  EXPIRED: 'bg-brick-soft text-brick',
  DELETED: 'bg-ink/10 text-ink-soft',
}

const LABEL: Record<ListingStatus, string> = {
  PENDING: 'Pending review',
  APPROVED: 'Live',
  REJECTED: 'Rejected',
  EXPIRED: 'Expired',
  DELETED: 'Deleted',
}

export function StatusBadge({ status }: { status: ListingStatus }) {
  return (
    <span className={`inline-block rounded px-2 py-0.5 text-sm font-medium ${STYLES[status]}`}>
      {LABEL[status]}
    </span>
  )
}
