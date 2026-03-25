import { useState, useEffect, useRef } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'
import RichEditor from '../components/RichEditor'

const CATEGORIES = ['튜토리얼', '아키텍처', '알고리즘', 'DevOps', '데이터베이스', '프론트엔드', '백엔드', '기타']

function isContentEmpty(html) {
  if (!html) return true
  const stripped = html.replace(/<[^>]*>/g, '').trim()
  return stripped.length === 0
}

function draftKey(id) { return `doc_draft_${id || 'new'}` }

export default function DocFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)

  const [form, setForm] = useState({ title: '', content: '', category: '기타', tags: [] })
  const [tagInput, setTagInput] = useState('')
  const [isDirty, setIsDirty] = useState(false)
  const [hasDraft, setHasDraft] = useState(false)
  const [saving, setSaving] = useState(false)
  const [saveStatus, setSaveStatus] = useState('')
  const [error, setError] = useState('')
  const autoSaveTimer = useRef(null)

  // Load existing doc or check for draft
  useEffect(() => {
    const key = draftKey(id)
    if (isEdit) {
      api.get(`/docs/${id}`).then(r => {
        const d = r.data.data
        setForm({
          title: d.title || '',
          content: d.content || '',
          category: d.category || '기타',
          tags: d.tags ? d.tags.split(',').map(t => t.trim()).filter(Boolean) : [],
        })
        const saved = localStorage.getItem(key)
        if (saved) setHasDraft(true)
      }).catch(() => setError('문서를 불러오지 못했습니다.'))
    } else {
      const saved = localStorage.getItem(key)
      if (saved) setHasDraft(true)
    }
    return () => { if (autoSaveTimer.current) clearTimeout(autoSaveTimer.current) }
  }, [id])

  // Auto-save draft to localStorage (2s debounce)
  useEffect(() => {
    if (!isDirty) return
    if (autoSaveTimer.current) clearTimeout(autoSaveTimer.current)
    autoSaveTimer.current = setTimeout(() => {
      localStorage.setItem(draftKey(id), JSON.stringify({ ...form, savedAt: Date.now() }))
      setSaveStatus('saved')
      setTimeout(() => setSaveStatus(''), 2500)
    }, 2000)
  }, [form, isDirty, id])

  // Warn on browser close/refresh when dirty
  useEffect(() => {
    const handler = (e) => {
      if (isDirty) { e.preventDefault(); e.returnValue = '' }
    }
    window.addEventListener('beforeunload', handler)
    return () => window.removeEventListener('beforeunload', handler)
  }, [isDirty])

  const update = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }))
    setIsDirty(true)
    setError('')
  }

  // Restore draft
  const restoreDraft = () => {
    try {
      const d = JSON.parse(localStorage.getItem(draftKey(id)) || '{}')
      setForm({
        title: d.title || '',
        content: d.content || '',
        category: d.category || '기타',
        tags: Array.isArray(d.tags) ? d.tags : [],
      })
      setHasDraft(false)
      setIsDirty(true)
    } catch { setHasDraft(false) }
  }

  // Tag chip helpers
  const addTag = () => {
    const t = tagInput.trim().replace(/,$/, '')
    if (!t || form.tags.includes(t) || form.tags.length >= 8) return
    update('tags', [...form.tags, t])
    setTagInput('')
  }
  const removeTag = tag => update('tags', form.tags.filter(t => t !== tag))
  const handleTagKey = (e) => {
    if (e.key === 'Enter' || e.key === ',') { e.preventDefault(); addTag() }
    else if (e.key === 'Backspace' && !tagInput && form.tags.length > 0) {
      removeTag(form.tags[form.tags.length - 1])
    }
  }

  const handleSubmit = async () => {
    setError('')
    if (!form.title.trim()) { setError('제목을 입력해 주세요.'); return }
    if (isContentEmpty(form.content)) { setError('내용을 입력해 주세요.'); return }

    setSaving(true)
    const payload = {
      title: form.title.trim(),
      content: form.content,
      category: form.category,
      tags: form.tags.length > 0 ? form.tags.join(', ') : null,
      markdown: false,
    }
    try {
      if (isEdit) {
        await api.put(`/docs/${id}`, payload)
        localStorage.removeItem(draftKey(id))
        setIsDirty(false)
        navigate(`/docs/${id}`)
      } else {
        const r = await api.post('/docs', payload)
        localStorage.removeItem(draftKey(id))
        setIsDirty(false)
        navigate(`/docs/${r.data.data.id}`)
      }
    } catch (err) {
      setError(
        err.response?.data?.error?.message ||
        err.response?.data?.message ||
        '저장에 실패했습니다. 다시 시도해 주세요.'
      )
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="doc-editor-page">

      {/* ── Sticky toolbar ───────────────────────────────── */}
      <div className="doc-editor-bar">
        <button className="btn btn-ghost btn-sm" onClick={() => navigate(-1)}>← 뒤로</button>
        <span className="doc-editor-bar-title">{isEdit ? '문서 수정' : '새 문서 작성'}</span>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          {saveStatus === 'saved' && (
            <span className="doc-save-status saved">✓ 초안 저장됨</span>
          )}
          <button
            className="btn btn-ghost btn-sm"
            onClick={() => navigate(-1)}
            disabled={saving}
          >취소</button>
          <button
            className="btn btn-blue btn-md"
            onClick={handleSubmit}
            disabled={saving}
          >{saving ? '저장 중...' : isEdit ? '수정 완료' : '발행하기'}</button>
        </div>
      </div>

      {/* ── Draft restore banner ─────────────────────────── */}
      {hasDraft && (
        <div className="doc-draft-banner">
          <span>💾 이전에 작성 중이던 임시 초안이 있습니다.</span>
          <button className="btn btn-ghost btn-xs" onClick={restoreDraft}>복원하기</button>
          <button
            className="btn btn-ghost btn-xs"
            style={{ color: 'var(--t4)' }}
            onClick={() => { localStorage.removeItem(draftKey(id)); setHasDraft(false) }}
          >무시</button>
        </div>
      )}

      {/* ── Main layout ──────────────────────────────────── */}
      <div className="doc-editor-layout">

        {/* ─ Content area ─ */}
        <div className="doc-editor-main">
          <input
            className="doc-title-input"
            placeholder="제목을 입력하세요..."
            value={form.title}
            onChange={e => update('title', e.target.value)}
            maxLength={200}
          />
          <div style={{ marginTop: 12 }}>
            <RichEditor
              value={form.content}
              onChange={val => update('content', val)}
              placeholder="본문을 작성하세요. 헤더, 이미지, 코드 블록 등을 자유롭게 활용하세요."
              height={540}
            />
          </div>
          {error && (
            <div className="alert alert-error" style={{ marginTop: 12 }}>{error}</div>
          )}
        </div>

        {/* ─ Settings sidebar ─ */}
        <aside className="doc-editor-sidebar-panel">

          {/* Category */}
          <div className="doc-editor-sidebar-block">
            <div className="doc-editor-sidebar-label">카테고리</div>
            <div className="doc-cat-grid">
              {CATEGORIES.map(c => (
                <button
                  key={c}
                  type="button"
                  className={`doc-cat-btn${form.category === c ? ' active' : ''}`}
                  onClick={() => update('category', c)}
                >{c}</button>
              ))}
            </div>
          </div>

          {/* Tags */}
          <div className="doc-editor-sidebar-block">
            <div className="doc-editor-sidebar-label">태그</div>
            <div
              className="tag-chip-input"
              onClick={e => e.currentTarget.querySelector('input')?.focus()}
            >
              {form.tags.map(t => (
                <span key={t} className="tag-chip">
                  {t}
                  <button type="button" onClick={() => removeTag(t)}>×</button>
                </span>
              ))}
              <input
                className="tag-chip-text-input"
                placeholder={form.tags.length > 0 ? '' : 'Enter / 쉼표로 추가...'}
                value={tagInput}
                onChange={e => setTagInput(e.target.value)}
                onKeyDown={handleTagKey}
                onBlur={addTag}
                disabled={form.tags.length >= 8}
              />
            </div>
            <div style={{ fontSize: '.73rem', color: 'var(--t5)', marginTop: 5 }}>
              최대 8개 · Enter 또는 쉼표로 추가
            </div>
          </div>

          {/* Keyboard shortcuts */}
          <div className="doc-editor-sidebar-block" style={{ background: 'var(--blue-xl)', border: '1px solid rgba(0,113,227,.14)' }}>
            <div className="doc-editor-sidebar-label" style={{ color: 'var(--blue)' }}>에디터 단축키</div>
            <div style={{ fontSize: '.76rem', color: 'var(--t3)', lineHeight: 2 }}>
              <code style={{ background: 'var(--surface)', padding: '1px 5px', borderRadius: 4, fontSize: '.72rem' }}>Ctrl+B</code> 굵게<br/>
              <code style={{ background: 'var(--surface)', padding: '1px 5px', borderRadius: 4, fontSize: '.72rem' }}>Ctrl+I</code> 기울임<br/>
              <code style={{ background: 'var(--surface)', padding: '1px 5px', borderRadius: 4, fontSize: '.72rem' }}>Ctrl+Z</code> 실행 취소<br/>
              <code style={{ background: 'var(--surface)', padding: '1px 5px', borderRadius: 4, fontSize: '.72rem' }}>Ctrl+Shift+Z</code> 재실행
            </div>
          </div>

          {/* Tips */}
          <div className="doc-editor-sidebar-block" style={{ background: 'var(--surface-2)', border: '1px solid var(--b1)' }}>
            <div className="doc-editor-sidebar-label">작성 팁</div>
            <ul style={{ fontSize: '.75rem', color: 'var(--t4)', lineHeight: 2, paddingLeft: 14 }}>
              <li>이미지는 툴바의 📷 아이콘으로 업로드</li>
              <li>코드는 코드 블록으로 가독성 향상</li>
              <li>헤더(H1~H3)로 목차가 자동 생성됨</li>
              <li>2초 후 자동으로 초안이 저장됩니다</li>
            </ul>
          </div>

        </aside>
      </div>
    </div>
  )
}
