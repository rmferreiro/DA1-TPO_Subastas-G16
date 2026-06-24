USE subastas_bd;

-- Hibernate (por tener ddl-auto=update) creó erróneamente esta columna extra
-- basándose en el código Java anterior. La eliminamos para que solo quede la real (numeroPostor).
ALTER TABLE asistentes DROP COLUMN numero_postor;
