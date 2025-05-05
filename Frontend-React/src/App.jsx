import React, { useEffect, useState } from 'react';
import Map from './components/Map/Map.jsx';
import Navbar from './components/Navbar/Navbar.jsx';
import { FreeDrawContext, AuthContext } from './contexts/contexts.js';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
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

  useEffect(() => {
    const socket = new SockJS("https://localhost:8443/ws");
    const stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("Connected to WebSocket (SockJS)");

        stompClient.subscribe("/topic/locations", (message) => {
          try {
            const newShip = JSON.parse(message.body);
            console.log("Received WebSocket Data:", newShip);

            setShips((prevShips) => ({
              ...prevShips,
              [newShip.mmsi]: newShip,
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