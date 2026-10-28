#!/usr/bin/env bash
# 每天备份 autoroom 库。建议放到数据库机器上，配合 cron 0 点执行。
# 账号密码不要写进脚本，放到 /root/.my.cnf（权限 600）。

set -euo pipefail

DB_NAME="${DB_NAME:-autoroom}"
BACKUP_DIR="${BACKUP_DIR:-/data/mysql-backup}"
KEEP_DAYS="${KEEP_DAYS:-14}"
DATE_TAG="$(TZ=Asia/Shanghai date +%Y%m%d_%H%M%S)"
FILE="${BACKUP_DIR}/${DB_NAME}_${DATE_TAG}.sql.gz"

mkdir -p "${BACKUP_DIR}"

mysqldump \
  --defaults-extra-file=/root/.my.cnf \
  --single-transaction \
  --quick \
  --routines \
  --triggers \
  --events \
  --default-character-set=utf8mb4 \
  --set-gtid-purged=OFF \
  "${DB_NAME}" | gzip -9 > "${FILE}.tmp"

mv "${FILE}.tmp" "${FILE}"

find "${BACKUP_DIR}" -type f -name "${DB_NAME}_*.sql.gz" -mtime +"${KEEP_DAYS}" -delete

echo "backup ok: ${FILE} ($(du -h "${FILE}" | awk '{print $1}'))"
