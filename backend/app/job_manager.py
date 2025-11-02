"""Job Manager for tracking download jobs"""
from enum import Enum
from typing import Dict, Optional
from datetime import datetime


class JobStatus(str, Enum):
    PENDING = "pending"
    PROCESSING = "processing"
    COMPLETED = "completed"
    FAILED = "failed"
    CANCELLED = "cancelled"


class JobManager:
    """Manage download jobs and their status"""
    
    def __init__(self):
        self.jobs: Dict[str, Dict] = {}
    
    def create_job(
        self,
        job_id: str,
        url: str,
        quality: str,
        metadata: Optional[Dict] = None
    ):
        """Create a new job"""
        self.jobs[job_id] = {
            'job_id': job_id,
            'url': url,
            'quality': quality,
            'metadata': metadata,
            'status': JobStatus.PENDING,
            'progress': 0.0,
            'current_line': 'Job created',
            'error': None,
            'result_file': None,
            'created_at': datetime.now().isoformat(),
            'updated_at': datetime.now().isoformat()
        }
    
    def update_job(
        self,
        job_id: str,
        status: Optional[JobStatus] = None,
        progress: Optional[float] = None,
        current_line: Optional[str] = None,
        error: Optional[str] = None,
        result_file: Optional[str] = None
    ):
        """Update job status"""
        if job_id not in self.jobs:
            return
        
        job = self.jobs[job_id]
        
        if status is not None:
            job['status'] = status
        if progress is not None:
            job['progress'] = progress
        if current_line is not None:
            job['current_line'] = current_line
        if error is not None:
            job['error'] = error
        if result_file is not None:
            job['result_file'] = result_file
        
        job['updated_at'] = datetime.now().isoformat()
    
    def get_job(self, job_id: str) -> Optional[Dict]:
        """Get job details"""
        return self.jobs.get(job_id)
    
    def delete_job(self, job_id: str):
        """Delete a job"""
        if job_id in self.jobs:
            del self.jobs[job_id]
    
    def list_jobs(self) -> Dict[str, Dict]:
        """List all jobs"""
        return self.jobs
