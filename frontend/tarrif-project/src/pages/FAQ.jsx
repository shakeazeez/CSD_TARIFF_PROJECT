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

// Custom components
import { Header } from '../components/Header.jsx' // Header component

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
            <Header onMenuClick={onMenuClick} showUserInfo={true} />

            {/* MAIN CONTENT */}
            <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <motion.div
                    variants={itemVariants}
                    className="text-center mb-12"
                >
                    <div
                        className="backdrop-blur-sm inline-block px-6 py-4 rounded-lg shadow-md mb-4"
                        style={{
                            backgroundColor: `${colors.surface}40`,
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
                                className="backdrop-blur-sm transition-all duration-300 hover:shadow-lg"
                                style={{
                                    backgroundColor: colors.surface40,
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

            
        </motion.div>
    );
}