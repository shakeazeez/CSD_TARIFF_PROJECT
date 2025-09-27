/**
 * Select Component System - shadcn/ui Select/Dropdown Components
 * 
 * A comprehensive select/dropdown component built on Radix UI primitives.
 * Provides accessible, keyboard-navigable dropdowns with consistent styling.
 * 
 * Components included:
 * - Select: Root container with state management
 * - SelectTrigger: Clickable button that opens the dropdown
 * - SelectValue: Displays the selected value or placeholder
 * - SelectContent: Dropdown menu container with positioning
 * - SelectItem: Individual selectable options
 * - SelectSeparator: Visual separator between groups
 * - SelectLabel: Section labels within dropdown
 * 
 * Features:
 * - Full keyboard navigation support
 * - Screen reader accessible
 * - Customizable styling with Tailwind CSS
 * - Support for icons and complex content
 * - Portal rendering for proper z-index layering
 */

import * as React from "react"
import * as SelectPrimitive from "@radix-ui/react-select"
import { Check, ChevronDown, ChevronUp } from "lucide-react"
import { cn } from "../../lib/utils"

// Root Select component - manages state and provides context
const Select = SelectPrimitive.Root

// SelectGroup for organizing related options
const SelectGroup = SelectPrimitive.Group

// SelectValue displays the selected value
const SelectValue = SelectPrimitive.Value

// SelectTrigger - the clickable button that opens the dropdown
const SelectTrigger = React.forwardRef(({ className, children, ...props }, ref) => (
  <SelectPrimitive.Trigger
    ref={ref}
    className={cn(
      // Base layout and sizing
      "flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2",
      // Typography and text styling
      "text-sm placeholder:text-muted-foreground",
      // Focus states for accessibility
      "focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
      // Disabled state styling
      "disabled:cursor-not-allowed disabled:opacity-50",
      // Ensure proper spacing for icon
      "[&>span]:line-clamp-1",
      className
    )}
    {...props}
  >
    {children}
    <SelectPrimitive.Icon asChild>
      <ChevronDown className="h-4 w-4 opacity-50" />
    </SelectPrimitive.Icon>
  </SelectPrimitive.Trigger>
))
SelectTrigger.displayName = SelectPrimitive.Trigger.displayName

// SelectScrollUpButton for long lists
const SelectScrollUpButton = React.forwardRef(({ className, ...props }, ref) => (
  <SelectPrimitive.ScrollUpButton
    ref={ref}
    className={cn(
      "flex cursor-default items-center justify-center py-1",
      className
    )}
    {...props}
  >
    <ChevronUp className="h-4 w-4" />
  </SelectPrimitive.ScrollUpButton>
))
SelectScrollUpButton.displayName = SelectPrimitive.ScrollUpButton.displayName

// SelectScrollDownButton for long lists
const SelectScrollDownButton = React.forwardRef(({ className, ...props }, ref) => (
  <SelectPrimitive.ScrollDownButton
    ref={ref}
    className={cn(
      "flex cursor-default items-center justify-center py-1",
      className
    )}
    {...props}
  >
    <ChevronDown className="h-4 w-4" />
  </SelectPrimitive.ScrollDownButton>
))
SelectScrollDownButton.displayName = SelectPrimitive.ScrollDownButton.displayName

// SelectContent - the dropdown menu container
const SelectContent = React.forwardRef(({ className, children, position = "popper", ...props }, ref) => (
  <SelectPrimitive.Portal>
    <SelectPrimitive.Content
      ref={ref}
      className={cn(
        // Base container styling with solid background
        "relative z-50 max-h-96 min-w-[8rem] overflow-hidden rounded-md border bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 shadow-lg backdrop-blur-sm",
        // Animation classes
        "data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95",
        // Position-specific animations
        position === "popper" &&
          "data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2",
        className
      )}
      position={position}
      {...props}
    >
      <SelectScrollUpButton />
      <SelectPrimitive.Viewport
        className={cn(
          "p-1",
          position === "popper" &&
            "h-[var(--radix-select-trigger-height)] w-full min-w-[var(--radix-select-trigger-width)]"
        )}
      >
        {children}
      </SelectPrimitive.Viewport>
      <SelectScrollDownButton />
    </SelectPrimitive.Content>
  </SelectPrimitive.Portal>
))
SelectContent.displayName = SelectPrimitive.Content.displayName

// SelectLabel - for grouping and labeling sections
const SelectLabel = React.forwardRef(({ className, ...props }, ref) => (
  <SelectPrimitive.Label
    ref={ref}
    className={cn("py-1.5 pl-8 pr-2 text-sm font-semibold", className)}
    {...props}
  />
))
SelectLabel.displayName = SelectPrimitive.Label.displayName

// SelectItem - individual selectable options
const SelectItem = React.forwardRef(({ className, children, ...props }, ref) => (
  <SelectPrimitive.Item
    ref={ref}
    className={cn(
      // Base layout and spacing
      "relative flex w-full cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none",
      // Interactive states
      "focus:bg-accent focus:text-accent-foreground",
      // Disabled state
      "data-[disabled]:pointer-events-none data-[disabled]:opacity-50",
      className
    )}
    {...props}
  >
    {/* Check icon for selected state */}
    <span className="absolute left-2 flex h-3.5 w-3.5 items-center justify-center">
      <SelectPrimitive.ItemIndicator>
        <Check className="h-4 w-4" />
      </SelectPrimitive.ItemIndicator>
    </span>

    <SelectPrimitive.ItemText>{children}</SelectPrimitive.ItemText>
  </SelectPrimitive.Item>
))
SelectItem.displayName = SelectPrimitive.Item.displayName

// SelectSeparator - visual divider between sections
const SelectSeparator = React.forwardRef(({ className, ...props }, ref) => (
  <SelectPrimitive.Separator
    ref={ref}
    className={cn("-mx-1 my-1 h-px bg-muted", className)}
    {...props}
  />
))
SelectSeparator.displayName = SelectPrimitive.Separator.displayName

export {
  Select,
  SelectGroup,
  SelectValue,
  SelectTrigger,
  SelectContent,
  SelectLabel,
  SelectItem,
  SelectSeparator,
  SelectScrollUpButton,
  SelectScrollDownButton,
}