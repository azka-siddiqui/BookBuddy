import { useState } from 'react';
import { catalogApi, profileApi } from '../api/endpoints';

/**
 * Book card matching the Figma design: title, author(s), page count, and the
 * three actions (Add to Wishlist, Start Reading, Already Read). Each action gives
 * lightweight inline feedback on success or failure.
 */
export default function BookCard({ book }) {
  const [status, setStatus] = useState(null);
  const [busy, setBusy] = useState(false);

  async function run(label, fn) {
    setBusy(true);
    setStatus(null);
    try {
      await fn();
      setStatus({ type: 'ok', message: label });
    } catch (err) {
      setStatus({ type: 'error', message: err.message });
    } finally {
      setBusy(false);
    }
  }

  const addToWishlist = () =>
    run('Added to wishlist', async () => {
      await catalogApi.addToWishlist(book.id);
      await profileApi.updateProgress(book.id, 'WISHLIST', 0);
    });

  const startReading = () =>
    run('Marked as reading', () => profileApi.updateProgress(book.id, 'IN_PROGRESS', 0));

  const alreadyRead = () =>
    run('Marked as read', () =>
      profileApi.updateProgress(book.id, 'FINISHED', book.pageCount || 0)
    );

  return (
    <article className="book-card">
      <h3 className="book-title">{book.title}</h3>
      {book.authors?.length > 0 && (
        <p className="book-authors">by {book.authors.join(', ')}</p>
      )}
      {book.pageCount > 0 && <span className="book-pages">{book.pageCount} pages</span>}

      <div className="book-actions">
        <button className="btn btn-outline" disabled={busy} onClick={addToWishlist}>
          Add to Wishlist
        </button>
        <button className="btn btn-outline" disabled={busy} onClick={startReading}>
          Start Reading
        </button>
        <button className="btn btn-outline" disabled={busy} onClick={alreadyRead}>
          Already Read
        </button>
      </div>

      {status && (
        <p className={`book-status ${status.type === 'error' ? 'is-error' : 'is-ok'}`}>
          {status.message}
        </p>
      )}
    </article>
  );
}
