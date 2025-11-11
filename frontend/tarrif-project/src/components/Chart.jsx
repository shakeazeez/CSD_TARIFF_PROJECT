import {
  Chart as ChartJS,
  CategoryScale,   // x-axis
  LinearScale,    // y-axis
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
} from "chart.js";
import { Line } from "react-chartjs-2";
import { useTheme } from "../contexts/use-theme.js"; // Import theme context
import { useState } from "react";

// Register components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

/**
 * Chart component with Theme Integration
 *
 * A reusable chart built on top of react-chartjs-2 and chart.js.
 * Now includes dark/light mode support with custom theme colors.
 * 
 * @author Yong Huey
 * @version 1.1.0 - Added theme integration
 * 
 * @param {Object} props
 * @param {Array<string>} props.labels - Array of labels for the x-axis.
 * @param {Array<Array<number>>} props.value - Array of numeric values for the y-axis. Must match labels length.
 * @param {string} props.title - Title displayed at the top of the chart.
 * @param {Array<string>} props.legend - Label shown in the chart legend.
 * @param {number} props.baseCost - Base cost in USD for calculating total cost with tariff.
 *
 * @example
 * const value = [
 *              [300, 200, 700, 200],
 *              [100, 600, 100, 600],
 *              [500, 400, 500, 900],
 * ];
 * const labels = ["Jan", "Feb", "Mar", "Apr"];
 * const title = "Sales";
 * const legend = ["Shop 1", "Shop 2", "Shop 3"];
 *
 * <Chart
 *   labels={labels}
 *   value={value}
 *   title={title}
 *   legend={legend}
 * />
 */

const Chart = ({ labels, value, title, legend, baseCost }) => {
    // Get theme colors for chart styling
    const { colors } = useTheme();
    
    // State to track the currently highlighted value and its index
    const [highlightedData, setHighlightedData] = useState(null);
    
    // Theme-aware color palette for chart lines
    function getThemeColors(index) {
        const themeColorPalette = [
            colors.accent,        // Primary accent color
            colors.info,          // Info blue
            colors.success,       // Success green
            colors.warning,       // Warning orange
            colors.error,         // Error red
            '#8b5cf6',           // Purple
            '#ec4899',           // Pink
            '#06b6d4',           // Cyan
        ];
        
        const baseColor = themeColorPalette[index % themeColorPalette.length];
        
        return {
            borderColor: baseColor,
            backgroundColor: `${baseColor}20`, // 20% opacity
            pointBackgroundColor: baseColor,
            pointBorderColor: colors.background,
            pointHoverBackgroundColor: colors.background,
            pointHoverBorderColor: baseColor
        };
    }

    const datasets = legend.map((name, i) => {
        const themeColors = getThemeColors(i);
        return {
            label: name,
            data: value[i],
            ...themeColors,
            tension: 0.1, // Slight curve for smoother lines
            borderWidth: 2,
            pointRadius: 4,
            pointHoverRadius: 6
        };
    });

    const data = { labels, datasets };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
            mode: 'index',
            intersect: false,
        },
        onHover: (event, activeElements, chart) => {
            if (activeElements && activeElements.length > 0) {
                const dataIndex = activeElements[0].index;
                const datasetIndex = activeElements[0].datasetIndex;
                const hoveredValue = value[datasetIndex][dataIndex];
                const hoveredLabel = labels[dataIndex];
                setHighlightedData({ value: hoveredValue, label: hoveredLabel });
            } else {
                setHighlightedData(null);
            }
        },
        plugins: {
            title: {
                display: true,
                text: title,
                color: colors.foreground,
                font: {
                    size: 16,
                    weight: 'bold'
                },
                padding: {
                    top: 10,
                    bottom: 20
                }
            },
            legend: {
                display: true,
                position: 'top',
                labels: {
                    color: colors.foreground,
                    usePointStyle: true,
                    padding: 20,
                    font: {
                        size: 12
                    }
                }
            },
            tooltip: {
                backgroundColor: colors.surface,
                titleColor: colors.foreground,
                bodyColor: colors.foreground,
                borderColor: colors.border,
                borderWidth: 1,
                cornerRadius: 8,
                displayColors: true,
                titleFont: {
                    size: 14,
                    weight: 'bold'
                },
                bodyFont: {
                    size: 13
                }
            }
        },
        scales: {
            x: {
                display: true,
                title: {
                    display: true,
                    text: 'Time Period',
                    color: colors.foreground,
                    font: {
                        size: 12,
                        weight: 'bold'
                    }
                },
                ticks: {
                    color: colors.muted,
                    font: {
                        size: 11
                    }
                },
                grid: {
                    color: `${colors.border}80`, // Semi-transparent grid lines
                    drawBorder: false
                }
            },
            y: {
                display: true,
                title: {
                    display: true,
                    text: 'Tariff Rate (%)',
                    color: colors.foreground,
                    font: {
                        size: 12,
                        weight: 'bold'
                    }
                },
                ticks: {
                    color: colors.muted,
                    font: {
                        size: 11
                    },
                    callback: function(value) {
                        return value + '%';
                    }
                },
                grid: {
                    color: `${colors.border}80`, // Semi-transparent grid lines
                    drawBorder: false
                }
            }
        }
    };
    
    return (
        <div style={{ height: '400px', width: '100%', position: 'relative' }}>
            {/* Zoomed-in number display at top right */}
            {value && value.length > 0 && value[0] && value[0].length > 0 && (
                <div style={{
                    position: 'absolute',
                    top: '10px',
                    right: '10px',
                    zIndex: 10,
                    backgroundColor: colors.surface,
                    border: `2px solid ${colors.accent}`,
                    borderRadius: '8px',
                    padding: '8px 12px',
                    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                    minWidth: '120px'
                }}>
                    <div style={{
                        fontSize: '18px',
                        fontWeight: 'bold',
                        color: colors.accent,
                        textAlign: 'center',
                        lineHeight: '1.2'
                    }}>
                        {(() => {
                            const currentValue = highlightedData ? highlightedData.value : value[0][value[0].length - 1];
                            const tariffRate = typeof currentValue === 'number' ? currentValue : parseFloat(currentValue) || 0;
                            const calculatedCost = baseCost ? (parseFloat(baseCost) + (tariffRate / 100) * parseFloat(baseCost)) : null;
                            return `${tariffRate.toFixed(2)}%`;
                        })()}
                    </div>
                    <div style={{
                        fontSize: '11px',
                        color: colors.muted,
                        textAlign: 'center',
                        marginTop: '2px'
                    }}>
                        {highlightedData ? highlightedData.label : (labels && labels.length > 0 ? labels[labels.length - 1] : 'Latest')}
                    </div>
                    {baseCost && (
                        <div style={{
                            fontSize: '12px',
                            color: colors.foreground,
                            textAlign: 'center',
                            marginTop: '4px',
                            fontWeight: 'bold'
                        }}>
                            ${(() => {
                                const currentValue = highlightedData ? highlightedData.value : value[0][value[0].length - 1];
                                const tariffRate = typeof currentValue === 'number' ? currentValue : parseFloat(currentValue) || 0;
                                const calculatedCost = parseFloat(baseCost) + (tariffRate / 100) * parseFloat(baseCost);
                                return calculatedCost.toFixed(2);
                            })()}
                        </div>
                    )}
                </div>
            )}
            <Line data={data} options={options} />
        </div>
    );
}

export default Chart;