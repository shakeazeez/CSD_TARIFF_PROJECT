/**
 * Input Component - shadcn/ui Input Field
 * 
 * A styled input component with consistent design and accessibility features.
 * Provides a clean, modern input field that integrates with form libraries
 * and supports all standard HTML input types.
 * 
 * Features:
 * - Consistent styling with rounded borders and focus states
 * - Proper accessibility with focus-visible indicators
 * - Support for all HTML input types (text, email, password, number, etc.)
 * - File input styling for upload fields
 * - Disabled state handling
 * - Forward ref support for form library integration
 * - Customizable through className prop
 */

import * as React from "react"
import { cn } from "../../lib/utils"

// Input component with comprehensive styling and accessibility
const Input = React.forwardRef(({ className, type, ...props }, ref) => {
  return (
    <input
      type={type}
      className={cn(
        // Base styles for layout and typography
        "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background",
        // File input specific styling  
        "file:border-0 file:bg-transparent file:text-sm file:font-medium",
        // Placeholder styling
        "placeholder:text-muted-foreground",
        // Focus state with ring indicator for accessibility
        "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2",
        // Disabled state styling
        "disabled:cursor-not-allowed disabled:opacity-50",
        className
      )}
      ref={ref}
      {...props}
    />
  )
})
Input.displayName = "Input"

export { Input }