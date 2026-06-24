package ar.edu.uade.grupo16.subastas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubastasApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubastasApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner fixDatabase(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				// Fix Lote 2
				Integer prodId2 = null;
				try { prodId2 = jdbcTemplate.queryForObject("SELECT identificador FROM productos WHERE descripcionCatalogo LIKE '%oleo%' OR descripcionCatalogo LIKE '%óleo%' LIMIT 1", Integer.class); } catch(Exception e) {}
				if (prodId2 == null) {
				    try { prodId2 = jdbcTemplate.queryForObject("SELECT producto FROM itemsCatalogo LIMIT 1 OFFSET 1", Integer.class); } catch(Exception e) {}
				}
				if (prodId2 != null) {
					byte[] imageBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("C:\\Users\\ezehu\\.gemini\\antigravity\\brain\\a64161aa-a570-4881-88e5-996ccb795a4d\\media__1780948167603.jpg"));
					jdbcTemplate.update("UPDATE productos SET descripcionCatalogo='Cuadro al óleo', descripcionCompleta='Cuadro al óleo' WHERE identificador=?", prodId2);
					int rows = jdbcTemplate.update("UPDATE fotos SET foto=? WHERE producto=?", imageBytes, prodId2);
					if (rows == 0) jdbcTemplate.update("INSERT INTO fotos (producto, foto) VALUES (?, ?)", prodId2, imageBytes);
				}

				// Fix Lote 3
				Integer prodId3 = null;
				try { prodId3 = jdbcTemplate.queryForObject("SELECT identificador FROM productos WHERE descripcionCatalogo LIKE '%vajilla%' OR descripcionCompleta LIKE '%vajilla%' LIMIT 1", Integer.class); } catch (Exception e) {}

				if (prodId3 == null) {
					// El producto no existe en DB, lo creamos
					Integer revisorId = jdbcTemplate.queryForObject("SELECT revisor FROM productos LIMIT 1", Integer.class);
					Integer duenioId = jdbcTemplate.queryForObject("SELECT duenio FROM productos LIMIT 1", Integer.class);
					
					org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
					jdbcTemplate.update(connection -> {
						java.sql.PreparedStatement ps = connection.prepareStatement("INSERT INTO productos (fecha, disponible, descripcionCatalogo, descripcionCompleta, revisor, duenio, tipo_producto, estado_revision) VALUES (CURDATE(), 'si', 'Juego de vajilla de plata sterling de 3 piezas', 'Juego de vajilla de plata sterling de 3 piezas', ?, ?, 'ESTANDAR', 'ACEPTADO')", java.sql.Statement.RETURN_GENERATED_KEYS);
						ps.setObject(1, revisorId);
						ps.setObject(2, duenioId);
						return ps;
					}, keyHolder);
					
					prodId3 = keyHolder.getKey().intValue();
					
					// Lo agregamos al catálogo
					Integer catalogoId = jdbcTemplate.queryForObject("SELECT identificador FROM catalogos LIMIT 1", Integer.class);
					jdbcTemplate.update("INSERT INTO itemsCatalogo (catalogo, producto, precioBase, comision, subastado, orden) VALUES (?, ?, 500.00, 50.00, 'no', 3)", catalogoId, prodId3);
					jdbcTemplate.update("UPDATE productos SET descripcionCatalogo='Juego de vajilla de plata sterling de 3 piezas', descripcionCompleta='Juego de vajilla de plata sterling de 3 piezas' WHERE identificador=?", prodId3);
				}
				
				byte[] imageBytes3 = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("C:\\Users\\ezehu\\.gemini\\antigravity\\brain\\a64161aa-a570-4881-88e5-996ccb795a4d\\media__1780948854369.png"));
				int rows3 = jdbcTemplate.update("UPDATE fotos SET foto=? WHERE producto=?", imageBytes3, prodId3);
				if (rows3 == 0) jdbcTemplate.update("INSERT INTO fotos (producto, foto) VALUES (?, ?)", prodId3, imageBytes3);

				// Agregar columna pagado si no existe
				try {
					jdbcTemplate.execute("ALTER TABLE registrosSubasta ADD COLUMN pagado BOOLEAN DEFAULT FALSE");
				} catch (Exception e) {
					// Ignorar si la columna ya existe
				}

				// Resetear estado de todos los items
				jdbcTemplate.update("UPDATE itemsCatalogo SET subastado='no'");
				jdbcTemplate.update("DELETE FROM pujos");
				jdbcTemplate.update("DELETE FROM registrosSubasta");

				System.out.println("====== FIX APLICADO: Lotes creados, sincronizados y reseteados exitosamente ======");
			} catch (Exception e) {
				System.err.println("Error aplicando fix de base de datos: " + e.getMessage());
			}
		};
	}

}
