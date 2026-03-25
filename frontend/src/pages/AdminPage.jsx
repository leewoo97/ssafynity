import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/axios'
import { useAuthStore } from '../store/authStore'
import dayjs from 'dayjs'

export default function AdminPage() {
  const { member } = useAuthStore()
  const navigate = useNavigate()
  const [stats, setStats] = useState({})
  const [members, setMembers] = useState([])
  const [reports, setReports] = useState([])
  const [tab, setTab] = useState('stats')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (member?.role !== 'ADMIN') { navigate('/'); return }
    Promise.all([
      api.get('/admin/dashboard').catch(() => ({ data: { data: {} } })),
      api.get('/admin/members').catch(() => ({ data: { data: [] } })),
      api.get('/admin/reports').catch(() => ({ data: { data: [] } })),
    ]).then(([s, m, r]) => {
      setStats(s.data.data || {})
      setMembers(m.data.data || [])
      setReports(r.data.data || [])
    }).finally(() => setLoading(false))
  }, [member])

  const deleteMember = async (id) => {
    if (!window.confirm('이 회원을 삭제하시겠습니까? 복구할 수 없습니다.')) return
    await api.delete(`/admin/members/${id}`)
    setMembers(members.filter(m => m.id !== id))
  }

  const resolveReport = async (id) => {
    await api.post(`/admin/reports/${id}/resolve`)
    setReports(reports.filter(r => r.id !== id))
  }

  if (loading) return <div className="empty"><div className="empty-icon">⏳</div></div>

  return (
    <div className="section-sm"><div className="container">
    <div>
      <div className="section-head"><h2>관리자 대시보드</h2></div>

      {/* 통계 */}
      <div className="stat-grid" style={{ marginBottom: 32 }}>
        {[
          { label: '전체 회원', value: stats.totalMembers ?? '-', icon: '👥' },
          { label: '오늘 가입', value: stats.todayJoins ?? '-', icon: '✨' },
          { label: '전체 게시글', value: stats.totalPosts ?? '-', icon: '📝' },
          { label: '신고 접수', value: stats.pendingReports ?? '-', icon: '🚨' },
        ].map(s => (
          <div key={s.label} className="stat-card">
            <div style={{ fontSize: 28, marginBottom: 8 }}>{s.icon}</div>
            <div style={{ fontSize: '1.8rem', fontWeight: 700, color: 'var(--blue)' }}>{s.value}</div>
            <div style={{ fontSize: '.82rem', color: 'var(--t4)', marginTop: 2 }}>{s.label}</div>
          </div>
        ))}
      </div>

      <div className="tabs">
        <button className={`tab${tab === 'stats' ? ' active' : ''}`} onClick={() => setTab('stats')}>통계</button>
        <button className={`tab${tab === 'members' ? ' active' : ''}`} onClick={() => setTab('members')}>회원 관리</button>
        <button className={`tab${tab === 'reports' ? ' active' : ''}`} onClick={() => setTab('reports')}>신고 관리</button>
      </div>

      {tab === 'members' && (
        <div className="card" style={{ padding: 0, marginTop: 16 }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '.88rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--b1)' }}>
                {['ID', '닉네임', '이메일', '캠퍼스', '역할', '상태', '가입일', '관리'].map(h => (
                  <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: 'var(--t3)' }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {members.map(m => (
                <tr key={m.id} style={{ borderBottom: '1px solid var(--b1)' }}>
                  <td style={{ padding: '10px 16px', color: 'var(--t4)' }}>{m.id}</td>
                  <td style={{ padding: '10px 16px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <div className="av av-xs">{m.nickname?.charAt(0)?.toUpperCase()}</div>
                      {m.nickname}
                    </div>
                  </td>
                  <td style={{ padding: '10px 16px', color: 'var(--t3)' }}>{m.email}</td>
                  <td style={{ padding: '10px 16px' }}>{m.campus}</td>
                  <td style={{ padding: '10px 16px' }}>
                    <span className={`pill ${m.role === 'ADMIN' ? 'pill-blue' : 'pill-gray'}`}>{m.role}</span>
                  </td>
                  <td style={{ padding: '10px 16px' }}>
                    <span className={`pill ${m.status === 'BANNED' ? 'pill-red' : 'pill-green'}`}>
                      {m.status === 'BANNED' ? '정지' : '활성'}
                    </span>
                  </td>
                  <td style={{ padding: '10px 16px', color: 'var(--t4)' }}>{dayjs(m.createdAt).format('YY.MM.DD')}</td>
                  <td style={{ padding: '10px 16px' }}>
                    {m.role !== 'ADMIN' && (
                      <button className="btn btn-danger btn-xs" onClick={() => deleteMember(m.id)}>삭제</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'reports' && (
        <div className="card" style={{ padding: 0, marginTop: 16 }}>
          {reports.length === 0 ? (
            <div className="empty" style={{ padding: 40 }}>
              <div className="empty-icon">✅</div>
              <div className="empty-title">처리할 신고가 없습니다</div>
            </div>
          ) : (
            reports.map(r => (
              <div key={r.id} className="post-row" style={{ padding: '14px 20px' }}>
                <div className="post-row-main">
                  <div className="post-row-title">{r.targetType} #{r.targetId} — {r.reason}</div>
                  <div className="post-row-meta">
                    <span>신고자: {r.reporterNickname}</span>
                  </div>
                </div>
                <button className="btn btn-ghost btn-xs" onClick={() => resolveReport(r.id)}>처리</button>
              </div>
            ))
          )}
        </div>
      )}

      {tab === 'stats' && (
        <div className="card" style={{ marginTop: 16 }}>
          <p style={{ color: 'var(--t3)' }}>통계 차트 기능은 추후 추가 예정입니다.</p>
        </div>
      )}
    </div>
    </div></div>
  )
}
