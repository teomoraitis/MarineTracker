import React from 'react';
import Toggle from '../Toggle/Toggle.jsx';

const ShipPopUp = ({}) => {
  return (
    <section className='absolute bg-[white] w-[15vw] z-10 top-8 right-8 rounded-[25px] p-5 shadow-[10px_10px_4px_rgba(0,0,0,0.25)]'>
      <h6 className='font-bold'>Ship Info</h6>
      <div className='flex flex-row gap-4'>
        <p className="font-regular">Path history (12 hours)</p>
        <Toggle />
      </div>
      <div className='flex flex-row gap-4 content-center'>
        <p className="font-regular">In my fleet</p>
        <input type='checkbox' />
      </div>
    </section>
  );
}
 
export default ShipPopUp;