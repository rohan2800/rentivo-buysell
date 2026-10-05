import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import type { Category } from '../lib/types'

interface Props {
  selectedId?: number
  onSelect: (id: number | undefined) => void
}

export function CategoryTabs({ selectedId, onSelect }: Props) {
  const [categories, setCategories] = useState<Category[]>([])

  useEffect(() => {
    api.categories.list().then(setCategories).catch(() => undefined)
  }, [])

  return (
    <div className="flex flex-wrap gap-2">
      <button
        type="button"
        onClick={() => onSelect(undefined)}
        className={`px-3 py-1.5 text-sm font-medium ${
          selectedId === undefined ? 'bg-ink text-paper' : 'bg-paper-raised text-ink-soft'
        }`}
      >
        All
      </button>
      {categories.map((c) => (
        <button
          key={c.id}
          type="button"
          onClick={() => onSelect(c.id)}
          className={`px-3 py-1.5 text-sm font-medium ${
            selectedId === c.id ? 'bg-ink text-paper' : 'bg-paper-raised text-ink-soft'
          }`}
        >
          {c.name}
        </button>
      ))}
    </div>
  )
}
