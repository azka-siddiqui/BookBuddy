import { useEffect, useState } from 'react';
import { clubsApi, recommendationsApi } from '../api/endpoints';

/**
 * Community page: find books you have in common with another reader (R8) and
 * browse / join all book clubs.
 */
export default function CommunityPage() {
  const [otherUser, setOtherUser] = useState('');
  const [common, setCommon] = useState(null);
  const [commonError, setCommonError] = useState(null);
  const [searching, setSearching] = useState(false);

  const [clubs, setClubs] = useState([]);
  const [clubsError, setClubsError] = useState(null);
  const [joinStatus, setJoinStatus] = useState({});

  useEffect(() => {
    clubsApi.list().then(setClubs).catch((e) => setClubsError(e.message));
  }, []);

  async function findCommon(e) {
    e.preventDefault();
    if (!otherUser.trim()) return;
    setSearching(true);
    setCommonError(null);
    setCommon(null);
    try {
      const result = await recommendationsApi.common(otherUser.trim());
      setCommon(result);
    } catch (err) {
      setCommonError(
        err.status === 404 ? `No reader found with username "${otherUser}".` : err.message
      );
    } finally {
      setSearching(false);
    }
  }

  async function joinClub(clubId) {
    setJoinStatus((s) => ({ ...s, [clubId]: 'joining' }));
    try {
      const updated = await clubsApi.join(clubId);
      setClubs((list) => list.map((c) => (c.id === clubId ? updated : c)));
      setJoinStatus((s) => ({ ...s, [clubId]: 'joined' }));
    } catch (err) {
      setJoinStatus((s) => ({ ...s, [clubId]: err.message }));
    }
  }

  return (
    <div>
      <section className="card section-card">
        <h2 className="section-title">👥 Books in Common</h2>
        <p className="empty-state">
          See which books you and another reader have both tracked.
        </p>
        <form className="search-bar" onSubmit={findCommon}>
          <input
            placeholder="Enter a reader's username (e.g. sam)"
            value={otherUser}
            onChange={(e) => setOtherUser(e.target.value)}
          />
          <button className="btn btn-primary" type="submit" disabled={searching}>
            {searching ? 'Searching…' : 'Find'}
          </button>
        </form>

        {commonError && <div className="error-banner">{commonError}</div>}
        {common && common.length === 0 && (
          <p className="empty-state">No books in common yet.</p>
        )}
        {common && common.length > 0 && (
          <div className="book-grid">
            {common.map((book) => (
              <article className="book-card" key={book.id}>
                <h3 className="book-title">{book.title}</h3>
                {book.authors?.length > 0 && (
                  <p className="book-authors">by {book.authors.join(', ')}</p>
                )}
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="card section-card">
        <h2 className="section-title">📚 Book Clubs</h2>
        {clubsError && <div className="error-banner">{clubsError}</div>}
        {clubs.length === 0 ? (
          <p className="empty-state">No clubs available.</p>
        ) : (
          <div className="book-grid">
            {clubs.map((club) => {
              const status = joinStatus[club.id];
              const full = club.full;
              return (
                <div className="club-card" key={club.id}>
                  <h3 className="book-title">{club.name}</h3>
                  <p className="club-reason">
                    {club.genres?.join(', ')} · {club.memberCount}/{club.capacity} members
                  </p>
                  {status === 'joined' ? (
                    <span className="book-status is-ok">Joined ✓</span>
                  ) : (
                    <button
                      className="btn btn-primary"
                      disabled={status === 'joining' || full}
                      onClick={() => joinClub(club.id)}
                    >
                      {full ? 'Full' : status === 'joining' ? 'Joining…' : 'Join club'}
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
