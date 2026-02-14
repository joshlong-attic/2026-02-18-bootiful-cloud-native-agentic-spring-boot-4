PGPASSWORD=secret pg_dump \
  --no-owner \
  --no-privileges \
  --no-tablespaces \
  --schema=public \
  --inserts \
  --no-comments \
  --if-exists \
  --clean \
  -h localhost -U myuser -d mydatabase  | sed 's/public\.//g' >  out.sql
