import { User } from './user.model';
import { Project } from './project.model';

export interface Task {
  id: number;
  title: string;
  description?: string;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  dueDate: string;
  projectId: number;
  project?: Project;
  assignedUser?: User;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  dueDate: string;
  projectId: number;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  status?: 'TODO' | 'IN_PROGRESS' | 'DONE';
  dueDate?: string;
  projectId?: number;
}

export interface UpdateTaskStatusRequest {
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
}

export interface AssignTaskRequest {
  userId: number;
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';


