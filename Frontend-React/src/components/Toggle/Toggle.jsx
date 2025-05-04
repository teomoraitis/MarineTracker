import React, { useState } from 'react';

const Toggle = ({}) => {
  const [toggled, setToggled] = useState(false);

  return (
    <button
      className={`relative inline-block min-w-[50px] h-[28px] cursor-pointer border border-[#aaa] bg-[#b7b9ba] rounded-full transition-colors duration-100 ${toggled && 'bg-[#15b58e] hover:border-[#6f6f6f]'}`}
      onClick={() => setToggled(!toggled)}
    >
      <div className={`absolute top-1/2 left-[3px] h-[20px] w-[20px] bg-white rounded-full transition-all duration-150 translate-y-[-50%] peer-checked:translate-x-[22px] ${toggled && 'left-[25px]'}`} />
    </button>
  );
}

export default Toggle;