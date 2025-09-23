/**
 * Autocomplete Dropdown Component - Enhanced Search Implementation with Custom Theme
 *
 * A searchable dropdown with custom theme integration and proper background styling.
 * Built with Radix UI Popover for better control over positioning and styling.
 * 
 * Features:
 * - Real-time search filtering
 * - Custom theme color integration
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
import { useTheme } from "../contexts/ThemeContext.jsx"

const Dropdown = ({ title, options = [], onChange }) => {
  // Get theme colors for custom styling
  const { colors } = useTheme()
  
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
              "flex h-10 w-full items-center justify-between rounded-md border px-3 py-2 text-sm",
              "focus:outline-none focus:ring-2 focus:ring-offset-2 transition-all duration-300",
              "disabled:cursor-not-allowed disabled:opacity-50 [&>span]:line-clamp-1"
            )}
            style={{
              backgroundColor: colors.input,
              borderColor: colors.border,
              color: colors.foreground
            }}
          >
            <span 
              className="transition-colors duration-300"
              style={{ 
                color: selectedLabel ? colors.foreground : colors.muted 
              }}
            >
              {selectedLabel || title}
            </span>
            <ChevronDown 
              className="h-4 w-4 opacity-50 transition-colors duration-300"
              style={{ color: colors.muted }}
            />
          </button>
        </PopoverPrimitive.Trigger>
        
        <PopoverPrimitive.Portal>
          <PopoverPrimitive.Content
            className={cn(
              "z-50 w-[var(--radix-popover-trigger-width)] rounded-md border p-0 shadow-lg transition-colors duration-300",
              "data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95",
              "data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2"
            )}
            style={{
              backgroundColor: colors.surface,
              borderColor: colors.border,
              color: colors.foreground
            }}
            side="bottom"
            align="start"
            sideOffset={4}
          >
            <div 
              className="p-2 border-b transition-colors duration-300"
              style={{ borderColor: colors.border }}
            >
              <div className="relative">
                <Search 
                  className="absolute left-2 top-1/2 h-4 w-4 -translate-y-1/2 transition-colors duration-300"
                  style={{ color: colors.muted }}
                />
                <Input
                  ref={searchInputRef}
                  placeholder="Search countries..."
                  value={searchValue}
                  onChange={(e) => setSearchValue(e.target.value)}
                  onKeyDown={handleKeyDown}
                  className="pl-8 h-8 transition-colors duration-300"
                  style={{
                    backgroundColor: colors.input,
                    borderColor: colors.border,
                    color: colors.foreground
                  }}
                />
              </div>
            </div>
            
            <div className="max-h-60 overflow-auto p-1">
              {filteredOptions.length === 0 ? (
                <div 
                  className="py-2 px-3 text-sm text-center transition-colors duration-300"
                  style={{ color: colors.muted }}
                >
                  No countries found
                </div>
              ) : (
                filteredOptions.map((option, index) => (
                  <button
                    key={option.code}
                    onClick={() => handleSelect(option)}
                    className={cn(
                      "relative flex w-full cursor-pointer select-none items-center rounded-sm py-2 pl-8 pr-2 text-sm outline-none transition-all duration-200"
                    )}
                    style={{
                      backgroundColor: (selectedValue === option.code || highlightedIndex === index) 
                        ? `${colors.accent}20` 
                        : 'transparent',
                      color: colors.foreground
                    }}
                    onMouseEnter={(e) => {
                      setHighlightedIndex(index)
                      e.target.style.backgroundColor = `${colors.accent}10`
                    }}
                    onMouseLeave={(e) => {
                      if (selectedValue !== option.code && highlightedIndex !== index) {
                        e.target.style.backgroundColor = 'transparent'
                      }
                    }}
                  >
                    <span className="absolute left-2 flex h-3.5 w-3.5 items-center justify-center">
                      {selectedValue === option.code && (
                        <Check 
                          className="h-4 w-4 transition-colors duration-300"
                          style={{ color: colors.accent }}
                        />
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