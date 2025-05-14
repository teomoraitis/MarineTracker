import React, { useState, useContext } from 'react';
import Toggle from '../Toggle/Toggle.jsx';
import { SelectedShipContext } from '../../contexts/contexts.js';

const ShipPopUp = ({}) => {
  const shipContext = useContext(SelectedShipContext);
  const {
    ship,
    setSelectedShipInfo,
    showPath,
    setShowPath
  } = shipContext;

  return (
    <section className='absolute bg-[white] w-[15vw] z-10 top-8 right-8 rounded-[25px] p-5 shadow-[10px_10px_4px_rgba(0,0,0,0.25)]'>
      <h6 className='font-bold'>Ship Info</h6>
      <div className='flex flex-row gap-4'>
        <p className="font-regular">Path history (12 hours)</p>
        <Toggle
          value={showPath}
          onChange={(val) => setShowPath(val)}
        />
      </div>
      <div className='flex flex-row gap-4 content-center'>
        <p className="font-regular">In my fleet</p>
        <input
          type='checkbox'
          checked={ship.inFleet}
          onChange={() => setSelectedShipInfo({ ...ship, inFleet: !ship.inFleet })}
        />
      </div>
    </section>
  );
}
 
export default ShipPopUp;