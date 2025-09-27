/**
 * Label Component - shadcn/ui Form Label
 * 
 * A semantic label component for form fields with consistent typography
 * and accessibility features. Provides proper association with form controls
 * and handles disabled states elegantly.
 * 
 * Features:
 * - Semantic HTML label element for proper accessibility
 * - Consistent typography with medium font weight
 * - Peer-disabled state handling (when associated input is disabled)
 * - Forward ref support for form library integration
 * - Uses class-variance-authority for variant management
 * 
 * Usage:
 * <Label htmlFor="email">Email Address</Label>
 * <Input id="email" type="email" />
 */

import * as React from "react"
import { cva } from "class-variance-authority"
import { cn } from "../../lib/utils"

// Label variant definitions using class-variance-authority
// Defines consistent styling for all label instances
const labelVariants = cva(
  // Base styles with proper typography and accessibility features
  "text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
)

// Label component with semantic HTML and accessibility features
const Label = React.forwardRef(({ className, ...props }, ref) => (
  <label
    ref={ref}
    className={cn(labelVariants(), className)}
    {...props}
  />
))
Label.displayName = "Label"

export { Label }