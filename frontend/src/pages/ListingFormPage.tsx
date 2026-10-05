import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Spinner } from '../components/Spinner'
import { ApiError, api } from '../lib/api'
import { useAuth } from '../lib/auth'
import { useToast } from '../lib/toast'
import type { Category, CategoryField, CreateListingRequest, ImageResponse, OwnedListing } from '../lib/types'

const EMPTY: CreateListingRequest = {
  title: '',
  categoryId: 0,
  listingType: 'RENT',
  price: 0,
  priceUnit: 'TOTAL',
  description: '',
  state: '',
  city: '',
  locality: '',
  pincode: '',
  address: '',
  contactPhone: '',
  fields: {},
}

function DynamicFieldInput({
  field,
  value,
  onChange,
}: {
  field: CategoryField
  value: string
  onChange: (value: string) => void
}) {
  if (field.type === 'TEXTAREA') {
    return (
      <textarea
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={field.required}
        rows={3}
        className="border border-line bg-white px-3 py-2 text-sm"
      />
    )
  }
  if (field.type === 'BOOLEAN') {
    return (
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={field.required}
        className="border border-line bg-white px-3 py-2 text-sm"
      >
        <option value="">Select</option>
        <option value="true">Yes</option>
        <option value="false">No</option>
      </select>
    )
  }
  if (field.type === 'SELECT') {
    return (
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={field.required}
        className="border border-line bg-white px-3 py-2 text-sm"
      >
        <option value="">Select</option>
        {field.options.map((o) => (
          <option key={o} value={o}>
            {o}
          </option>
        ))}
      </select>
    )
  }
  if (field.type === 'MULTI_SELECT') {
    const selected = new Set(value ? value.split(',') : [])
    return (
      <div className="flex flex-wrap gap-3">
        {field.options.map((o) => (
          <label key={o} className="flex items-center gap-1.5 text-sm text-ink">
            <input
              type="checkbox"
              checked={selected.has(o)}
              onChange={(e) => {
                const next = new Set(selected)
                if (e.target.checked) next.add(o)
                else next.delete(o)
                onChange(Array.from(next).join(','))
              }}
            />
            {o}
          </label>
        ))}
      </div>
    )
  }
  if (field.type === 'DATE') {
    return (
      <input
        type="date"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={field.required}
        className="border border-line bg-white px-3 py-2 text-sm"
      />
    )
  }
  return (
    <input
      type={field.type === 'NUMBER' || field.type === 'DECIMAL' ? 'number' : 'text'}
      step={field.type === 'DECIMAL' ? 'any' : undefined}
      value={value}
      onChange={(e) => onChange(e.target.value)}
      required={field.required}
      className="border border-line bg-white px-3 py-2 text-sm"
    />
  )
}

export function ListingFormPage() {
  const { id } = useParams<{ id: string }>()
  const editingId = id ? Number(id) : undefined
  const navigate = useNavigate()
  const { user } = useAuth()
  const toast = useToast()

  const [categories, setCategories] = useState<Category[]>([])
  const [form, setForm] = useState<CreateListingRequest>({
    ...EMPTY,
    contactPhone: user?.phone ?? '',
  })
  const [currentId, setCurrentId] = useState<number | undefined>(editingId)
  const [images, setImages] = useState<ImageResponse[]>([])
  const [loading, setLoading] = useState(editingId !== undefined)
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.categories.list().then(setCategories).catch(() => undefined)
  }, [])

  function populateFromExisting(l: OwnedListing) {
    const fields: Record<number, string> = {}
    for (const f of l.fields) fields[f.fieldId] = f.value
    setForm({
      title: l.title,
      categoryId: l.category.id,
      listingType: l.listingType,
      price: l.price,
      priceUnit: l.priceUnit,
      description: l.description,
      state: l.state,
      city: l.city,
      locality: l.locality,
      pincode: l.pincode ?? '',
      address: l.address ?? '',
      contactPhone: l.contactPhone,
      fields,
    })
    setImages(l.images)
  }

  useEffect(() => {
    if (editingId === undefined) return
    api.listings
      .mine()
      .then((all) => {
        const existing = all.find((l) => l.id === editingId)
        if (!existing) {
          setError('Listing not found.')
          return
        }
        populateFromExisting(existing)
      })
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [editingId])

  const selectedCategory = categories.find((c) => c.id === form.categoryId)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setSaving(true)
    try {
      if (currentId) {
        await api.listings.update(currentId, form)
        toast.show('Changes saved')
      } else {
        const created = await api.listings.create(form)
        setCurrentId(created.id)
        navigate(`/listings/${created.id}/edit`, { replace: true })
        toast.show('Listing posted — now add some photos')
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not save listing')
    } finally {
      setSaving(false)
    }
  }

  async function handleUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file || !currentId) return
    setUploading(true)
    try {
      const img = await api.listings.addImage(currentId, file)
      setImages((prev) => [...prev, img])
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Image upload failed')
    } finally {
      setUploading(false)
      e.target.value = ''
    }
  }

  async function handleRemoveImage(imageId: number) {
    if (!currentId) return
    await api.listings.removeImage(currentId, imageId)
    setImages((prev) => prev.filter((i) => i.id !== imageId))
  }

  async function handleMoveImage(index: number, direction: -1 | 1) {
    if (!currentId) return
    const target = index + direction
    if (target < 0 || target >= images.length) return
    const next = [...images]
    const [moved] = next.splice(index, 1)
    next.splice(target, 0, moved)
    setImages(next)
    try {
      await api.listings.reorderImages(currentId, next.map((i) => i.id))
    } catch (err) {
      setImages(images) // revert on failure
      toast.show(err instanceof ApiError ? err.message : 'Could not reorder photos', 'error')
    }
  }

  if (loading) return <Spinner />

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="mb-6 font-display text-2xl font-bold text-ink">
        {currentId ? 'Edit listing' : 'Post a listing'}
      </h1>

      <form onSubmit={handleSubmit} className="flex flex-col gap-5">
        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Title
          <input
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
            maxLength={200}
            className="border border-line bg-white px-3 py-2"
          />
        </label>

        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Category
          <select
            value={form.categoryId || ''}
            onChange={(e) => setForm({ ...form, categoryId: Number(e.target.value), fields: {} })}
            required
            className="border border-line bg-white px-3 py-2"
          >
            <option value="">Select a category</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>

        <div className="grid grid-cols-2 gap-4">
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Listing type
            <select
              value={form.listingType}
              onChange={(e) => setForm({ ...form, listingType: e.target.value as 'RENT' | 'SALE' })}
              className="border border-line bg-white px-3 py-2"
            >
              <option value="RENT">For rent</option>
              <option value="SALE">For sale</option>
            </select>
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Price unit
            <select
              value={form.priceUnit}
              onChange={(e) => setForm({ ...form, priceUnit: e.target.value as typeof form.priceUnit })}
              className="border border-line bg-white px-3 py-2"
            >
              <option value="TOTAL">Total</option>
              <option value="PER_DAY">Per day</option>
              <option value="PER_MONTH">Per month</option>
              <option value="PER_YEAR">Per year</option>
            </select>
          </label>
        </div>

        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Price (₹)
          <input
            type="number"
            min={0}
            value={form.price || ''}
            onChange={(e) => setForm({ ...form, price: Number(e.target.value) })}
            required
            className="border border-line bg-white px-3 py-2"
          />
        </label>

        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Description
          <textarea
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            required
            rows={4}
            maxLength={3000}
            className="border border-line bg-white px-3 py-2"
          />
        </label>

        <div className="grid grid-cols-2 gap-4">
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            State
            <input
              value={form.state}
              onChange={(e) => setForm({ ...form, state: e.target.value })}
              required
              className="border border-line bg-white px-3 py-2"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            City
            <input
              value={form.city}
              onChange={(e) => setForm({ ...form, city: e.target.value })}
              required
              className="border border-line bg-white px-3 py-2"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Locality
            <input
              value={form.locality}
              onChange={(e) => setForm({ ...form, locality: e.target.value })}
              required
              className="border border-line bg-white px-3 py-2"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Pincode
            <input
              value={form.pincode}
              onChange={(e) => setForm({ ...form, pincode: e.target.value })}
              className="border border-line bg-white px-3 py-2"
            />
          </label>
        </div>

        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Address (not shown publicly)
          <input
            value={form.address}
            onChange={(e) => setForm({ ...form, address: e.target.value })}
            className="border border-line bg-white px-3 py-2"
          />
        </label>

        <label className="flex flex-col gap-1 text-sm font-medium text-ink">
          Contact number (must match your login number)
          <input
            value={form.contactPhone}
            onChange={(e) => setForm({ ...form, contactPhone: e.target.value })}
            required
            className="border border-line bg-white px-3 py-2"
          />
        </label>

        {selectedCategory && selectedCategory.fields.filter((f) => f.active).length > 0 && (
          <fieldset className="flex flex-col gap-4 border-t border-line pt-4">
            <legend className="mb-1 text-sm font-semibold text-ink">
              {selectedCategory.name} details
            </legend>
            {selectedCategory.fields
              .filter((f) => f.active)
              .map((f) => (
                <label key={f.id} className="flex flex-col gap-1 text-sm font-medium text-ink">
                  {f.name}
                  {f.required && <span className="text-brick"> *</span>}
                  <DynamicFieldInput
                    field={f}
                    value={form.fields[f.id] ?? ''}
                    onChange={(v) => setForm({ ...form, fields: { ...form.fields, [f.id]: v } })}
                  />
                </label>
              ))}
          </fieldset>
        )}

        {error && <p className="text-sm text-brick">{error}</p>}

        <button
          type="submit"
          disabled={saving}
          className="bg-ochre px-5 py-2.5 font-semibold text-ink hover:bg-ochre-dark disabled:opacity-60"
        >
          {saving ? 'Saving…' : currentId ? 'Save changes' : 'Post listing'}
        </button>
      </form>

      {currentId && (
        <div className="mt-8 border-t border-line pt-6">
          <h2 className="mb-3 font-display text-lg font-semibold text-ink">Photos</h2>
          <div className="flex flex-wrap gap-3">
            {images.map((img, index) => (
              <div key={img.id} className="group relative h-24 w-24 overflow-hidden rounded">
                <img src={img.url} alt="" className="h-full w-full object-cover" />
                {index === 0 && (
                  <span className="absolute left-1 top-1 bg-ink/80 px-1.5 text-[10px] text-paper">
                    Cover
                  </span>
                )}
                <button
                  type="button"
                  onClick={() => handleRemoveImage(img.id)}
                  className="absolute right-1 top-1 hidden bg-brick px-1.5 text-xs text-paper group-hover:block"
                >
                  Remove
                </button>
                <div className="absolute inset-x-0 bottom-0 hidden justify-center gap-1 bg-ink/70 py-0.5 group-hover:flex">
                  <button
                    type="button"
                    onClick={() => handleMoveImage(index, -1)}
                    disabled={index === 0}
                    className="px-1.5 text-xs text-paper disabled:opacity-30"
                  >
                    ◀
                  </button>
                  <button
                    type="button"
                    onClick={() => handleMoveImage(index, 1)}
                    disabled={index === images.length - 1}
                    className="px-1.5 text-xs text-paper disabled:opacity-30"
                  >
                    ▶
                  </button>
                </div>
              </div>
            ))}
            {images.length < 10 && (
              <label className="flex h-24 w-24 cursor-pointer items-center justify-center border border-dashed border-line text-xs text-ink-soft">
                {uploading ? 'Uploading…' : 'Add photo'}
                <input type="file" accept="image/*" onChange={handleUpload} className="hidden" />
              </label>
            )}
          </div>
          <p className="mt-2 text-xs text-ink-soft">
            Adding a photo sends an already-approved listing back for review.
          </p>
        </div>
      )}
    </div>
  )
}
