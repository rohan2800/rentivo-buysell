interface Props {
  page: number
  totalPages: number
  onChange: (page: number) => void
}

export function Pagination({ page, totalPages, onChange }: Props) {
  if (totalPages <= 1) return null
  return (
    <div className="mt-8 flex items-center justify-center gap-4">
      <button
        type="button"
        onClick={() => onChange(page - 1)}
        disabled={page <= 0}
        className="px-3 py-1.5 text-sm font-medium text-steel disabled:text-ink-soft/40"
      >
        Previous
      </button>
      <span className="text-sm text-ink-soft">
        Page {page + 1} of {totalPages}
      </span>
      <button
        type="button"
        onClick={() => onChange(page + 1)}
        disabled={page >= totalPages - 1}
        className="px-3 py-1.5 text-sm font-medium text-steel disabled:text-ink-soft/40"
      >
        Next
      </button>
    </div>
  )
}
