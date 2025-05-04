import React, { useState, useEffect } from 'react';

const NameSearch = ({}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedInputValue, setDebouncedInputValue] = useState("");

  const handleSearch = () => {
    console.log(searchTerm);
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
  );
}
 
export default NameSearch;