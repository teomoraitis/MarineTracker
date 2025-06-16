import React, { useEffect, useState } from 'react';
import Map from './components/Map/Map.jsx';
import Navbar from './components/Navbar/Navbar.jsx';
import { MapContext, FreeDrawContext, AuthContext, SelectedShipContext, FilterContext } from './contexts/contexts.js';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import './App.css';
import Filters from './components/Filters/Filters.jsx';
import ShipInfoPanel from './components/ShipPopUp/ShipInfoPanel.jsx';
import Footer from './components/Footer/Footer.jsx';
import FreedrawTooltip from './components/FreedrawTooltip/FreedrawTooltip.jsx';
import { login, logout } from './api/userApi.js';
import { getVesselPath } from './api/vesselsApi.js';


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

  const handleLogin = async (username = '', password = '') => {
    if (username.trim() == '' || password.trim() == '') return;

    try {
      const user = await login({
        username: username,
        password: password
      });
      setUser(user);
    } catch {
      console.error("Error during login");
    }
  };

  const handleLogout = async () => {
    try {
      await logout();
      setUser(null);
    } catch {
      console.error("Error during logout");
    }
  };

  useEffect(() => {
    const socket = new SockJS(`${process.env.REACT_APP_BACKEND_URL}ws`);
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


  const toggleShowPath = async (mmsi) => {
    setShowPath(!showPath);
    // fetch path for selected ship
    if (!showPath) {
      try {
        const path = await getVesselPath(mmsi);
        setPath(path);
      } catch {
        console.error("Error during path fetch");
        setPath([]);
      }
    } else {
      setPath([]);
    }
  };

  useEffect(() => {
    console.log("filters: ", filters);
  }, [filters]);

  return (
    <AuthContext.Provider value={{ user, handleLogin, handleLogout }}>
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