import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import dayjs from 'dayjs'

export default function HomePage() {
  const [data, setData] = useState(null)

  useEffect(() => {
    api.get('/home').then(res => setData(res.data.data)).catch(() => {})
  }, [])

  const memberCount = data?.memberCount ?? 0
  const postCount = data?.postCount ?? 0
  const docCount = data?.docCount ?? 0
  const videoCount = data?.videoCount ?? 0
  const hotPosts = data?.hotPosts ?? []
  const topLiked = data?.topLiked ?? []
  const latestDocs = data?.latestDocs ?? []
  const pinnedDocs = data?.pinnedDocs ?? []
  const latestVideos = data?.latestVideos ?? []
  const upcomingEvents = data?.upcomingEvents ?? []

  return (
    <>
      {/* Hero */}
      <section className="hero">
        <div className="hero-glow"></div>
        <div className="container">
          <span className="hero-eyebrow">SSAFY 개발자 커뮤니티</span>
          <h1 className="headline-xl">
            함께 성장하는<br /><span className="gradient-text">SSAFYnity</span>
          </h1>
          <p className="hero-sub">
            기술 문서, 강의 영상, 스터디 이벤트, 프로젝트 쇼케이스까지<br />
            개발자를 위한 모든 것이 여기에 있습니다.
          </p>
          <div className="hero-btns">
            <Link to="/docs" className="btn btn-blue btn-lg">문서 탐색하기</Link>
            <Link to="/posts" className="btn btn-ghost btn-lg">커뮤니티 →</Link>
          </div>
        </div>
      </section>

      {/* Stats */}
      <div className="stats-strip">
        <div className="stats-row">
          <div className="stat-item">
            <span className="stat-num">{memberCount}</span>
            <span className="stat-lbl">멤버</span>
          </div>
          <div className="stat-item">
            <span className="stat-num">{postCount}</span>
            <span className="stat-lbl">게시글</span>
          </div>
          <div className="stat-item">
            <span className="stat-num">{docCount}</span>
            <span className="stat-lbl">기술 문서</span>
          </div>
          <div className="stat-item">
            <span className="stat-num">{videoCount}</span>
            <span className="stat-lbl">강의 영상</span>
          </div>
        </div>
      </div>

      {/* Quick Nav */}
      <div className="section-sm">
        <div className="container">
          <div className="quick-grid">
            <Link to="/posts" className="quick-item">
              <div className="quick-icon qi-blue">💬</div>
              <strong>커뮤니티</strong>
              <p>질문·정보·잡담</p>
            </Link>
            <Link to="/docs" className="quick-item">
              <div className="quick-icon qi-purple">📚</div>
              <strong>기술 문서</strong>
              <p>튜토리얼·아키텍처</p>
            </Link>
            <Link to="/videos" className="quick-item">
              <div className="quick-icon qi-red" style={{ background: 'rgba(255,59,48,.1)' }}>🎬</div>
              <strong>강의 영상</strong>
              <p>세미나·코드리뷰</p>
            </Link>
            <Link to="/events" className="quick-item">
              <div className="quick-icon qi-orange">🎯</div>
              <strong>이벤트</strong>
              <p>스터디·해커톤</p>
            </Link>
            <Link to="/projects" className="quick-item">
              <div className="quick-icon qi-teal">💻</div>
              <strong>프로젝트</strong>
              <p>포트폴리오·쇼케이스</p>
            </Link>
          </div>
        </div>
      </div>

      {/* Main 2-col */}
      <div className="section-sm">
        <div className="container">
          <div className="two-col">
            <div>
              {/* Hot Posts */}
              <div className="card card-flush" style={{ marginBottom: 16 }}>
                <div className="section-head" style={{ padding: '20px 24px 16px', margin: 0, borderBottom: '1px solid var(--b1)' }}>
                  <h3>🔥 이번 주 인기글</h3>
                  <Link to="/posts" className="btn btn-tinted btn-xs">전체보기</Link>
                </div>
                <div style={{ padding: '0 24px 8px' }}>
                  {hotPosts.length === 0 ? (
                    <div className="empty" style={{ padding: '40px 0' }}>
                      <div className="empty-icon">📭</div>
                      <div className="empty-title">게시글이 없습니다.</div>
                    </div>
                  ) : hotPosts.map(post => (
                    <div key={post.id} className="post-row">
                      <div className="post-row-main">
                        <Link to={`/posts/${post.id}`} className="post-row-title">{post.title}</Link>
                        <div className="post-row-meta">
                          <span className="pill pill-gray">{post.category}</span>
                          <span>{post.authorNickname}</span>
                          <span>조회 {post.viewCount}</span>
                        </div>
                      </div>
                      <div className="post-row-stats">
                        <span>👍 {post.likeCount}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Latest Docs */}
              <div className="card card-flush">
                <div className="section-head" style={{ padding: '20px 24px 16px', margin: 0, borderBottom: '1px solid var(--b1)' }}>
                  <h3>📚 최신 기술 문서</h3>
                  <Link to="/docs" className="btn btn-tinted btn-xs">전체보기</Link>
                </div>
                <div style={{ padding: '0 24px 8px' }}>
                  {latestDocs.length === 0 ? (
                    <div className="empty" style={{ padding: '40px 0' }}>
                      <div className="empty-icon">📄</div>
                      <div className="empty-title">문서가 없습니다.</div>
                    </div>
                  ) : latestDocs.map(doc => (
                    <div key={doc.id} className="post-row">
                      <div className="post-row-main">
                        <Link to={`/docs/${doc.id}`} className="post-row-title">{doc.title}</Link>
                        <div className="post-row-meta">
                          <span className="pill pill-gray">{doc.category}</span>
                          <span>{doc.authorNickname}</span>
                          {doc.tags && doc.tags.split(',').slice(0, 2).map((tag, i) => (
                            <span key={i} className="tag">{tag.trim()}</span>
                          ))}
                        </div>
                      </div>
                      <div className="post-row-stats">
                        <span>👁 {doc.viewCount}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Sidebar */}
            <aside>
              {pinnedDocs.length > 0 && (
                <div className="sidebar-block">
                  <div className="sidebar-block-head">📌 추천 문서</div>
                  <div className="sidebar-block-body">
                    {pinnedDocs.map(doc => (
                      <div key={doc.id} className="sidebar-row">
                        <div style={{ flex: 1, minWidth: 0 }}>
                          <Link to={`/docs/${doc.id}`} className="sidebar-row-title">{doc.title}</Link>
                          <div className="sidebar-row-meta">{doc.category}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="sidebar-block">
                <div className="sidebar-block-head">📅 다가오는 이벤트</div>
                <div className="sidebar-block-body">
                  {upcomingEvents.length === 0 ? (
                    <div style={{ padding: '16px 18px', fontSize: '.82rem', color: 'var(--t4)' }}>예정된 이벤트가 없습니다.</div>
                  ) : upcomingEvents.map(ev => (
                    <div key={ev.id} className="sidebar-row">
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <Link to={`/events/${ev.id}`} className="sidebar-row-title">{ev.title}</Link>
                        <div className="sidebar-row-meta">
                          <span className="pill pill-blue" style={{ fontSize: '.66rem' }}>{ev.eventType}</span>
                          <span>{ev.startDate ? dayjs(ev.startDate).format('MM.DD') : ''}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="sidebar-block">
                <div className="sidebar-block-head">🎬 최신 영상</div>
                <div className="sidebar-block-body">
                  {latestVideos.map(vid => (
                    <div key={vid.id} className="sidebar-row">
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <Link to={`/videos/${vid.id}`} className="sidebar-row-title">{vid.title}</Link>
                        <div className="sidebar-row-meta">{vid.category}</div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="sidebar-block">
                <div className="sidebar-block-head">👍 추천 TOP5</div>
                <div className="sidebar-block-body">
                  {topLiked.map((post, i) => (
                    <div key={post.id} className="sidebar-row">
                      <div className="sidebar-row-num">{i + 1}</div>
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <Link to={`/posts/${post.id}`} className="sidebar-row-title">{post.title}</Link>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </aside>
          </div>
        </div>
      </div>
    </>
  )
}
