import React from 'react';
import Toggle from '../Toggle/Toggle.jsx';
import NameSearch from '../NameSearch/NameSearch.jsx';

const Filters = ({}) => {
  return (
    <section className="w-[20vw] p-6 flex flex-col gap-10">
      <div>
        <h6 className='text-lg font-bold'>Filters</h6>
        <NameSearch />
      </div>

      <div>
        <div className='flex gap-5'>
          <h6 className='text-lg font-bold'>Zone of Interest</h6>
          <Toggle />
        </div>
      </div>

    </section>
  );
};
 
export default Filters;