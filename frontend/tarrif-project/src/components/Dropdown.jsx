import Select from "react-select";

/**
 * Dropdown component
 *
 * A reusable dropdown built on top of react-select.
 *
 * @param {Object} props
 * @param {string} props.title - Label text shown above the dropdown.
 * @param {Array<{id: string, code: string}>} props.options - List of options (must have `id` and `code`).
 * @param {function} [props.onChange] - Callback fired when an option is selected.
 *
 * @example
 * const countryOptions = [
 *   { code: "702", id: "Singapore" },
 *   { code: "840", id: "United States" }
 * ];
 *
 * <Dropdown
 *   title="Country"
 *   options={countryOptions}
 *   onChange={(selected) => console.log(selected)}
 * />
 */

const Dropdown = ({ title, options, onChange }) => {
  return (
    <div className="dropdown-container">
      <label className="dropdown-label">{title}
        <Select
            options={options}
            getOptionLabel={(option) => option.id}
            getOptionValue={(option) => option.code}
            onChange={onChange}
        />
      </label>
    </div>
  );
};

export default Dropdown;