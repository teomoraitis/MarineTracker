import React, { useContext, useState } from 'react';
import ShipShipGoLogo from '../../assets/images/shipshipgo.png';
import NavbarItem from './NavbarItem.jsx';
import { AuthContext } from '../../contexts/contexts.js';

const AuthModal = ({ title, onClose, onSubmit }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white p-6 rounded-lg w-96 shadow-lg">
        <h3 className="text-xl font-bold mb-4">{title}</h3>
        <div className="flex flex-col gap-3">
          <input
            type="email"
            placeholder="Email"
            className="border border-gray-300 rounded px-3 py-2"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <input
            type="password"
            placeholder="Password"
            className="border border-gray-300 rounded px-3 py-2"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <div className="flex justify-end gap-2 mt-4">
            <button
              onClick={onClose}
              className="px-4 py-2 text-sm bg-gray-200 hover:bg-gray-300 rounded" > Cancel
            </button>
            <button
              onClick={() => onSubmit(email, password)}
              className="px-4 py-2 text-sm bg-blue-600 text-white hover:bg-blue-700 rounded" > Submit
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

const Navbar = ({}) => {
  const authContext = useContext(AuthContext);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [showSignupModal, setShowSignupModal] = useState(false);
  const [showHelpModal, setShowHelpModal] = useState(false);
  return (
    <div className='h-[10vh] bg-[#E8E8E8] flex flex-row justify-between content-center px-3'>
      <div className='flex flex-row content-center h-2/3 my-auto'>
        <img src={ShipShipGoLogo} alt="logo" className='cursor-pointer'/>
        <NavbarItem
          label="Help"
          onClick={() => {setShowHelpModal(true)}}
        />
      </div>
      <div className='flex flex-row content-center h-2/3 my-auto'>
        <NavbarItem
          label="Sign Up"
          onClick={() => setShowSignupModal(true)}
        />
        <NavbarItem
          label="Login"
          onClick={() => setShowLoginModal(true)}
        />
        {showLoginModal && (
      <AuthModal
        title="Login"
        onClose={() => setShowLoginModal(false)}
        onSubmit={(email, password) => {
          console.log("Login with:", email, password);
          setShowLoginModal(false);
          if (authContext.user) { //Temporary for debugging
            authContext.logout();
          } else {
            authContext.login(); 
          }

        }}
      />
    )}
    {showSignupModal && (
      <AuthModal
        title="Sign Up"
        onClose={() => setShowSignupModal(false)}
        onSubmit={(email, password) => {
          console.log("Sign up with:", email, password);
          setShowSignupModal(false);
        }}
      />
    )}
      {showHelpModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-lg w-80 shadow-lg text-center">
            <h3 className="text-lg font-semibold mb-4">Help</h3>
            <p className="text-sm text-gray-700">Temporary Text</p>
            <button
              onClick={() => setShowHelpModal(false)}
              className="mt-6 px-4 py-2 text-sm bg-blue-600 text-white hover:bg-blue-700 rounded"
            >
              Close
            </button>
          </div>
        </div>
        )}
      </div>
    </div>
  );
}
 
export default Navbar;