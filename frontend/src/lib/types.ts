// Mirrors the backend DTOs exactly (com.rentivo.backend.*.dto.*). Keep in sync by hand —
// there is no shared schema between the two repos yet.

export type Role = 'USER' | 'PROVIDER' | 'ADMIN'

export type ListingType = 'RENT' | 'SALE'
export type PriceUnit = 'TOTAL' | 'PER_DAY' | 'PER_MONTH' | 'PER_YEAR'
export type ListingStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'DELETED' | 'EXPIRED'
export type FieldType =
  | 'TEXT'
  | 'NUMBER'
  | 'DECIMAL'
  | 'BOOLEAN'
  | 'DATE'
  | 'SELECT'
  | 'MULTI_SELECT'
  | 'TEXTAREA'

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface AuthResponse {
  token: string
  userId: number
  name: string
  phone: string
  role: Role
}

export interface SendOtpResponse {
  message: string
  developmentOtp: string | null
}

export interface CategoryField {
  id: number
  name: string
  type: FieldType
  required: boolean
  options: string[]
  sortOrder: number
  active: boolean
}

export interface Category {
  id: number
  name: string
  active: boolean
  systemCategory: boolean
  fields: CategoryField[]
}

export interface ImageResponse {
  id: number
  url: string
  sortOrder: number
}

export interface FieldValueResponse {
  fieldId: number
  name: string
  value: string
}

export interface CategoryRef {
  id: number
  name: string
}

export interface ListingSummary {
  id: number
  title: string
  category: string
  listingType: ListingType
  price: number
  priceUnit: PriceUnit
  city: string
  locality: string
  thumbnailUrl: string | null
  createdAt: string
}

export interface PublicListing {
  id: number
  title: string
  category: CategoryRef
  listingType: ListingType
  price: number
  priceUnit: PriceUnit
  description: string
  state: string
  city: string
  locality: string
  pincode: string | null
  latitude: number | null
  longitude: number | null
  images: ImageResponse[]
  fields: FieldValueResponse[]
  contactLocked: boolean
  expiresAt: string | null
  createdAt: string
}

export interface OwnedListing {
  id: number
  ownerId: number
  title: string
  category: CategoryRef
  listingType: ListingType
  price: number
  priceUnit: PriceUnit
  description: string
  state: string
  city: string
  locality: string
  pincode: string | null
  address: string | null
  latitude: number | null
  longitude: number | null
  contactPhone: string
  status: ListingStatus
  rejectionReason: string | null
  images: ImageResponse[]
  fields: FieldValueResponse[]
  expiresAt: string | null
  createdAt: string
  updatedAt: string | null
}

export interface CreateListingRequest {
  title: string
  categoryId: number
  listingType: ListingType
  price: number
  priceUnit: PriceUnit
  description: string
  state: string
  city: string
  locality: string
  pincode?: string
  address?: string
  latitude?: number
  longitude?: number
  contactPhone: string
  fields: Record<number, string>
}

export interface SubscriptionPlan {
  id: number
  name: string
  price: number
  validityDays: number
  contactLimit: number
  description: string | null
  active: boolean
}

export interface SubscriptionStatus {
  active: boolean
  planName: string | null
  startAt: string | null
  endAt: string | null
  contactLimit: number
  contactsUsed: number
  contactsRemaining: number
  message: string
}

export interface ContactResponse {
  unlocked: boolean
  ownerName: string
  ownerPhone: string
  contactsUsed: number
  contactsRemaining: number
  message: string
}

export type NotificationType = 'LISTING_APPROVED' | 'LISTING_REJECTED' | 'CONTACT_UNLOCKED'

export interface AppNotification {
  id: number
  type: NotificationType
  title: string
  body: string
  listingId: number | null
  read: boolean
  createdAt: string
}

export interface FavoriteStatus {
  favorited: boolean
}

export interface FieldRequest {
  id?: number
  name: string
  type: FieldType
  required: boolean
  optionsCsv?: string
  sortOrder?: number
}

export interface CategoryRequest {
  name: string
  active?: boolean
  fields?: FieldRequest[]
}

// ---- admin ----

export interface AdminUser {
  id: number
  name: string
  phone: string
  role: Role
  active: boolean
  phoneVerified: boolean
  createdAt: string
}

export interface AdminDashboard {
  totalUsers: number
  activeUsers: number
  totalListings: number
  pendingListings: number
  approvedListings: number
  rejectedListings: number
  subscriptionPlans: number
  activeSubscriptions: number
  successfulPayments: number
  revenue: number
}

export interface PlanRequest {
  name: string
  price: number
  validityDays: number
  contactLimit: number
  description?: string
  active?: boolean
}

export interface AuditLogEntry {
  id: number
  adminId: number
  adminPhone: string
  action: string
  targetType: string
  targetId: number | null
  detail: string | null
  createdAt: string
}

export interface ListingFilter {
  categoryId?: number
  city?: string
  type?: ListingType
  minPrice?: number
  maxPrice?: number
  q?: string
  page?: number
  size?: number
}
