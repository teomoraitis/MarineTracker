import React from 'react';

const HoverInfo = ({ children, tooltip }) => {
  return (
    <div className="relative group inline-block">
      {children}
      <div className="absolute z-10 bottom-full mb-2 w-max max-w-[200px] rounded-md bg-gray-800 px-3 py-2 text-sm text-white opacity-0 transition-opacity duration-200 group-hover:opacity-100
        left-1/2 -translate-x-1/2
        group-hover:left-auto group-hover:right-0 group-hover:translate-x-0
        lg:group-hover:left-1/2 lg:group-hover:-translate-x-1/2 lg:group-hover:right-auto
        pointer-events-none"
      >
        {tooltip}
      </div>
    </div>
  );
};

export default HoverInfo;