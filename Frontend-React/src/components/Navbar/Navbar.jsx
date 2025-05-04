import React from 'react';
import ShipShipGoLogo from '../../assets/images/shipshipgo.png';
import NavbarItem from './NavbarItem.jsx';

const Navbar = ({}) => {
  return (
    <div className='h-[10vh] bg-[#E8E8E8] flex flex-row justify-between content-center px-3'>
      <div className='flex flex-row content-center h-2/3 my-auto'>
        <img src={ShipShipGoLogo} alt="logo"/>
        <NavbarItem
          label="Help"
          onClick={() => {}}
        />
      </div>
      <div className='flex flex-row content-center h-2/3 my-auto'>
        <NavbarItem
          label="Sign Up"
          onClick={() => {}}
        />
        <NavbarItem
          label="Login"
          onClick={() => {}}
        />
      </div>
    </div>
  );
}
 
export default Navbar;