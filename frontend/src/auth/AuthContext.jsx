import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../api/endpoints';
import { getToken, setToken } from '../api/client';

const AuthContext = createContext(null);

const USER_KEY = 'bookbuddy.user';

function loadStoredUser() {
  const raw = localStorage.getItem(USER_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(loadStoredUser);
  const [initializing, setInitializing] = useState(true);

  // On mount, if we have a token but no user, verify it against /me.
  useEffect(() => {
    const token = getToken();
    if (token && !user) {
      authApi
        .me()
        .then((me) => persistUser(me))
        .catch(() => logout())
        .finally(() => setInitializing(false));
    } else {
      setInitializing(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function persistUser(u) {
    const normalized = {
      userId: u.userId,
      username: u.username,
      displayName: u.displayName,
    };
    setUser(normalized);
    localStorage.setItem(USER_KEY, JSON.stringify(normalized));
  }

  async function login(username, password) {
    const res = await authApi.login(username, password);
    setToken(res.token);
    persistUser(res);
    return res;
  }

  async function register(username, displayName, password) {
    const res = await authApi.register(username, displayName, password);
    setToken(res.token);
    persistUser(res);
    return res;
  }

  function logout() {
    setToken(null);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }

  const value = useMemo(
    () => ({ user, initializing, login, register, logout }),
    [user, initializing]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
