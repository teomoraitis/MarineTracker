import React, { useContext, useState } from 'react';
import Toggle from '../Toggle/Toggle.jsx';
import NameSearch from '../NameSearch/NameSearch.jsx';
import { FreeDrawContext, FilterContext, ZoiContext } from '../../contexts/contexts.js';
import HoverInfo from '../HoverInfo/HoverInfo.jsx';
import { saveZoneOfInterest } from '../../api/zoneApi.js';
import "./Filters.css";

const Filters = ({}) => {
  const { freeDrawOn, setFreeDraw } = useContext(FreeDrawContext);
  const { filters, onFilterChange } = useContext(FilterContext);
  const { zoi, setZoi } = useContext(ZoiContext);

  const [showTypesDropdownFilters, setShowTypesDropdownFilters] = useState(false);
  const [showTypesDropdownZoi, setShowTypesDropdownZoi] = useState(false);

  const shipTypes = [
    "Anti-pollution", "Cargo", "Cargo-hazarda(major)", "Cargo-hazardb", "Cargo-hazardc(minor)", "Cargo-hazardd(recognizable)",
    "Divevessel", "Dredger", "Fishing", "High-speedcraft", "Lawenforce", "Localvessel", "Militaryops", "Other", "Passenger",
    "Pilotvessel", "Pleasurecraft", "Sailingvessel", "Sar", "Specialcraft", "Tanker", "Tanker-hazarda(major)", "Tanker-hazardb",
    "Tanker-hazardc(minor)", "Tanker-hazardd(recognizable)", "Tug", "Unknown", "Wingingrnd"
  ];

  const toggleType = (type, list, setList) => {
    if (list.includes(type)) {
      setList(list.filter(t => t !== type));
    } else {
      setList([...list, type]);
    }
  };

  const saveZoi = async () => {
    await saveZoneOfInterest(zoi.area, zoi.restrictions.speed, zoi.restrictions.types)
  };

  return (
    <section className="w-[20vw] p-6 flex flex-col gap-10 overflow-y-scroll scrollbar">
      {/* Section 1 */}
      <div className='flex flex-col gap-4'>
        <h6 className='text-lg font-bold'>Filters</h6>
        <NameSearch />
        
        {/* First Type Dropdown */}
        <div className='flex flex-col gap-2'>
          <div
            className='flex flex-row gap-3 items-center cursor-pointer'
            onClick={() => setShowTypesDropdownFilters(!showTypesDropdownFilters)}
          >
            <div className='flex flex-row items-center'>
              <h6 className='text-sm font-light text-left'>📋 Type:</h6>
              <HoverInfo tooltip="Types to look out for">🛈</HoverInfo>
            </div>
            <span className='text-xs text-gray-500'>{showTypesDropdownFilters ? '▲' : '▼'}</span>
          </div>

          {showTypesDropdownFilters && (
            <div className="flex flex-col border border-gray-300 rounded-lg px-2 py-1 shadow-sm max-h-40 overflow-y-auto">
              {shipTypes.map((type, index) => (
                <div
                  key={index}
                  onClick={() => toggleType(type, filters.vesselTypes, (types) => onFilterChange({ ...filters, vesselTypes: types }))}
                  className={`text-sm px-2 py-1 rounded cursor-pointer ${
                    filters.vesselTypes.includes(type)
                      ? 'bg-blue-200 text-blue-800'
                      : 'hover:bg-gray-100'
                  }`}
                >
                  {type}
                </div>
              ))}
            </div>
          )}

          {}
          {filters.vesselTypes.length > 0 && (
            <div className="flex flex-wrap gap-2 mt-2">
              {filters.vesselTypes.map((type, index) => (
                <span
                  key={index}
                  className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded-full border border-blue-300"
                >
                  {type}
                </span>
              ))}
            </div>
          )}
        </div>

        <div className='flex flex-row gap-5 items-center'>
          <h6 className='text-sm font-regular p-0'>🚢 MyFleet</h6>
          <Toggle
            value={filters.showOnlyFleet}
            onChange={(val) => onFilterChange({
              ...filters,
              showOnlyFleet: val
            })}
          />
        </div>
      </div>

      {}
      <div className='flex flex-col gap-4'>
        <div>
          <div className='flex flex-row gap-5 items-center'>
            <h6 className='text-lg font-bold'>Zone of Interest</h6>
            <Toggle
              value={zoi.show}
              onChange={(val) => {
                if (zoi.show) {
                  saveZoi();
                  setFreeDraw(false);
                }
                setZoi({
                  ...zoi,
                  show: val
                })
              }}
              disabled={zoi.show && freeDrawOn && (zoi.area.length == 0 || zoi.restrictions.speed == 0 || zoi.restrictions.types.length == 0)}
            />
          </div>
          {
            zoi.show && (
              <button
                className='w-fit p-1'
                onClick={() => {
                  if (freeDrawOn) {
                    saveZoi();
                  }
                  setFreeDraw(!freeDrawOn)
                }}
                disabled={freeDrawOn && (zoi.area.length == 0 || zoi.restrictions.speed == 0 || zoi.restrictions.types.length == 0)}
              >
                <h6 className='text-sm font-light text-left'>{freeDrawOn ? "✅ Save" : "🗺️ Edit"}</h6>
              </button>
            )
          }
        </div>

        {
          zoi.show && (
            <>
              <h6 className='text-sm font-light italic'>Restrictions:</h6>
              <div className='flex flex-row gap-5 items-center'>
                <h6 className='text-sm font-light'>🚤 Speed:
                <HoverInfo tooltip="Max speed in knots">🛈</HoverInfo>
                </h6>
                <input
                  className="w-16 px-1 py-1 border border-gray-300 rounded-lg shadow-sm focus:outline-none appearance-none"
                  min='0'
                  max='20'
                  type='number'
                  disabled={!freeDrawOn}
                  value={zoi.restrictions.speed}
                  onChange={(e) => setZoi({
                    ...zoi,
                    restrictions: { ...zoi.restrictions, speed: e.target.value }
                  })}
                />
              </div>

              {/* Second Type Dropdown */}
              <div className='flex flex-col gap-2'>
                <div
                  className='flex flex-row gap-3 items-center cursor-pointer'
                  onClick={() => setShowTypesDropdownZoi(!showTypesDropdownZoi)}
                >
                  <div className='flex flex-row items-center'>
                    <h6 className='text-sm font-light text-left'>📋 Type:</h6>
                    <HoverInfo tooltip="Types to look out for">🛈</HoverInfo>
                  </div>
                  <span className='text-xs text-gray-500'>{zoi.show ? '▲' : '▼'}</span>
                </div>

                {
                  showTypesDropdownZoi && freeDrawOn && (
                  <div className="flex flex-col border border-gray-300 rounded-lg px-2 py-1 shadow-sm max-h-40 overflow-y-auto">
                    {shipTypes.map((type, index) => (
                      <div
                        key={index}
                        onClick={() => toggleType(type, zoi.restrictions.types, (types) => setZoi({ ...zoi, restrictions: { ...zoi.restrictions, types: types }}))}
                        className={`text-sm px-2 py-1 rounded cursor-pointer ${
                          zoi.restrictions.types.includes(type)
                            ? 'bg-green-200 text-green-800'
                            : 'hover:bg-gray-100'
                        }`}
                      >
                        {type}
                      </div>
                    ))}
                  </div>
                  )
                }

                {/* Selected tags for Type 2 */}
                {zoi.restrictions.types.length > 0 && (
                  <div className="flex flex-wrap gap-2 mt-2">
                    {zoi.restrictions.types.map((type, index) => (
                      <span
                        key={index}
                        className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded-full border border-green-300"
                      >
                        {type}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </>
          )
        }
      </div>
    </section>
  );
};

export default Filters;
