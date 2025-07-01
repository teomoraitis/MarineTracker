import React, { useState, useEffect, useContext } from 'react';
import { getVessel, listVessels } from '../../api/vesselsApi';
import { SelectedShipContext } from '../../contexts/contexts';

const NameSearch = ({}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedInputValue, setDebouncedInputValue] = useState("");
  const [results, setResults] = useState([]);
  const { setSelectedShipInfo } = useContext(SelectedShipContext);

  const handleSearch = async () => {
    if (searchTerm.trim() === '' || debouncedInputValue.trim() === '') {
      setResults([]);
      return;
    }

    const vessels = await listVessels({ params: { name: searchTerm } });

    setResults(vessels.content.map(vessel => ({ mmsi: vessel.mmsi, name: vessel.name })));
  };

  const handleResultClick = async (result) => {
    setResults([]);
    setSearchTerm('')
    setDebouncedInputValue('');

    const vessel = await getVessel(result.mmsi);
    setSelectedShipInfo(vessel);
  };

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      setDebouncedInputValue(searchTerm);
    }, 750);
    return () => clearTimeout(timeoutId);
  }, [searchTerm, 750]);

  useEffect(() => {
    handleSearch();
  }, [debouncedInputValue]);

  return (
    <div className='relative'>
      <form
        className='bg-[#E8E8E8] p-1 rounded-full'
        onSubmit={(e) => {
          e.preventDefault();
          handleSearch();
        }}
      >
        <input
          className='text-center m-auto w-full bg-inherit'
          placeholder='Name 🔎'
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value.toUpperCase())}
        />
      </form>
      {
        results.length > 0 && (
          <div className='absolute z-10 my-1 bg-[#E8E8E8] w-full border rounded-md  flex flex-col gap-1'>
            {
              results.map(result => {
                return (
                  <div
                    key={result.mmsi}
                    className='flex flex-row items-center justify-between p-3 hover:bg-[#b7b9ba] transition-colors duration-100 cursor-pointer'
                    onClick={() => handleResultClick(result)}
                  >
                    <p className='text-sm text-start'>{result.mmsi}</p>
                    <p className='text-sm text-end'>{result.name}</p>
                  </div>
                )
              })
            }
          </div>
        )
      }
    </div>
  );
}
 
export default NameSearch;