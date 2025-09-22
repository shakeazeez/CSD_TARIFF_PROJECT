/**
 * Autocomplete Dropdown Component - Enhanced Search Implementation
 *
 * A searchable dropdown with proper background styling and autocomplete functionality.
 * Built with Radix UI Popover for better control over positioning and styling.
 * 
 * Features:
 * - Real-time search filtering
 * - Solid background with proper contrast
 * - Keyboard navigation support
 * - Click outside to close
 * - Maintains existing API compatibility
 * 
 * @param {Object} props
 * @param {string} props.title - Placeholder text shown in the dropdown.
 * @param {Array<{id: string, code: string}>} props.options - List of options (must have `id` and `code`).
 * @param {function} [props.onChange] - Callback fired when an option is selected.
 */

import * as React from "react"
import * as PopoverPrimitive from "@radix-ui/react-popover"
import { Check, ChevronDown, Search } from "lucide-react"
import { cn } from "../lib/utils"
import { Input } from "./ui/input"

const Dropdown = ({ title, options = [], onChange }) => {
  const [open, setOpen] = React.useState(false)
  const [searchValue, setSearchValue] = React.useState("")
  const [selectedValue, setSelectedValue] = React.useState("")
  const [selectedLabel, setSelectedLabel] = React.useState("")
  const [highlightedIndex, setHighlightedIndex] = React.useState(-1)
  const searchInputRef = React.useRef(null)

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
    setSelectedValue(option.code)
    setSelectedLabel(option.id)
    setOpen(false)
    setSearchValue("")
    setHighlightedIndex(-1)
    
    if (onChange) {
      onChange(option)
    }
  }

  // Reset search when dropdown closes
  const handleOpenChange = (newOpen) => {
    setOpen(newOpen)
    if (!newOpen) {
      setSearchValue("")
      setHighlightedIndex(-1)
    } else {
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

  // Reset highlighted index when filtered options change
  React.useEffect(() => {
    setHighlightedIndex(-1)
  }, [filteredOptions])

  return (
    <div className="w-full">
      <PopoverPrimitive.Root open={open} onOpenChange={handleOpenChange}>
        <PopoverPrimitive.Trigger asChild>
          <button
            className={cn(
              "flex h-10 w-full items-center justify-between rounded-md border border-input bg-white/50 dark:bg-gray-800/50 px-3 py-2 text-sm ring-offset-background",
              "placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
              "disabled:cursor-not-allowed disabled:opacity-50 [&>span]:line-clamp-1"
            )}
          >
            <span className={selectedLabel ? "text-foreground" : "text-muted-foreground"}>
              {selectedLabel || title}
            </span>
            <ChevronDown className="h-4 w-4 opacity-50" />
          </button>
        </PopoverPrimitive.Trigger>
        
        <PopoverPrimitive.Portal>
          <PopoverPrimitive.Content
            className={cn(
              "z-50 w-[var(--radix-popover-trigger-width)] rounded-md border bg-white dark:bg-gray-800 p-0 text-gray-900 dark:text-gray-100 shadow-lg",
              "data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95",
              "data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2"
            )}
            side="bottom"
            align="start"
            sideOffset={4}
          >
            <div className="p-2 border-b border-gray-200 dark:border-gray-700">
              <div className="relative">
                <Search className="absolute left-2 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                <Input
                  ref={searchInputRef}
                  placeholder="Search countries..."
                  value={searchValue}
                  onChange={(e) => setSearchValue(e.target.value)}
                  onKeyDown={handleKeyDown}
                  className="pl-8 h-8 bg-gray-50 dark:bg-gray-700 border-gray-200 dark:border-gray-600"
                />
              </div>
            </div>
            
            <div className="max-h-60 overflow-auto p-1">
              {filteredOptions.length === 0 ? (
                <div className="py-2 px-3 text-sm text-gray-500 dark:text-gray-400 text-center">
                  No countries found
                </div>
              ) : (
                filteredOptions.map((option, index) => (
                  <button
                    key={option.code}
                    onClick={() => handleSelect(option)}
                    className={cn(
                      "relative flex w-full cursor-pointer select-none items-center rounded-sm py-2 pl-8 pr-2 text-sm outline-none",
                      "hover:bg-gray-100 dark:hover:bg-gray-700 focus:bg-gray-100 dark:focus:bg-gray-700",
                      (selectedValue === option.code || highlightedIndex === index) && "bg-blue-100 dark:bg-blue-900/30"
                    )}
                    onMouseEnter={() => setHighlightedIndex(index)}
                  >
                    <span className="absolute left-2 flex h-3.5 w-3.5 items-center justify-center">
                      {selectedValue === option.code && (
                        <Check className="h-4 w-4 text-blue-600 dark:text-blue-400" />
                      )}
                    </span>
                    <span className="block truncate">{option.id}</span>
                    <span className="ml-auto text-xs text-gray-500 dark:text-gray-400">
                      {option.code}
                    </span>
                  </button>
                ))
              )}
            </div>
          </PopoverPrimitive.Content>
        </PopoverPrimitive.Portal>
      </PopoverPrimitive.Root>
    </div>
  )
}

export default Dropdown