import { useEffect, useState } from 'react'
import { AdminLayout } from '../../components/AdminLayout'
import { Spinner } from '../../components/Spinner'
import { ApiError, api } from '../../lib/api'
import { useToast } from '../../lib/toast'
import type { Category, CategoryRequest, FieldRequest, FieldType } from '../../lib/types'

const FIELD_TYPES: FieldType[] = [
  'TEXT',
  'TEXTAREA',
  'NUMBER',
  'DECIMAL',
  'BOOLEAN',
  'DATE',
  'SELECT',
  'MULTI_SELECT',
]

function toFieldRequests(category?: Category): FieldRequest[] {
  if (!category) return []
  return category.fields
    .filter((f) => f.active)
    .map((f) => ({
      id: f.id,
      name: f.name,
      type: f.type,
      required: f.required,
      optionsCsv: f.options.join(','),
      sortOrder: f.sortOrder,
    }))
}

function CategoryForm({
  initial,
  onSaved,
  onCancel,
}: {
  initial?: Category
  onSaved: () => void
  onCancel: () => void
}) {
  const toast = useToast()
  const [name, setName] = useState(initial?.name ?? '')
  const [fields, setFields] = useState<FieldRequest[]>(toFieldRequests(initial))
  const [saving, setSaving] = useState(false)

  function updateField(index: number, patch: Partial<FieldRequest>) {
    setFields((prev) => prev.map((f, i) => (i === index ? { ...f, ...patch } : f)))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    const body: CategoryRequest = { name, active: initial?.active ?? true, fields }
    try {
      if (initial) {
        await api.admin.categories.update(initial.id, body)
      } else {
        await api.admin.categories.create(body)
      }
      toast.show('Category saved')
      onSaved()
    } catch (err) {
      toast.show(err instanceof ApiError ? err.message : 'Could not save category', 'error')
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4 border border-line p-4">
      <label className="flex flex-col gap-1 text-sm font-medium text-ink">
        Category name
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          className="border border-line bg-white px-3 py-2"
        />
      </label>

      <div className="flex flex-col gap-3">
        <p className="text-sm font-medium text-ink">Fields</p>
        {fields.map((f, i) => (
          <div key={i} className="flex flex-wrap items-center gap-2 border border-line p-2">
            <input
              value={f.name}
              onChange={(e) => updateField(i, { name: e.target.value })}
              placeholder="Field name"
              required
              className="border border-line bg-white px-2 py-1 text-sm"
            />
            <select
              value={f.type}
              onChange={(e) => updateField(i, { type: e.target.value as FieldType })}
              className="border border-line bg-white px-2 py-1 text-sm"
            >
              {FIELD_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
            {(f.type === 'SELECT' || f.type === 'MULTI_SELECT') && (
              <input
                value={f.optionsCsv ?? ''}
                onChange={(e) => updateField(i, { optionsCsv: e.target.value })}
                placeholder="Option1,Option2,Option3"
                className="min-w-48 flex-1 border border-line bg-white px-2 py-1 text-sm"
              />
            )}
            <label className="flex items-center gap-1 text-xs text-ink">
              <input
                type="checkbox"
                checked={f.required}
                onChange={(e) => updateField(i, { required: e.target.checked })}
              />
              Required
            </label>
            <button
              type="button"
              onClick={() => setFields((prev) => prev.filter((_, idx) => idx !== i))}
              className="text-xs font-medium text-brick"
            >
              Remove
            </button>
          </div>
        ))}
        <button
          type="button"
          onClick={() =>
            setFields((prev) => [
              ...prev,
              { name: '', type: 'TEXT', required: false, sortOrder: prev.length },
            ])
          }
          className="self-start border border-dashed border-line px-3 py-1.5 text-sm text-steel"
        >
          Add field
        </button>
      </div>

      <div className="flex gap-2">
        <button
          type="submit"
          disabled={saving}
          className="bg-ochre px-4 py-2 font-semibold text-ink hover:bg-ochre-dark disabled:opacity-60"
        >
          {saving ? 'Saving…' : 'Save category'}
        </button>
        <button type="button" onClick={onCancel} className="px-4 py-2 text-sm font-medium text-steel">
          Cancel
        </button>
      </div>
    </form>
  )
}

export function AdminCategoriesPage() {
  const toast = useToast()
  const [categories, setCategories] = useState<Category[] | null>(null)
  const [editing, setEditing] = useState<Category | 'new' | null>(null)

  function reload() {
    api.admin.categories.list().then(setCategories).catch(() => setCategories([]))
  }

  useEffect(reload, [])

  async function toggleActive(c: Category) {
    try {
      await api.admin.categories.setActive(c.id, !c.active)
      toast.show(c.active ? 'Category deactivated' : 'Category activated')
      reload()
    } catch (err) {
      toast.show(err instanceof ApiError ? err.message : 'Could not update category', 'error')
    }
  }

  if (!categories) return <AdminLayout><Spinner /></AdminLayout>

  return (
    <AdminLayout>
      <div className="flex flex-col gap-4">
        {editing === null && (
          <button
            type="button"
            onClick={() => setEditing('new')}
            className="self-start bg-ochre px-4 py-2 font-semibold text-ink hover:bg-ochre-dark"
          >
            New category
          </button>
        )}

        {editing === 'new' && (
          <CategoryForm
            onSaved={() => {
              setEditing(null)
              reload()
            }}
            onCancel={() => setEditing(null)}
          />
        )}

        <ul className="flex flex-col divide-y divide-line">
          {categories.map((c) => (
            <li key={c.id} className="flex flex-col gap-2 py-3">
              <div className="flex items-center justify-between">
                <div>
                  <span className="font-medium text-ink">{c.name}</span>
                  <span className="ml-2 text-xs text-ink-soft">
                    {c.fields.filter((f) => f.active).length} fields
                  </span>
                  {!c.active && <span className="ml-2 text-xs text-brick">Inactive</span>}
                </div>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => setEditing(editing === c ? null : c)}
                    className="border border-line px-3 py-1 text-xs font-medium text-steel"
                  >
                    Edit
                  </button>
                  {!c.systemCategory && (
                    <button
                      type="button"
                      onClick={() => toggleActive(c)}
                      className="border border-line px-3 py-1 text-xs font-medium text-brick"
                    >
                      {c.active ? 'Deactivate' : 'Activate'}
                    </button>
                  )}
                </div>
              </div>
              {editing === c && (
                <CategoryForm
                  initial={c}
                  onSaved={() => {
                    setEditing(null)
                    reload()
                  }}
                  onCancel={() => setEditing(null)}
                />
              )}
            </li>
          ))}
        </ul>
      </div>
    </AdminLayout>
  )
}
