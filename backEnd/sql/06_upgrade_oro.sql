USE subastas_bd;

-- Actualizamos a Juan Pablo Rodríguez (identificador 1) a categoría 'oro'
-- para que pueda ver y acceder a la nueva subasta según las reglas del negocio.
UPDATE clientes SET categoria = 'oro' WHERE identificador = 1;
