import Cookies from 'js-cookie';

const COOKIE_NAME = 'blogToken'; // <--- custom cookie name

export const isAuthenticated = () => !!Cookies.get(COOKIE_NAME);

export const logout = () => {
  Cookies.remove(COOKIE_NAME);
  localStorage.removeItem('token'); // optional
};
