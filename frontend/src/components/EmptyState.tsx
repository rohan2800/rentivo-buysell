export function EmptyState({ title, body }: { title: string; body?: string }) {
  return (
    <div className="flex flex-col items-center gap-1 border border-dashed border-line py-16 text-center">
      <p className="font-display text-lg font-semibold text-ink">{title}</p>
      {body && <p className="max-w-sm text-sm text-ink-soft">{body}</p>}
    </div>
  )
}
