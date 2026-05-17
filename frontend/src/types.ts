export interface EnrollRecord {
  studentId: string;
  courseId: string;
  courseName: string;
  courseType: string;
}

export type ClassifiedResult = Record<string, EnrollRecord[]>;

export interface ImportResult {
  total: number;
  duplicated: number;
  records: EnrollRecord[];
  classified: ClassifiedResult;
}

export interface SampleDataResult {
  records: EnrollRecord[];
  classified: ClassifiedResult;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T | null;
}

export interface CourseInfo {
  courseId: string;
  courseName: string;
  courseType: string;
  capacity: number;
  teacherName: string;
  credits: number;
  semester: string;
}

export type SearchType = 'studentId' | 'courseId' | 'courseName' | 'courseType';
