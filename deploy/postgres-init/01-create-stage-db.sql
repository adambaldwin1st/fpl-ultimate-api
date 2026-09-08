-- postgres:16's docker-entrypoint-initdb.d only runs these on a fresh (empty) data volume.
-- If the postgres_data volume already exists from before staging was added, run this manually:
--   docker compose exec postgres psql -U <FPL_DB_USER> -d fpl_ultimate -c "CREATE DATABASE fpl_ultimate_stage;"
CREATE DATABASE fpl_ultimate_stage;
