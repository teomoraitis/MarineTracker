import React, { useEffect, useState } from 'react';
import Map from './components/Map/Map.jsx';
import Navbar from './components/Navbar/Navbar.jsx';
import { MapContext, FreeDrawContext, AuthContext, SelectedShipContext, FilterContext } from './contexts/contexts.js';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import './App.css';
import Filters from './components/Filters/Filters.jsx';
//import ShipPopUp from './components/ShipPopUp/ShipPopUp.jsx';
import ShipInfoPanel from './components/ShipPopUp/ShipInfoPanel.jsx';
import Footer from './components/Footer/Footer.jsx';
import FreedrawTooltip from './components/FreedrawTooltip/FreedrawTooltip.jsx';

const App = () => {
  const [user, setUser] = useState(null);
  const [freeDrawOn, setFreeDraw] = useState(false);
  const [selectedShip, setSelectedShip] = useState({});
  const [showPath, setShowPath] = useState(false);
  const [path, setPath] = useState([]);
  const [ships, setShips] = useState([]);
  const [hideShipInfo, setHideShipInfo] = useState(false);
  const [filters, setFilters] = useState({
    types: [],
    fleetOnly: false,
    zoi: {
      show: false,
      area: [],
      restrictions: {
        speed: 0,
        types: [],
      },
    },
    bounds: [],
  });

  const login = (email = '', password = '') => {
    let userType = "normal";
    if(email === "admin") userType = "admin" ;
    if(email === "log") userType = "loggedIn" ;
    const mockGuest = {
      name: userType,
      token: "a1",
    } ;
    const mockUser = {
      name: userType,
      token: "b2",
    } ;
    const mockAdmin = {
      name: userType,
      token: "c3",
    } ;
    if(email === "admin") setUser(mockAdmin);
    else if(email === "log") setUser(mockUser);
  };

  const logout = () => {
    setUser(null);
  };

  useEffect(() => {
    const socket = new SockJS("https://localhost:8443/ws");
    const stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("Connected to WebSocket (SockJS)");

        stompClient.subscribe("/topic/locations", (message) => {
          try {
            const update = JSON.parse(message.body);

            const updateObject = update.setShips.reduce((agg, shipUpdate) => {
              return {
                ...agg,
                [shipUpdate.mmsi]: shipUpdate,
              };
            }, {});

            setShips((prevShips) => ({
              ...prevShips,
              ...updateObject,
            }));
          } catch (error) {
            console.error("Error parsing WebSocket message:", error);
          }
        });
      },
      onStompError: (frame) => {
        console.error("STOMP Error:", frame.headers["message"]);
      },
    });

    stompClient.activate();

    return () => stompClient.deactivate();
  }, []);

  useEffect(() => {
  const dummyShip = {
    mmsi: 999999999,
    status: 0,
    turn: 0,
    speed: 12.5,
    course: 90,
    heading: 25,
    lon: 23.7365,
    lat: 37.9756,
    timestamp: Date.now(),
  };

  setShips((prevShips) => ({
    ...prevShips,
    [dummyShip.mmsi]: dummyShip,
  }));
}, []);


  const toggleShowPath = () => {
    setShowPath(!showPath);
    console.log("showPath: ", showPath);
    // fetch path for selected ship
    setPath([]);
  };

  useEffect(() => {
    console.log("selectedShip: ", selectedShip);
  }, [selectedShip.mmsi]);

  useEffect(() => {
    console.log("filters: ", filters);
  }, [filters]);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      <MapContext.Provider value={{ ships, setShips }}>
        <FreeDrawContext.Provider value={{freeDrawOn: freeDrawOn, setFreeDraw: setFreeDraw}}>
          <SelectedShipContext.Provider
            value={{
            ship: selectedShip,
            setSelectedShipInfo: (shipInfo) => {
              setSelectedShip(shipInfo);
              setHideShipInfo(false); 
            },
            showPath: showPath,
            toggleShowPath,
            path: path,
            hideShipInfo,
            setHideShipInfo
          }}
          >
            <FilterContext.Provider
              value={{
                filters: filters,
                onFilterChange: setFilters
              }}
            >
              <div className='h-dvh'>
                <Navbar />
                <div className='relative w-full flex flex-row'>
                  { user && <Filters /> }
                  <div className='relative w-full flex flex-row'>
                    <Map ships={ships}/>
                    <ShipInfoPanel />
                    <FreedrawTooltip />
                  </div>
                </div>
                <Footer />
              </div>
            </FilterContext.Provider>
          </SelectedShipContext.Provider>
        </FreeDrawContext.Provider>
      </MapContext.Provider>
    </AuthContext.Provider>
  );
};
 
export default App;