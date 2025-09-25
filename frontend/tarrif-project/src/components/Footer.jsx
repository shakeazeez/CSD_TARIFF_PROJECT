// ====================================
// FOOTER COMPONENT
// ====================================

import { motion } from 'framer-motion'
import { useTheme } from '../contexts/ThemeContext.jsx'

export function Footer() {
    const { colors } = useTheme()

    return (
        <motion.footer
            initial={{ y: 100, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ duration: 0.6 }}
            className="backdrop-blur-sm border-t z-50"
            style={{
                backgroundColor: `${colors.surface}40`,
                borderColor: colors.border
            }}
        >
            <div className="w-full px-4 sm:px-6 lg:px-8 py-4 text-center">
                <span style={{ color: colors.foreground }}>
                    © 2025 GoatTariff. All rights reserved.
                </span>
            </div>
        </motion.footer>
    )
}