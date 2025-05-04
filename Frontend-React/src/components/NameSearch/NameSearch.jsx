import React, { useState, useEffect } from 'react';

const NameSearch = ({}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedInputValue, setDebouncedInputValue] = useState("");
  const [results, setResults] = useState([]);

  const handleSearch = () => {
    if (searchTerm.trim() === '' || debouncedInputValue.trim() === '') {
      setResults([]);
      return;
    }

    console.log(searchTerm);
    setResults([{id: 1, label: 'tset'}, {id: 2, label: 'tset'}, {id: 3, label: 'tset'}]);
  };

  const handleResultClick = () => {
    setResults([]);
    setSearchTerm('')
    setDebouncedInputValue('');
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
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </form>
      {
        results.length > 0 && (
          <div className='absolute z-10 my-1 bg-[#E8E8E8] w-full border rounded-md  flex flex-col gap-1'>
            {
              results.map(result => {
                return (
                  <div
                    key={result.id}
                    className='p-3 hover:bg-[#b7b9ba] transition-colors duration-100 cursor-pointer'
                    onClick={handleResultClick}
                  >
                    {result.label}
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