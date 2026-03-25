import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function DocDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const [doc, setDoc] = useState(null)
  const [loading, setLoading] = useState(true)
  const [toc, setToc] = useState([])
  const [activeId, setActiveId] = useState('')
  const contentRef = useRef(null)

  useEffect(() => {
    api.get(`/docs/${id}`).then(r => setDoc(r.data.data)).finally(() => setLoading(false))
  }, [id])

  // Build TOC from rendered headings after doc loads
  useEffect(() => {
    if (!doc || !contentRef.current) return
    const headings = contentRef.current.querySelectorAll('h1, h2, h3')
    if (headings.length === 0) return
    const items = []
    headings.forEach((h, i) => {
      const hid = `doc-h-${i}`
      h.id = hid
      items.push({ id: hid, text: h.textContent.trim(), level: parseInt(h.tagName[1]) })
    })
    setToc(items)
  }, [doc])

  // Scroll spy
  useEffect(() => {
    if (toc.length === 0) return
    const observer = new IntersectionObserver(
      entries => {
        entries.forEach(e => { if (e.isIntersecting) setActiveId(e.target.id) })
      },
      { rootMargin: '-60px 0px -70% 0px', threshold: 0 }
    )
    toc.forEach(item => {
      const el = document.getElementById(item.id)
      if (el) observer.observe(el)
    })
    return () => observer.disconnect()
  }, [toc])

  const handleDelete = async () => {
    if (!window.confirm('삭제하시겠습니까?')) return
    await api.delete(`/docs/${id}`)
    navigate('/docs')
  }

  if (loading) return <div className="empty"><div className="empty-icon">⏳</div></div>
  if (!doc) return <div className="empty"><div className="empty-title">문서를 찾을 수 없습니다</div></div>

  const isAuthor = member?.id === doc.authorId
  const isAdmin = member?.role === 'ADMIN'

  return (
    <div className="section-sm"><div className="container">
      <div className="two-col">

        {/* ── Main content ─────────────────────────────────── */}
        <div>
          <div className="card">
            {/* Header */}
            <div className="post-header">
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', gap: 6, marginBottom: 10, flexWrap: 'wrap', alignItems: 'center' }}>
                  <span className="pill pill-blue">{doc.category}</span>
                  {doc.pinned && <span className="pill" style={{ background: 'rgba(255,149,0,.1)', color: 'var(--orange)' }}>📌 고정</span>}
                </div>
                <h1 className="post-title">{doc.title}</h1>
                {doc.tags && (
                  <div style={{ display: 'flex', gap: 5, flexWrap: 'wrap', marginTop: 10 }}>
                    {doc.tags.split(',').map(t => (
                      <span key={t} className="pill pill-gray">{t.trim()}</span>
                    ))}
                  </div>
                )}
              </div>
              {(isAuthor || isAdmin) && (
                <div style={{ display: 'flex', gap: 6, flexShrink: 0 }}>
                  <Link to={`/docs/${id}/edit`} className="btn btn-ghost btn-sm">수정</Link>
                  <button className="btn btn-danger btn-sm" onClick={handleDelete}>삭제</button>
                </div>
              )}
            </div>

            {/* Meta row */}
            <div className="post-row-meta" style={{ margin: '14px 0 24px' }}>
              <Link to={`/profile/${doc.authorId}`} style={{ display: 'flex', alignItems: 'center', gap: 8, textDecoration: 'none' }}>
                <span className="av av-sm">{doc.authorNickname?.charAt(0)?.toUpperCase()}</span>
                <span style={{ fontWeight: 600, color: 'var(--t1)', fontSize: '.88rem' }}>{doc.authorNickname}</span>
              </Link>
              <span style={{ color: 'var(--t5)', fontSize: '.8rem' }}>·</span>
              <span style={{ color: 'var(--t4)', fontSize: '.82rem' }}>{dayjs(doc.createdAt).format('YYYY.MM.DD HH:mm')}</span>
              {doc.updatedAt && doc.updatedAt !== doc.createdAt && (
                <span style={{ color: 'var(--t5)', fontSize: '.78rem' }}>
                  (수정됨 {dayjs(doc.updatedAt).format('MM.DD')})
                </span>
              )}
              <span style={{ color: 'var(--t4)', fontSize: '.82rem', marginLeft: 'auto' }}>👁 {doc.viewCount}</span>
            </div>

            {/* Body */}
            <div
              ref={contentRef}
              className="post-body md-body"
              dangerouslySetInnerHTML={{ __html: doc.content }}
            />

            {/* Footer actions */}
            <div className="post-actions">
              <Link to="/docs" className="btn btn-ghost btn-sm" style={{ marginRight: 'auto' }}>← 목록으로</Link>
              {(isAuthor || isAdmin) && (
                <>
                  <Link to={`/docs/${id}/edit`} className="btn btn-ghost btn-sm">수정</Link>
                  <button className="btn btn-danger btn-sm" onClick={handleDelete}>삭제</button>
                </>
              )}
              {member && !isAuthor && (
                <Link to={`/report?targetType=DOC&targetId=${doc.id}`} className="btn btn-ghost btn-sm">신고</Link>
              )}
            </div>
          </div>
        </div>

        {/* ── Sidebar ──────────────────────────────────────── */}
        <aside>
          {/* Author card */}
          <div className="sidebar-block" style={{ marginBottom: 14 }}>
            <div className="sidebar-block-head">✍️ 작성자</div>
            <div className="sidebar-block-body">
              <div style={{ padding: '16px 18px', display: 'flex', alignItems: 'center', gap: 12 }}>
                <div className="av av-md">{doc.authorNickname?.charAt(0)?.toUpperCase()}</div>
                <div>
                  <div style={{ fontSize: '.9rem', fontWeight: 600, color: 'var(--t1)' }}>{doc.authorNickname}</div>
                  <div style={{ fontSize: '.78rem', color: 'var(--t4)', marginTop: 2 }}>
                    {dayjs(doc.createdAt).format('YYYY년 MM월 DD일')} 작성
                  </div>
                </div>
              </div>
              <div style={{ padding: '0 18px 16px' }}>
                <Link to={`/profile/${doc.authorId}`} className="btn btn-ghost btn-sm" style={{ width: '100%', textAlign: 'center' }}>
                  프로필 보기
                </Link>
              </div>
            </div>
          </div>

          {/* TOC */}
          {toc.length > 0 && (
            <div className="sidebar-block">
              <div className="sidebar-block-head">📋 목차</div>
              <div className="sidebar-block-body" style={{ padding: '10px 14px' }}>
                <nav className="doc-toc-nav">
                  {toc.map(item => (
                    <a
                      key={item.id}
                      href={`#${item.id}`}
                      className={`doc-toc-link level-${item.level}${activeId === item.id ? ' active' : ''}`}
                      onClick={e => {
                        e.preventDefault()
                        document.getElementById(item.id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
                      }}
                    >{item.text}</a>
                  ))}
                </nav>
              </div>
            </div>
          )}
        </aside>

      </div>
    </div></div>
  )
}
