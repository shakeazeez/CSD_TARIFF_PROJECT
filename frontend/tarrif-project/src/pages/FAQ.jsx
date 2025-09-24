// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import { useState } from 'react' // React hooks for state management
import { useNavigate } from 'react-router-dom' // Navigation hook

// Animation library for smooth transitions
import { motion, AnimatePresence } from 'framer-motion'

// shadcn/ui components - Modern, accessible UI components
import { Button } from '../components/ui/button' // Customizable button component
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card' // Card layout components
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '../components/ui/collapsible' // Collapsible components

// Theme and icon components
import { useTheme } from '../contexts/ThemeContext.jsx' // Custom theme context for component-level theming
import { useAuth } from '../contexts/AuthContext.jsx' // Authentication context for user management
import {
    HelpCircle,
    ChevronDown,
    Calculator,
    Globe,
    TrendingUp,
    ArrowLeft,
    Sun,
    Moon,
    Home,
    Menu
} from 'lucide-react' // SVG icons

// ====================================
// ANIMATION VARIANTS
// ====================================

const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
        opacity: 1,
        transition: {
            duration: 0.6,
            staggerChildren: 0.1
        }
    }
}

const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
        opacity: 1,
        y: 0,
        transition: {
            duration: 0.5,
            ease: "easeOut"
        }
    }
}

// ====================================
// FAQ COMPONENT
// ====================================

export function FAQ({ onMenuClick }){
    // ====================================
    // THEME INTEGRATION
    // ====================================

    // Get theme context for component-level color management
    const { colors, toggleTheme, isDark } = useTheme();

    // Get authentication context for user management
    const { user } = useAuth();

    // Navigation hook
    const navigate = useNavigate();

    // ====================================
    // STATE VARIABLES
    // ====================================

    const [openItems, setOpenItems] = useState(new Set());

    // ====================================
    // FAQ DATA
    // ====================================

    const faqData = [
        {
            id: 'what-is-goattariff',
            question: 'What is GoatTariff?',
            answer: 'GoatTariff is a comprehensive platform for calculating international trade tariffs and analyzing global trade costs. We provide real-time tariff calculations, historical trend analysis, and professional tools to help businesses optimize their international trade strategies.'
        },
        {
            id: 'how-accurate',
            question: 'How accurate are the tariff calculations?',
            answer: 'Our calculations are based on official tariff schedules and trade data from national customs authorities and international trade organizations. We update our database regularly to ensure accuracy, but always verify with official sources for critical transactions.'
        },
        {
            id: 'what-countries',
            question: 'Which countries are covered?',
            answer: 'We cover tariff data for over 200 countries and territories worldwide, including all major trading nations. Our database includes both importing and exporting country perspectives for comprehensive trade analysis.'
        },
        {
            id: 'hs-codes',
            question: 'What are HS codes and why do I need them?',
            answer: 'HS (Harmonized System) codes are international product classification codes used to identify goods for customs purposes. They determine the applicable tariff rates and trade regulations. You\'ll need the specific HS code for your product to get accurate tariff calculations.'
        },
        {
            id: 'historical-data',
            question: 'What historical data is available?',
            answer: 'We provide historical tariff trend analysis showing how rates have changed over time. This helps businesses understand market trends, anticipate changes, and make informed decisions about pricing and market entry strategies.'
        },
        {
            id: 'real-time-updates',
            question: 'Are the tariff rates updated in real-time?',
            answer: 'We update our tariff database regularly with the latest available information. While we strive to provide current data, tariff rates can change, and we recommend checking with official customs authorities for the most up-to-date rates.'
        },
        {
            id: 'free-to-use',
            question: 'Is GoatTariff free to use?',
            answer: 'We offer both free and premium features. Basic tariff calculations are available for free, while advanced features like detailed historical analysis, bulk calculations, and priority support are available with premium subscriptions.'
        },
        {
            id: 'how-to-get-started',
            question: 'How do I get started with tariff calculations?',
            answer: 'Simply select your importing and exporting countries, enter the HS code for your product, input the item value, and click calculate. Our platform will provide you with the applicable tariff rates and total landed costs.'
        },
        {
            id: 'support',
            question: 'What kind of support do you provide?',
            answer: 'We offer comprehensive support including detailed FAQs, user guides, and customer service. Premium users receive priority support and dedicated account management for complex trade scenarios.'
        },
        {
            id: 'data-privacy',
            question: 'How do you protect my data and privacy?',
            answer: 'We take data privacy seriously and comply with international data protection regulations. Your calculation data is encrypted and never shared with third parties. We only use aggregated, anonymized data for platform improvements.'
        }
    ];

    // ====================================
    // UTILITY FUNCTIONS
    // ====================================

    const toggleItem = (id) => {
        const newOpenItems = new Set(openItems);
        if (newOpenItems.has(id)) {
            newOpenItems.delete(id);
        } else {
            newOpenItems.add(id);
        }
        setOpenItems(newOpenItems);
    };

    // ====================================
    // COMPONENT RENDER (JSX)
    // ====================================

    return(
        <motion.div
            initial="hidden"
            animate="visible"
            variants={containerVariants}
            className="min-h-screen"
            style={{
                background: 'transparent'
            }}
        >

            {/* HEADER */}
            <motion.header
                initial={{ y: -100, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                transition={{ duration: 0.6 }}
                className="backdrop-blur-sm border-b sticky top-0 z-50"
                style={{
                    backgroundColor: `${colors.surface}cc`,
                    borderColor: colors.border
                }}
            >
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center py-4">
                        {/* Logo and navigation buttons */}
                      <div className="flex items-center space-x-4">
                        {/* Menu button */}
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={onMenuClick}
                            className="text-white hover:bg-white/10"
                        >
                            <Menu className="h-5 w-5" />
                        </Button>

                        <motion.div
                            className="flex items-center space-x-3"
                            whileHover={{ scale: 1.05 }}
                        >
                        <div
                            className="p-2 rounded-lg shadow-lg"
                            style={{ backgroundColor: colors.accent }}
                        >
                            <img
                                src="/tempGOAT.png"
                                alt="GoatTariff Logo"
                                className="h-12 w-12 object-contain"
                            />
                        </div>
                        <span className="text-xl font-bold" style={{ color: colors.foreground }}>
                            GoatTariff
                        </span>
                        </motion.div>
                    </div>

                        {/* Right side - User/Login + Theme toggle */}
                        <div className="flex items-center space-x-3">
                            {user ? (
                                <div className="flex items-center space-x-3">
                                    <div className="hidden sm:flex items-center space-x-2 px-3 py-1 rounded-lg text-sm"
                                         style={{ backgroundColor: colors.surface, border: `1px solid ${colors.border}` }}>
                                        <div className="w-6 h-6 rounded-full bg-gradient-to-r from-blue-500 to-purple-600 flex items-center justify-center">
                                            <span className="text-white text-xs font-semibold">
                                                {user?.email?.charAt(0).toUpperCase() || 'U'}
                                            </span>
                                        </div>
                                        <span style={{ color: colors.foreground }}>{user?.email || 'User'}</span>
                                    </div>
                                    <Button
                                        onClick={() => navigate('/calculator')}
                                        className="transition-all duration-300 text-white"
                                        style={{
                                            backgroundColor: colors.accent,
                                            borderColor: colors.accent
                                        }}
                                        size="sm"
                                    >
                                        <Calculator className="h-4 w-4 mr-2" />
                                        Calculator
                                    </Button>
                                </div>
                            ) : (
                                <Button
                                    onClick={() => navigate('/login')}
                                    className="transition-all duration-300 text-white"
                                    style={{
                                        backgroundColor: colors.accent,
                                        borderColor: colors.accent
                                    }}
                                    size="sm"
                                >
                                    Login
                                </Button>
                            )}

                            <motion.div
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                            >
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={toggleTheme}
                                    className="transition-all duration-300 shadow-md"
                                    style={{
                                        borderColor: colors.accent,
                                        backgroundColor: colors.surface,
                                        color: colors.accent,
                                        borderWidth: '1px'
                                    }}
                                    onMouseEnter={(e) => {
                                        e.target.style.backgroundColor = colors.accent;
                                        e.target.style.color = 'white';
                                    }}
                                    onMouseLeave={(e) => {
                                        e.target.style.backgroundColor = colors.surface;
                                        e.target.style.color = colors.accent;
                                    }}
                                >
                                    {isDark ? (
                                        <Sun className="h-4 w-4" />
                                    ) : (
                                        <Moon className="h-4 w-4" />
                                    )}
                                </Button>
                            </motion.div>
                        </div>
                    </div>
                </div>
            </motion.header>

            {/* MAIN CONTENT */}
            <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <motion.div
                    variants={itemVariants}
                    className="text-center mb-12"
                >
                    <div
                        className="inline-block px-6 py-4 rounded-lg shadow-md mb-4"
                        style={{
                            backgroundColor: `${colors.surface}ee`,
                            border: `1px solid ${colors.border}`
                        }}
                    >
                        <motion.div

                            transition={{
                                duration: 4,
                                repeat: Infinity,
                                repeatType: "reverse"
                            }}
                            className="inline-block mb-6"
                        >
                            <div
                                className="p-4 rounded-2xl shadow-lg mx-auto w-fit"
                                style={{ backgroundColor: colors.accent }}
                            >
                                <HelpCircle className="h-12 w-12 text-white" />
                            </div>
                            
                        </motion.div>

                    
                        <h1 className="text-3xl sm:text-4xl font-bold mb-2" style={{ color: colors.foreground }}>
                            Frequently Asked Questions
                        </h1>
                        <p className="text-lg" style={{ color: colors.muted }}>
                            Find answers to common questions about GoatTariff and international trade tariff calculations.
                        </p>
                    </div>
                </motion.div>

                {/* FAQ ITEMS */}
                <div className="space-y-4">
                    {faqData.map((faq, index) => (
                        <motion.div
                            key={faq.id}
                            variants={itemVariants}
                            initial="hidden"
                            animate="visible"
                            transition={{ delay: index * 0.05 }}
                        >
                            <Card
                                className="transition-all duration-300 hover:shadow-lg"
                                style={{
                                    backgroundColor: colors.surface,
                                    borderColor: colors.border
                                }}
                            >
                                <Collapsible
                                    open={openItems.has(faq.id)}
                                    onOpenChange={() => toggleItem(faq.id)}
                                >
                                    <CollapsibleTrigger asChild>
                                        <CardHeader className="cursor-pointer hover:bg-opacity-50 transition-colors duration-300">
                                            <CardTitle className="flex items-center justify-between text-left text-lg"
                                                       style={{ color: colors.foreground }}>
                                                <span>{faq.question}</span>
                                                <motion.div
                                                    animate={{ rotate: openItems.has(faq.id) ? 180 : 0 }}
                                                    transition={{ duration: 0.2 }}
                                                >
                                                    <ChevronDown className="h-5 w-5" style={{ color: colors.accent }} />
                                                </motion.div>
                                            </CardTitle>
                                        </CardHeader>
                                    </CollapsibleTrigger>
                                    <CollapsibleContent>
                                        <CardContent>
                                            <p className="text-base leading-relaxed" style={{ color: colors.muted }}>
                                                {faq.answer}
                                            </p>
                                        </CardContent>
                                    </CollapsibleContent>
                                </Collapsible>
                            </Card>
                        </motion.div>
                    ))}
                </div>

                {/* BOTTOM CTA */}
                <motion.div
                    variants={itemVariants}
                    className="text-center mt-12 p-8 rounded-2xl"
                    style={{
                        backgroundColor: `${colors.surface}80`,
                        border: `1px solid ${colors.border}`
                    }}
                >
                    <h2 className="text-2xl font-bold mb-4" style={{ color: colors.foreground }}>
                        Ready to Calculate Tariffs?
                    </h2>
                    <p className="text-lg mb-6 max-w-2xl mx-auto" style={{ color: colors.muted }}>
                        Get started with our professional tariff calculation tools and optimize your international trade strategy.
                    </p>
                    <div className="flex flex-col sm:flex-row gap-4 justify-center">
                        <Button
                            onClick={() => navigate('/calculator')}
                            size="lg"
                            className="text-white"
                            style={{
                                backgroundColor: colors.accent,
                                borderColor: colors.accent
                            }}
                        >
                            <Calculator className="h-5 w-5 mr-2" />
                            Start Calculating
                        </Button>
                        <Button
                            variant="outline"
                            size="lg"
                            onClick={() => navigate('/')}
                            style={{
                                borderColor: colors.accent,
                                backgroundColor: colors.surface,
                                color: colors.accent
                            }}
                        >
                            <Home className="h-5 w-5 mr-2" />
                            Back to Home
                        </Button>
                    </div>
                </motion.div>
            </main>

            {/* FOOTER */}
            <footer
                className="border-t mt-16 py-8 px-4 sm:px-6 lg:px-8"
                style={{
                    backgroundColor: colors.surface,
                    borderColor: colors.border,
                    color: colors.muted
                }}
            >
                <div className="max-w-4xl mx-auto text-center">
                    <div className="flex items-center justify-center space-x-2 mb-4">
                        <img
                            src="/tempGOAT.png"
                            alt="GoatTariff Logo"
                            className="h-5 w-5 object-contain"
                        />
                        <span className="font-semibold" style={{ color: colors.foreground }}>
                            GoatTariff
                        </span>
                    </div>
                    <p className="text-sm mb-4">
                        Professional tariff calculation and global trade analysis platform.
                    </p>
                    <div className="flex items-center justify-center space-x-4 text-xs">
                        <span>© 2025 GoatTariff. All rights reserved.</span>
                    </div>
                </div>
            </footer>
        </motion.div>
    );
}