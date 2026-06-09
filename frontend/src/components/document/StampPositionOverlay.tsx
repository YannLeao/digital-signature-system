import { useEffect, useMemo, useState, type PointerEvent } from 'react'

import type { PdfPageSize, StampPosition } from '../../types/document'
import { SignatureStampPreview } from './SignatureStampPreview'

const stampPdfWidth = 310
const stampPdfHeight = 70

type StampPositionOverlayProps = {
  currentPage: number
  pageSize: PdfPageSize
  position: StampPosition | null
  scale: number
  signerEmail: string | null
  onPositionChange: (position: StampPosition) => void
}

export function StampPositionOverlay({
  currentPage,
  pageSize,
  position,
  scale,
  signerEmail,
  onPositionChange,
}: StampPositionOverlayProps) {
  const [dragging, setDragging] = useState(false)
  const stampSize = useMemo(
    () => ({
      height: stampPdfHeight * scale,
      width: stampPdfWidth * scale,
    }),
    [scale],
  )

  useEffect(() => {
    if (!position || position.page !== currentPage) {
      const x = Math.max(5, Math.min(80, pageSize.width - stampPdfWidth + 5))
      const y = Math.max(55, Math.min(150, pageSize.height - 15))
      onPositionChange({
        page: currentPage,
        x,
        y,
      })
    }
  }, [currentPage, onPositionChange, pageSize.height, pageSize.width, position])

  const overlayPosition = useMemo(() => {
    if (!position || position.page !== currentPage) {
      return {
        left: 0,
        top: 0,
      }
    }

    return {
      left: (position.x - 5) * scale,
      top: (pageSize.height - position.y - 15) * scale,
    }
  }, [currentPage, pageSize.height, position, scale])

  function updateFromPointer(event: PointerEvent<HTMLDivElement>) {
    const bounds = event.currentTarget.getBoundingClientRect()
    const left = clamp(
      event.clientX - bounds.left - stampSize.width / 2,
      0,
      bounds.width - stampSize.width,
    )
    const top = clamp(
      event.clientY - bounds.top - stampSize.height / 2,
      0,
      bounds.height - stampSize.height,
    )

    onPositionChange({
      page: currentPage,
      x: roundCoordinate(left / scale + 5),
      y: roundCoordinate(pageSize.height - top / scale - 15),
    })
  }

  return (
    <div
      aria-label="Area de posicionamento do selo"
      className="absolute inset-0 cursor-crosshair"
      onPointerDown={(event) => {
        setDragging(true)
        updateFromPointer(event)
      }}
      onPointerMove={(event) => {
        if (dragging) {
          updateFromPointer(event)
        }
      }}
      onPointerUp={() => setDragging(false)}
      role="application"
    >
      <div
        className="absolute cursor-move select-none"
        style={{
          height: stampSize.height,
          left: overlayPosition.left,
          top: overlayPosition.top,
          width: stampSize.width,
        }}
      >
        <SignatureStampPreview signerEmail={signerEmail} />
      </div>
    </div>
  )
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max)
}

function roundCoordinate(value: number): number {
  return Math.round(value * 100) / 100
}
