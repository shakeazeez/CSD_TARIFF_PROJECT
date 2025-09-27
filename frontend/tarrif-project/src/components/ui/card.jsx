/**
 * Card Component System - shadcn/ui Card Components
 * 
 * A comprehensive set of card components for building structured content layouts.
 * Provides semantic HTML structure with consistent styling and proper accessibility.
 * 
 * Components included:
 * - Card: Main container with rounded borders and shadow
 * - CardHeader: Top section for titles and descriptions
 * - CardTitle: Semantic heading element for card titles
 * - CardDescription: Subtitle or description text
 * - CardContent: Main content area with appropriate spacing
 * - CardFooter: Bottom section for actions or metadata
 * 
 * All components support forwardRef for proper component composition and ref handling.
 */

import * as React from "react"
import { cn } from "../../lib/utils"

// Main Card container - provides base styling and structure
const Card = React.forwardRef(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "rounded-lg border bg-card text-card-foreground shadow-sm",
      className
    )}
    {...props}
  />
))
Card.displayName = "Card"

// Card header section - typically contains title and description
const CardHeader = React.forwardRef(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex flex-col space-y-1.5 p-6", className)}
    {...props}
  />
))
CardHeader.displayName = "CardHeader"

// Card title - semantic h3 element with appropriate typography
const CardTitle = React.forwardRef(({ className, ...props }, ref) => (
  <h3
    ref={ref}
    className={cn(
      "text-2xl font-semibold leading-none tracking-tight",
      className
    )}
    {...props}
  />
))
CardTitle.displayName = "CardTitle"

// Card description - supporting text with muted styling
const CardDescription = React.forwardRef(({ className, ...props }, ref) => (
  <p
    ref={ref}
    className={cn("text-sm text-muted-foreground", className)}
    {...props}
  />
))
CardDescription.displayName = "CardDescription"

// Card content area - main content with appropriate padding
const CardContent = React.forwardRef(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />
))
CardContent.displayName = "CardContent"

// Card footer - typically for actions or metadata
const CardFooter = React.forwardRef(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex items-center p-6 pt-0", className)}
    {...props}
  />
))
CardFooter.displayName = "CardFooter"

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent }