-- AI 调用使用量记录表
CREATE TABLE ai_usage (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    job_id VARCHAR(50),
    cost_cents BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_ai_usage_user_date ON ai_usage(user_id, created_at);
CREATE INDEX idx_ai_usage_job ON ai_usage(job_id);
