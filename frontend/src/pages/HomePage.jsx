import { useEffect, useState } from 'react';
import { catalogApi } from '../api/endpoints';
import { useAuth } from '../auth/AuthContext';
import BookCard from '../components/BookCard';

/**
 * Home page: search books by title and browse the top-N books by rating or
 * wishlist count, matching the Figma home design.
 */
export default function HomePage() {
  const { user } = useAuth();
  const [query, setQuery] = useState('');
  const [topBy, setTopBy] = useState('rating'); // 'rating' | 'wishlist'
  const [topN, setTopN] = useState(5);
  const [books, setBooks] = useState([]);
  const [mode, setMode] = useState('top'); // 'top' | 'search'
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  async function loadTop() {
    setLoading(true);
    setError(null);
    try {
      const results = await catalogApi.top(topBy, topN);
      setBooks(results);
      setMode('top');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadTop();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [topBy, topN]);

  async function handleSearch(e) {
    e.preventDefault();
    if (!query.trim()) {
      loadTop();
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const results = await catalogApi.search(query.trim());
      setBooks(results);
      setMode('search');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <div className="page-heading">
        <h2>Hello, {user?.displayName}!</h2>
        <p>Discover your next favorite book</p>
      </div>

      <form className="search-bar" onSubmit={handleSearch}>
        <input
          placeholder="Search books by title…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <button className="btn btn-primary" type="submit">
          Search
        </button>
      </form>

      <div className="controls-row">
        <div className="top-controls">
          <span>Top</span>
          <input
            type="number"
            min="1"
            max="20"
            value={topN}
            onChange={(e) => setTopN(Number(e.target.value) || 1)}
          />
          <span>books by</span>
          <div className="toggle-group">
            <button
              className={topBy === 'rating' ? 'active' : ''}
              onClick={() => setTopBy('rating')}
              type="button"
            >
              Rating
            </button>
            <button
              className={topBy === 'wishlist' ? 'active' : ''}
              onClick={() => setTopBy('wishlist')}
              type="button"
            >
              Wishlist count
            </button>
          </div>
        </div>
        {mode === 'search' && (
          <button className="btn btn-ghost" type="button" onClick={loadTop}>
            Clear search
          </button>
        )}
      </div>

      {error && <div className="error-banner">{error}</div>}
      {loading ? (
        <p className="empty-state">Loading books…</p>
      ) : books.length === 0 ? (
        <p className="empty-state">No books found. Try a different search.</p>
      ) : (
        <div className="book-grid">
          {books.map((book) => (
            <BookCard key={book.id} book={book} />
          ))}
        </div>
      )}
    </div>
  );
}
