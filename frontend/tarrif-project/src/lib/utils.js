/**
 * Utility Functions - Class Name Management
 * 
 * This file provides utility functions for handling CSS class names
 * in a React/Tailwind CSS environment.
 * 
 * Key Function:
 * - cn(): Combines and deduplicates CSS class names intelligently
 * 
 * Dependencies:
 * - clsx: Conditional class name utility
 * - tailwind-merge: Tailwind-specific class deduplication
 * 
 * Usage Example:
 * cn("text-red-500", isActive && "bg-blue-500", className)
 * cn("p-4 text-lg", "p-2") // Result: "text-lg p-2" (p-2 overrides p-4)
 */

import { clsx } from "clsx"
import { twMerge } from "tailwind-merge"

/**
 * Combines class names with intelligent Tailwind CSS deduplication
 * 
 * This function combines clsx (for conditional classes) with tailwind-merge
 * (for proper Tailwind class deduplication). It ensures that conflicting
 * Tailwind classes are properly resolved with the last one taking precedence.
 * 
 * @param {...(string|object|array)} inputs - Class names, objects, or arrays
 * @returns {string} - Merged and deduplicated class string
 * 
 * Examples:
 * cn("px-4 py-2", "px-6") → "py-2 px-6"
 * cn("text-red-500", condition && "text-blue-500") → conditionally applied
 * cn(["base-class", "another-class"], { "conditional": isTrue })
 */
export function cn(...inputs) {
  return twMerge(clsx(inputs))
}