import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/axios'
import dayjs from 'dayjs'

const TABS = ['대시보드', '회원 관리', '신고 처리']

export default function AdminPage() {
  const [tab, setTab] = useState('대시보드')
  const [stats, setStats] = useState(null)
  const [members, setMembers] = useState([])
  const [memberPage, setMemberPage] = useState(0)
  const [memberTotal, setMemberTotal] = useState(0)
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (tab === '대시보드') {
      setLoading(true)
      api.get('/admin/dashboard').then(r => setStats(r.data.data)).finally(() => setLoading(false))
    }
    if (tab === '회원 관리') {
      setLoading(true)
      api.get('/admin/members', { params: { page:0, size:20 } }).then(r => {
        setMembers(r.data.data?.content || [])
        setMemberTotal(r.data.data?.totalElements || 0)
      }).finally(() => setLoading(false))
    }
    if (tab === '신고 처리') {
      setLoading(true)
      api.get('/admin/reports').then(r => setReports(r.data.data || [])).finally(() => setLoading(false))
    }
  }, [tab])

  const handleBan = async (memberId, banned) => {
    await api.patch(`/admin/members/${memberId}/${banned ? 'unban' : 'ban'}`)
    api.get('/admin/members', { params: { page:0, size:20 } }).then(r => setMembers(r.data.data?.content || []))
  }

  const handleResolve = async (reportId) => {
    await api.patch(`/admin/reports/${reportId}/resolve`)
    setReports(prev => prev.map(r => r.id === reportId ? { ...r, resolved:true } : r))
  }

  return (
    <div>
      <h2>관리자 대시보드</h2>
      <div style={{ display:'flex', gap:8, marginBottom:24, borderBottom:'1px solid var(--color-border)' }}>
        {TABS.map(t => (
          <button key={t} onClick={() => setTab(t)}
            style={{ padding:'8px 16px', border:'none', background:'none', cursor:'pointer',
              borderBottom: tab===t ? '2px solid var(--color-primary)' : '2px solid transparent',
              color: tab===t ? 'var(--color-primary)' : 'inherit', fontWeight: tab===t ? 600 : 400 }}>
            {t}
          </button>
        ))}
      </div>

      {loading && <div>로딩 중...</div>}

      {tab === '대시보드' && !loading && stats && (
        <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill,minmax(200px,1fr))', gap:16 }}>
          {[
            ['총 회원수', stats.totalMembers],
            ['총 게시글', stats.totalPosts],
            ['총 이벤트', stats.totalEvents],
            ['총 프로젝트', stats.totalProjects],
            ['미처리 신고', stats.pendingReports],
          ].map(([label, val]) => (
            <div key={label} className="card" style={{ padding:24, textAlign:'center' }}>
              <div style={{ fontSize:32, fontWeight:700, color:'var(--color-primary)', marginBottom:4 }}>{val ?? '-'}</div>
              <div style={{ fontSize:13, color:'var(--color-text-muted)' }}>{label}</div>
            </div>
          ))}
        </div>
      )}

      {tab === '회원 관리' && !loading && (
        <div>
          <div style={{ marginBottom:8, fontSize:14, color:'var(--color-text-muted)' }}>총 {memberTotal}명</div>
          <table style={{ width:'100%', borderCollapse:'collapse', fontSize:14 }}>
            <thead>
              <tr style={{ background:'var(--color-surface-secondary)', borderBottom:'1px solid var(--color-border)' }}>
                <th style={{ padding:'10px 12px', textAlign:'left' }}>ID</th>
                <th style={{ padding:'10px 12px', textAlign:'left' }}>닉네임</th>
                <th style={{ padding:'10px 12px', textAlign:'left' }}>이메일</th>
                <th style={{ padding:'10px 12px', textAlign:'left' }}>캠퍼스</th>
                <th style={{ padding:'10px 12px', textAlign:'left' }}>역할</th>
                <th style={{ padding:'10px 12px', textAlign:'left' }}>상태</th>
                <th style={{ padding:'10px 12px', textAlign:'left' }}>가입일</th>
                <th style={{ padding:'10px 12px', textAlign:'left' }}>관리</th>
              </tr>
            </thead>
            <tbody>
              {members.map(m => (
                <tr key={m.id} style={{ borderBottom:'1px solid var(--color-border)' }}>
                  <td style={{ padding:'10px 12px' }}>{m.id}</td>
                  <td style={{ padding:'10px 12px' }}><Link to={`/members/${m.id}`}>{m.nickname}</Link></td>
                  <td style={{ padding:'10px 12px', fontSize:12 }}>{m.email}</td>
                  <td style={{ padding:'10px 12px' }}>{m.campus}</td>
                  <td style={{ padding:'10px 12px' }}>
                    <span style={{ fontSize:11, padding:'2px 6px', borderRadius:3, background: m.role==='ADMIN' ? '#fff3cd' : '#e9ecef' }}>{m.role}</span>
                  </td>
                  <td style={{ padding:'10px 12px' }}>
                    <span style={{ color: m.banned ? '#e74c3c' : 'green', fontSize:12 }}>{m.banned ? '정지' : '활성'}</span>
                  </td>
                  <td style={{ padding:'10px 12px', fontSize:12 }}>{m.createdAt?.slice(0,10)}</td>
                  <td style={{ padding:'10px 12px' }}>
                    {m.role !== 'ADMIN' && (
                      <button onClick={() => handleBan(m.id, m.banned)} className="btn" style={{ fontSize:12, padding:'3px 8px', background: m.banned ? 'green' : '#e74c3c', color:'#fff' }}>
                        {m.banned ? '활성화' : '정지'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === '신고 처리' && !loading && (
        <div>
          {reports.map(r => (
            <div key={r.id} className="card" style={{ padding:'14px 20px', marginBottom:8, display:'flex', justifyContent:'space-between', alignItems:'center', opacity: r.resolved ? 0.5 : 1 }}>
              <div>
                <div style={{ fontWeight:500 }}>{r.reportType} 신고</div>
                <div style={{ fontSize:13, color:'var(--color-text-muted)', marginTop:2 }}>{r.reason}</div>
                <div style={{ fontSize:11, color:'var(--color-text-muted)', marginTop:2 }}>
                  신고자: {r.reporterNickname} | {dayjs(r.createdAt).format('YYYY.MM.DD HH:mm')}
                </div>
              </div>
              {!r.resolved ? (
                <button onClick={() => handleResolve(r.id)} className="btn btn-primary" style={{ fontSize:12 }}>처리 완료</button>
              ) : (
                <span style={{ fontSize:12, color:'green' }}>처리됨</span>
              )}
            </div>
          ))}
          {reports.length === 0 && <p style={{ textAlign:'center', color:'var(--color-text-muted)', padding:40 }}>신고가 없습니다.</p>}
        </div>
      )}
    </div>
  )
}
