USE subastas_bd;

-- Agregamos una tarjeta de crédito internacional (USD) verificada para Juan Pablo
INSERT INTO medios_pago (cliente_id, tipo, banco, numero_cuenta, moneda, verificado, activo, monto_reservado) VALUES
(1, 'TARJETA_CREDITO', 'Visa Internacional', '4111111111111111', 'USD', 1, 1, 0.00);
