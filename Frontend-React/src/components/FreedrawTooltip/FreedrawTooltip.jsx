import React, { useContext} from 'react';
import { FreeDrawContext } from '../../contexts/contexts';


const FreedrawTooltip = () => {
  const freeDrawContext = useContext(FreeDrawContext);
  return (
    <section
      className={`
        absolute bg-white w-fit max-w-sm z-10 bottom-8 left-1/2 -translate-x-1/2 rounded-[25px] p-5 
        shadow-[10px_10px_4px_rgba(0,0,0,0.25)] 
        transition-all duration-300 ease-in-out
        ${freeDrawContext.freeDrawOn ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-5 pointer-events-none'}
      `}
    >
      <h6 className="font-bold text-center break-words whitespace-normal">
        Use Ctrl+Z to reset the polygon.
      </h6>
    </section>
  );
};

export default FreedrawTooltip;