import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api/axios'
import dayjs from 'dayjs'
import './PostPages.css'

const CATEGORIES = ['전체', '잡담', '질문', '정보공유', '취업정보', '스터디']

export default function PostListPage() {
  const [posts, setPosts] = useState([])
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [searchParams, setSearchParams] = useSearchParams()

  const category = searchParams.get('category') || ''
  const keyword = searchParams.get('keyword') || ''
  const page = parseInt(searchParams.get('page') || '0')

  useEffect(() => {
    setLoading(true)
    const params = { page, size: 20 }
    if (category) params.category = category
    if (keyword) params.keyword = keyword
    api.get('/posts', { params })
      .then(r => {
        setPosts(r.data.data.content)
        setTotalPages(r.data.data.totalPages)
      })
      .finally(() => setLoading(false))
  }, [category, keyword, page])

  const setParam = (key, value) => {
    const p = new URLSearchParams(searchParams)
    if (value) p.set(key, value); else p.delete(key)
    p.delete('page')
    setSearchParams(p)
  }

  return (
    <div>
      <div className="page-header">
        <h2>게시판</h2>
        <Link to="/posts/new" className="btn btn-primary">글쓰기</Link>
      </div>

      {/* 카테고리 필터 */}
      <div className="category-tabs">
        {CATEGORIES.map(c => (
          <button
            key={c}
            className={`tab ${(c === '전체' ? !category : category === c) ? 'active' : ''}`}
            onClick={() => setParam('category', c === '전체' ? '' : c)}
          >
            {c}
          </button>
        ))}
      </div>

      {/* 검색 */}
      <div className="search-bar">
        <input
          className="form-control"
          placeholder="검색어 입력..."
          defaultValue={keyword}
          onKeyDown={(e) => e.key === 'Enter' && setParam('keyword', e.target.value)}
        />
      </div>

      {loading ? (
        <div className="loading">로딩 중...</div>
      ) : (
        <>
          <div className="post-list">
            {posts.length === 0 ? (
              <p className="empty">게시글이 없습니다.</p>
            ) : posts.map(post => (
              <div key={post.id} className="post-item card">
                <div className="post-meta">
                  <span className="post-category">{post.category}</span>
                  <span className="post-campus">{post.campus}</span>
                </div>
                <Link to={`/posts/${post.id}`} className="post-title">
                  {post.title}
                </Link>
                <div className="post-info">
                  <span>{post.authorNickname}</span>
                  <span>{dayjs(post.createdAt).format('YYYY.MM.DD')}</span>
                  <span>👁 {post.viewCount}</span>
                  <span>💬 {post.commentCount}</span>
                  <span>❤️ {post.likeCount}</span>
                </div>
              </div>
            ))}
          </div>

          {/* 페이지네이션 */}
          <div className="pagination">
            <button
              disabled={page === 0}
              onClick={() => setParam('page', String(page - 1))}
            >이전</button>
            {Array.from({ length: totalPages }, (_, i) => (
              <button
                key={i}
                className={page === i ? 'active' : ''}
                onClick={() => setParam('page', String(i))}
              >{i + 1}</button>
            ))}
            <button
              disabled={page >= totalPages - 1}
              onClick={() => setParam('page', String(page + 1))}
            >다음</button>
          </div>
        </>
      )}
    </div>
  )
}
