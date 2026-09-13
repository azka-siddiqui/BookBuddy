import { useEffect, useState } from 'react';
import { profileApi } from '../api/endpoints';
import { useAuth } from '../auth/AuthContext';

const TABS = [
  { key: 'WISHLIST', label: 'Wishlist' },
  { key: 'IN_PROGRESS', label: 'In Progress' },
  { key: 'FINISHED', label: 'Finished' },
  { key: 'ALL', label: 'Reading History' },
];

/**
 * Profile page: header, the four stat tiles (total / in-progress / completed /
 * day streak), and status-filtered reading lists, matching the Figma profile view.
 */
export default function ProfilePage() {
  const { user } = useAuth();
  const [summary, setSummary] = useState(null);
  const [activeTab, setActiveTab] = useState('WISHLIST');
  const [items, setItems] = useState([]);
  const [error, setError] = useState(null);
  const [loadingList, setLoadingList] = useState(false);

  useEffect(() => {
    profileApi.summary().then(setSummary).catch((e) => setError(e.message));
  }, []);

  useEffect(() => {
    setLoadingList(true);
    const status = activeTab === 'ALL' ? undefined : activeTab;
    profileApi
      .books(status)
      .then(setItems)
      .catch((e) => setError(e.message))
      .finally(() => setLoadingList(false));
  }, [activeTab]);

  const tiles = [
    { label: 'Total Books', value: summary?.totalBooks ?? '—' },
    { label: 'In Progress', value: summary?.inProgress ?? '—' },
    { label: 'Completed', value: summary?.completed ?? '—' },
    { label: 'Day Streak', value: summary?.dayStreak ?? '—', streak: true },
  ];

  return (
    <div>
      {error && <div className="error-banner">{error}</div>}

      <div className="card section-card">
        <div className="profile-header">
          <div className="profile-avatar">
            {(user?.displayName || '?').charAt(0).toUpperCase()}
          </div>
          <div className="profile-name">
            <h2>{user?.displayName}</h2>
            <span className="profile-persona">Casual Reader</span>
          </div>
        </div>

        <div className="stat-tiles">
          {tiles.map((t) => (
            <div key={t.label} className={`stat-tile${t.streak ? ' streak' : ''}`}>
              <div className="stat-value">{t.value}</div>
              <div className="stat-label">{t.label}</div>
            </div>
          ))}
        </div>
      </div>

      <div className="card section-card">
        <div className="tabs">
          {TABS.map((tab) => (
            <button
              key={tab.key}
              className={`tab${activeTab === tab.key ? ' active' : ''}`}
              onClick={() => setActiveTab(tab.key)}
            >
              {tab.label}
              {tab.key !== 'ALL' && summary && ` (${countFor(tab.key, summary)})`}
            </button>
          ))}
        </div>

        {loadingList ? (
          <p className="empty-state">Loading…</p>
        ) : items.length === 0 ? (
          <p className="empty-state">Nothing here yet.</p>
        ) : (
          <div className="book-grid">
            {items.map((item) => (
              <ProgressCard key={item.bookId} item={item} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

function countFor(status, summary) {
  switch (status) {
    case 'IN_PROGRESS':
      return summary.inProgress;
    case 'FINISHED':
      return summary.completed;
    case 'WISHLIST':
      return Math.max(
        summary.totalBooks - summary.inProgress - summary.completed,
        0
      );
    default:
      return summary.totalBooks;
  }
}

function ProgressCard({ item }) {
  return (
    <article className="book-card">
      <h3 className="book-title">{item.title || item.bookId}</h3>
      {item.authors?.length > 0 && (
        <p className="book-authors">by {item.authors.join(', ')}</p>
      )}
      <div className="detail-row">
        <span className="detail-label">Status</span>
        <span>{item.status}</span>
      </div>
      <div className="detail-row">
        <span className="detail-label">Page Reached</span>
        <span>{item.pageReached}</span>
      </div>
      <div className="detail-row">
        <span className="detail-label">Out of</span>
        <span>{item.totalPages}</span>
      </div>
    </article>
  );
}
