package com.breez.notes.data.mapper

import com.breez.notes.data.local.entity.AttachmentEntity
import com.breez.notes.data.local.entity.NoteEntity
import com.breez.notes.data.local.entity.NoteWithAttachments
import com.breez.notes.domain.model.AttachmentType
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.model.NoteAttachment
import com.breez.notes.domain.model.Recurrence

fun NoteWithAttachments.toDomain(): Note = note.toDomain(attachments.map { it.toDomain() })

fun NoteEntity.toDomain(attachments: List<NoteAttachment> = emptyList()): Note = Note(
    id = id,
    title = title,
    body = body,
    folderId = folderId,
    colorHex = colorHex,
    isPinned = isPinned,
    sortOrder = sortOrder,
    reminderAt = reminderAt,
    meetingPlace = meetingPlace.orEmpty(),
    meetingLat = meetingLat,
    meetingLng = meetingLng,
    locationReminder = locationReminder,
    recurrence = Recurrence.from(repeatUnit, repeatInterval, repeatWeekDays, repeatUntilAt),
    attachments = attachments,
    isChecklist = isChecklist,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    body = body,
    folderId = folderId,
    colorHex = colorHex,
    isPinned = isPinned,
    sortOrder = sortOrder,
    reminderAt = reminderAt,
    meetingPlace = meetingPlace.ifBlank { null },
    meetingLat = meetingLat,
    meetingLng = meetingLng,
    locationReminder = locationReminder,
    repeatUnit = recurrence.unit.name,
    repeatInterval = recurrence.interval,
    repeatWeekDays = recurrence.encodeWeekDays(),
    repeatUntilAt = recurrence.untilAt,
    isChecklist = isChecklist,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AttachmentEntity.toDomain(): NoteAttachment = NoteAttachment(
    id = id,
    noteId = noteId,
    type = runCatching { AttachmentType.valueOf(type) }.getOrDefault(AttachmentType.PHOTO),
    fileName = fileName,
    mimeType = mimeType,
    ocrText = ocrText,
    createdAt = createdAt
)

fun NoteAttachment.toEntity(): AttachmentEntity = AttachmentEntity(
    id = id,
    noteId = noteId,
    type = type.name,
    fileName = fileName,
    mimeType = mimeType,
    ocrText = ocrText,
    createdAt = createdAt
)
