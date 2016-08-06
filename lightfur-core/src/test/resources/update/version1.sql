-- LIGHTFUR { "database": "lightfur_upgrade_test", "version": 1 } END

ALTER TABLE test ADD COLUMN flag INTEGER;

ALTER TABLE test ADD COLUMN last_date DATE;
