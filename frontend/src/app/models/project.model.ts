export interface Project {
  id: number;
  name: string;
  description?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ON_HOLD';
  dueDate: string;
  createdAt: string;
  updatedAt?: string;
  tasks?: Task[];
  assignedUsers?: User[];
  progress?: number;
}

export interface CreateProjectRequest {
  name: string;
  description?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  dueDate: string;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH';
  status?: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'ON_HOLD';
  dueDate?: string;
}

export interface ProjectStatistics {
  totalProjects: number;
  activeProjects: number;
  completedProjects: number;
  overdueProjects: number;
  averageProgress: number;
}

// Import interfaces that are referenced
interface Task {
  id: number;
  title: string;
  status: string;
  priority: string;
  dueDate: string;
}

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
}
