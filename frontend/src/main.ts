import { ApiResponse, EnrollRecord, ImportResult, SampleDataResult, SearchType } from './types.js';

const BASE_URL = '';

function getElement<T extends HTMLElement>(id: string): T {
  const el = document.getElementById(id);
  if (!el) throw new Error(`Element #${id} not found`);
  return el as T;
}

const csvInput = getElement<HTMLTextAreaElement>('csvInput');
const importBtn = getElement<HTMLButtonElement>('importBtn');
const importMsg = getElement<HTMLSpanElement>('importMsg');
const searchKeyword = getElement<HTMLInputElement>('searchKeyword');
const searchType = getElement<HTMLSelectElement>('searchType');
const searchBtn = getElement<HTMLButtonElement>('searchBtn');
const displayArea = getElement<HTMLDivElement>('displayArea');

async function fetchApi<T>(url: string, options?: RequestInit): Promise<ApiResponse<T>> {
  const res = await fetch(BASE_URL + url, options);
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: '网络错误' }));
    throw new Error(err.message || `HTTP ${res.status}`);
  }
  return res.json();
}

function renderTable(records: EnrollRecord[]): string {
  if (records.length === 0) {
    return '<p class="no-result">无匹配选课记录</p>';
  }
  return `
    <table>
      <thead>
        <tr>
          <th>学生ID</th>
          <th>课程ID</th>
          <th>课程名称</th>
          <th>课程类型</th>
        </tr>
      </thead>
      <tbody>
        ${records.map(r => `
          <tr>
            <td>${escapeHtml(r.studentId)}</td>
            <td>${escapeHtml(r.courseId)}</td>
            <td>${escapeHtml(r.courseName)}</td>
            <td>${escapeHtml(r.courseType)}</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `;
}

function renderClassified(classified: { [key: string]: EnrollRecord[] }): string {
  const order = ['专业课', '公共课', '选修课'];
  return order.map(type => {
    const records = classified[type] || [];
    return `
      <div class="category-group">
        <h3>${escapeHtml(type)} (${records.length} 条)</h3>
        ${renderTable(records)}
      </div>
    `;
  }).join('');
}

function escapeHtml(str: string): string {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

async function loadSampleData(): Promise<void> {
  try {
    displayArea.innerHTML = '<p class="loading">加载中...</p>';
    const res = await fetchApi<SampleDataResult>('/api/enrollment/sample');
    if (res.code === 200 && res.data) {
      displayArea.innerHTML = renderClassified(res.data.classified);
    }
  } catch (e) {
    displayArea.innerHTML = '<p class="no-result">加载样例数据失败，请确保后端服务已启动</p>';
  }
}

async function handleImport(): Promise<void> {
  const csvData = csvInput.value.trim();
  if (!csvData) {
    importMsg.textContent = '请输入CSV数据';
    importMsg.className = 'msg error';
    return;
  }
  try {
    importMsg.textContent = '处理中...';
    importMsg.className = 'msg';
    const formData = new URLSearchParams();
    formData.append('csvData', csvData);
    const res = await fetchApi<ImportResult>('/api/enrollment/import', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: formData.toString()
    });
    if (res.code === 200 && res.data) {
      importMsg.textContent = res.message;
      importMsg.className = 'msg success';
      displayArea.innerHTML = renderClassified(res.data.classified);
    } else {
      importMsg.textContent = res.message;
      importMsg.className = 'msg error';
    }
  } catch (e: any) {
    importMsg.textContent = e.message || '导入失败';
    importMsg.className = 'msg error';
  }
}

async function handleSearch(): Promise<void> {
  const keyword = searchKeyword.value.trim();
  if (!keyword) {
    displayArea.innerHTML = '<p class="no-result">请输入检索关键词</p>';
    return;
  }
  const type = searchType.value as SearchType;
  try {
    displayArea.innerHTML = '<p class="loading">检索中...</p>';
    const params = new URLSearchParams({ keyword, type });
    const res = await fetchApi<EnrollRecord[]>(`/api/enrollment/search?${params}`);
    if (res.code === 200) {
      const records = res.data || [];
      displayArea.innerHTML = renderTable(records);
    }
  } catch (e: any) {
    displayArea.innerHTML = '<p class="no-result">检索失败: ' + escapeHtml(e.message || '未知错误') + '</p>';
  }
}

importBtn.addEventListener('click', handleImport);
searchBtn.addEventListener('click', handleSearch);
searchKeyword.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') handleSearch();
});

document.addEventListener('DOMContentLoaded', loadSampleData);
