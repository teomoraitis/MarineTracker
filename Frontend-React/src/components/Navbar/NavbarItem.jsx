import React from 'react';

const NavbarItem = ({ label, onClick }) => {
  return (
    <h3
      className='m-auto text-xl font-light px-5 py-2 mx-2 text-center cursor-pointer hover:bg-[#D0D0D0] rounded-md'
      onClick={onClick}
    >
      {label}
    </h3>
  );
}
 
export default NavbarItem;