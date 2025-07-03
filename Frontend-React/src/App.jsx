import React, { useEffect, useRef, useState } from 'react';
import Map from './components/Map/Map.jsx';
import Navbar from './components/Navbar/Navbar.jsx';
import { MapContext, FreeDrawContext, AuthContext, SelectedShipContext, FilterContext, ZoiContext, NotificationContext } from './contexts/contexts.js';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import './App.css';
import Filters from './components/Filters/Filters.jsx';
import ShipInfoPanel from './components/ShipPopUp/ShipInfoPanel.jsx';
import Footer from './components/Footer/Footer.jsx';
import FreedrawTooltip from './components/FreedrawTooltip/FreedrawTooltip.jsx';
import { getUser, login, logout } from './api/userApi.js';
import { getVesselPath } from './api/vesselsApi.js';
import { getNotifications, getUnreadNotificationsCount } from './api/notificationsApi.js';


const App = () => {
  const [user, setUser] = useState(null);
  const [freeDrawOn, setFreeDraw] = useState(false);
  const [selectedShip, setSelectedShip] = useState({});
  const [path, setPath] = useState([]);
  const [pathShipMmsi, setPathShipMmsi] = useState(null);
  const [isPathVisible, setIsPathVisible] = useState(false); 
  const [ships, setShips] = useState([]);
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
  const [notifications, setNotifications] = useState([]);
  const [unreadNotificationsCount, setUnreadNotificationsCount] = useState(0);

  const stompClientRef = useRef(null);
  const subscriptionRef = useRef(null);
  const pingIntervalRef = useRef(null);
  
  const reloadNotifications = async () => {
    const notifications = await getNotifications();
    setNotifications(notifications);
    const unreadNotificationsCount = await getUnreadNotificationsCount();
    setUnreadNotificationsCount(unreadNotificationsCount);
  };

  const handleLogin = async (username = '', password = '') => {
    if (username.trim() == '' || password.trim() == '') return;

    try {
      const user = await login({ username, password });
      setUser(user);
      await reloadNotifications();
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
      return true;
    }
    return false;
  };

  const setup = async () => {
    if (await checkIfUserIsLoggedIn()) {
      await reloadNotifications();
    }
  };

  useEffect(() => {
    setup();
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

            if (update.notifications && Array.isArray(update.notifications) && update.notifications.length > 0) {
              reloadNotifications();
            }
          } catch (error) {
            console.error("Error parsing WebSocket message:", error);
            console.error("Message body:", message.body);
          }
        });

        // start ping interval
        pingIntervalRef.current = setInterval(() => {
          try {
            stompClient.publish({ destination: "/app/ping", body: "" });
          } catch (e) {
            console.error("ping STOMP failed");
          }
        }, 30000);
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

  
  const loadShipPath = async (mmsi) => {
    try {
      const path = await getVesselPath(mmsi);
      setPath(path);
      setPathShipMmsi(mmsi);
    } catch {
      console.error('Error fetching vessel path');
      setPath([]);
      setPathShipMmsi(null);
    }
  };

  const toggleShowPath = async () => {
    if (isPathVisible) {
      setIsPathVisible(false);
    } else {
      if (selectedShip?.mmsi) {
        await loadShipPath(selectedShip.mmsi);
        setIsPathVisible(true);
      }
    }
  };

  const handleShipSelect = async (shipInfo) => {
    setSelectedShip(shipInfo);
    setHideShipInfo(false);

    if (isPathVisible && shipInfo?.mmsi) {
      await loadShipPath(shipInfo.mmsi);
    } else {
      setPath([]);
      setPathShipMmsi(null);
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
              setSelectedShipInfo: handleShipSelect, 
              showPath: isPathVisible && pathShipMmsi === selectedShip?.mmsi,
              toggleShowPath,
              path,
              hideShipInfo,
              setHideShipInfo,
            }}
          >
            <NotificationContext.Provider
              value={{
                notifications,
                unreadNotificationsCount,
                setNotifications,
                setUnreadNotificationsCount
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
                  <div className='h-full'>
                    <Navbar />
                    <div className='h-[85vh] relative w-full flex flex-row'>
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
            </NotificationContext.Provider>
          </SelectedShipContext.Provider>
        </FreeDrawContext.Provider>
      </MapContext.Provider>
    </AuthContext.Provider>
  );
};

export default App;