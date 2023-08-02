///Configuraramos el entorno:
Asegúrate de tener Java JDK instalado en tu sistema. Puedes verificarlo ejecutando java -version en la línea de comandos.

Instala una IDE, como Eclipse, IntelliJ o Visual Studio Code.

Crear el proyecto con Spring Initializr:
Ingresa a https://start.spring.io/

Selecciona las dependencias necesarias (Spring Web, Spring Data JPA, H2 Database, Spring Security, etc.).

Descarga el proyecto y ábrelo en tu IDE.

Configurar la base de datos:
En el archivo application.properties (o application.yml) dentro de la carpeta src/main/resources, configura la base de datos H2. Por ejemplo:

properties
Copy code
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driverClassName=org.h2.Driver
Configurar la seguridad con JWT:
Crea una clase de configuración que extienda WebSecurityConfigurerAdapter. Sobreescribe el método configure para configurar la autenticación y la autorización.

java
Copy code
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/public/**").permitAll() // Endpoint público para login o registro
            .antMatchers("/api/**").authenticated() // Resto de endpoints protegidos
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // Deshabilita manejo de sesiones por parte de Spring Security
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // Configurar el AuthenticationManager para manejar la autenticación (p. ej., con usuarios en memoria, base de datos, etc.)
    }
}
Crear los modelos y repositorios:
Crea las clases Java para las entidades TipoCambio y Auditoria, y anota las clases con las anotaciones de JPA (@Entity, @Id, @GeneratedValue, etc.).

java
Copy code
@Entity
public class TipoCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String monedaOrigen;

    @Column(nullable = false)
    private String monedaDestino;

    @Column(nullable = false)
    private double tipoCambio;

    // Getters y setters, constructores, etc.
}

@Entity
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    // Getters y setters, constructores, etc.
}
Crea los repositorios para estas entidades mediante interfaces que extiendan JpaRepository<T, ID>, donde T es la entidad y ID es el tipo de dato del ID de la entidad.

java
Copy code
@Repository
public interface TipoCambioRepository extends JpaRepository<TipoCambio, Long> {
    // Agregar consultas personalizadas si es necesario
}

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    // Agregar consultas personalizadas si es necesario
}
Implementar los servicios:
Crea los servicios que implementen la lógica de negocio relacionada con los tipos de cambio y la auditoría. Puedes definir interfaces y sus implementaciones.

java
Copy code
public interface TipoCambioService {
    List<TipoCambio> getAllTiposDeCambio();
    TipoCambio crearTipoCambio(TipoCambio tipoCambio);
    TipoCambio actualizarTipoCambio(Long id, TipoCambio tipoCambio);
}

@Service
public class TipoCambioServiceImpl implements TipoCambioService {

    @Autowired
    private TipoCambioRepository tipoCambioRepository;

    @Override
    public List<TipoCambio> getAllTiposDeCambio() {
        return tipoCambioRepository.findAll();
    }

    @Override
    public TipoCambio crearTipoCambio(TipoCambio tipoCambio) {
        return tipoCambioRepository.save(tipoCambio);
    }

    @Override
    public TipoCambio actualizarTipoCambio(Long id, TipoCambio tipoCambio) {
        TipoCambio tipoCambioExistente = tipoCambioRepository.findById(id).orElse(null);
        if (tipoCambioExistente == null) {
            throw new IllegalArgumentException("Tipo de cambio no encontrado");
        }

        tipoCambioExistente.setMonedaOrigen(tipoCambio.getMonedaOrigen());
        tipoCambioExistente.setMonedaDestino(tipoCambio.getMonedaDestino());
        tipoCambioExistente.setTipoCambio(tipoCambio.getTipoCambio());

        return tipoCambioRepository.save(tipoCambioExistente);
    }
}
Desarrollar los controladores:
Crea los controladores REST para gestionar las operaciones de tipo de cambio (registro, actualización y búsqueda) y la autenticación con JWT.

java
Copy code
@RestController
@RequestMapping("/api/tipos-cambio")
public class TipoCambioController {

    @Autowired
    private TipoCambioService tipoCambioService;

    @GetMapping
    public List<TipoCambio> getAllTiposDeCambio() {
        return tipoCambioService.getAllTiposDeCambio();
    }

    @PostMapping
    public TipoCambio crearTipoCambio(@RequestBody TipoCambio tipoCambio) {
        return tipoCambioService.crearTipoCambio(tipoCambio);
    }

    @PutMapping("/{id}")
    public TipoCambio actualizarTipoCambio(@PathVariable Long id, @RequestBody TipoCambio tipoCambio) {
        return tipoCambioService.actualizarTipoCambio(id, tipoCambio);
    }
}
Agregar la funcionalidad de auditoría:
Agrega lógica en los servicios y controladores para registrar la auditoría funcional en la base de datos cada vez que se realice un cambio de tipo de cambio.

java
Copy code
public interface AuditoriaService {
    void registrarAuditoria(String usuario, LocalDateTime fecha);
}

@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Override
    public void registrarAuditoria(String usuario, LocalDateTime fecha) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setFecha(fecha);

        auditoriaRepository.save(auditoria);
    }
}



En el servicio TipoCambioServiceImpl, puedes inyectar AuditoriaService y llamar al método registrarAuditoria para registrar la auditoría en el momento adecuado.

Probar la API con Postman:
Util