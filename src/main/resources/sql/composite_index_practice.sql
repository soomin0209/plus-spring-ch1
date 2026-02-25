# 1. 테이블 생성
CREATE TABLE events (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(100),
                        start_date DATE,
                        end_date DATE
);

# 2. 더미 데이터 넣기(약 1000건의 임의 날짜 데이터 생성)
INSERT INTO events (title, start_date, end_date)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT
    CONCAT('event', n),
    DATE_ADD('2025-01-01', INTERVAL FLOOR(RAND() * 90) DAY),
    DATE_ADD('2025-01-01', INTERVAL FLOOR(RAND() * 90) + 5 DAY)
FROM seq;

# 3. 복합 인덱스 생성
CREATE INDEX idx_start_end ON events(start_date, end_date);

# 4. 인덱스가 걸리는 경우
EXPLAIN FORMAT=TRADITIONAL
SELECT * FROM events
WHERE start_date = '2025-02-01'
  AND end_date = '2025-02-10';

EXPLAIN FORMAT=TRADITIONAL
SELECT * FROM events
WHERE start_date = '2025-02-10';

# 5. 인덱스가 안걸리는 경우
EXPLAIN FORMAT=TRADITIONAL
SELECT * FROM events
WHERE end_date = '2025-02-10';

# 순서 반대 인덱스 실험
DROP INDEX idx_start_end ON events;
CREATE INDEX idx_end_start ON events(end_date, start_date);

EXPLAIN FORMAT=TRADITIONAL
SELECT * FROM events
WHERE start_date = '2025-02-01'
  AND end_date = '2025-02-10';
# 인덱스 사용하긴 하지만 순서 일치시켜주는 것이 좋음



DROP TABLE events;
CREATE TABLE events (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(100),
                        start_date DATE,
                        end_date DATE,
                        any_date DATE
);

INSERT INTO events (title, start_date, end_date, any_date)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000
)
SELECT
    CONCAT('event', n),
    DATE_ADD('2025-01-01', INTERVAL FLOOR(RAND() * 90) DAY),
    DATE_ADD('2025-01-01', INTERVAL FLOOR(RAND() * 90) + 5 DAY),
    DATE_ADD('2025-01-01', INTERVAL FLOOR(RAND() * 90) + 5 DAY)

FROM seq;

CREATE INDEX idx_start_end ON events(start_date, end_date, any_date);

# 인덱스 걸림
EXPLAIN FORMAT=TRADITIONAL
SELECT * FROM events
WHERE start_date = '2025-02-01';

EXPLAIN FORMAT=TRADITIONAL
SELECT * FROM events
WHERE start_date = '2025-02-01'
  AND end_date = '2025-02-10';

EXPLAIN FORMAT=TRADITIONAL
SELECT * FROM events
WHERE start_date = '2025-02-01'
  AND end_date = '2025-02-10'
  AND any_date = '2025-02-10';

# end_date가 없어서 start_date까지만 활용
EXPLAIN FORMAT=TRADITIONAL
SELECT * FROM events
WHERE start_date = '2025-02-01'
  AND any_date = '2025-02-10';

# 가장 기준이 되는 start_date가 없어도 인덱스 활용 X
EXPLAIN FORMAT=TRADITIONAL
SELECT * FROM events
WHERE end_date = '2025-02-01'
  AND any_date = '2025-02-10';