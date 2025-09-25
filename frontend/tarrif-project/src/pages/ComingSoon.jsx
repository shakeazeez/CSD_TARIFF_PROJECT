import React from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Clock, Sparkles } from 'lucide-react';
import { Button } from '../components/ui/button';
import { useTheme } from '../contexts/ThemeContext';
import underConstruction from '../assets/UnderConstruction.png'

const ComingSoon = ({ feature = "This feature" }) => {
  const navigate = useNavigate();
  const { colors } = useTheme();

  return (
    <div className="min-h-screen flex items-center justify-center p-4 relative overflow-hidden" style={{ background: 'transparent' }}>
      {/* Animated background */}
      <div className="absolute inset-0" style={{ background: 'transparent' }}>
        <div className="absolute inset-0 bg-gradient-to-br from-blue-50 to-purple-50 dark:from-slate-900 dark:to-slate-800" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_40%,rgba(120,119,198,0.1),transparent_50%)]" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_70%_80%,rgba(255,107,107,0.1),transparent_50%)]" />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="relative z-10 max-w-md mx-auto text-center"
      >
        {/* Back button */}
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.2 }}
          className="mb-8"
        >
          <Button
            variant="ghost"
            onClick={() => navigate(-1)}
            className="mb-4"
            style={{ color: colors.foreground }}
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
          <img src={underConstruction}/>
        </motion.div>

        {/* Main content */}
        <motion.div
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ delay: 0.3, duration: 0.5 }}
          className="bg-white/80 dark:bg-slate-800/80 backdrop-blur-lg rounded-2xl p-8 shadow-xl border border-slate-200 dark:border-slate-700"
        >
          {/* Animated clock icon */}
          <motion.div
            animate={{
              rotate: [0, 10, -10, 0],
              scale: [1, 1.1, 1]
            }}
            transition={{
              duration: 2,
              repeat: Infinity,
              repeatType: "reverse"
            }}
            className="mx-auto mb-6 w-20 h-20 rounded-full flex items-center justify-center"
            style={{ backgroundColor: colors.accent }}
          >
            <Clock className="h-10 w-10 text-white" />
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5 }}
            className="text-3xl font-bold mb-4"
            style={{ color: colors.foreground }}
          >
            Coming Soon
          </motion.h1>

          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.6 }}
            className="text-lg mb-6"
            style={{ color: colors.muted }}
          >
            {feature} is currently under development. We're working hard to bring you this exciting feature!
          </motion.p>

          {/* Feature highlights */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.7 }}
            className="space-y-3 mb-8"
          >
            <div className="flex items-center justify-center space-x-2 text-sm">
              <Sparkles className="h-4 w-4" style={{ color: colors.accent }} />
              <span style={{ color: colors.foreground }}>Innovative design</span>
            </div>
            <div className="flex items-center justify-center space-x-2 text-sm">
              <Sparkles className="h-4 w-4" style={{ color: colors.accent }} />
              <span style={{ color: colors.foreground }}>Advanced functionality</span>
            </div>
            <div className="flex items-center justify-center space-x-2 text-sm">
              <Sparkles className="h-4 w-4" style={{ color: colors.accent }} />
              <span style={{ color: colors.foreground }}>Seamless integration</span>
            </div>
          </motion.div>

          {/* Action buttons */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.8 }}
            className="space-y-3"
          >
            <Button
              onClick={() => navigate('/')}
              className="w-full"
              style={{
                backgroundColor: colors.accent,
                borderColor: colors.accent
              }}
            >
              Go to Homepage
            </Button>

            <Button
              variant="outline"
              onClick={() => navigate('/calculator')}
              className="w-full"
              style={{
                borderColor: colors.accent,
                color: colors.accent,
                backgroundColor: colors.surface
              }}
            >
              Try Calculator
            </Button>
          </motion.div>
        </motion.div>

        {/* Floating elements */}
        <div className="absolute inset-0 pointer-events-none overflow-hidden">
          {[...Array(6)].map((_, i) => (
            <motion.div
              key={i}
              className="absolute w-2 h-2 bg-blue-400 rounded-full opacity-60"
              style={{
                left: `${Math.random() * 100}%`,
                top: `${Math.random() * 100}%`,
              }}
              animate={{
                y: [-20, 20, -20],
                opacity: [0.3, 0.8, 0.3],
              }}
              transition={{
                duration: 3 + Math.random() * 2,
                repeat: Infinity,
                delay: Math.random() * 2,
              }}
            />
          ))}
        </div>
      </motion.div>
    </div>
  );
};

export default ComingSoon;