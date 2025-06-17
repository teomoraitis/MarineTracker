import React, { useContext, useState } from 'react';
import ShipShipGoLogo from '../../assets/images/shipshipgo.png';
import NavbarItem from './NavbarItem.jsx';
import { AuthContext } from '../../contexts/contexts.js';
import { signup } from '../../api/userApi.js';

const AuthModal = ({ title, onClose, onSubmit }) => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [acceptTerms, setAcceptTerms] = useState(false);

  const isSignUp = title.toLowerCase().includes('sign');

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white p-6 rounded-lg w-[400px] shadow-lg">
        <h3 className="text-2xl font-bold mb-4 text-center">{title}</h3>
        <div className="flex flex-col gap-3">
          {isSignUp && (
            <input
              type="email"
              placeholder="e-mail *"
              className="border border-gray-300 rounded px-3 py-2"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          )}
          <input
            type="username"
            placeholder="username *"
            className="border border-gray-300 rounded px-3 py-2"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <input
            type="password"
            placeholder="password *"
            className="border border-gray-300 rounded px-3 py-2"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          {isSignUp && (
            <label className="text-sm text-gray-600">
              <input
                type="checkbox"
                className="mr-2"
                checked={acceptTerms}
                onChange={(e) => setAcceptTerms(e.target.checked)}
              />
              I accept the <a href="#" className="text-blue-600 underline">terms and conditions</a>
            </label>
          )}
          {!isSignUp && (
            <div className="text-sm text-blue-600 underline cursor-pointer w-fit">
              I forgot my password
            </div>
          )}
        </div>
        <div className="flex justify-end gap-2 mt-6">
          <button
            onClick={onClose}
            className="px-12 py-2 text-sm bg-gray-200 hover:bg-gray-300 rounded ml-2 mr-4"
          >
            Cancel
          </button>
          <button
            onClick={() => onSubmit(username, password, email)}
            disabled={(isSignUp && !acceptTerms) || (!username.trim()) || (!password.trim()) || (isSignUp && !email)}
            className={`px-12 py-2 text-sm rounded ml-2 mr-4 text-white ${isSignUp && !acceptTerms ? 'bg-blue-300 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700'}`}
          >
            {isSignUp ? 'Sign Up' : 'Login'}
          </button>
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
      {authContext.user ? (
        <>
          {authContext.user.name === 'admin' && (
            <NavbarItem label="Admin" onClick={() => alert("Go to admin panel")} />
          )}
          <NavbarItem
            label="Notifications"
            onClick={() => alert("Place popup in here I think!")}
          />
          <NavbarItem
            label="Logout"
            onClick={() => authContext.handleLogout()}
          />
        </>
      ) : (
        <>
          <NavbarItem
            label="Sign Up"
            onClick={() => setShowSignupModal(true)}
          />
          <NavbarItem
            label="Login"
            onClick={() => setShowLoginModal(true)}
          />
        </>
      )}
      {showLoginModal && (
      <AuthModal
        title="Login"
        onClose={() => setShowLoginModal(false)}
        onSubmit={(username, password) => {
          setShowLoginModal(false);
          if (authContext.user) { 
            authContext.handleLogout();
          } else {
            authContext.handleLogin(username, password); 
          }

        }}
      />
    )}
    {showSignupModal && (
      <AuthModal
        title="Sign Up"
        onClose={() => setShowSignupModal(false)}
        onSubmit={async (username, password, email) => {
          try {
            await signup({email, username, password})
            setShowSignupModal(false);
            authContext.handleLogin(username, password);
          } catch {
            console.error("Error during signup");
          }
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