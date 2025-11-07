/**
 * Button Component - shadcn/ui Button Implementation
 * 
 * A versatile button component with multiple variants and sizes.
 * Uses class-variance-authority (CVA) for variant management and Tailwind CSS for styling.
 * Supports different visual styles (default, outline, destructive, etc.) and sizes.
 * 
 * Features:
 * - Multiple variants: default, destructive, outline, secondary, ghost, link
 * - Multiple sizes: default, sm, lg, icon
 * - Forward ref support for proper component composition
 * - Accessible design with focus states and disabled states
 * - Tailwind CSS integration with consistent theming
 */

import * as React from "react"
import { cn } from "../../lib/utils"
import { buttonVariants } from "./button-variants"

// Button component with forwardRef for proper ref handling
const Button = React.forwardRef(({ className, variant, size, asChild = false, ...props }, ref) => {
  // Use span if asChild is true (for composition), otherwise use button element
  const Comp = asChild ? "span" : "button"
  return (
    <Comp
      className={cn(buttonVariants({ variant, size, className }))}
      ref={ref}
      {...props}
    />
  )
})
Button.displayName = "Button"

export { Button }