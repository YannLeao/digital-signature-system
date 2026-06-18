import { z } from 'zod'

const metadataSchema = z
  .union([z.record(z.string(), z.unknown()), z.string()])
  .nullable()
  .optional()
  .transform((metadata) => {
    if (!metadata) {
      return null
    }

    if (typeof metadata !== 'string') {
      return metadata
    }

    try {
      const parsed: unknown = JSON.parse(metadata)
      return z.record(z.string(), z.unknown()).catch({}).parse(parsed)
    } catch {
      return {}
    }
  })

export const activeSessionSchema = z.object({
  sessionId: z.uuid(),
  deviceInfo: z.string().catch('Dispositivo'),
  ip: z.string().catch('IP indisponivel'),
  userAgent: z.string().nullable().catch(null),
  createdAt: z.string().min(1),
  lastSeenAt: z.string().min(1),
})

export const activeSessionsSchema = z.array(activeSessionSchema)

export const activityLogEntrySchema = z.object({
  id: z.uuid(),
  timestampUtc: z.string().min(1),
  ip: z.string().nullable().catch(null),
  userAgent: z.string().nullable().catch(null),
  action: z.string().min(1),
  result: z.string().min(1),
  metadata: metadataSchema,
})

const springPageSchema = z.object({
  content: z.array(activityLogEntrySchema),
  number: z.number().int().nonnegative().catch(0),
  size: z.number().int().positive().catch(10),
  totalElements: z.number().int().nonnegative().catch(0),
  totalPages: z.number().int().nonnegative().catch(0),
})

const customPageSchema = z.object({
  items: z.array(activityLogEntrySchema),
  page: z.number().int().nonnegative().catch(0),
  size: z.number().int().positive().catch(10),
  totalElements: z.number().int().nonnegative().catch(0),
  totalPages: z.number().int().nonnegative().catch(0),
})

export const activityLogPageSchema = z
  .union([springPageSchema, customPageSchema, z.array(activityLogEntrySchema)])
  .transform((value) => {
    if (Array.isArray(value)) {
      return {
        items: value,
        page: 0,
        size: value.length || 10,
        totalElements: value.length,
        totalPages: value.length > 0 ? 1 : 0,
      }
    }

    if ('content' in value) {
      return {
        items: value.content,
        page: value.number,
        size: value.size,
        totalElements: value.totalElements,
        totalPages: value.totalPages,
      }
    }

    return value
  })

export const securityActionResponseSchema = z.object({
  message: z.string().min(1).catch('Acao concluida.'),
})
