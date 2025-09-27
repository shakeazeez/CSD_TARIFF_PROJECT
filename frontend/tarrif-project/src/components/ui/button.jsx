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
import { cva } from "class-variance-authority"
import { cn } from "../../lib/utils"

// Button variant definitions using class-variance-authority
// Defines base styles and variants for different button types
const buttonVariants = cva(
  // Base classes applied to all button variants
  "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      // Visual style variants
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive:
          "bg-destructive text-destructive-foreground hover:bg-destructive/90",
        outline:
          "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
        secondary:
          "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
      },
      // Size variants for different use cases
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
        icon: "h-10 w-10",
      },
    },
    // Default values when no variant/size is specified
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

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

export { Button, buttonVariants }