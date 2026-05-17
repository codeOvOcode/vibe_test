/*
 * ============================================================
 * 代码来源标注
 * ============================================================
 * AI工具: Sisyphus (DeepSeek-V4-Pro)
 * AI生成比例: 85%
 * AI生成部分: fetchApi封装、renderTable/renderClassified渲染函数、
 *             escapeHtml安全函数、handleImport/handleSearch事件处理、
 *             元素获取与事件绑定基础结构
 * 手动修改: 分类展示顺序固定为"专业课→公共课→选修课"、
 *           loadSampleData错误提示"请确保后端服务已启动"、
 *           Enter键搜索支持
 * 修改原因: 完善前后端衔接——固定展示顺序确保一致性，
 *           友好错误提示帮助定位后端未启动问题
 * 详见: AI提示词与代码标注.md → 提示词三
 * ============================================================
 */
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
const excelFileInput = getElement<HTMLInputElement>('excelFile');
const importExcelBtn = getElement<HTMLButtonElement>('importExcelBtn');
const importExcelMsg = getElement<HTMLSpanElement>('importExcelMsg');
const searchKeyword = getElement<HTMLInputElement>('searchKeyword');
const searchType = getElement<HTMLSelectElement>('searchType');
const searchBtn = getElement<HTMLButtonElement>('searchBtn');
const exportBtn = getElement<HTMLButtonElement>('exportBtn');
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

async function handleImportExcel(): Promise<void> {
  const file = excelFileInput.files?.[0];
  if (!file) {
    importExcelMsg.textContent = '请选择Excel文件';
    importExcelMsg.className = 'msg error';
    return;
  }
  if (!file.name.toLowerCase().endsWith('.xlsx')) {
    importExcelMsg.textContent = '仅支持 .xlsx 格式文件';
    importExcelMsg.className = 'msg error';
    return;
  }
  try {
    importExcelMsg.textContent = '上传处理中...';
    importExcelMsg.className = 'msg';
    const fd = new FormData();
    fd.append('file', file);
    const res = await fetchApi<ImportResult>('/api/enrollment/import-excel', {
      method: 'POST',
      body: fd
    });
    if (res.code === 200 && res.data) {
      importExcelMsg.textContent = res.message;
      importExcelMsg.className = 'msg success';
      displayArea.innerHTML = renderClassified(res.data.classified);
    } else {
      importExcelMsg.textContent = res.message;
      importExcelMsg.className = 'msg error';
    }
  } catch (e: any) {
    importExcelMsg.textContent = e.message || 'Excel导入失败';
    importExcelMsg.className = 'msg error';
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

function handleExport(): void {
  const link = document.createElement('a');
  link.href = '/api/enrollment/export';
  link.download = '';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

importBtn.addEventListener('click', handleImport);
importExcelBtn.addEventListener('click', handleImportExcel);
searchBtn.addEventListener('click', handleSearch);
exportBtn.addEventListener('click', handleExport);
searchKeyword.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') handleSearch();
});

document.addEventListener('DOMContentLoaded', loadSampleData);
