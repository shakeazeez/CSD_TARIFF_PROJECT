import {
  Chart as ChartJS,
  CategoryScale, // x-axis
  LinearScale, // y-axis
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import { Line } from "react-chartjs-2";
import { useTheme } from "../hooks/useTheme.js"; // Import theme context

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
 * IndustryChart component with Theme Integration
 *
 * A reusable chart built on top of react-chartjs-2 and chart.js.
 * Now includes dark/light mode support with custom theme colors.
 * Similar to Chart component but without title display.
 *
 * @author Generated from Chart component
 * @version 1.0.0
 *
 * @param {Object} props
 * @param {Array<string>} props.labels - Array of labels for the x-axis.
 * @param {Array<Array<number>>} props.value - Array of numeric values for the y-axis. Must match labels length.
 *
 * @example
 * const value = [
 *              [300, 200, 700, 200],
 *              [100, 600, 100, 600],
 *              [500, 400, 500, 900],
 * ];
 * const labels = ["Jan", "Feb", "Mar", "Apr"];
 *
 * <IndustryChart
 *   labels={labels}
 *   value={value}
 * />
 */

const IndustryChart = ({ labels, value }) => {
  // Get theme colors for chart styling
  const { colors } = useTheme();

  // Calculate dynamic max value based on data
  const calculateMaxValue = () => {
    if (!value || value.length === 0) return 100;

    const allValues = value.flat().filter((val) => val != null && !isNaN(val));
    if (allValues.length === 0) return 100;

    const maxDataValue = Math.max(...allValues);
    return maxDataValue + 5;
  };

  // Theme-aware color palette for chart lines
  function getThemeColors(index) {
    const themeColorPalette = [
      colors.accent, // Primary accent color
      colors.info, // Info blue
      colors.success, // Success green
      colors.warning, // Warning orange
      colors.error, // Error red
      "#8b5cf6", // Purple
      "#ec4899", // Pink
      "#06b6d4", // Cyan
    ];

    const baseColor = themeColorPalette[index % themeColorPalette.length];

    return {
      borderColor: baseColor,
      backgroundColor: `${baseColor}20`, // 20% opacity
      pointBackgroundColor: baseColor,
      pointBorderColor: colors.background,
      pointHoverBackgroundColor: colors.background,
      pointHoverBorderColor: baseColor,
    };
  }

  const datasets = value.map((dataArray, i) => {
    const themeColors = getThemeColors(i);
    return {
      label: `Tariff Rate`,
      data: dataArray,
      ...themeColors,
      tension: 0.1, // Slight curve for smoother lines
      borderWidth: 2,
      pointRadius: 4,
      pointHoverRadius: 6,
    };
  });

  const data = { labels, datasets };
  console.log("chart data: {}", data);

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      mode: "index",
      intersect: false,
    },
    plugins: {
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
          weight: "bold",
        },
        bodyFont: {
          size: 13,
        },
        callbacks: {
          label: function(context) {
            const value = context.parsed.y;
            return `${value.toFixed(2)}%`;
          }
        }
      },
    },
    scales: {
      x: {
        display: true,
        title: {
          display: true,
          text: "Time Period",
          color: colors.foreground,
          font: {
            size: 12,
            weight: "bold",
          },
        },
        ticks: {
          color: colors.muted,
          font: {
            size: 11,
          },
        },
        grid: {
          color: `${colors.border}80`, // Semi-transparent grid lines
          drawBorder: false,
        },
      },
      y: {
        display: true,
        title: {
          display: true,
          text: "Tariff Rate (%)",
          color: colors.foreground,
          align: "center",
          font: {
            size: 12,
            weight: "bold",
          },
        },
        min: 0,
        max: calculateMaxValue(),
        ticks: {
          stepSize: 5,
          color: colors.muted,
          font: {
            size: 11,
          },
          callback: function (value) {
            return value.toFixed(2) +`%`;
          },
        },
        grid: {
          color: `${colors.border}80`, // Semi-transparent grid lines
          drawBorder: false,
        },
      },
    },
  };

  return (
    <div style={{ height: "300px", width: "100%" }}>
      <Line data={data} options={options} />
    </div>
  );
};

export default IndustryChart;
