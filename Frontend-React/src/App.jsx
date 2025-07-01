import React, { useEffect, useRef, useState } from 'react';
import Map from './components/Map/Map.jsx';
import Navbar from './components/Navbar/Navbar.jsx';
import { MapContext, FreeDrawContext, AuthContext, SelectedShipContext, FilterContext, ZoiContext } from './contexts/contexts.js';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import './App.css';
import Filters from './components/Filters/Filters.jsx';
import ShipInfoPanel from './components/ShipPopUp/ShipInfoPanel.jsx';
import Footer from './components/Footer/Footer.jsx';
import FreedrawTooltip from './components/FreedrawTooltip/FreedrawTooltip.jsx';
import { getUser, login, logout } from './api/userApi.js';
import { getVesselPath } from './api/vesselsApi.js';


const App = () => {
  const [user, setUser] = useState(null);
  const [freeDrawOn, setFreeDraw] = useState(false);
  const [selectedShip, setSelectedShip] = useState({});
  const [showPath, setShowPath] = useState(false);
  const [path, setPath] = useState([]);
  const [ships, setShips] = useState({});
  const [hideShipInfo, setHideShipInfo] = useState(false);
  const [filters, setFilters] = useState({
    vesselTypes: [],
    showOnlyFleet: false,
    bounds: [],
  });
  const [zoi, setZoi] = useState({
    show: false,
    area: [],
    restrictions: {
      speed: 0,
      types: [],
    },
  });

  const stompClientRef = useRef(null);
  const subscriptionRef = useRef(null);

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
      setFreeDraw(false);
      setZoi({ show: false, area: [], restrictions: { types: [], speed: 0 }});
    } catch {
      console.error("Error during logout");
    }
  };

  const checkIfUserIsLoggedIn = async () => {
    const user = await getUser();
    if (user != null) {
      setUser(user);
    }
  };

  useEffect(() => {
    checkIfUserIsLoggedIn();
  }, []);

  useEffect(() => {
    const socket = new SockJS(`${process.env.REACT_APP_BACKEND_URL}ws/${user == null ? 'guest' : 'auth'}`);
    const stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      debug: (str) => {
        // console.log('STOMP Debug:', str);
      },
      onConnect: () => {
        console.log("Connected to WebSocket (SockJS)");

        if (subscriptionRef.current) {
          subscriptionRef.current.unsubscribe();
        }

        subscriptionRef.current = stompClient.subscribe(user == null ? '/topic/guest' : `/user/queue/vessels`, (message) => {
          try {
            const update = JSON.parse(message.body);

            if (update.setShips && Array.isArray(update.setShips)) {
              const updateObject = update.setShips.reduce((agg, shipUpdate) => {
                console.log(shipUpdate);
                return {
                  ...agg,
                  [shipUpdate.mmsi]: shipUpdate,
                };
              }, {});

              setShips((prevShips) => ({
                ...prevShips,
                ...updateObject,
              }));
            }
          } catch (error) {
            console.error("Error parsing WebSocket message:", error);
            console.error("Message body:", message.body);
          }
        });
      },
      onStompError: (frame) => {
        console.error("STOMP Error:", frame);
      },
      onWebSocketError: (event) => {
        console.error("WebSocket Error:", event);
      },
    });

    stompClientRef.current = stompClient;
    stompClient.activate();

    return () => {
      if (stompClient.connected) {
        stompClient.deactivate();
      }
    };
  }, [user]);


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
    if (!stompClientRef.current || !stompClientRef.current.connected) {
      console.warn("WebSocket client not connected, cannot send filters");
      return;
    }

    try {
      stompClientRef.current.publish({
        destination: "/app/filters",
        body: JSON.stringify(filters)
      });
    } catch (error) {
      console.error("Error sending filters via WebSocket:", error);
    }
    setShips({})
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
              <ZoiContext.Provider
                value={{
                  zoi: zoi,
                  setZoi: setZoi,
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
              </ZoiContext.Provider>
            </FilterContext.Provider>
          </SelectedShipContext.Provider>
        </FreeDrawContext.Provider>
      </MapContext.Provider>
    </AuthContext.Provider>
  );
};
 
export default App;