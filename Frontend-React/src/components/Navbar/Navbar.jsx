import React, { useContext, useState } from 'react';
import ShipShipGoLogo from '../../assets/images/shipshipgo.png';
import NavbarItem from './NavbarItem.jsx';
import { AuthContext, NotificationContext } from '../../contexts/contexts.js';
import { signup } from '../../api/userApi.js';
import AdminExportButton from '../Admin/AdminExportButton.jsx';
import NavyBell from '../../assets/images/navybell.png';
import BlueCaptain from '../../assets/images/shipcaptain.png';
import { dismissAllNotifications, getNotifications, getUnreadNotificationsCount } from '../../api/notificationsApi.js';

const AuthModal = ({ title, onClose, onSubmit, setShowTermsModal, setShowForgotPasswordModal   }) => {
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
              I accept the{" "}
              <span
                onClick={() => setShowTermsModal(true)}
                className="text-blue-600 underline cursor-pointer"
              >
                terms and conditions
              </span>
            </label>
          )}
          {!isSignUp && (
            <div
              className="text-sm text-blue-600 underline cursor-pointer w-fit"
              onClick={() => setShowTermsModal && setShowTermsModal(false) || setShowForgotPasswordModal(true)} 
            >
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
  const [showTermsModal, setShowTermsModal] = useState(false);
  const [showForgotPasswordModal, setShowForgotPasswordModal] = useState(false);
  const [showUserInfo, setShowUserInfo] = useState(false);

  // const [notifications, setNotifications] = useState([
  //   { id: 1, message: 'ZoI breach detection' },
  //   { id: 2, message: 'ZoI max speed breach' },
  //   { id: 3, message: 'Kraken spotted' },
  // ]);
  const {
    notifications,
    unreadNotificationsCount,
    setNotifications,
    setUnreadNotificationsCount
  } = useContext(NotificationContext);

  const [showNotifications, setShowNotifications] = useState(false);

  const reloadNotifications = async () => {
    const notifications = await getNotifications();
    setNotifications(notifications);
    const unreadNotificationsCount = await getUnreadNotificationsCount();
    setUnreadNotificationsCount(unreadNotificationsCount);
  };

  const dismissNotification = async (id) => {
    try {
      await dismissNotification(id);
      await reloadNotifications();
    } catch (e) {
      console.error("Could not dismiss notification", e);
    }
  };

  const dismissAll = async () => {
    try {
      await dismissAllNotifications();
      await reloadNotifications();
    } catch (e) {
      console.error("Could not dismiss notifications", e);
    }
    setShowNotifications(false);
  };

  return (
    <div className='h-[10vh] bg-[#E8E8E8] flex flex-row justify-between content-center px-3'>
      <div className='flex flex-row content-center h-2/3 my-auto'>
        <img
          src={ShipShipGoLogo}
          alt="logo"
          className="cursor-pointer"
          onClick={() => {
            window.location.reload();
          }}
        />
        <NavbarItem
          label="Help"
          onClick={() => {setShowHelpModal(true)}}
        />
      </div>
      <div className='flex flex-row content-center h-2/3 my-auto'>
      {authContext.user ? (
        <>
          <div className="flex flex-row items-center justify-between gap-4">
            {authContext.user.username === 'admin' && (
              <AdminExportButton />
            )}
            <div className="flex flex-row items-center justify-between gap-4"></div>
            <div className="relative cursor-pointer" onClick={() => setShowNotifications(prev => !prev)}>
              <img
                src={NavyBell}
                alt="Notifications"
                className="w-6 h-6"
              />
              {unreadNotificationsCount > 0 && (
                <span className="absolute -top-2 -right-2 bg-red-600 text-white text-xs font-semibold px-1.5 py-0.5 rounded-full">
                  {unreadNotificationsCount}
                </span>
              )}
            </div>
            <div className="flex flex-row items-center justify-between gap-4"></div>
            <div className="relative cursor-pointer" onClick={() => setShowUserInfo(prev => !prev)}>
              <img
                src={BlueCaptain}
                alt="User Info"
                className="w-6 h-6"
              />
            </div>
            <NavbarItem
              label="Logout"
              onClick={() => authContext.handleLogout()}
            />
          </div>
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
      {showNotifications && (
        <div className="absolute top-[10vh] right-2 w-80 bg-white shadow-lg rounded-lg border z-50">
          <div className="p-4 border-b flex justify-between items-center">
            <span className="font-semibold text-gray-800">Notifications</span>
            <button
              onClick={dismissAll}
              className="text-xs text-blue-600 hover:underline"
            >
              Dismiss All
            </button>
          </div>
          <ul className="max-h-60 overflow-y-auto">
            {notifications.length === 0 ? (
              <li className="p-4 text-sm text-gray-500">No notifications</li>
            ) : (
              notifications.map(n => (
                <li key={n.id} className="px-4 py-3 flex justify-between items-center hover:bg-gray-50">
                  <span className="text-sm text-gray-800">{n.message}</span>
                  <button
                    onClick={() => dismissNotification(n.id)}
                    className="text-xs text-red-500 hover:underline ml-2"
                  >
                    Dismiss
                  </button>
                </li>
              ))
            )}
          </ul>
        </div>
      )}
      {showUserInfo && (
        <div className="absolute top-[10vh] right-20 w-72 bg-white shadow-lg rounded-lg border z-50">
          <div className="p-4 border-b flex justify-between items-center">
            <span className="font-semibold text-gray-800">User Info</span>
            <button
              onClick={() => setShowUserInfo(false)}
              className="text-xs text-blue-600 hover:underline"
            >
              Close
            </button>
          </div>
          <div className="p-4 text-sm text-gray-700">
            <p><strong>Username:</strong> {authContext.user.username}</p>
            <p><strong>Email:</strong> {authContext.user.email}</p>
            <p><strong>Role:</strong> {(authContext.user.username === 'admin' ? 'admin' : 'user')}</p>
          </div>
        </div>
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
        setShowForgotPasswordModal={setShowForgotPasswordModal}
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
        setShowTermsModal={setShowTermsModal}
      />
    )}
      {showHelpModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-8 rounded-lg w-full max-w-xl shadow-2xl text-left">
            <h3 className="text-xl font-semibold mb-4">Help</h3>

            <div className="text-sm text-gray-700 space-y-4">
              <p>Welcome to Marine Tracker! Here is how you can use this site.</p>

              <div>
                <p className="font-semibold">As a casual user:</p>
                <ul className="list-disc list-inside ml-4">
                  <li>You can zoom in and out of the map to view ship positions more accurately.</li>
                  <li>You can click on any ship and view information about it, such as its position, direction, and speed.</li>
                  <li>You can easily create an account by clicking on the Sign Up button to access more features.</li>
                </ul>
              </div>

              <div>
                <p className="font-semibold">As a logged in user:</p>
                <ul className="list-disc list-inside ml-4">
                  <li>You can add ships to your fleet and track them more easily by clicking on them and then the corresponding button 
                    in the top right popup.</li>
                  <li>You can view ship path history (last 12 hours) in the same way.</li>
                  <li>You can apply filters for ship type, speed, and region at the left of your screen.</li>
                  <li>You can draw Zones of Interest on the map to monitor specific areas by clicking the corresponding button, "Edit"
                    and then drawing on the map.</li>
                  <li>You can view any noticiations you receive by clicking the button at the top right of your screen.</li>
                </ul>
              </div>
            </div>

            <button
              onClick={() => setShowHelpModal(false)}
              className="mt-6 px-4 py-2 text-sm bg-blue-600 text-white hover:bg-blue-700 rounded"
            >
              Close
            </button>
          </div>
        </div>
      )}
      {showTermsModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-8 rounded-lg w-full max-w-xl shadow-2xl text-left">
            <h3 className="text-xl font-semibold mb-4">Terms and Conditions</h3>
            <div className="text-sm text-gray-700 space-y-4 max-h-[60vh] overflow-y-auto">
              <p>
                By signing up to Marine Tracker, you agree to the following terms:
              </p>
              <ul className="list-disc list-inside ml-4 space-y-2">
                <li>You will use this service lawfully and responsibly.</li>
                <li>You are aware that this is a university project and will treat it accordingly.</li>
                <li>Marine Tracker is not to be reproduced in part or in its entirety without explicit consent from its developers.</li>
                <li>You have read the README file at https://github.com/sdi2000150/MarineTracker/blob/main/README.md.</li>
                <li>You are aware that the data displayed in Marine Tracker is historic and publicly available, not live.</li>
                <li>You are advised to kindly report any bugs you may encounter to the developers of this site.</li>
                <li>If you have read thus far into the terms and conditions and are our professor, you are legally obligated to pass
                  all of us with a grade of 9 or above.
                </li>
                <li>These terms are subject to change at any time.</li>
              </ul>
            </div>
            <button
              onClick={() => setShowTermsModal(false)}
              className="mt-6 px-4 py-2 text-sm bg-blue-600 text-white hover:bg-blue-700 rounded"
            >
              Close
            </button>
          </div>
        </div>
      )}
      {showForgotPasswordModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-8 rounded-lg w-full max-w-md shadow-2xl">
            <h3 className="text-xl font-semibold mb-4 text-center">Reset Password</h3>
            <div className="text-sm text-gray-700 space-y-4 max-h-[60vh] overflow-y-auto">
              <p>
                This is a university project. As a result, password recovery is as of now unavailable. You can create a 
                new user account, manually reset the database or contact the demo admin. We are sorry for any inconvenience this
                may have caused.
              </p>
            </div>
            <button
              onClick={() => setShowForgotPasswordModal(false)}
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