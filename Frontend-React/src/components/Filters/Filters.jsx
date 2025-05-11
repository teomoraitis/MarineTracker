import React, { useContext } from 'react';
import Toggle from '../Toggle/Toggle.jsx';
import NameSearch from '../NameSearch/NameSearch.jsx';
import { FreeDrawContext, FilterContext } from '../../contexts/contexts.js';
import HoverInfo from '../HoverInfo/HoverInfo.jsx';

const Filters = ({}) => {
  const { freeDrawOn, setFreeDraw } = useContext(FreeDrawContext);
  const { filters, onFilterChange } = useContext(FilterContext);

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
          <Toggle
            value={filters.fleetOnly}
            onChange={(val) => onFilterChange({
              ...filters,
              fleetOnly: val
            })}
          />
        </div>
      </div>

      <div className='flex flex-col gap-4'>
        <div>
          <div className='flex flex-row gap-5 items-center'>
            <h6 className='text-lg font-bold'>Zone of Interest</h6>
            <Toggle
              value={filters.zoi.show}
              onChange={(val) => onFilterChange({
                ...filters,
                zoi: { ...filters.zoi, show: val }
              })}
            />
          </div>
          {
            filters.zoi.show && (
              <button
                className='w-fit p-1'
                onClick={() => setFreeDraw(!freeDrawOn)}
              >
                <h6 className='text-sm font-light text-left'>{freeDrawOn ? "Save" : "✎ Edit"}</h6>
              </button>
            )
          }
        </div>

        {
          filters.zoi.show && (
            <>
              <h6 className='text-sm font-light italic'>Restrictions:</h6>
              <div className='flex flex-row gap-5 items-center'>
                <h6 className='text-sm font-light'>Speed:
                  <HoverInfo tooltip="Max speed">🛈</HoverInfo>
                </h6>
                <input
                  className="w-16 px-1 py-1 border border-gray-300 rounded-lg shadow-sm focus:outline-none appearance-none"
                  min='0'
                  max='20'
                  type='number'
                  value={filters.zoi.restrictions.speed}
                  onChange={(e) => onFilterChange({
                    ...filters,
                    zoi: {
                      ...filters.zoi,
                      restrictions: { ...filters.zoi.restrictions, speed: e.target.value }
                    }
                  })}
                />
              </div>

              <div className='fex flex-row gap-5 items-center'>
                <h6 className='text-sm font-light text-left'>Type:
                  <HoverInfo tooltip="Types to look out for">🛈</HoverInfo>
                </h6>
              </div>
            </>
          )
        }
      </div>

    </section>
  );
};
 
export default Filters;