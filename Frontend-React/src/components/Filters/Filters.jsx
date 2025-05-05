import React, { useContext } from 'react';
import Toggle from '../Toggle/Toggle.jsx';
import NameSearch from '../NameSearch/NameSearch.jsx';
import { FreeDrawContext } from '../../contexts/contexts.js';
import HoverInfo from '../HoverInfo/HoverInfo.jsx';

const Filters = ({}) => {
  const freeDrawContext = useContext(FreeDrawContext);

  return (
    <section className="w-[20vw] p-6 flex flex-col gap-10">
      <div className='flex flex-col gap-4'>
        <h6 className='text-lg font-bold'>Filters</h6>
        <NameSearch />
        <div className='flex flex-row gap-5 items-center'>
          <h6 className='text-sm font-regular p-0'>Type</h6>
        </div>
        <div className='flex flex-row gap-5 items-center'>
          <h6 className='text-sm font-regular p-0'>MyFleet</h6>
          <Toggle />
        </div>
      </div>

      <div className='flex flex-col gap-4'>
        <div>
          <div className='flex flex-row gap-5 items-center'>
            <h6 className='text-lg font-bold'>Zone of Interest</h6>
            <Toggle />
          </div>
          <button
            className='w-fit p-1'
            onClick={() => freeDrawContext.setFreeDraw(!freeDrawContext.freeDrawOn)}
          >
            <h6 className='text-sm font-light text-left'>{freeDrawContext.freeDrawOn ? "Save" : "✎ Edit"}</h6>
          </button>
        </div>

        <h6 className='text-sm font-light italic'>Restrictions:</h6>
        <div className='flex flex-row gap-5 items-center'>
          <h6 className='text-sm font-light'>Speed:
            <HoverInfo tooltip="Max speed">🛈</HoverInfo>
          </h6>
          <input
            className="w-16 px-1 py-1 border border-gray-300 rounded-lg shadow-sm focus:outline-none appearance-none"
            min='1'
            max='20'
            type='number'
          />
        </div>

        <div className='fex flex-row gap-5 items-center'>
          <h6 className='text-sm font-light text-left'>Type:
            <HoverInfo tooltip="Types to look out for">🛈</HoverInfo>
          </h6>
        </div>
      </div>

    </section>
  );
};
 
export default Filters;