import React, { useState } from 'react';

const Toggle = ({ value, onChange, disabled }) => {

  return (
    <button
      className={`relative inline-block min-w-[50px] h-[28px] shadow-sm cursor-pointer border border-[#aaa] rounded-full transition-colors duration-100 ${value && !disabled ? 'bg-[#15b58e] hover:border-[#6f6f6f]' : 'bg-[#b7b9ba]'}`}
      onClick={() => (disabled ? null : onChange(!value))}
    >
      <div className={`absolute top-1/2 h-[20px] w-[20px] bg-white rounded-full transition-all duration-150 translate-y-[-50%] peer-checked:translate-x-[22px] ${value ? 'left-[25px]' : 'left-[3px]'}`} />
    </button>
  );
}

export default Toggle;