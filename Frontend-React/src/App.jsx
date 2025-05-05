import React, { useState } from 'react';
import Map from './components/Map/Map.jsx';
import Navbar from './components/Navbar/Navbar.jsx';
import { FreeDrawContext } from './contexts/contexts.js';
import './App.css';
import Filters from './components/Filters/Filters.jsx';
import ShipPopUp from './components/ShipPopUp/ShipPopUp.jsx';
import Footer from './components/Footer/Footer.jsx';
import FreedrawTooltip from './components/FreedrawTooltip/FreedrawTooltip.jsx';

const App = () => {
  const [freeDrawOn, setFreeDraw] = useState(false);

  return (
    <FreeDrawContext.Provider value={{freeDrawOn: freeDrawOn, setFreeDraw: setFreeDraw}}>
      <div className='h-dvh'>
        <Navbar />
        <div className='relative w-full flex flex-row'>
          <Filters />
          <div className='relative w-full flex flex-row'>
            <Map />
            <ShipPopUp />
            <FreedrawTooltip />
          </div>
        </div>
        <Footer />
      </div>
    </FreeDrawContext.Provider>
  );
};
 
export default App;