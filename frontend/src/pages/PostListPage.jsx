import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

const CATEGORIES = ['', '일반', '질문', '정보', '공지', '잡담']
const CAT_LABEL = { '': '전체' }

export default function PostListPage() {
  const { member } = useAuthStore()
  const [posts, setPosts] = useState([])
  const [topLiked, setTopLiked] = useState([])
  const [topViewed, setTopViewed] = useState([])
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [searchParams, setSearchParams] = useSearchParams()
  const [kw, setKw] = useState(searchParams.get('keyword') || '')

  const category = searchParams.get('category') || ''
  const keyword = searchParams.get('keyword') || ''
  const sort = searchParams.get('sort') || 'latest'
  const page = parseInt(searchParams.get('page') || '0')

  useEffect(() => {
    setLoading(true)
    const params = { page, size: 20, sort }
    if (category) params.category = category
    if (keyword) params.keyword = keyword
    api.get('/posts', { params })
      .then(r => { setPosts(r.data.data.content); setTotalPages(r.data.data.totalPages) })
      .finally(() => setLoading(false))
  }, [category, keyword, sort, page])

  useEffect(() => {
    api.get('/posts', { params: { sort: 'popular', size: 5 } })
      .then(r => setTopLiked(r.data.data.content || []))
      .catch(() => {})
    api.get('/posts', { params: { sort: 'views', size: 5 } })
      .then(r => setTopViewed(r.data.data.content || []))
      .catch(() => {})
  }, [])

  const setParam = (key, value) => {
    const p = new URLSearchParams(searchParams)
    if (value) p.set(key, value); else p.delete(key)
    p.delete('page')
    setSearchParams(p)
  }

  const handleSearch = (e) => {
    e.preventDefault()
    setParam('keyword', kw)
  }

  return (
    <>
      <div className="page-header">
        <div className="container">
          <div className="page-header-inner">
            <div>
              <div className="label" style={{ marginBottom: 6 }}>Community</div>
              <h1>게시판</h1>
              <p>SSAFY인들의 이야기를 나눠보세요</p>
            </div>
            {member && <Link to="/posts/new" className="btn btn-blue btn-md">✏️ 글쓰기</Link>}
          </div>
        </div>
      </div>

      <div className="section-sm">
        <div className="container">
          <div className="two-col">
            <div>
              <div className="tabs">
                {CATEGORIES.map(c => (
                  <button key={c} onClick={() => setParam('category', c)}
                    className={`tab${category === c ? ' active' : ''}`}>
                    {CAT_LABEL[c]}
                  </button>
                ))}
              </div>

              <form className="search-row" style={{ marginBottom: 16 }} onSubmit={handleSearch}>
                <input name="keyword" className="search-input" placeholder="검색어 입력..."
                  value={kw} onChange={e => setKw(e.target.value)} />
                <select name="sort" className="search-select" value={sort} onChange={e => setParam('sort', e.target.value)}>
                  <option value="latest">최신순</option>
                  <option value="views">조회순</option>
                  <option value="popular">인기순</option>
                </select>
                <button type="submit" className="btn btn-blue btn-md">검색</button>
              </form>

              {loading ? (
                <div className="empty"><div className="empty-icon">⏳</div></div>
              ) : (
                <>
                  <div className="card card-flush">
                    <div className="table-wrap">
                      <table className="table">
                        <thead>
                          <tr>
                            <th>제목</th><th>카테고리</th><th>작성자</th><th>조회</th><th>좋아요</th><th>날짜</th>
                          </tr>
                        </thead>
                        <tbody>
                          {posts.length === 0 ? (
                            <tr><td colSpan={6} style={{ textAlign: 'center', padding: '40px 0', color: 'var(--t4)' }}>게시글이 없습니다</td></tr>
                          ) : posts.map(post => (
                            <tr key={post.id}>
                              <td><Link to={`/posts/${post.id}`} className="table-link">{post.title}</Link></td>
                              <td><span className="pill pill-gray">{post.category}</span></td>
                              <td>{post.authorNickname}</td>
                              <td>{post.viewCount}</td>
                              <td>{post.likeCount}</td>
                              <td style={{ color: 'var(--t5)', fontSize: '.8rem' }}>{dayjs(post.createdAt).format('MM.DD')}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {totalPages > 1 && (
                    <div className="pagination">
                      {page > 0 && <span onClick={() => setParam('page', String(page - 1))} style={{ cursor: 'pointer' }}>‹</span>}
                      {Array.from({ length: totalPages }, (_, i) => (
                        <span key={i} onClick={() => setParam('page', String(i))}
                          className={page === i ? 'active' : ''} style={{ cursor: 'pointer' }}>{i + 1}</span>
                      ))}
                      {page < totalPages - 1 && <span onClick={() => setParam('page', String(page + 1))} style={{ cursor: 'pointer' }}>›</span>}
                    </div>
                  )}
                </>
              )}
            </div>

            <aside>
              <div className="sidebar-block">
                <div className="sidebar-block-head">🔥 인기 게시글</div>
                <div className="sidebar-block-body">
                  {topLiked.map(p => (
                    <div key={p.id} className="sidebar-row">
                      <Link to={`/posts/${p.id}`} className="sidebar-row-title">{p.title}</Link>
                      <span className="sidebar-row-meta">❤ {p.likeCount}</span>
                    </div>
                  ))}
                </div>
              </div>
              <div className="sidebar-block">
                <div className="sidebar-block-head">👁 많이 본 글</div>
                <div className="sidebar-block-body">
                  {topViewed.map(p => (
                    <div key={p.id} className="sidebar-row">
                      <Link to={`/posts/${p.id}`} className="sidebar-row-title">{p.title}</Link>
                      <span className="sidebar-row-meta">👁 {p.viewCount}</span>
                    </div>
                  ))}
                </div>
              </div>
              {member && (
                <Link to="/posts/new" className="btn btn-blue btn-md" style={{ width: '100%', textAlign: 'center' }}>✏️ 글쓰기</Link>
              )}
            </aside>
          </div>
        </div>
      </div>
    </>
  )
}
