/**
 * MultiSelect Dropdown Component - Enhanced Search Implementation with Custom Theme
 *
 * A searchable multi-select dropdown with custom theme integration and proper background styling.
 * Built with Radix UI Popover for better control over positioning and styling.
 *
 * Features:
 * - Real-time search filtering
 * - Custom theme color integration
 * - Solid background with proper contrast
 * - Keyboard navigation support
 * - Click outside to close
 * - Maintains existing API compatibility
 * - Multiple selection with chips
 *
 * @param {Object} props
 * @param {string} props.title - Placeholder text shown in the dropdown.
 * @param {Array<{id: string, code: string}>} props.options - List of options (must have `id` and `code`).
 * @param {function} [props.onChange] - Callback fired when selections change.
 * @param {Array<string>} [props.value] - Array of selected codes.
 */

import * as React from "react"
import * as PopoverPrimitive from "@radix-ui/react-popover"
import { Check, ChevronDown, Search, X } from "lucide-react"
import { cn } from "../lib/utils"
import { Input } from "./ui/input"
import { useTheme } from "../contexts/use-theme.js"

const MultiSelect = ({ title, options = [], onChange, value = [] }) => {
  // Get theme colors for custom styling
  const { colors } = useTheme()

  const [open, setOpen] = React.useState(false)
  const [searchValue, setSearchValue] = React.useState("")
  const [selectedValues, setSelectedValues] = React.useState(value || [])
  const [highlightedIndex, setHighlightedIndex] = React.useState(-1)
  const [dropdownSide, setDropdownSide] = React.useState("bottom")
  const searchInputRef = React.useRef(null)
  const triggerRef = React.useRef(null)

  // Update selected values when value prop changes
  React.useEffect(() => {
    setSelectedValues(value || [])
  }, [value])

  // Filter options based on search input
  const filteredOptions = React.useMemo(() => {
    if (!searchValue) return options
    return options.filter(option =>
      option.id.toLowerCase().includes(searchValue.toLowerCase()) ||
      option.code.toLowerCase().includes(searchValue.toLowerCase())
    )
  }, [options, searchValue])

  // Handle option selection
  const handleSelect = (option) => {
    let newSelected
    if (selectedValues.includes(option.code)) {
      // Remove if already selected
      newSelected = selectedValues.filter(code => code !== option.code)
    } else {
      // Add if not selected
      newSelected = [...selectedValues, option.code]
    }
    setSelectedValues(newSelected)
    setSearchValue("")
    setHighlightedIndex(-1)

    if (onChange) {
      onChange(newSelected)
    }
  }

  // Remove selected item
  const removeSelected = (code) => {
    const newSelected = selectedValues.filter(c => c !== code)
    setSelectedValues(newSelected)
    if (onChange) {
      onChange(newSelected)
    }
  }

  // Reset search when dropdown closes
  const handleOpenChange = (newOpen) => {
    setOpen(newOpen)
    if (!newOpen) {
      setSearchValue("")
      setHighlightedIndex(-1)
    } else {
      // Calculate optimal dropdown position
      if (triggerRef.current) {
        const rect = triggerRef.current.getBoundingClientRect()
        const viewportHeight = window.innerHeight
        const dropdownHeight = 300 // Estimated dropdown height
        const spaceBelow = viewportHeight - rect.bottom
        const spaceAbove = rect.top

        if (spaceBelow < dropdownHeight && spaceAbove > spaceBelow) {
          setDropdownSide("top")
        } else {
          setDropdownSide("bottom")
        }
      }

      // Focus search input when opening
      setTimeout(() => {
        searchInputRef.current?.focus()
      }, 0)
    }
  }

  // Handle keyboard navigation
  const handleKeyDown = (e) => {
    if (!open) return

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault()
        setHighlightedIndex(prev =>
          prev < filteredOptions.length - 1 ? prev + 1 : 0
        )
        break
      case 'ArrowUp':
        e.preventDefault()
        setHighlightedIndex(prev =>
          prev > 0 ? prev - 1 : filteredOptions.length - 1
        )
        break
      case 'Enter':
        e.preventDefault()
        if (highlightedIndex >= 0 && filteredOptions[highlightedIndex]) {
          handleSelect(filteredOptions[highlightedIndex])
        }
        break
      case 'Escape':
        setOpen(false)
        break
    }
  }

  // Get display names for selected values
  const selectedLabels = selectedValues.map(code => {
    const option = options.find(opt => opt.code === code)
    return option ? option.id : code
  })

  return (
    <PopoverPrimitive.Root open={open} onOpenChange={handleOpenChange}>
      <PopoverPrimitive.Trigger asChild>
        <button
          ref={triggerRef}
          type="button"
          className={cn(
            "flex min-h-10 w-full items-center justify-between rounded-md border bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
            "transition-colors duration-300"
          )}
          style={{
            backgroundColor: colors.input,
            borderColor: colors.border,
            color: colors.foreground
          }}
        >
          <div className="flex flex-wrap gap-1 flex-1">
            {selectedLabels.length > 0 ? (
              selectedLabels.map((label, index) => (
                <span
                  key={selectedValues[index]}
                  className="inline-flex items-center gap-1 rounded-md px-2 py-1 text-xs"
                  style={{
                    backgroundColor: colors.accent,
                    color: 'white'
                  }}
                >
                  {label}
                  <X
                    className="h-3 w-3 cursor-pointer hover:text-destructive"
                    onClick={(e) => {
                      e.stopPropagation()
                      removeSelected(selectedValues[index])
                    }}
                  />
                </span>
              ))
            ) : (
              <span className="text-muted-foreground">{title}</span>
            )}
          </div>
          <ChevronDown className="h-4 w-4 opacity-50" />
        </button>
      </PopoverPrimitive.Trigger>
      <PopoverPrimitive.Content
        className="w-full p-0 z-50"
        side={dropdownSide}
        align="start"
        style={{
          backgroundColor: colors.surface,
          borderColor: colors.border,
          color: colors.foreground,
          boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)'
        }}
        onKeyDown={handleKeyDown}
      >
        <div className="p-2">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 opacity-50" />
            <Input
              ref={searchInputRef}
              placeholder="Search..."
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              className="pl-9"
              style={{
                backgroundColor: colors.input,
                borderColor: colors.border,
                color: colors.foreground
              }}
            />
          </div>
        </div>
        <div className="max-h-64 overflow-y-auto">
          {filteredOptions.length > 0 ? (
            filteredOptions.map((option, index) => (
              <div
                key={option.code}
                className={cn(
                  "flex items-center px-3 py-2 cursor-pointer hover:bg-accent",
                  highlightedIndex === index && "bg-accent",
                  selectedValues.includes(option.code) && "bg-accent"
                )}
                style={{
                  color: colors.foreground
                }}
                onClick={() => handleSelect(option)}
              >
                <Check
                  className={cn(
                    "mr-2 h-4 w-4",
                    selectedValues.includes(option.code) ? "opacity-100" : "opacity-0"
                  )}
                />
                {option.id}
              </div>
            ))
          ) : (
            <div className="px-3 py-2 text-muted-foreground">No options found</div>
          )}
        </div>
      </PopoverPrimitive.Content>
    </PopoverPrimitive.Root>
  )
}

export default MultiSelect