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
 * Chart component
 *
 * A reusable chart built on top of react-chartjs-2 and chart.js.
 * 
 * @author Yong Huey
 * @version 1.0.1
 * 
 * @param {Object} props
 * @param {Array<string>} props.labels - Array of labels for the x-axis.
 * @param {Array<Array<number>>} props.value - Array of numeric values for the y-axis. Must match labels length.
 * @param {string} props.title - Title displayed at the top of the chart.
 * @param {Array<string>} props.legend - Label shown in the chart legend.
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

const Chart = ({ labels, value, title, legend }) => {
    function randomColor() {
        const r = Math.floor(Math.random() * 255);
        const g = Math.floor(Math.random() * 255);
        const b = Math.floor(Math.random() * 255);
        return {borderColor : `rgba(${r},${g},${b},1)`,
                backgroundColor : `rgba(${r},${g},${b},0.2)`};
    }

    const datasets = legend.map((name, i) => {
        const { borderColor, backgroundColor } = randomColor();
        return {label: name,
        data: value[i],
        borderColor,
        backgroundColor,
        tension: 0
        };
    });

    const data = {labels, datasets};

    const options = {
    responsive: true,
    plugins: {
            legend: { position: "top" },
            title: { display: true, text: title }
        }
    };
    return(<Line data={data} options={options} />);
}

export default Chart;