import React from 'react';

const Dropdown = ({ options, value, onChange, placeholder }) => {
  return (
    <select value={value} onChange={(e) => onChange(e.target.value)} className="border p-2 rounded">
      <option value="">{placeholder}</option>
      {options.map((option) => (
        <option key={option} value={option}>
          {option}
        </option>
      ))}
    </select>
  );
};

export default Dropdown;