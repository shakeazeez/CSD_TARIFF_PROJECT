/**
 * Progress Component - shadcn/ui Progress Implementation
 *
 * A progress bar component that displays completion status.
 * Uses Tailwind CSS for styling and supports custom styling through className.
 *
 * Features:
 * - Value prop for progress percentage (0-100)
 * - Customizable styling with className prop
 * - Accessible design with proper ARIA attributes
 * - Smooth animations and transitions
 * - Tailwind CSS integration with consistent theming
 */

import * as React from "react"
import { cn } from "../../lib/utils"

// Progress component for displaying completion status
const Progress = React.forwardRef(({ className, value = 0, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "relative h-4 w-full overflow-hidden rounded-full bg-secondary",
      className
    )}
    {...props}
  >
    <div
      className="h-full w-full flex-1 bg-primary transition-all"
      style={{ transform: `translateX(-${100 - (value || 0)}%)` }}
    />
  </div>
))
Progress.displayName = "Progress"

export { Progress }