import React, { useState } from 'react';
import Map from './components/Map/Map.jsx';
import Navbar from './components/Navbar/Navbar.jsx';
import { FreeDrawContext, AuthContext } from './contexts/contexts.js';
import './App.css';
import Filters from './components/Filters/Filters.jsx';
import ShipPopUp from './components/ShipPopUp/ShipPopUp.jsx';
import Footer from './components/Footer/Footer.jsx';
import FreedrawTooltip from './components/FreedrawTooltip/FreedrawTooltip.jsx';

const App = () => {
  const [user, setUser] = useState(null);
  const [freeDrawOn, setFreeDraw] = useState(false);

  const login = () => {
    const mockUser = { name: "Alice", token: "abc123" };
    setUser(mockUser);
  };

  const logout = () => {
    setUser(null);
  };


  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      <FreeDrawContext.Provider value={{freeDrawOn: freeDrawOn, setFreeDraw: setFreeDraw}}>
        <div className='h-dvh'>
          <Navbar />
          <div className='relative w-full flex flex-row'>
            { user && <Filters /> }
            <div className='relative w-full flex flex-row'>
              <Map />
              <ShipPopUp />
              <FreedrawTooltip />
            </div>
          </div>
          <Footer />
        </div>
      </FreeDrawContext.Provider>
    </AuthContext.Provider>
  );
};
 
export default App;