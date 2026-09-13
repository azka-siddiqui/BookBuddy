import { useEffect, useState } from 'react';
import { clubsApi, recommendationsApi } from '../api/endpoints';
import BookCard from '../components/BookCard';

/**
 * Recommendations page: personalized book suggestions (R11) plus genre-based book
 * club recommendations (R12), matching the Figma recommendations design.
 */
export default function RecommendationsPage() {
  const [books, setBooks] = useState([]);
  const [clubs, setClubs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [joinStatus, setJoinStatus] = useState({});

  useEffect(() => {
    let active = true;
    Promise.all([recommendationsApi.books(6), clubsApi.recommendations(5)])
      .then(([bookRecs, clubRecs]) => {
        if (!active) return;
        setBooks(bookRecs);
        setClubs(clubRecs);
      })
      .catch((err) => active && setError(err.message))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, []);

  async function joinClub(clubId) {
    setJoinStatus((s) => ({ ...s, [clubId]: 'joining' }));
    try {
      await clubsApi.join(clubId);
      setJoinStatus((s) => ({ ...s, [clubId]: 'joined' }));
    } catch (err) {
      setJoinStatus((s) => ({ ...s, [clubId]: err.message }));
    }
  }

  if (loading) return <p className="empty-state">Loading recommendations…</p>;

  return (
    <div>
      {error && <div className="error-banner">{error}</div>}

      <section className="card section-card">
        <h2 className="section-title">⭐ Recommended Books</h2>
        {books.length === 0 ? (
          <p className="empty-state">
            Start reading a few books and we'll suggest more you might like.
          </p>
        ) : (
          <div className="book-grid">
            {books.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>
        )}
      </section>

      <section className="card section-card">
        <h2 className="section-title">👥 Book Club Recommendations</h2>
        {clubs.length === 0 ? (
          <p className="empty-state">No club recommendations yet.</p>
        ) : (
          <div className="book-grid">
            {clubs.map((club) => {
              const status = joinStatus[club.id];
              return (
                <div className="club-card" key={club.id}>
                  <h3 className="book-title">{club.name}</h3>
                  <p className="club-reason">{club.reason}</p>
                  {status === 'joined' ? (
                    <span className="book-status is-ok">Joined ✓</span>
                  ) : (
                    <button
                      className="btn btn-primary"
                      disabled={status === 'joining'}
                      onClick={() => joinClub(club.id)}
                    >
                      {status === 'joining' ? 'Joining…' : 'Join club'}
                    </button>
                  )}
                  {status && status !== 'joined' && status !== 'joining' && (
                    <p className="book-status is-error">{status}</p>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );
}
