import React, { useContext } from 'react';
import ShipShipGoLogo from '../../assets/images/shipshipgo.png';
import NavbarItem from './NavbarItem.jsx';
import { AuthContext } from '../../contexts/contexts.js';

const Navbar = ({}) => {
  const authContext = useContext(AuthContext);

  return (
    <div className='h-[10vh] bg-[#E8E8E8] flex flex-row justify-between content-center px-3'>
      <div className='flex flex-row content-center h-2/3 my-auto'>
        <img src={ShipShipGoLogo} alt="logo" className='cursor-pointer'/>
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
          onClick={() => authContext.user ? authContext.logout() : authContext.login() }
        />
      </div>
    </div>
  );
}
 
export default Navbar;