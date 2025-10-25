/**
 * Calendar Component - Date Picker with Custom Theme Integration
 *
 * A date picker component that displays a calendar grid for date selection.
 * Built with Radix UI Popover for better control over positioning and styling.
 * Follows the same styling patterns as the Dropdown component.
 *
 * Features:
 * - Calendar grid display with month/year navigation
 * - dd/mm/yyyy format support
 * - Custom theme color integration
 * - Keyboard navigation support
 * - Click outside to close
 * - Maintains consistent styling with Dropdown component
 *
 * @param {Object} props
 * @param {string} props.placeholder - Placeholder text shown when no date is selected.
 * @param {Date} [props.selectedDate] - Currently selected date.
 * @param {function} [props.onDateSelect] - Callback fired when a date is selected.
 * @param {Date} [props.minDate] - Minimum selectable date.
 * @param {Date} [props.maxDate] - Maximum selectable date.
 */

import * as React from "react"
import * as PopoverPrimitive from "@radix-ui/react-popover"
import { ChevronDown } from "lucide-react"
import { cn } from "../../lib/utils"
import { useTheme } from "../../contexts/ThemeContext.jsx"

const Calendar = ({
  placeholder = "Select date",
  selectedDate,
  onDateSelect,
  minDate,
  maxDate
}) => {
  // Get theme colors for custom styling
  const { colors } = useTheme()

  const [open, setOpen] = React.useState(false)
  const [currentMonth, setCurrentMonth] = React.useState(new Date().getMonth())
  const [currentYear, setCurrentYear] = React.useState(new Date().getFullYear())
  const [showMonthSelector, setShowMonthSelector] = React.useState(false)
  const [showYearSelector, setShowYearSelector] = React.useState(false)

  // Handle popover open/close
  const handleOpenChange = (newOpen) => {
    setOpen(newOpen)
    if (!newOpen) {
      setShowMonthSelector(false)
      setShowYearSelector(false)
    }
  }

  // Format date to yyyy-mm-dd
  const formatDate = (date) => {
    if (!date) return ""
    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  // Get days in month
  const getDaysInMonth = (month, year) => {
    return new Date(year, month + 1, 0).getDate()
  }

  // Get first day of month (0 = Sunday, 1 = Monday, etc.)
  const getFirstDayOfMonth = (month, year) => {
    return new Date(year, month, 1).getDay()
  }

  // Check if date is today
  const isToday = (day, month, year) => {
    const today = new Date()
    return today.getDate() === day &&
           today.getMonth() === month &&
           today.getFullYear() === year
  }

  // Check if date is selected
  const isSelected = (day, month, year) => {
    if (!selectedDate) return false
    return selectedDate.getDate() === day &&
           selectedDate.getMonth() === month &&
           selectedDate.getFullYear() === year
  }

  // Check if date is disabled
  const isDisabled = (day, month, year) => {
    const date = new Date(year, month, day)
    if (minDate && date < minDate) return true
    if (maxDate && date > maxDate) return true
    return false
  }

  // Handle date selection
  const handleDateSelect = (day, month, year) => {
    const selectedDate = new Date(year, month, day)
    setOpen(false)
    setShowMonthSelector(false)
    setShowYearSelector(false)
    if (onDateSelect) {
      onDateSelect(selectedDate)
    }
  }

  // Handle month selection
  const handleMonthSelect = (monthIndex) => {
    setCurrentMonth(monthIndex)
    setShowMonthSelector(false)
  }

  // Handle year selection
  const handleYearSelect = (year) => {
    setCurrentYear(year)
    setShowYearSelector(false)
  }

  // Generate years for selector (current year ± 10 years)
  const generateYears = () => {
    const years = []
    const currentYearNum = new Date().getFullYear()
    for (let i = currentYearNum - 10; i <= currentYearNum + 10; i++) {
      years.push(i)
    }
    return years
  }

  // Generate calendar days
  const renderCalendarDays = () => {
    const daysInMonth = getDaysInMonth(currentMonth, currentYear)
    const firstDay = getFirstDayOfMonth(currentMonth, currentYear)
    const days = []

    // Add empty cells for days before the first day of the month
    for (let i = 0; i < firstDay; i++) {
      days.push(
        <div key={`empty-${i}`} className="h-8 w-8"></div>
      )
    }

    // Add days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      const isTodayDate = isToday(day, currentMonth, currentYear)
      const isSelectedDate = isSelected(day, currentMonth, currentYear)
      const isDisabledDate = isDisabled(day, currentMonth, currentYear)

      days.push(
        <button
          key={day}
          onClick={() => !isDisabledDate && handleDateSelect(day, currentMonth, currentYear)}
          disabled={isDisabledDate}
          className={cn(
            "h-8 w-8 rounded-md text-sm font-medium transition-all duration-200",
            "hover:bg-accent hover:text-accent-foreground",
            "focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
            "disabled:opacity-50 disabled:cursor-not-allowed",
            "flex items-center justify-center", // Better centering
            isSelectedDate && "bg-primary text-primary-foreground",
            isTodayDate && !isSelectedDate && "bg-accent text-accent-foreground"
          )}
          style={{
            backgroundColor: isSelectedDate ? colors.primary : isTodayDate ? colors.accent : 'transparent',
            color: isSelectedDate ? colors.primaryForeground : isTodayDate ? colors.accentForeground : colors.foreground,
            borderColor: colors.border
          }}
        >
          {day}
        </button>
      )
    }

    return days
  }

  const monthNames = [
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  ]

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
                color: selectedDate ? colors.foreground : colors.muted
              }}
            >
              {selectedDate ? formatDate(selectedDate) : placeholder}
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
              "z-50 w-80 rounded-md border p-0 shadow-lg transition-colors duration-300",
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
            {/* Calendar Header */}
            <div
              className="flex items-center justify-center p-4 border-b transition-colors duration-300"
              style={{ borderColor: colors.border }}
            >
              <div className="flex items-center space-x-2">
                {/* Month Selector */}
                <button
                  onClick={() => {
                    setShowMonthSelector(!showMonthSelector)
                    setShowYearSelector(false)
                  }}
                  className="text-sm font-medium hover:bg-accent px-2 py-1 rounded border transition-colors duration-200 flex items-center justify-center min-w-[80px]"
                  style={{
                    color: colors.foreground,
                    backgroundColor: showMonthSelector ? colors.accent : 'transparent',
                    borderColor: colors.border
                  }}
                >
                  {monthNames[currentMonth]}
                </button>

                {/* Year Selector */}
                <button
                  onClick={() => {
                    setShowYearSelector(!showYearSelector)
                    setShowMonthSelector(false)
                  }}
                  className="text-sm font-medium hover:bg-accent px-2 py-1 rounded border transition-colors duration-200 flex items-center justify-center min-w-[60px]"
                  style={{
                    color: colors.foreground,
                    backgroundColor: showYearSelector ? colors.accent : 'transparent',
                    borderColor: colors.border
                  }}
                >
                  {currentYear}
                </button>
              </div>
            </div>

            {/* Month Selector Dropdown */}
            {showMonthSelector && (
              <div
                className="grid grid-cols-3 gap-1 p-2 border-b transition-colors duration-300"
                style={{ borderColor: colors.border }}
              >
                {monthNames.map((month, index) => (
                  <button
                    key={month}
                    onClick={() => handleMonthSelect(index)}
                    className={cn(
                      "text-xs py-1 px-2 rounded transition-colors duration-200 text-center",
                      "hover:bg-accent hover:text-accent-foreground"
                    )}
                    style={{
                      backgroundColor: currentMonth === index ? colors.accent : 'transparent',
                      color: currentMonth === index ? colors.accentForeground : colors.foreground
                    }}
                  >
                    {month.slice(0, 3)}
                  </button>
                ))}
              </div>
            )}

            {/* Year Selector Dropdown */}
            {showYearSelector && (
              <div
                className="max-h-32 overflow-y-auto p-2 border-b transition-colors duration-300"
                style={{ borderColor: colors.border }}
              >
                <div className="grid grid-cols-4 gap-1">
                  {generateYears().map((year) => (
                    <button
                      key={year}
                      onClick={() => handleYearSelect(year)}
                      className={cn(
                        "text-xs py-1 px-2 rounded transition-colors duration-200 text-center",
                        "hover:bg-accent hover:text-accent-foreground"
                      )}
                      style={{
                        backgroundColor: currentYear === year ? colors.accent : 'transparent',
                        color: currentYear === year ? colors.accentForeground : colors.foreground
                      }}
                    >
                      {year}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Days of Week Header */}
            <div className="grid grid-cols-7 gap-1 p-4 pt-2">
              {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map((day) => (
                <div
                  key={day}
                  className="h-8 w-8 text-center text-xs font-medium text-muted-foreground flex items-center justify-center"
                  style={{ color: colors.muted }}
                >
                  {day}
                </div>
              ))}
            </div>

            {/* Calendar Grid */}
            <div className="grid grid-cols-7 gap-1 p-4 pt-0 pb-4">
              {renderCalendarDays()}
            </div>
          </PopoverPrimitive.Content>
        </PopoverPrimitive.Portal>
      </PopoverPrimitive.Root>
    </div>
  )
}

export default Calendar