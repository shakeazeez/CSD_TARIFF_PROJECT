/**
 * Collapsible Component - shadcn/ui Collapsible Implementation
 *
 * A collapsible component that allows content to be expanded and collapsed.
 * Uses Radix UI primitives for accessibility and functionality.
 *
 * Features:
 * - Smooth animations with Framer Motion integration ready
 * - Accessible design with proper ARIA attributes
 * - Forward ref support for proper component composition
 * - Customizable trigger and content areas
 */

import * as React from "react"
import * as CollapsiblePrimitive from "@radix-ui/react-collapsible"

const Collapsible = CollapsiblePrimitive.Root

const CollapsibleTrigger = CollapsiblePrimitive.CollapsibleTrigger

const CollapsibleContent = CollapsiblePrimitive.CollapsibleContent

export { Collapsible, CollapsibleTrigger, CollapsibleContent }